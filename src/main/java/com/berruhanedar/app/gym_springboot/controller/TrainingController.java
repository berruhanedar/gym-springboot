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
    public ResponseEntity<List<TraineeTrainingResponseDTO>> getTraineeTrainings(@PathVariable @NotBlank String username, @Valid @ModelAttribute TraineeTrainingsFilterDTO filter) {
        List<TraineeTrainingResponseDTO> response = trainingService.getTraineeTrainings(username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Get trainer trainings")
    @GetMapping("/trainers/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDTO>> getTrainerTrainings(@PathVariable @NotBlank String username, @Valid @ModelAttribute TrainerTrainingsFilterDTO filter) {
        List<TrainerTrainingResponseDTO> response = trainingService.getTrainerTrainings(username, filter);
        return ResponseEntity.ok(response);
    }

    @ApiOperation(value = "Add training")
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody NewTrainingRequestDTO request) {
        trainingService.createTraining(request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes() {
        List<TrainingTypeResponseDTO> response = trainingService.getTrainingTypes();
        return ResponseEntity.ok(response);
    }
}