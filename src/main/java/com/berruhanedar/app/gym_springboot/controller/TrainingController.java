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
    @GetMapping("/trainee/{username}")
    public ResponseEntity<List<TrainingResponseDTO>> getTraineeTrainings(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username, @Valid TraineeTrainingsFilterDTO filter) {
        List<TrainingResponseDTO> response = trainingService.getTraineeTrainings(credentials, username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get trainer trainings")
    @GetMapping("/trainer/{username}")
    public ResponseEntity<List<TrainingResponseDTO>> getTrainerTrainings(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username, @Valid TrainerTrainingsFilterDTO filter) {
        List<TrainingResponseDTO> response = trainingService.getTrainerTrainings(credentials, username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Add training")
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid CredentialsDTO credentials, @Valid @RequestBody NewTrainingRequestDTO request) {
        trainingService.createTraining(credentials, request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes() {
        List<TrainingTypeResponseDTO> response = trainingService.getTrainingTypes();
        return ResponseEntity.ok(response);
    }

}