package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
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

}
