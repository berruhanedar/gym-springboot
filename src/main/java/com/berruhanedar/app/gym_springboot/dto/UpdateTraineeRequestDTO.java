package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTraineeRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @Past
    private LocalDate dateOfBirth;

    @Size(max = 255)
    private String address;

    @NotNull
    private Boolean isActive;
}