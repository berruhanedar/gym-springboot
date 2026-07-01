package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final TraineeMapper traineeMapper = Mappers.getMapper(TraineeMapper.class);
    private final TrainerMapper trainerMapper = Mappers.getMapper(TrainerMapper.class);
    private final TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void shouldMapTraineeCreateUpdateAndResponseDtos() {
        NewTraineeRequestDTO create = new NewTraineeRequestDTO();
        create.setFirstName("Alice");
        create.setLastName("Walker");
        create.setDateOfBirth(LocalDate.of(1999, 1, 1));
        create.setAddress("Old Address");

        Trainee trainee = traineeMapper.toEntity(create);
        trainee.setUsername("Alice.Walker");
        trainee.setPassword("secret");
        trainee.setIsActive(true);

        assertThat(traineeMapper.toDTO(trainee).getUsername()).isEqualTo("Alice.Walker");

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setUsername("Alice.Walker");
        update.setFirstName("Alicia");
        update.setLastName("Stone");
        update.setDateOfBirth(LocalDate.of(1998, 2, 2));
        update.setAddress("New Address");
        update.setIsActive(false);

        traineeMapper.updateFromDTO(update, trainee);

        assertThat(trainee.getFirstName()).isEqualTo("Alicia");
        assertThat(trainee.getLastName()).isEqualTo("Stone");
        assertThat(trainee.getAddress()).isEqualTo("New Address");
        assertThat(trainee.getIsActive()).isFalse();
        assertThat(trainee.getUsername()).isEqualTo("Alice.Walker");
    }

    @Test
    void shouldMapTrainerCreateUpdateAndResponseDtos() {
        TrainingType yoga = trainingType(1L, "Yoga");

        NewTrainerRequestDTO create = new NewTrainerRequestDTO();
        create.setFirstName("Bob");
        create.setLastName("Miller");
        create.setSpecializationName("Yoga");

        Trainer trainer = trainerMapper.toEntity(create);
        trainer.setUsername("Bob.Miller");
        trainer.setPassword("secret");
        trainer.setIsActive(true);
        trainer.setSpecialization(yoga);

        assertThat(trainerMapper.toDTO(trainer).getSpecializationName()).isEqualTo("Yoga");

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setUsername("Bob.Miller");
        update.setFirstName("Bobby");
        update.setLastName("Miles");
        update.setIsActive(false);

        trainerMapper.updateFromDTO(update, trainer);

        assertThat(trainer.getFirstName()).isEqualTo("Bobby");
        assertThat(trainer.getLastName()).isEqualTo("Miles");
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
        assertThat(trainer.getIsActive()).isFalse();
        assertThat(trainer.getUsername()).isEqualTo("Bob.Miller");
    }

    @Test
    void shouldMapTrainingCreateAndResponseDtos() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Alice");
        trainee.setLastName("Walker");

        Trainer trainer = new Trainer();
        trainer.setFirstName("Bob");
        trainer.setLastName("Miller");

        TrainingType yoga = trainingType(30L, "Yoga");

        NewTrainingRequestDTO create = new NewTrainingRequestDTO();
        create.setTraineeUsername("Alice.Walker");
        create.setTrainerUsername("Bob.Miller");
        create.setTrainingName("Morning Yoga");
        create.setTrainingDate(LocalDate.now().plusDays(1));
        create.setTrainingDuration(45);

        Training training = trainingMapper.toEntity(create);

        assertThat(training.getTrainee()).isNull();
        assertThat(training.getTrainer()).isNull();
        assertThat(training.getTrainingType()).isNull();

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(yoga);

        TrainingResponseDTO response = trainingMapper.toDTO(training);

        assertThat(response.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(response.getTrainingTypeName()).isEqualTo("Yoga");
        assertThat(response.getTrainingDuration()).isEqualTo(45);
        assertThat(response.getTrainerName()).isEqualTo("Bob Miller");
        assertThat(response.getTraineeName()).isEqualTo("Alice Walker");
    }

    @Test
    void shouldReturnNullWhenMappingNullSources() {
        assertThat(traineeMapper.toDTO(null)).isNull();
        assertThat(traineeMapper.toEntity(null)).isNull();
        assertThat(trainerMapper.toDTO(null)).isNull();
        assertThat(trainerMapper.toEntity(null)).isNull();
        assertThat(trainingMapper.toDTO(null)).isNull();
        assertThat(trainingMapper.toEntity(null)).isNull();
    }

    private TrainingType trainingType(Long id, String name) {
        TrainingType type = new TrainingType();
        type.setId(id);
        type.setTrainingTypeName(name);
        return type;
    }
}