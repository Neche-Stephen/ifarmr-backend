package org.ifarmr.controller;

import org.ifarmr.payload.request.TokenRequest;
import org.ifarmr.service.NotificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationTokenController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTokenController.class);

    @Autowired
    private NotificationTokenService tokenService;

    @PostMapping("/firebase-save")
    public ResponseEntity<String> saveToken(@RequestBody TokenRequest tokenRequest) {
        logger.info("Entering saveToken method");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Authentication failed. User is not authenticated.");
            return ResponseEntity.status(403).body("User not authenticated.");
        }

        String userName = authentication.getName();
        logger.info("Authenticated user: {}", userName);

        try {
            boolean tokenExists = tokenService.tokenExists(tokenRequest.getToken());
            if (tokenExists) {
                logger.warn("Token already exists for user: {}", userName);
                return ResponseEntity.status(409).body("Token already exists.");
            } else {
                tokenService.saveToken(userName, tokenRequest.getToken());
                logger.info("Token saved successfully for user: {}", userName);
                return ResponseEntity.ok("Token saved successfully.");
            }
        } catch (Exception e) {
            logger.error("Failed to save token for user: {}. Error: {}", userName, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to save token: " + e.getMessage());
        }
    }

    @DeleteMapping("/firebase-delete")
    public ResponseEntity<String> deleteToken(@RequestBody TokenRequest tokenRequest) {
        logger.info("Entering deleteToken method");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Authentication failed. User is not authenticated.");
            return ResponseEntity.status(403).body("User not authenticated.");
        }

        String userName = authentication.getName();
        logger.info("Authenticated user: {}", userName);

        try {
            tokenService.deleteToken(userName, tokenRequest.getToken());
            logger.info("Token deleted successfully for user: {}", userName);
            return ResponseEntity.ok("Token deleted successfully.");
        } catch (Exception e) {
            logger.error("Failed to delete token for user: {}. Error: {}", userName, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete token: " + e.getMessage());
        }
    }

}
