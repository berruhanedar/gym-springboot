package com.berruhanedar.app.gym_springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationResponseDTO {

    private String username;
    private String password;
}