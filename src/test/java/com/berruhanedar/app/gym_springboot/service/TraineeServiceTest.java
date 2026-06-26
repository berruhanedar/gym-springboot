package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class TraineeServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Test
    void shouldCreateTraineeWithGeneratedUsernameAndActiveStatus() {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName("Oliver");
        dto.setLastName("Taylor");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setAddress("London");

        TraineeResponseDTO response = gymFacade.createTrainee(dto);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("Oliver.Taylor");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void shouldUpdateTraineeWithoutChangingUsername() {
        NewTraineeRequestDTO create = new NewTraineeRequestDTO();
        create.setFirstName("Emily");
        create.setLastName("Johnson");
        create.setDateOfBirth(LocalDate.of(1999, 5, 10));

        TraineeResponseDTO saved = gymFacade.createTrainee(create);

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setId(saved.getId());
        update.setFirstName("EmilyUpdated");
        update.setLastName("Johnson");
        update.setDateOfBirth(LocalDate.of(1999, 5, 10));
        update.setAddress("New York");
        update.setIsActive(false);

        TraineeResponseDTO updated = gymFacade.updateTrainee(update);

        assertThat(updated.getFirstName()).isEqualTo("EmilyUpdated");
        assertThat(updated.getUsername()).isEqualTo(saved.getUsername());
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    void shouldDeleteTrainee() {
        NewTraineeRequestDTO create = new NewTraineeRequestDTO();
        create.setFirstName("David");
        create.setLastName("Miller");
        create.setDateOfBirth(LocalDate.of(1997, 1, 1));

        TraineeResponseDTO saved = gymFacade.createTrainee(create);

        gymFacade.deleteTrainee(saved.getId());

        assertThatThrownBy(() -> gymFacade.getTrainee(saved.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenTraineeNotFound() {
        assertThatThrownBy(() -> gymFacade.getTrainee(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingTrainee() {
        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setId(999L);
        update.setFirstName("Missing");
        update.setLastName("Trainee");
        update.setDateOfBirth(LocalDate.of(2000, 1, 1));
        update.setAddress("Nowhere");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainee(update))
                .isInstanceOf(EntityNotFoundException.class);
    }
}