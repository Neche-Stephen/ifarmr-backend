package org.ifarmr.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ifarmr.entity.ConfirmationTokenModel;
import org.ifarmr.entity.JToken;
import org.ifarmr.entity.Role;
import org.ifarmr.entity.User;
import org.ifarmr.enums.TokenType;
import org.ifarmr.exceptions.EmailAlreadyExistsException;
import org.ifarmr.exceptions.UsernameAlreadyExistsException;
import org.ifarmr.payload.request.EmailDetails;
import org.ifarmr.payload.request.UserRegisterRequest;
import org.ifarmr.repository.ConfirmationTokenRepository;
import org.ifarmr.repository.JTokenRepository;
import org.ifarmr.repository.RoleRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.AuthService;
import org.ifarmr.service.EmailService;
import org.ifarmr.utils.EmailUtil;
import org.ifarmr.utils.FullNameFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Optional<User> existingUserByUsername = userRepository.findByUsername(userRegisterRequest.getUsername());
        if(existingUserByUsername.isPresent()){
            throw new UsernameAlreadyExistsException("Username already exists. Choose another username!");
        }


        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isEmpty()) {
            throw new RuntimeException("Default role USER not found in the database.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(userRole.get());

        String formattedFullName = FullNameFormatter.formatFullName(userRegisterRequest.getFullName());

        User newUser = User.builder()
                .fullName(formattedFullName)
                .username(userRegisterRequest.getUsername())
                .email(userRegisterRequest.getEmail())
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(newUser);

        userRepository.save(savedUser);
        ConfirmationTokenModel confirmationToken = new ConfirmationTokenModel(savedUser);
        confirmationTokenRepository.save(confirmationToken);

        String confirmationUrl = EmailUtil.getVerificationUrl(confirmationToken.getToken());

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


}
