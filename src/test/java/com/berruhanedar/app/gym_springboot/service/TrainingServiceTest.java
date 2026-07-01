package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dto.*;
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
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldCreateTrainingWhenCredentialsAndReferencesAreValid() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Sophia", "Williams");
        RegistrationResponseDTO trainer = createTrainer("James", "Wilson", yoga);

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(
                        trainee.getUsername(),
                        trainer.getUsername(),
                        "Morning Yoga",
                        LocalDate.now().plusDays(1),
                        45
                )
        );

        List<TrainingResponseDTO> trainings = gymFacade.getTraineeTrainings(
                credentials(trainee),
                trainee.getUsername(),
                traineeFilter(null, null, null, null)
        );

        assertThat(trainings).hasSize(1);
        assertThat(trainings.get(0).getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(trainings.get(0).getTrainingTypeName()).isEqualTo("Yoga");
        assertThat(trainings.get(0).getTrainingDuration()).isEqualTo(45);
        assertThat(trainings.get(0).getTrainerName()).isEqualTo("James Wilson");
    }

    @Test
    void shouldFilterTraineeTrainingsByDateTrainerNameAndTrainingType() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TrainingType cardio = ensureTrainingType("Cardio");

        RegistrationResponseDTO trainee = createTrainee("Grace", "Hall");
        RegistrationResponseDTO yogaTrainer = createTrainer("Henry", "Young", yoga);
        RegistrationResponseDTO cardioTrainer = createTrainer("Liam", "Stone", cardio);

        gymFacade.createTraining(
                credentials(yogaTrainer),
                newTraining(trainee.getUsername(), yogaTrainer.getUsername(), "Yoga Match",
                        LocalDate.now().plusDays(2), 50)
        );

        gymFacade.createTraining(
                credentials(cardioTrainer),
                newTraining(trainee.getUsername(), cardioTrainer.getUsername(), "Cardio Out",
                        LocalDate.now().plusDays(2), 30)
        );

        gymFacade.createTraining(
                credentials(yogaTrainer),
                newTraining(trainee.getUsername(), yogaTrainer.getUsername(), "Old Yoga",
                        LocalDate.now().plusDays(10), 40)
        );

        List<TrainingResponseDTO> result = gymFacade.getTraineeTrainings(
                credentials(trainee),
                trainee.getUsername(),
                traineeFilter(
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3),
                        "Henry You",
                        "Yoga"
                )
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Yoga Match");
    }

    @Test
    void shouldFilterTrainerTrainingsByDateAndTraineeName() {
        TrainingType pilates = ensureTrainingType("Pilates");

        RegistrationResponseDTO trainer = createTrainer("Trainer", "Filter", pilates);
        RegistrationResponseDTO expectedTrainee = createTrainee("Expected", "Person");
        RegistrationResponseDTO otherTrainee = createTrainee("Other", "Person");

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(expectedTrainee.getUsername(), trainer.getUsername(), "Pilates Match",
                        LocalDate.now().plusDays(4), 55)
        );

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(otherTrainee.getUsername(), trainer.getUsername(), "Pilates Other",
                        LocalDate.now().plusDays(4), 35)
        );

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(expectedTrainee.getUsername(), trainer.getUsername(), "Pilates Later",
                        LocalDate.now().plusDays(20), 35)
        );

        List<TrainingResponseDTO> result = gymFacade.getTrainerTrainings(
                credentials(trainer),
                trainer.getUsername(),
                trainerFilter(
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(5),
                        "Expected Per"
                )
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Pilates Match");
        assertThat(result.get(0).getTraineeName()).isEqualTo("Expected Person");
    }

    @Test
    void shouldReturnAllMatchingTrainingsWhenOptionalCriteriaAreBlankOrNull() {
        TrainingType stretching = ensureTrainingType("Stretching");

        RegistrationResponseDTO trainee = createTrainee("Blank", "Criteria");
        RegistrationResponseDTO trainer = createTrainer("Blank", "Trainer", stretching);

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(trainee.getUsername(), trainer.getUsername(), "Stretch One",
                        LocalDate.now().plusDays(1), 20)
        );

        gymFacade.createTraining(
                credentials(trainer),
                newTraining(trainee.getUsername(), trainer.getUsername(), "Stretch Two",
                        LocalDate.now().plusDays(2), 25)
        );

        List<TrainingResponseDTO> result = gymFacade.getTraineeTrainings(
                credentials(trainee),
                trainee.getUsername(),
                traineeFilter(null, null, " ", "")
        );

        assertThat(result)
                .extracting(TrainingResponseDTO::getTrainingName)
                .containsExactlyInAnyOrder("Stretch One", "Stretch Two");
    }

    @Test
    void shouldThrowExceptionWhenTrainingReferencesAreMissing() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Lily", "Scott");
        RegistrationResponseDTO trainer = createTrainer("Valid", "Trainer", yoga);

        assertThatThrownBy(() -> gymFacade.createTraining(
                credentials(trainer),
                newTraining("missing.trainee", trainer.getUsername(), "Invalid Trainee",
                        LocalDate.now().plusDays(1), 60)
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");

        assertThatThrownBy(() -> gymFacade.createTraining(
                credentials(trainer),
                newTraining(trainee.getUsername(), "missing.trainer", "Invalid Trainer",
                        LocalDate.now().plusDays(1), 60)
        ))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    void shouldRejectTrainingOperationsWhenCredentialsAreInvalid() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Auth", "Trainee");
        RegistrationResponseDTO trainer = createTrainer("Auth", "Trainer", yoga);

        CredentialsDTO wrongTrainerCredentials = credentials(trainer.getUsername(), "wrong-pass");
        CredentialsDTO wrongTraineeCredentials = credentials(trainee.getUsername(), "wrong-pass");

        assertThatThrownBy(() -> gymFacade.createTraining(
                wrongTrainerCredentials,
                newTraining(trainee.getUsername(), trainer.getUsername(), "Auth Fail",
                        LocalDate.now().plusDays(1), 45)
        ))
                .isInstanceOf(AuthenticationException.class);

        assertThatThrownBy(() -> gymFacade.getTraineeTrainings(
                wrongTraineeCredentials,
                trainee.getUsername(),
                traineeFilter(null, null, null, null)
        ))
                .isInstanceOf(AuthenticationException.class);

        assertThatThrownBy(() -> gymFacade.getTrainerTrainings(
                wrongTrainerCredentials,
                trainer.getUsername(),
                trainerFilter(null, null, null)
        ))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldReturnTrainingTypes() {
        ensureTrainingType("Yoga");
        ensureTrainingType("Cardio");

        List<TrainingTypeResponseDTO> result = gymFacade.getTrainingTypes();

        assertThat(result)
                .extracting(TrainingTypeResponseDTO::getTrainingTypeName)
                .contains("Yoga", "Cardio");
    }

    private RegistrationResponseDTO createTrainee(String firstName, String lastName) {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return gymFacade.createTrainee(dto);
    }

    private RegistrationResponseDTO createTrainer(
            String firstName,
            String lastName,
            TrainingType specialization) {

        NewTrainerRequestDTO dto = new NewTrainerRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setSpecializationName(specialization.getTrainingTypeName());
        return gymFacade.createTrainer(dto);
    }

    private NewTrainingRequestDTO newTraining(
            String traineeUsername,
            String trainerUsername,
            String name,
            LocalDate date,
            Integer duration) {

        NewTrainingRequestDTO dto = new NewTrainingRequestDTO();
        dto.setTraineeUsername(traineeUsername);
        dto.setTrainerUsername(trainerUsername);
        dto.setTrainingName(name);
        dto.setTrainingDate(date);
        dto.setTrainingDuration(duration);
        return dto;
    }

    private CredentialsDTO credentials(RegistrationResponseDTO response) {
        return credentials(response.getUsername(), response.getPassword());
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private TraineeTrainingsFilterDTO traineeFilter(
            LocalDate periodFrom,
            LocalDate periodTo,
            String trainerName,
            String trainingType) {

        TraineeTrainingsFilterDTO filter = new TraineeTrainingsFilterDTO();
        filter.setPeriodFrom(periodFrom);
        filter.setPeriodTo(periodTo);
        filter.setTrainerName(trainerName);
        filter.setTrainingType(trainingType);
        return filter;
    }

    private TrainerTrainingsFilterDTO trainerFilter(
            LocalDate periodFrom,
            LocalDate periodTo,
            String traineeName) {

        TrainerTrainingsFilterDTO filter = new TrainerTrainingsFilterDTO();
        filter.setPeriodFrom(periodFrom);
        filter.setPeriodTo(periodTo);
        filter.setTraineeName(traineeName);
        return filter;
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