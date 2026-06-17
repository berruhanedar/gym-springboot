package com.berruhanedar.app.gym_springboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private String trainingType;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}