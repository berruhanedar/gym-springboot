package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Trainer Management")
@RestController
@RequestMapping("/api")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @ApiOperation(value = "Register trainer")
    @PostMapping("/trainers")
    public ResponseEntity<RegistrationResponseDTO> registerTrainer(@Valid @RequestBody NewTrainerRequestDTO request) {
        RegistrationResponseDTO response = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiOperation(value = "Get trainer profile")
    @GetMapping("/trainers/{username}")
    public ResponseEntity<TrainerResponseDTO> getTrainerProfile(@PathVariable @NotBlank String username) {
        TrainerResponseDTO response = trainerService.getTrainerByUsername(username);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update trainer profile")
    @PutMapping("/trainers")
    public ResponseEntity<TrainerResponseDTO> updateTrainerProfile(@Valid @RequestBody UpdateTrainerRequestDTO request) {
        TrainerResponseDTO response = trainerService.updateTrainer(request);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Activate or deactivate trainer")
    @PatchMapping("/trainers/activation")
    public ResponseEntity<Void> changeTrainerActivationStatus(@Valid @RequestBody UpdateActivationStatusDTO request) {
        trainerService.changeActivationStatus(request);
        return ResponseEntity.ok().build();
    }
}