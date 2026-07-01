package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrainerUsernameDTO {

    @NotBlank
    private String username;
}