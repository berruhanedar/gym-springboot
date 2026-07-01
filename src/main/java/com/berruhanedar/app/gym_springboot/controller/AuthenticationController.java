package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@Valid CredentialsDTO credentials) {
        authenticationService.authenticate(credentials);
        return ResponseEntity.ok().build();
    }
}