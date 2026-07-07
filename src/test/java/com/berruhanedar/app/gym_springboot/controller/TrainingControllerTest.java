package com.berruhanedar.app.gym_springboot.controller;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TrainingControllerTest {

    private final TrainingService trainingService = mock(TrainingService.class);
    private final TrainingController trainingController = new TrainingController(trainingService);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(trainingController)
                .setValidator(validator)
                .setControllerAdvice(
                        new BaseExceptionHandler(),
                        new MethodArgumentNotValidExceptionHandler())
                .build();
    }

    @Test
    void shouldGetTraineeTrainings() throws Exception {
        TraineeTrainingResponseDTO training = new TraineeTrainingResponseDTO();
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(LocalDate.of(2026, 7, 10));
        training.setTrainingTypeName("Yoga");
        training.setTrainingDuration(60);
        training.setTrainerName("Daniel Anderson");

        when(trainingService.getTraineeTrainings(
                eq("John.Doe"), any(TraineeTrainingsFilterDTO.class)))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainings/trainees/John.Doe/trainings")
                        .param("periodFrom", "2026-07-01")
                        .param("periodTo", "2026-07-31")
                        .param("trainerName", "Daniel")
                        .param("trainingType", "Yoga"))
                .andExpect(status().isOk());

        ArgumentCaptor<TraineeTrainingsFilterDTO> filterCaptor =
                ArgumentCaptor.forClass(TraineeTrainingsFilterDTO.class);

        verify(trainingService).getTraineeTrainings(
                eq("John.Doe"), filterCaptor.capture());

        assertThat(filterCaptor.getValue().getPeriodFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(filterCaptor.getValue().getPeriodTo()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(filterCaptor.getValue().getTrainerName()).isEqualTo("Daniel");
        assertThat(filterCaptor.getValue().getTrainingType()).isEqualTo("Yoga");
    }

    @Test
    void shouldReturnUnauthorizedWhenGetTraineeTrainingsFails() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password."))
                .when(trainingService)
                .getTraineeTrainings(eq("John.Doe"), any(TraineeTrainingsFilterDTO.class));

        mockMvc.perform(get("/api/trainings/trainees/John.Doe/trainings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetTrainerTrainings() throws Exception {
        TrainerTrainingResponseDTO training = new TrainerTrainingResponseDTO();
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(LocalDate.of(2026, 7, 10));
        training.setTrainingTypeName("Yoga");
        training.setTrainingDuration(60);
        training.setTraineeName("John Doe");

        when(trainingService.getTrainerTrainings(
                eq("Daniel.Anderson"), any(TrainerTrainingsFilterDTO.class)))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainings/trainers/Daniel.Anderson/trainings")
                        .param("periodFrom", "2026-07-01")
                        .param("periodTo", "2026-07-31")
                        .param("traineeName", "John"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrainerTrainingsFilterDTO> filterCaptor =
                ArgumentCaptor.forClass(TrainerTrainingsFilterDTO.class);

        verify(trainingService).getTrainerTrainings(
                eq("Daniel.Anderson"), filterCaptor.capture());

        assertThat(filterCaptor.getValue().getPeriodFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(filterCaptor.getValue().getPeriodTo()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(filterCaptor.getValue().getTraineeName()).isEqualTo("John");
    }

    @Test
    void shouldAddTraining() throws Exception {
        NewTrainingRequestDTO request = new NewTrainingRequestDTO();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Daniel.Anderson");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2026, 7, 10));
        request.setTrainingDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<NewTrainingRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(NewTrainingRequestDTO.class);

        verify(trainingService).createTraining(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getTraineeUsername()).isEqualTo("John.Doe");
        assertThat(requestCaptor.getValue().getTrainerUsername()).isEqualTo("Daniel.Anderson");
        assertThat(requestCaptor.getValue().getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(requestCaptor.getValue().getTrainingDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(requestCaptor.getValue().getTrainingDuration()).isEqualTo(60);
    }

    @Test
    void shouldReturnBadRequestWhenAddTrainingRequestIsInvalid() throws Exception {
        NewTrainingRequestDTO request = new NewTrainingRequestDTO();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Daniel.Anderson");
        request.setTrainingName("M");
        request.setTrainingDate(LocalDate.of(2026, 7, 10));
        request.setTrainingDuration(0);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).createTraining(any());
    }

    @Test
    void shouldGetTrainingTypes() throws Exception {
        TrainingTypeResponseDTO type = new TrainingTypeResponseDTO();
        type.setId(1L);
        type.setTrainingTypeName("Yoga");

        when(trainingService.getTrainingTypes())
                .thenReturn(List.of(type));

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk());

        verify(trainingService).getTrainingTypes();
    }
}