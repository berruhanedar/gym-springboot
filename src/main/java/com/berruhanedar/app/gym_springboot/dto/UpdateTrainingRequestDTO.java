package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTrainingRequestDTO {

    @NotNull
    private Long id;

    @NotNull
    private Long traineeId;

    @NotNull
    private Long trainerId;

    @NotBlank
    @Size(min = 2, max = 100)
    private String trainingName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String trainingType;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    @Min(1)
    private Integer trainingDuration;
}