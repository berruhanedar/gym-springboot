package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.dto.TokenResponseDTO;
import com.berruhanedar.app.gym_springboot.service.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Authentication")
@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @ApiOperation(value = "Authenticate user")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody CredentialsDTO credentials) {
        String token = authenticationService.login(credentials);
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @ApiOperation(value = "Change user password")
    @PutMapping("/login")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}