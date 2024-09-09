package org.ifarmr.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ifarmr.exceptions.TokenExpiredException;
import org.ifarmr.payload.request.ConfirmPasswordRequest;
import org.ifarmr.payload.request.LoginRequest;
import org.ifarmr.payload.request.PasswordResetRequest;
import org.ifarmr.payload.request.UserRegisterRequest;
import org.ifarmr.payload.response.LoginResponse;
import org.ifarmr.service.AuthService;
import org.ifarmr.service.TokenValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenValidationService tokenValidationService;
    private final AuthService authService;

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token){

        String result = tokenValidationService.validateToken(token);
        if ("Email confirmed successfully!".equals(result)) {
            return ResponseEntity.ok(Collections.singletonMap("message", result));
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", result));
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {

        try {
            String registerUser = authService.registerUser(userRegisterRequest);
            if (!registerUser.equals("Invalid Email domain")) {
                return ResponseEntity.ok("User registered successfully. Please check your email to confirm your account");
            } else {
                return ResponseEntity.badRequest().body("Invalid Email!!!");
            }

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    //important
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPasswordRequest(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        try {
            String response = authService.forgotPasswordRequest(passwordResetRequest);
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("An error occurred while sending the password reset email.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/confirm-forget-password-token")
    public ResponseEntity<?> confirmPasswordForget(@RequestParam("token") String token) {
        try {
            authService.validateToken(token);
            return ResponseEntity.ok("Token confirmed successfully.");
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> confirmPasswordReset(@RequestParam("token") String token, @Valid @RequestBody ConfirmPasswordRequest confirmPasswordRequest) throws MessagingException {
        String response = authService.confirmResetPassword(token, confirmPasswordRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            String validTokenMessage = tokenValidationService.validateToken(token);
            return ResponseEntity.ok(Collections.singletonMap("message", validTokenMessage));
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Token expired"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Invalid token"));
        }
    }

}
