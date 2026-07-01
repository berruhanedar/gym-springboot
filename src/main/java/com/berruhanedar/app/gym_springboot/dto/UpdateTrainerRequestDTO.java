package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateTrainerRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @NotNull
    private Boolean isActive;
}
