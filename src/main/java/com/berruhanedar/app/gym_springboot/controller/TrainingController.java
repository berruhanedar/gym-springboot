package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.dto.TraineeTrainingsFilterDTO;
import com.berruhanedar.app.gym_springboot.dto.TrainingResponseDTO;
import com.berruhanedar.app.gym_springboot.service.TrainingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/trainee/{username}")
    public ResponseEntity<List<TrainingResponseDTO>> getTraineeTrainings(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username, @Valid TraineeTrainingsFilterDTO filter) {
        List<TrainingResponseDTO> response = trainingService.getTraineeTrainings(credentials, username, filter);
        return ResponseEntity.ok(response);
    }
}