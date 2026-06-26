package com.berruhanedar.app.gym_springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredentialsDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}