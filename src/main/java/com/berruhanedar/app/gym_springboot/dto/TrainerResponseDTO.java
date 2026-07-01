package com.berruhanedar.app.gym_springboot.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrainerResponseDTO {

    private String username;
    private String firstName;
    private String lastName;
    private String specializationName;
    private Boolean isActive;

    private List<TraineeSummaryDTO> trainees;
}