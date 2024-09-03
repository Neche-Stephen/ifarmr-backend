package org.ifarmr.controller;

import org.ifarmr.payload.request.TokenRequest;
import org.ifarmr.service.NotificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationTokenController {

    @Autowired
    private NotificationTokenService tokenService;

    @PostMapping("/firebase-save")
    public ResponseEntity<String> saveToken(@RequestBody TokenRequest tokenRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("User not authenticated.");
        }

        String userName = authentication.getName();

        try {
            boolean tokenExists = tokenService.tokenExists(tokenRequest.getToken());
            if (tokenExists) {
                return ResponseEntity.status(409).body("Token already exists.");
            } else {
                tokenService.saveToken(userName, tokenRequest.getToken());
                return ResponseEntity.ok("Token saved successfully.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save token: " + e.getMessage());
        }
    }

    @DeleteMapping("/firebase-delete")
    public ResponseEntity<String> deleteToken(@RequestBody TokenRequest tokenRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("User not authenticated.");
        }

        String userName = authentication.getName();

        try {
            tokenService.deleteToken(userName, tokenRequest.getToken());
            return ResponseEntity.ok("Token deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete token: " + e.getMessage());
        }
    }

}
