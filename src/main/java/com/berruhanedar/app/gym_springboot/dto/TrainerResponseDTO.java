package com.berruhanedar.app.gym_springboot.dto;

import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import lombok.Data;

@Data
public class TrainerResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean isActive;
    private TrainingType specialization;
}
