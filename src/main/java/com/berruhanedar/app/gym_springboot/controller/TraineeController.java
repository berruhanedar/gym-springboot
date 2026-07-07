package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trainee Management")
@RestController
@RequestMapping("/api")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "Register trainee")
    @PostMapping("/trainees")
    public ResponseEntity<RegistrationResponseDTO> registerTrainee(@Valid @RequestBody NewTraineeRequestDTO request) {
        RegistrationResponseDTO response = traineeService.createTrainee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/trainees/{username}")
    public ResponseEntity<TraineeResponseDTO> getTraineeProfile(@PathVariable @NotBlank String username) {
        TraineeResponseDTO response = traineeService.getTraineeByUsername(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/trainees")
    public ResponseEntity<TraineeResponseDTO> updateTraineeProfile(@Valid @RequestBody UpdateTraineeRequestDTO request) {
        TraineeResponseDTO response = traineeService.updateTrainee(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/trainees/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable @NotBlank String username) {
        traineeService.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get active trainers not assigned to trainee")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/trainees/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryDTO>> getNotAssignedActiveTrainers(@PathVariable @NotBlank String username) {
        List<TrainerSummaryDTO> response = trainerService.getTrainersNotAssignedToTrainee(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee trainer list")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/trainees/trainers")
    public ResponseEntity<List<TrainerSummaryDTO>> updateTraineeTrainers(@Valid @RequestBody UpdateTraineeTrainersRequestDTO request) {
        List<TrainerSummaryDTO> response = traineeService.updateTraineeTrainers(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate or deactivate trainee")
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/trainees/activation")
    public ResponseEntity<Void> changeTraineeActivationStatus(@Valid @RequestBody UpdateActivationStatusDTO request) {
        traineeService.changeTraineeActivationStatus(request);
        return ResponseEntity.ok().build();
    }
}