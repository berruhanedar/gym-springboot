package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateActivationStatusDTO {

    @NotBlank
    private String username;

    @NotNull
    private Boolean isActive;
}
