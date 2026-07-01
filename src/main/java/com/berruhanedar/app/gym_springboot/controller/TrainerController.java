package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.dto.NewTrainerRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.RegistrationResponseDTO;
import com.berruhanedar.app.gym_springboot.dto.TrainerResponseDTO;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping("/trainers")
    public ResponseEntity<RegistrationResponseDTO> registerTrainer(@Valid @RequestBody NewTrainerRequestDTO request) {
        RegistrationResponseDTO response = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trainers/{username}")
    public ResponseEntity<TrainerResponseDTO> getTrainerProfile(@Valid CredentialsDTO credentials, @PathVariable @NotBlank String username) {
        TrainerResponseDTO response = trainerService.getTrainerByUsername(credentials, username);
        return ResponseEntity.ok(response);
    }
}