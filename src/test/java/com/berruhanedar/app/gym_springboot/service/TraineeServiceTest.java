package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
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
class TraineeServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldCreateTraineeWithGeneratedUsernamePasswordAndActiveStatus() {
        RegistrationResponseDTO response = createTrainee("  Oliver  ", "  Taylor  ", "London");

        assertThat(response.getUsername()).isEqualTo("Oliver.Taylor");
        assertThat(response.getPassword()).hasSize(10);
        assertThat(response.getPassword()).matches("[A-Za-z0-9]{10}");

        Trainee savedEntity = findTraineeEntity(response.getUsername());
        assertThat(savedEntity.getIsActive()).isTrue();
        assertThat(savedEntity.getAddress()).isEqualTo("London");
    }

    @Test
    void shouldUpdateTraineeWithoutChangingUsernameOrPassword() {
        RegistrationResponseDTO saved = createTrainee("Emily", "Johnson", "Old Address");
        CredentialsDTO credentials = credentials(saved.getUsername(), saved.getPassword());

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setUsername(saved.getUsername());
        update.setFirstName("Emma");
        update.setLastName("Stone");
        update.setDateOfBirth(LocalDate.of(1998, 5, 10));
        update.setAddress("New York");
        update.setIsActive(false);

        TraineeResponseDTO updated = gymFacade.updateTrainee(credentials, update);

        assertThat(updated.getFirstName()).isEqualTo("Emma");
        assertThat(updated.getLastName()).isEqualTo("Stone");
        assertThat(updated.getUsername()).isEqualTo(saved.getUsername());
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getAddress()).isEqualTo("New York");

        Trainee entity = findTraineeEntity(saved.getUsername());
        assertThat(entity.getPassword()).isEqualTo(saved.getPassword());
    }

    @Test
    void shouldGetTraineeByUsernameWhenCredentialsAreValid() {
        RegistrationResponseDTO saved = createTrainee("Mia", "Clark", "Istanbul");
        CredentialsDTO credentials = credentials(saved.getUsername(), saved.getPassword());

        TraineeResponseDTO response = gymFacade.getTraineeByUsername(credentials, saved.getUsername());

        assertThat(response.getUsername()).isEqualTo(saved.getUsername());
        assertThat(response.getFirstName()).isEqualTo("Mia");
        assertThat(response.getLastName()).isEqualTo("Clark");
        assertThat(response.getAddress()).isEqualTo("Istanbul");
    }

    @Test
    void shouldChangePasswordAndRejectOldPassword() {
        RegistrationResponseDTO saved = createTrainee("Noah", "Hill", null);

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername(saved.getUsername());
        request.setOldPassword(saved.getPassword());
        request.setNewPassword("NewPass123");

        gymFacade.changePassword(request);

        CredentialsDTO newCredentials = credentials(saved.getUsername(), "NewPass123");

        assertThatCode(() -> gymFacade.getTraineeByUsername(newCredentials, saved.getUsername()))
                .doesNotThrowAnyException();

        CredentialsDTO oldCredentials = credentials(saved.getUsername(), saved.getPassword());

        assertThatThrownBy(() -> gymFacade.getTraineeByUsername(oldCredentials, saved.getUsername()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldUpdateTraineeActivationStatus() {
        RegistrationResponseDTO saved = createTrainee("Ava", "King", null);
        CredentialsDTO credentials = credentials(saved.getUsername(), saved.getPassword());

        UpdateActivationStatusDTO deactivate = new UpdateActivationStatusDTO();
        deactivate.setUsername(saved.getUsername());
        deactivate.setIsActive(false);

        gymFacade.changeTraineeActivationStatus(credentials, deactivate);

        TraineeResponseDTO deactivated =
                gymFacade.getTraineeByUsername(credentials, saved.getUsername());

        assertThat(deactivated.getIsActive()).isFalse();

        UpdateActivationStatusDTO activate = new UpdateActivationStatusDTO();
        activate.setUsername(saved.getUsername());
        activate.setIsActive(true);

        gymFacade.changeTraineeActivationStatus(credentials, activate);

        TraineeResponseDTO activated =
                gymFacade.getTraineeByUsername(credentials, saved.getUsername());

        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        RegistrationResponseDTO saved = createTrainee("Delete", "ByUsername", null);
        CredentialsDTO credentials = credentials(saved.getUsername(), saved.getPassword());

        gymFacade.deleteTraineeByUsername(credentials, saved.getUsername());

        assertThatThrownBy(() -> gymFacade.getTraineeByUsername(credentials, saved.getUsername()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Trainer", "Owner", null);
        RegistrationResponseDTO firstTrainer = createTrainer("Assigned", "One", yoga);
        RegistrationResponseDTO secondTrainer = createTrainer("Assigned", "Two", yoga);

        TrainerUsernameDTO first = new TrainerUsernameDTO();
        first.setUsername(firstTrainer.getUsername());

        TrainerUsernameDTO second = new TrainerUsernameDTO();
        second.setUsername(secondTrainer.getUsername());

        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername(trainee.getUsername());
        request.setTrainers(List.of(first, second));

        List<TrainerSummaryDTO> updated =
                gymFacade.updateTraineeTrainers(credentials(trainee.getUsername(), trainee.getPassword()), request);

        assertThat(updated).hasSize(2);
        assertThat(countAssignedTrainers(trainee.getUsername())).isEqualTo(2);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTraineeTrainersWithMissingTrainer() {
        RegistrationResponseDTO trainee = createTrainee("Missing", "Trainer", null);

        TrainerUsernameDTO missingTrainer = new TrainerUsernameDTO();
        missingTrainer.setUsername("missing.trainer");

        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername(trainee.getUsername());
        request.setTrainers(List.of(missingTrainer));

        assertThatThrownBy(() ->
                gymFacade.updateTraineeTrainers(
                        credentials(trainee.getUsername(), trainee.getPassword()),
                        request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    void shouldThrowExpectedExceptionsForMissingTraineeAndWrongCredentials() {
        RegistrationResponseDTO saved = createTrainee("Wrong", "Password", null);
        CredentialsDTO validCredentials = credentials(saved.getUsername(), saved.getPassword());
        CredentialsDTO wrongCredentials = credentials(saved.getUsername(), "bad-password");

        assertThatThrownBy(() -> gymFacade.getTraineeByUsername(wrongCredentials, saved.getUsername()))
                .isInstanceOf(AuthenticationException.class);

        assertThatThrownBy(() -> gymFacade.getTraineeByUsername(validCredentials, "missing.username"))
                .isInstanceOf(EntityNotFoundException.class);

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setUsername("missing.username");
        update.setFirstName("Missing");
        update.setLastName("Trainee");
        update.setDateOfBirth(LocalDate.of(2000, 1, 1));
        update.setAddress("Nowhere");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainee(validCredentials, update))
                .isInstanceOf(EntityNotFoundException.class);

        assertThatThrownBy(() -> gymFacade.deleteTraineeByUsername(validCredentials, "missing.username"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private RegistrationResponseDTO createTrainee(String firstName, String lastName, String address) {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setAddress(address);
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

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private Trainee findTraineeEntity(String username) {
        return transactionTemplate.execute(status ->
                traineeDao.findByUsername(username).orElseThrow());
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

    private long countAssignedTrainers(String traineeUsername) {
        return transactionTemplate.execute(status -> {
            Trainee trainee = traineeDao.findByUsername(traineeUsername).orElseThrow();
            return (long) trainee.getTrainers().size();
        });
    }
}