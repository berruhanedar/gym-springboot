package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class UpdateTrainingTypeRequestDTO {

    @NotNull
    private Long id;

    @NotNull
    @Size(min = 2, max = 50)
    private String trainingTypeName;
}