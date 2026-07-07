package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TraineeControllerTest {

    private final TraineeService traineeService = mock(TraineeService.class);
    private final TrainerService trainerService = mock(TrainerService.class);

    private final TraineeController traineeController =
            new TraineeController(traineeService, trainerService);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(traineeController)
                .setValidator(validator)
                .setControllerAdvice(
                        new BaseExceptionHandler(),
                        new MethodArgumentNotValidExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterTrainee() throws Exception {
        NewTraineeRequestDTO request = new NewTraineeRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Istanbul");

        RegistrationResponseDTO response =
                new RegistrationResponseDTO("John.Doe", "password123");

        when(traineeService.createTrainee(any(NewTraineeRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<NewTraineeRequestDTO> captor =
                ArgumentCaptor.forClass(NewTraineeRequestDTO.class);

        verify(traineeService).createTrainee(captor.capture());

        assertThat(captor.getValue().getFirstName()).isEqualTo("John");
        assertThat(captor.getValue().getLastName()).isEqualTo("Doe");
        assertThat(captor.getValue().getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(captor.getValue().getAddress()).isEqualTo("Istanbul");
    }

    @Test
    void shouldReturnBadRequestWhenRegisterTraineeRequestIsInvalid() throws Exception {
        NewTraineeRequestDTO request = new NewTraineeRequestDTO();
        request.setLastName("Doe");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).createTrainee(any());
    }

    @Test
    void shouldGetTraineeProfile() throws Exception {
        TraineeResponseDTO response = new TraineeResponseDTO();
        response.setUsername("John.Doe");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setDateOfBirth(LocalDate.of(2000, 1, 1));
        response.setAddress("Istanbul");
        response.setIsActive(true);

        when(traineeService.getTraineeByUsername("John.Doe"))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainees/John.Doe"))
                .andExpect(status().isOk());

        verify(traineeService).getTraineeByUsername("John.Doe");
    }

    @Test
    void shouldReturnUnauthorizedWhenGetTraineeProfileFails() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password."))
                .when(traineeService).getTraineeByUsername("John.Doe");

        mockMvc.perform(get("/api/trainees/John.Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateTraineeProfile() throws Exception {
        UpdateTraineeRequestDTO request = new UpdateTraineeRequestDTO();
        request.setUsername("John.Doe");
        request.setFirstName("Johnny");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Ankara");
        request.setIsActive(true);

        TraineeResponseDTO response = new TraineeResponseDTO();
        response.setUsername("John.Doe");
        response.setFirstName("Johnny");
        response.setLastName("Doe");
        response.setDateOfBirth(LocalDate.of(2000, 1, 1));
        response.setAddress("Ankara");
        response.setIsActive(true);

        when(traineeService.updateTrainee(any(UpdateTraineeRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateTraineeRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(UpdateTraineeRequestDTO.class);

        verify(traineeService).updateTrainee(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getUsername()).isEqualTo("John.Doe");
        assertThat(requestCaptor.getValue().getFirstName()).isEqualTo("Johnny");
        assertThat(requestCaptor.getValue().getAddress()).isEqualTo("Ankara");
        assertThat(requestCaptor.getValue().getIsActive()).isTrue();
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTraineeRequestIsInvalid() throws Exception {
        UpdateTraineeRequestDTO request = new UpdateTraineeRequestDTO();
        request.setUsername("John.Doe");

        mockMvc.perform(put("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).updateTrainee(any());
    }

    @Test
    void shouldDeleteTraineeProfile() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Doe"))
                .andExpect(status().isOk());

        verify(traineeService).deleteTraineeByUsername("John.Doe");
    }

    @Test
    void shouldReturnUnauthorizedWhenDeleteTraineeProfileFails() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password."))
                .when(traineeService).deleteTraineeByUsername("John.Doe");

        mockMvc.perform(delete("/api/trainees/John.Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetNotAssignedActiveTrainers() throws Exception {
        TrainerSummaryDTO trainer = new TrainerSummaryDTO();
        trainer.setUsername("Bob.Miller");
        trainer.setFirstName("Bob");
        trainer.setLastName("Miller");
        trainer.setSpecializationName("Yoga");

        when(trainerService.getTrainersNotAssignedToTrainee("John.Doe"))
                .thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/John.Doe/unassigned-trainers"))
                .andExpect(status().isOk());

        verify(trainerService).getTrainersNotAssignedToTrainee("John.Doe");
    }

    @Test
    void shouldReturnUnauthorizedWhenGetNotAssignedActiveTrainersFails() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password."))
                .when(trainerService).getTrainersNotAssignedToTrainee("John.Doe");

        mockMvc.perform(get("/api/trainees/John.Doe/unassigned-trainers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateTraineeTrainers() throws Exception {
        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername("John.Doe");

        TrainerUsernameDTO trainerUsername = new TrainerUsernameDTO();
        trainerUsername.setUsername("Bob.Miller");
        request.setTrainers(List.of(trainerUsername));

        TrainerSummaryDTO responseTrainer = new TrainerSummaryDTO();
        responseTrainer.setUsername("Bob.Miller");
        responseTrainer.setFirstName("Bob");
        responseTrainer.setLastName("Miller");
        responseTrainer.setSpecializationName("Yoga");

        when(traineeService.updateTraineeTrainers(any(UpdateTraineeTrainersRequestDTO.class)))
                .thenReturn(List.of(responseTrainer));

        mockMvc.perform(put("/api/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateTraineeTrainersRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(UpdateTraineeTrainersRequestDTO.class);

        verify(traineeService).updateTraineeTrainers(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getTraineeUsername()).isEqualTo("John.Doe");
        assertThat(requestCaptor.getValue().getTrainers()).hasSize(1);
        assertThat(requestCaptor.getValue().getTrainers().get(0).getUsername()).isEqualTo("Bob.Miller");
    }

    @Test
    void shouldReturnBadRequestWhenUpdateTraineeTrainersRequestIsInvalid() throws Exception {
        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername("John.Doe");
        request.setTrainers(Collections.emptyList());

        mockMvc.perform(put("/api/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).updateTraineeTrainers(any());
    }

    @Test
    void shouldChangeTraineeActivationStatus() throws Exception {
        UpdateActivationStatusDTO request = new UpdateActivationStatusDTO();
        request.setUsername("John.Doe");
        request.setIsActive(false);

        mockMvc.perform(patch("/api/trainees/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateActivationStatusDTO> requestCaptor =
                ArgumentCaptor.forClass(UpdateActivationStatusDTO.class);

        verify(traineeService).changeTraineeActivationStatus(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getUsername()).isEqualTo("John.Doe");
        assertThat(requestCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    void shouldReturnBadRequestWhenChangeActivationStatusRequestIsInvalid() throws Exception {
        UpdateActivationStatusDTO request = new UpdateActivationStatusDTO();
        request.setUsername("John.Doe");

        mockMvc.perform(patch("/api/trainees/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(traineeService, never()).changeTraineeActivationStatus(any());
    }
}
