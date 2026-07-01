package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @PostMapping("/trainees")
    public ResponseEntity<RegistrationResponseDTO> registerTrainee(@Valid @RequestBody NewTraineeRequestDTO newTraineeRequestDTO) {
        RegistrationResponseDTO response = traineeService.createTrainee(newTraineeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trainees/{username}")
    public ResponseEntity<TraineeResponseDTO> getTraineeProfile(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username) {
        TraineeResponseDTO response = traineeService.getTraineeByUsername(credentials, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/trainees")
    public ResponseEntity<TraineeResponseDTO> updateTraineeProfile(@Valid CredentialsDTO credentials, @Valid @RequestBody UpdateTraineeRequestDTO request) {
        TraineeResponseDTO response = traineeService.updateTrainee(credentials, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/trainees/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username) {
        traineeService.deleteTraineeByUsername(credentials, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainees/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerResponseDTO>> getNotAssignedActiveTrainers(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username) {
        List<TrainerResponseDTO> response = trainerService.getTrainersNotAssignedToTrainee(credentials, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/trainees/trainers")
    public ResponseEntity<List<TrainerSummaryDTO>> updateTraineeTrainers(@Valid CredentialsDTO credentials, @Valid @RequestBody UpdateTraineeTrainersRequestDTO request) {
        List<TrainerSummaryDTO> response = traineeService.updateTraineeTrainers(credentials, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/trainees/activation")
    public ResponseEntity<Void> changeActivationStatus(@Valid CredentialsDTO credentials, @Valid @RequestBody UpdateActivationStatusDTO request) {
        traineeService.changeActivationStatus(credentials, request);
        return ResponseEntity.ok().build();
    }

}
