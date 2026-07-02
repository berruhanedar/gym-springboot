package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TrainerControllerTest {

    private final TrainerService trainerService = mock(TrainerService.class);
    private final TrainerController trainerController = new TrainerController(trainerService);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(trainerController)
                .setValidator(validator)
                .setControllerAdvice(
                        new BaseExceptionHandler(),
                        new MethodArgumentNotValidExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterTrainer() throws Exception {
        NewTrainerRequestDTO request = new NewTrainerRequestDTO();
        request.setFirstName("Daniel");
        request.setLastName("Anderson");
        request.setSpecializationName("Boxing");

        RegistrationResponseDTO response = new RegistrationResponseDTO("Daniel.Anderson", "password123");
        response.setUsername("Daniel.Anderson");
        response.setPassword("password123");

        when(trainerService.createTrainer(any(NewTrainerRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<NewTrainerRequestDTO> captor = ArgumentCaptor.forClass(NewTrainerRequestDTO.class);
        verify(trainerService).createTrainer(captor.capture());

        assertThat(captor.getValue().getFirstName()).isEqualTo("Daniel");
        assertThat(captor.getValue().getLastName()).isEqualTo("Anderson");
        assertThat(captor.getValue().getSpecializationName()).isEqualTo("Boxing");
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTrainerRequestIsInvalid() throws Exception {
        NewTrainerRequestDTO request = new NewTrainerRequestDTO();
        request.setFirstName("D");
        request.setLastName("Anderson");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).createTrainer(any());
    }

    @Test
    void shouldGetTrainerProfile() throws Exception {
        TrainerResponseDTO response = new TrainerResponseDTO();
        response.setUsername("Daniel.Anderson");
        response.setFirstName("Daniel");
        response.setLastName("Anderson");
        response.setSpecializationName("Boxing");
        response.setIsActive(true);

        when(trainerService.getTrainerByUsername(any(CredentialsDTO.class), eq("Daniel.Anderson")))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainers/Daniel.Anderson")
                        .param("username", "Daniel.Anderson")
                        .param("password", "password123"))
                .andExpect(status().isOk());

        ArgumentCaptor<CredentialsDTO> captor = ArgumentCaptor.forClass(CredentialsDTO.class);
        verify(trainerService).getTrainerByUsername(captor.capture(), eq("Daniel.Anderson"));

        assertThat(captor.getValue().getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(captor.getValue().getPassword()).isEqualTo("password123");
    }

    @Test
    void shouldReturnBadRequestWhenGetTrainerProfileCredentialsAreInvalid() throws Exception {
        mockMvc.perform(get("/api/trainers/Daniel.Anderson")
                        .param("username", "Daniel.Anderson"))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).getTrainerByUsername(any(), anyString());
    }

    @Test
    void shouldReturnUnauthorizedWhenGetTrainerProfileFails() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password."))
                .when(trainerService).getTrainerByUsername(any(CredentialsDTO.class), eq("Daniel.Anderson"));

        mockMvc.perform(get("/api/trainers/Daniel.Anderson")
                        .param("username", "Daniel.Anderson")
                        .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateTrainerProfile() throws Exception {
        UpdateTrainerRequestDTO request = new UpdateTrainerRequestDTO();
        request.setUsername("Daniel.Anderson");
        request.setFirstName("Dan");
        request.setLastName("Anderson");
        request.setIsActive(true);

        TrainerResponseDTO response = new TrainerResponseDTO();
        response.setUsername("Daniel.Anderson");
        response.setFirstName("Dan");
        response.setLastName("Anderson");
        response.setSpecializationName("Boxing");
        response.setIsActive(true);

        when(trainerService.updateTrainer(any(CredentialsDTO.class), any(UpdateTrainerRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/trainers")
                        .param("username", "Daniel.Anderson")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<CredentialsDTO> credentialsCaptor = ArgumentCaptor.forClass(CredentialsDTO.class);
        ArgumentCaptor<UpdateTrainerRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(UpdateTrainerRequestDTO.class);
        verify(trainerService).updateTrainer(credentialsCaptor.capture(), requestCaptor.capture());

        assertThat(credentialsCaptor.getValue().getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(credentialsCaptor.getValue().getPassword()).isEqualTo("password123");
        assertThat(requestCaptor.getValue().getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(requestCaptor.getValue().getFirstName()).isEqualTo("Dan");
        assertThat(requestCaptor.getValue().getIsActive()).isTrue();
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTrainerRequestIsInvalid() throws Exception {
        UpdateTrainerRequestDTO request = new UpdateTrainerRequestDTO();
        request.setUsername("Daniel.Anderson");

        mockMvc.perform(put("/api/trainers")
                        .param("username", "Daniel.Anderson")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).updateTrainer(any(), any());
    }

    @Test
    void shouldChangeTrainerActivationStatus() throws Exception {
        UpdateActivationStatusDTO request = new UpdateActivationStatusDTO();
        request.setUsername("Daniel.Anderson");
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainers/activation")
                        .param("username", "Daniel.Anderson")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<CredentialsDTO> credentialsCaptor = ArgumentCaptor.forClass(CredentialsDTO.class);
        ArgumentCaptor<UpdateActivationStatusDTO> requestCaptor =
                ArgumentCaptor.forClass(UpdateActivationStatusDTO.class);
        verify(trainerService).changeActivationStatus(credentialsCaptor.capture(), requestCaptor.capture());

        assertThat(credentialsCaptor.getValue().getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(requestCaptor.getValue().getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(requestCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    void shouldReturnBadRequestWhenChangeTrainerActivationStatusRequestIsInvalid() throws Exception {
        UpdateActivationStatusDTO request = new UpdateActivationStatusDTO();
        request.setUsername("Daniel.Anderson");

        mockMvc.perform(patch("/api/trainers/activation")
                        .param("username", "Daniel.Anderson")
                        .param("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).changeActivationStatus(any(), any());
    }
}
