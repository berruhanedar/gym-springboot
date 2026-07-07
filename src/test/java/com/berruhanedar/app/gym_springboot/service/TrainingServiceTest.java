package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateTrainingWhenReferencesAreValid() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Sophia", "Williams");
        RegistrationResponseDTO trainer = createTrainer("James", "Wilson", yoga);

        authenticateAs(trainer.getUsername());

        gymFacade.createTraining(
                newTraining(
                        trainee.getUsername(),
                        trainer.getUsername(),
                        "Morning Yoga",
                        LocalDate.now().plusDays(1),
                        45
                )
        );

        authenticateAs(trainee.getUsername());

        List<TraineeTrainingResponseDTO> trainings = gymFacade.getTraineeTrainings(
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

        authenticateAs(yogaTrainer.getUsername());
        gymFacade.createTraining(
                newTraining(trainee.getUsername(), yogaTrainer.getUsername(), "Yoga Match",
                        LocalDate.now().plusDays(2), 50)
        );

        authenticateAs(cardioTrainer.getUsername());
        gymFacade.createTraining(
                newTraining(trainee.getUsername(), cardioTrainer.getUsername(), "Cardio Out",
                        LocalDate.now().plusDays(2), 30)
        );

        authenticateAs(yogaTrainer.getUsername());
        gymFacade.createTraining(
                newTraining(trainee.getUsername(), yogaTrainer.getUsername(), "Old Yoga",
                        LocalDate.now().plusDays(10), 40)
        );

        authenticateAs(trainee.getUsername());

        List<TraineeTrainingResponseDTO> result = gymFacade.getTraineeTrainings(
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

        authenticateAs(trainer.getUsername());

        gymFacade.createTraining(
                newTraining(expectedTrainee.getUsername(), trainer.getUsername(), "Pilates Match",
                        LocalDate.now().plusDays(4), 55)
        );

        gymFacade.createTraining(
                newTraining(otherTrainee.getUsername(), trainer.getUsername(), "Pilates Other",
                        LocalDate.now().plusDays(4), 35)
        );

        gymFacade.createTraining(
                newTraining(expectedTrainee.getUsername(), trainer.getUsername(), "Pilates Later",
                        LocalDate.now().plusDays(20), 35)
        );

        List<TrainerTrainingResponseDTO> result = gymFacade.getTrainerTrainings(
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

        authenticateAs(trainer.getUsername());

        gymFacade.createTraining(
                newTraining(trainee.getUsername(), trainer.getUsername(), "Stretch One",
                        LocalDate.now().plusDays(1), 20)
        );

        gymFacade.createTraining(
                newTraining(trainee.getUsername(), trainer.getUsername(), "Stretch Two",
                        LocalDate.now().plusDays(2), 25)
        );

        authenticateAs(trainee.getUsername());

        List<TraineeTrainingResponseDTO> result = gymFacade.getTraineeTrainings(
                trainee.getUsername(),
                traineeFilter(null, null, " ", "")
        );

        assertThat(result)
                .extracting(TraineeTrainingResponseDTO::getTrainingName)
                .containsExactlyInAnyOrder("Stretch One", "Stretch Two");
    }

    @Test
    void shouldThrowExceptionWhenTraineeDoesNotExist() {
        TrainingType yoga = ensureTrainingType("Yoga");
        RegistrationResponseDTO trainer = createTrainer("Valid", "Trainer", yoga);

        authenticateAs(trainer.getUsername());

        assertThatThrownBy(() ->
                gymFacade.createTraining(
                        newTraining(
                                "missing.trainee",
                                trainer.getUsername(),
                                "Training",
                                LocalDate.now().plusDays(1),
                                60)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExist() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Lily", "Scott");
        RegistrationResponseDTO trainer = createTrainer("Valid", "Trainer", yoga);

        authenticateAs(trainer.getUsername());

        assertThatThrownBy(() ->
                gymFacade.createTraining(
                        newTraining(
                                trainee.getUsername(),
                                "missing.trainer",
                                "Training",
                                LocalDate.now().plusDays(1),
                                60)))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Trainer is not authorized");
    }

    @Test
    void shouldRejectTrainingOperationsWhenAuthenticatedUserIsNotAuthorized() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Auth", "Trainee");
        RegistrationResponseDTO trainer = createTrainer("Auth", "Trainer", yoga);

        authenticateAs("another.user");

        assertThatThrownBy(() -> gymFacade.createTraining(
                newTraining(trainee.getUsername(), trainer.getUsername(), "Auth Fail",
                        LocalDate.now().plusDays(1), 45)
        ))
                .isInstanceOf(AuthenticationException.class);

        assertThatThrownBy(() -> gymFacade.getTraineeTrainings(
                trainee.getUsername(),
                traineeFilter(null, null, null, null)
        ))
                .isInstanceOf(AuthenticationException.class);

        assertThatThrownBy(() -> gymFacade.getTrainerTrainings(
                trainer.getUsername(),
                trainerFilter(null, null, null)
        ))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldReturnTrainingTypes() {
        ensureTrainingType("Yoga");
        ensureTrainingType("Cardio");
        ensureTrainingType("Pilates");

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

    private void authenticateAs(String username) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
