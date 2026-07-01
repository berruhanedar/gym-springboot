package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTraineeTrainersRequestDTO {

    @NotBlank
    private String traineeUsername;

    @NotEmpty
    private List<TrainerUsernameDTO> trainers;
}