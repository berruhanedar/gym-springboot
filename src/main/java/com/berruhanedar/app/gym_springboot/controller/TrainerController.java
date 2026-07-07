package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trainer Management")
@RestController
@RequestMapping("/api")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(summary = "Register trainer")
    @PostMapping("/trainers")
    public ResponseEntity<RegistrationResponseDTO> registerTrainer(@Valid @RequestBody NewTrainerRequestDTO request) {
        RegistrationResponseDTO response = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainer profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/trainers/{username}")
    public ResponseEntity<TrainerResponseDTO> getTrainerProfile(@PathVariable @NotBlank String username) {
        TrainerResponseDTO response = trainerService.getTrainerByUsername(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/trainers")
    public ResponseEntity<TrainerResponseDTO> updateTrainerProfile(@Valid @RequestBody UpdateTrainerRequestDTO request) {
        TrainerResponseDTO response = trainerService.updateTrainer(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate or deactivate trainer")
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/trainers/activation")
    public ResponseEntity<Void> changeTrainerActivationStatus(@Valid @RequestBody UpdateActivationStatusDTO request) {
        trainerService.changeActivationStatus(request);
        return ResponseEntity.ok().build();
    }
}