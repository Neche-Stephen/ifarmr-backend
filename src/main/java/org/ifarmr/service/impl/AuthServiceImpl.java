package org.ifarmr.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ifarmr.config.JwtService;
import org.ifarmr.entity.ConfirmationTokenModel;
import org.ifarmr.entity.JToken;
import org.ifarmr.entity.Role;
import org.ifarmr.entity.User;
import org.ifarmr.enums.TokenType;
import org.ifarmr.exceptions.*;
import org.ifarmr.payload.request.*;
import org.ifarmr.payload.response.LoginResponse;
import org.ifarmr.repository.ConfirmationTokenRepository;
import org.ifarmr.repository.JTokenRepository;
import org.ifarmr.repository.RoleRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.AuthService;
import org.ifarmr.service.EmailService;
import org.ifarmr.utils.EmailUtil;
import org.ifarmr.utils.FullNameFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final JTokenRepository jTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailUtil emailUtil;


    @Value("${baseUrl}")
    private String baseUrl;


    @Override
    public String registerUser(UserRegisterRequest userRegisterRequest) throws MessagingException {
        // Validate email format
        String emailRegex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(userRegisterRequest.getEmail());

        if(!matcher.matches()){
            return "Invalid Email domain";
        }

        String[] emailParts = userRegisterRequest.getEmail().split("\\.");
        if (emailParts.length < 2 || emailParts[emailParts.length - 1].length() < 2) {
            System.out.println("Invalid email domain. Email parts: " + Arrays.toString(emailParts));
            return "Invalid Email domain";
        }

        Optional<User> existingUser = userRepository.findByEmail(userRegisterRequest.getEmail());

        if(existingUser.isPresent()){
            throw new EmailAlreadyExistsException("Email already exists. Login to your account!");
        }

        Optional<User> existingUserByUsername = userRepository.findByUsername(userRegisterRequest.getUserName());
        if(existingUserByUsername.isPresent()){
            throw new UsernameAlreadyExistsException("Username already exists. Choose another username!");
        }

        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isEmpty()) {
            throw new NotFoundException("Default role USER not found in the database.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(userRole.get());

        String formattedFullName = FullNameFormatter.formatFullName(userRegisterRequest.getFullName());

        User newUser = User.builder()
                .fullName(formattedFullName)
                .username(userRegisterRequest.getUserName())
                .email(userRegisterRequest.getEmail())
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .roles(roles)
                .gender(userRegisterRequest.getGender())
                .build();

        User savedUser = userRepository.save(newUser);

        userRepository.save(savedUser);
        ConfirmationTokenModel confirmationToken = new ConfirmationTokenModel(savedUser);
        confirmationTokenRepository.save(confirmationToken);

        String confirmationUrl = emailUtil.getVerificationUrl(confirmationToken.getToken());

        //Sending mail

        EmailDetails emailDetails = EmailDetails.builder()
                .fullName(savedUser.getFullName())
                .recipient(savedUser.getEmail())
                .subject("IFARMR REGISTRATION SUCCESSFUL")
                .link(confirmationUrl)
                .build();
        emailService.sendEmailAlerts(emailDetails, "email-verification");

        return "Confirmed Email";
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = JToken.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        jTokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = jTokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        jTokenRepository.saveAll(validUserTokens);
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        authenticationManager.authenticate( new UsernamePasswordAuthenticationToken
                (loginRequest.getUsername(), loginRequest.getPassword()) );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if(!user.isEnabled()){
            throw new DisabledException("User account is not enabled, please check your email to enable it");
        }

        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user,jwtToken);



        return org.ifarmr.payload.response.LoginResponse.builder()
                .id(user.getId())
                .token(jwtToken)
                .username(user.getUsername())
                .profilePicture(user.getDisplayPhoto())
                .role(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .message("Login Success")

                .build();
    }

    //important
    @Override
    public String forgotPasswordRequest(PasswordResetRequest passwordResetRequest) throws MessagingException {
        Optional<User> userOptional = userRepository.findByEmail(passwordResetRequest.getEmail());

        User user = userOptional.get();
        ConfirmationTokenModel confirmationToken = new ConfirmationTokenModel(user);
        confirmationTokenRepository.save(confirmationToken);

        String resetUrl = emailUtil.getResetPasswordUrl(confirmationToken.getToken());

        EmailDetails emailDetails = EmailDetails.builder()
                .fullName(user.getFullName())
                .recipient(user.getEmail())
                .link(resetUrl)
                .subject("IFARMR PASSWORD RESET")
                .build();
        emailService.sendEmailAlerts(emailDetails, "forgot-password");

        return "Password reset email sent";
    }


    @Override
    public String confirmResetPassword(String token, ConfirmPasswordRequest confirmPasswordRequest) throws MessagingException {
        Optional<ConfirmationTokenModel> tokenOptional = confirmationTokenRepository.findByToken(token);


        ConfirmationTokenModel confirmationToken = tokenOptional.get();

        // Check if token is expired
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token has expired");
        }

        // Validate password match
        if (!confirmPasswordRequest.getNewPassword().equals(confirmPasswordRequest.getConfirmNewPassword())) {
            throw new PasswordMismatchException("New passwords do not match");
        }

        // Update the user's password
        User user = confirmationToken.getUser();
        user.setPassword(passwordEncoder.encode(confirmPasswordRequest.getNewPassword()));
        userRepository.save(user);

        // Delete the token after successful password reset
        confirmationTokenRepository.delete(confirmationToken);

        sendPasswordChangeConfirmationEmail(user);


        return "Password reset successfully";
    }

    private void sendPasswordChangeConfirmationEmail(User user) throws MessagingException {
        String loginUrl = baseUrl + "/login";  // Adjust the login URL as necessary

        EmailDetails emailDetails = EmailDetails.builder()
                .fullName(user.getFullName())
                .recipient(user.getEmail())
                .subject("Your iFarmr Password Has Been Successfully Changed")
                .link(loginUrl)  // Include the login link in the email
                .build();

        emailService.sendEmailAlerts(emailDetails, "password-change-confirmation");
    }



    @Override
    public void validateToken(String token) {
        Optional<ConfirmationTokenModel> tokenOptional = confirmationTokenRepository.findByToken(token);


        ConfirmationTokenModel confirmationToken = tokenOptional.get();

        // Check if token is expired
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token has expired");
        }
    }




}
