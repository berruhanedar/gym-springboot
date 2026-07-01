package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TraineeResponseDTO {

    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive;

    private List<TrainerSummaryDTO> trainers;
}