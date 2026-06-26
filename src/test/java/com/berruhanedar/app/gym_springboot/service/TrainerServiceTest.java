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
class TrainerServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Test
    void shouldCreateTrainerWithGeneratedUsernameAndActiveStatus() {
        NewTrainerRequestDTO dto = new NewTrainerRequestDTO();
        dto.setFirstName("Daniel");
        dto.setLastName("Anderson");
        dto.setSpecialization("Boxing");

        TrainerResponseDTO response = gymFacade.createTrainer(dto);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getSpecialization()).isEqualTo("Boxing");
    }

    @Test
    void shouldGenerateUniqueUsernameAcrossTraineesAndTrainers() {
        NewTrainerRequestDTO trainer = new NewTrainerRequestDTO();
        trainer.setFirstName("Michael");
        trainer.setLastName("Brown");
        trainer.setSpecialization("Fitness");

        TrainerResponseDTO first = gymFacade.createTrainer(trainer);

        NewTraineeRequestDTO trainee = new NewTraineeRequestDTO();
        trainee.setFirstName("Michael");
        trainee.setLastName("Brown");
        trainee.setDateOfBirth(LocalDate.of(1998, 1, 1));

        TraineeResponseDTO second = gymFacade.createTrainee(trainee);

        assertThat(first.getUsername()).isEqualTo("Michael.Brown");
        assertThat(second.getUsername()).isEqualTo("Michael.Brown1");
    }

    @Test
    void shouldUpdateTrainerWithoutChangingUsername() {
        NewTrainerRequestDTO create = new NewTrainerRequestDTO();
        create.setFirstName("Thomas");
        create.setLastName("White");
        create.setSpecialization("Fitness");

        TrainerResponseDTO saved = gymFacade.createTrainer(create);

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setId(saved.getId());
        update.setFirstName("ThomasUpdated");
        update.setLastName("White");
        update.setSpecialization("Crossfit");
        update.setIsActive(false);

        TrainerResponseDTO updated = gymFacade.updateTrainer(update);

        assertThat(updated.getFirstName()).isEqualTo("ThomasUpdated");
        assertThat(updated.getUsername()).isEqualTo(saved.getUsername());
        assertThat(updated.getSpecialization()).isEqualTo("Crossfit");
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    void shouldGetTrainer() {
        NewTrainerRequestDTO create = new NewTrainerRequestDTO();
        create.setFirstName("Laura");
        create.setLastName("Green");
        create.setSpecialization("Pilates");

        TrainerResponseDTO saved = gymFacade.createTrainer(create);

        TrainerResponseDTO found = gymFacade.getTrainer(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getUsername()).isEqualTo("Laura.Green");
    }

    @Test
    void shouldThrowExceptionWhenTrainerNotFound() {
        assertThatThrownBy(() -> gymFacade.getTrainer(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingTrainer() {
        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setId(999L);
        update.setFirstName("Missing");
        update.setLastName("Trainer");
        update.setSpecialization("Fitness");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainer(update))
                .isInstanceOf(EntityNotFoundException.class);
    }
}