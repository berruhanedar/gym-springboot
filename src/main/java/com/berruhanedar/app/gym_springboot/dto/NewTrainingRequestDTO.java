package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewTrainingRequestDTO {

    @NotBlank
    private String traineeUsername;

    @NotBlank
    private String trainerUsername;

    @NotBlank
    @Size(min = 2, max = 100)
    private String trainingName;

    @NotNull
    @FutureOrPresent
    private LocalDate trainingDate;

    @NotNull
    @Min(1)
    private Integer trainingDuration;
}