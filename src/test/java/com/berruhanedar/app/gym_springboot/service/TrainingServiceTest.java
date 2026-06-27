package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TrainingServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldCreateAndGetTrainingWhenCredentialsAndReferencesAreValid() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TraineeResponseDTO trainee = createTrainee("Sophia", "Williams");
        TrainerResponseDTO trainer = createTrainer("James", "Wilson", yoga);

        TrainingResponseDTO created = gymFacade.createTraining(
                trainerCredentials(trainer),
                newTraining(trainee.getId(), trainer.getId(), "Morning Yoga", "Yoga", LocalDate.now().plusDays(1), 45));
        TrainingResponseDTO found = gymFacade.getTraining(traineeCredentials(trainee), created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getTraineeId()).isEqualTo(trainee.getId());
        assertThat(found.getTrainerId()).isEqualTo(trainer.getId());
        assertThat(found.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(found.getTrainingTypeName()).isEqualTo("Yoga");
        assertThat(found.getTrainingDuration()).isEqualTo(45);
    }

    @Test
    void shouldFilterTraineeTrainingsByDateTrainerNameAndTrainingType() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TrainingType cardio = ensureTrainingType("Cardio");
        TraineeResponseDTO trainee = createTrainee("Grace", "Hall");
        TrainerResponseDTO yogaTrainer = createTrainer("Henry", "Young", yoga);
        TrainerResponseDTO cardioTrainer = createTrainer("Liam", "Stone", cardio);

        TrainingResponseDTO expected = gymFacade.createTraining(trainerCredentials(yogaTrainer),
                newTraining(trainee.getId(), yogaTrainer.getId(), "Yoga Match", "Yoga", LocalDate.now().plusDays(2), 50));
        gymFacade.createTraining(trainerCredentials(cardioTrainer),
                newTraining(trainee.getId(), cardioTrainer.getId(), "Cardio Out", "Cardio", LocalDate.now().plusDays(2), 30));
        gymFacade.createTraining(trainerCredentials(yogaTrainer),
                newTraining(trainee.getId(), yogaTrainer.getId(), "Old Yoga", "Yoga", LocalDate.now().plusDays(10), 40));

        List<TrainingResponseDTO> result = gymFacade.getTraineeTrainings(
                traineeCredentials(trainee),
                trainee.getUsername(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "Henry You",
                "Yoga");

        assertThat(result)
                .extracting(TrainingResponseDTO::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void shouldFilterTrainerTrainingsByDateAndTraineeName() {
        TrainingType pilates = ensureTrainingType("Pilates");
        TrainerResponseDTO trainer = createTrainer("Trainer", "Filter", pilates);
        TraineeResponseDTO expectedTrainee = createTrainee("Expected", "Person");
        TraineeResponseDTO otherTrainee = createTrainee("Other", "Person");

        TrainingResponseDTO expected = gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(expectedTrainee.getId(), trainer.getId(), "Pilates Match", "Pilates", LocalDate.now().plusDays(4), 55));
        gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(otherTrainee.getId(), trainer.getId(), "Pilates Other", "Pilates", LocalDate.now().plusDays(4), 35));
        gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(expectedTrainee.getId(), trainer.getId(), "Pilates Later", "Pilates", LocalDate.now().plusDays(20), 35));

        List<TrainingResponseDTO> result = gymFacade.getTrainerTrainings(
                trainerCredentials(trainer),
                trainer.getUsername(),
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(5),
                "Expected Per");

        assertThat(result)
                .extracting(TrainingResponseDTO::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void shouldReturnAllMatchingTrainingsWhenOptionalCriteriaAreBlankOrNull() {
        TrainingType stretching = ensureTrainingType("Stretching");
        TraineeResponseDTO trainee = createTrainee("Blank", "Criteria");
        TrainerResponseDTO trainer = createTrainer("Blank", "Trainer", stretching);

        TrainingResponseDTO first = gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(trainee.getId(), trainer.getId(), "Stretch One", "Stretching", LocalDate.now().plusDays(1), 20));
        TrainingResponseDTO second = gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(trainee.getId(), trainer.getId(), "Stretch Two", "Stretching", LocalDate.now().plusDays(2), 25));

        List<TrainingResponseDTO> result = gymFacade.getTraineeTrainings(
                traineeCredentials(trainee), trainee.getUsername(), null, null, " ", "");

        assertThat(result)
                .extracting(TrainingResponseDTO::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldThrowExceptionWhenTrainingOrReferencesAreMissing() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TraineeResponseDTO trainee = createTrainee("Lily", "Scott");
        TrainerResponseDTO trainer = createTrainer("Valid", "Trainer", yoga);

        assertThatThrownBy(() -> gymFacade.getTraining(traineeCredentials(trainee), 999L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(999L, trainer.getId(), "Invalid Trainee", "Yoga", LocalDate.now().plusDays(1), 60)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");
        assertThatThrownBy(() -> gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(trainee.getId(), 999L, "Invalid Trainer", "Yoga", LocalDate.now().plusDays(1), 60)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");
        assertThatThrownBy(() -> gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(trainee.getId(), trainer.getId(), "Invalid Type", "UnknownType", LocalDate.now().plusDays(1), 60)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Training type not found");
    }

    @Test
    void shouldRejectTrainingOperationsWhenCredentialsAreInvalid() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TraineeResponseDTO trainee = createTrainee("Auth", "Trainee");
        TrainerResponseDTO trainer = createTrainer("Auth", "Trainer", yoga);
        CredentialsDTO wrongTrainerCredentials = credentials(trainer.getUsername(), "wrong-pass");
        CredentialsDTO wrongTraineeCredentials = credentials(trainee.getUsername(), "wrong-pass");

        assertThatThrownBy(() -> gymFacade.createTraining(wrongTrainerCredentials,
                newTraining(trainee.getId(), trainer.getId(), "Auth Fail", "Yoga", LocalDate.now().plusDays(1), 45)))
                .isInstanceOf(AuthenticationException.class);

        TrainingResponseDTO saved = gymFacade.createTraining(trainerCredentials(trainer),
                newTraining(trainee.getId(), trainer.getId(), "Auth Ok", "Yoga", LocalDate.now().plusDays(1), 45));

        assertThatThrownBy(() -> gymFacade.getTraining(wrongTraineeCredentials, saved.getId()))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> gymFacade.getTraineeTrainings(wrongTraineeCredentials,
                trainee.getUsername(), null, null, null, null))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> gymFacade.getTrainerTrainings(wrongTrainerCredentials,
                trainer.getUsername(), null, null, null))
                .isInstanceOf(AuthenticationException.class);
    }

    private TraineeResponseDTO createTrainee(String firstName, String lastName) {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return gymFacade.createTrainee(dto);
    }

    private TrainerResponseDTO createTrainer(String firstName, String lastName, TrainingType specialization) {
        NewTrainerRequestDTO dto = new NewTrainerRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setSpecializationName(specialization.getTrainingTypeName());
        return gymFacade.createTrainer(dto);
    }

    private NewTrainingRequestDTO newTraining(Long traineeId,
                                              Long trainerId,
                                              String name,
                                              String trainingType,
                                              LocalDate date,
                                              Integer duration) {
        NewTrainingRequestDTO dto = new NewTrainingRequestDTO();
        dto.setTraineeId(traineeId);
        dto.setTrainerId(trainerId);
        dto.setTrainingName(name);
        dto.setTrainingTypeName(trainingType);
        dto.setTrainingDate(date);
        dto.setTrainingDuration(duration);
        return dto;
    }

    private CredentialsDTO traineeCredentials(TraineeResponseDTO trainee) {
        return transactionTemplate.execute(status -> {
            Trainee entity = traineeDao.findByUsername(trainee.getUsername()).orElseThrow();
            return credentials(entity.getUsername(), entity.getPassword());
        });
    }

    private CredentialsDTO trainerCredentials(TrainerResponseDTO trainer) {
        return transactionTemplate.execute(status -> {
            Trainer entity = trainerDao.findByUsername(trainer.getUsername()).orElseThrow();
            return credentials(entity.getUsername(), entity.getPassword());
        });
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private TrainingType ensureTrainingType(String name) {
        return transactionTemplate.execute(status -> {
            var session = sessionFactory.getCurrentSession();
            TrainingType existing = session.createQuery(
                            "FROM TrainingType t WHERE LOWER(t.trainingTypeName) = LOWER(:name)",
                            TrainingType.class)
                    .setParameter("name", name)
                    .uniqueResult();
            if (existing != null) {
                return existing;
            }
            TrainingType type = new TrainingType();
            type.setTrainingTypeName(name);
            session.persist(type);
            return type;
        });
    }
}
