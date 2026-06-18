package com.berruhanedar.app.gym_springboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    private Long id;
    private String trainingTypeName;
}
