package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateTraineeTrainersRequestDTO {

    @NotNull
    private String traineeUsername;

    @NotEmpty
    private Set<Long> trainerIds;
}