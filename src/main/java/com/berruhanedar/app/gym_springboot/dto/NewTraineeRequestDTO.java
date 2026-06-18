package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewTraineeRequestDTO {

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
}
