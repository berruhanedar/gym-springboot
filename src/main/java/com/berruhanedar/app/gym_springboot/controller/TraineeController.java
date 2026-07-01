package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.NewTraineeRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.RegistrationResponseDTO;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
