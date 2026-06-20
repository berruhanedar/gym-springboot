package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TrainingServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Test
    void shouldCreateTrainingWhenTraineeAndTrainerExist() {
        TraineeResponseDTO traineeResponse = createTrainee("Sophia", "Williams");
        TrainerResponseDTO trainerResponse = createTrainer("James", "Wilson", "Yoga");

        NewTrainingRequestDTO training = new NewTrainingRequestDTO();
        training.setTraineeId(traineeResponse.getId());
        training.setTrainerId(trainerResponse.getId());
        training.setTrainingName("Yoga Session");
        training.setTrainingTypeName("Yoga");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(45);

        TrainingResponseDTO response = gymFacade.createTraining(training);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void shouldGetTraining() {
        TraineeResponseDTO traineeResponse = createTrainee("Grace", "Hall");
        TrainerResponseDTO trainerResponse = createTrainer("Henry", "Young", "Cardio");

        NewTrainingRequestDTO training = new NewTrainingRequestDTO();
        training.setTraineeId(traineeResponse.getId());
        training.setTrainerId(trainerResponse.getId());
        training.setTrainingName("Cardio Session");
        training.setTrainingTypeName("Cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(30);

        TrainingResponseDTO saved = gymFacade.createTraining(training);
        TrainingResponseDTO found = gymFacade.getTraining(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getTrainingName()).isEqualTo("Cardio Session");
        assertThat(found.getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void shouldThrowExceptionWhenTrainingNotFound() {
        assertThatThrownBy(() -> gymFacade.getTraining(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenCreatingTrainingWithInvalidTrainee() {
        NewTrainingRequestDTO training = new NewTrainingRequestDTO();
        training.setTraineeId(999L);
        training.setTrainerId(1L);
        training.setTrainingName("Invalid Training");
        training.setTrainingTypeName("Yoga");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        assertThatThrownBy(() -> gymFacade.createTraining(training))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenCreatingTrainingWithInvalidTrainer() {
        TraineeResponseDTO traineeResponse = createTrainee("Lily", "Scott");

        NewTrainingRequestDTO training = new NewTrainingRequestDTO();
        training.setTraineeId(traineeResponse.getId());
        training.setTrainerId(999L);
        training.setTrainingName("Invalid Training");
        training.setTrainingTypeName("Yoga");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        assertThatThrownBy(() -> gymFacade.createTraining(training))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private TraineeResponseDTO createTrainee(String firstName, String lastName) {
        NewTraineeRequestDTO trainee = new NewTraineeRequestDTO();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(LocalDate.of(1996, 1, 1));

        return gymFacade.createTrainee(trainee);
    }

    private TrainerResponseDTO createTrainer(String firstName, String lastName, String specialization) {
        NewTrainerRequestDTO trainer = new NewTrainerRequestDTO();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);

        return gymFacade.createTrainer(trainer);
    }
}