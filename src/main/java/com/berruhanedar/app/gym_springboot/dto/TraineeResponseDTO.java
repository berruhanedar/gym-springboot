package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TraineeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean isActive;
    private LocalDate dateOfBirth;
    private String address;
}
