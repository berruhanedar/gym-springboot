package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TrainingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Training Management")
@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @ApiOperation(value = "Get trainee trainings")
    @GetMapping("/trainees/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponseDTO>> getTraineeTrainings(@Valid @ModelAttribute CredentialsDTO credentials, @PathVariable @NotBlank String username, @Valid @ModelAttribute TraineeTrainingsFilterDTO filter) {
        List<TraineeTrainingResponseDTO> response = trainingService.getTraineeTrainings(credentials, username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get trainer trainings")
    @GetMapping("/trainers/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDTO>> getTrainerTrainings(@Valid @ModelAttribute CredentialsDTO credentials, @PathVariable @NotBlank String username, @Valid TrainerTrainingsFilterDTO filter) {
        List<TrainerTrainingResponseDTO> response = trainingService.getTrainerTrainings(credentials, username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Add training")
    @PostMapping("/trainings")
    public ResponseEntity<Void> addTraining(@Valid @ModelAttribute CredentialsDTO credentials, @Valid @RequestBody NewTrainingRequestDTO request) {
        trainingService.createTraining(credentials, request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes(@Valid @ModelAttribute CredentialsDTO credentials) {
        List<TrainingTypeResponseDTO> response = trainingService.getTrainingTypes(credentials);
        return ResponseEntity.ok(response);
    }

}