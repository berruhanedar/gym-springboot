package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingResponseDTO {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private String trainingTypeName;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}
