package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthenticationControllerTest {

    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final AuthenticationController authenticationController =
            new AuthenticationController(authenticationService);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(authenticationController)
                .setValidator(validator)
                .setControllerAdvice(
                        new BaseExceptionHandler(),
                        new MethodArgumentNotValidExceptionHandler())
                .build();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");
        credentials.setPassword("password123");

        when(authenticationService.login(any(CredentialsDTO.class)))
                .thenReturn("jwt-token");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk());

        ArgumentCaptor<CredentialsDTO> captor = ArgumentCaptor.forClass(CredentialsDTO.class);
        verify(authenticationService).login(captor.capture());

        assertThat(captor.getValue().getUsername()).isEqualTo("John.Doe");
        assertThat(captor.getValue().getPassword()).isEqualTo("password123");
    }

    @Test
    void shouldReturnBadRequestWhenLoginCredentialsAreInvalid() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");
        credentials.setPassword("wrongPassword");

        doThrow(new AuthenticationException("Invalid username or password."))
                .when(authenticationService).login(any(CredentialsDTO.class));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("John.Doe");
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<ChangePasswordRequestDTO> captor =
                ArgumentCaptor.forClass(ChangePasswordRequestDTO.class);

        verify(authenticationService).changePassword(captor.capture());

        assertThat(captor.getValue().getUsername()).isEqualTo("John.Doe");
        assertThat(captor.getValue().getOldPassword()).isEqualTo("oldPassword");
        assertThat(captor.getValue().getNewPassword()).isEqualTo("newPassword");
    }

    @Test
    void shouldReturnBadRequestWhenChangePasswordRequestIsInvalid() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("John.Doe");

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).changePassword(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenChangePasswordFails() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("John.Doe");
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        doThrow(new AuthenticationException("Invalid username or password."))
                .when(authenticationService).changePassword(any(ChangePasswordRequestDTO.class));

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}