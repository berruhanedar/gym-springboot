package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Trainee Management")
@RestController
@RequestMapping("/api")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @ApiOperation(value = "Register trainee")
    @PostMapping("/trainees")
    public ResponseEntity<RegistrationResponseDTO> registerTrainee(@Valid @RequestBody NewTraineeRequestDTO request) {
        RegistrationResponseDTO response = traineeService.createTrainee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiOperation(value = "Get trainee profile")
    @GetMapping("/trainees/{username}")
    public ResponseEntity<TraineeResponseDTO> getTraineeProfile(@PathVariable @NotBlank String username) {
        TraineeResponseDTO response = traineeService.getTraineeByUsername(username);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update trainee profile")
    @PutMapping("/trainees")
    public ResponseEntity<TraineeResponseDTO> updateTraineeProfile(@Valid @RequestBody UpdateTraineeRequestDTO request) {
        TraineeResponseDTO response = traineeService.updateTrainee(request);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Delete trainee profile")
    @DeleteMapping("/trainees/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable @NotBlank String username) {
        traineeService.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get active trainers not assigned to trainee")
    @GetMapping("/trainees/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryDTO>> getNotAssignedActiveTrainers(@PathVariable @NotBlank String username) {
        List<TrainerSummaryDTO> response = trainerService.getTrainersNotAssignedToTrainee(username);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Update trainee trainer list")
    @PutMapping("/trainees/trainers")
    public ResponseEntity<List<TrainerSummaryDTO>> updateTraineeTrainers(@Valid @RequestBody UpdateTraineeTrainersRequestDTO request) {
        List<TrainerSummaryDTO> response = traineeService.updateTraineeTrainers(request);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Activate or deactivate trainee")
    @PatchMapping("/trainees/activation")
    public ResponseEntity<Void> changeTraineeActivationStatus(@Valid @RequestBody UpdateActivationStatusDTO request) {
        traineeService.changeTraineeActivationStatus(request);
        return ResponseEntity.ok().build();
    }
}