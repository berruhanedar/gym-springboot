package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TraineeTrainingResponseDTO {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingTypeName;
    private Integer trainingDuration;
    private String trainerName;
}