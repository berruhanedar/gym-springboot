package com.berruhanedar.app.gym_springboot.dto;

import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateTrainerRequestDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @NotNull
    private TrainingType specialization;

    @NotNull
    private Boolean isActive;
}
