package com.checkmate.bub.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth(Authentication authentication) {
        log.info("Authentication check request received");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        
        log.info("User authenticated: {}", authentication.getName());
        return ResponseEntity.ok("Authenticated");
    }
}