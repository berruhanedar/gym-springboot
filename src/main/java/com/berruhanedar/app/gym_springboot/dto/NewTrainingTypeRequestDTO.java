package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class NewTrainingTypeRequestDTO {

    @NotNull
    @Size(min = 2, max = 50)
    private String trainingTypeName;
}