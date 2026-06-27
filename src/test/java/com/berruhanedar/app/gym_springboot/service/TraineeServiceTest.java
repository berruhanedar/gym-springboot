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
import java.util.Set;

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
    private TrainerDao trainerDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldCreateTraineeWithGeneratedUsernamePasswordAndActiveStatus() {
        TraineeResponseDTO response = createTrainee("  Oliver  ", "  Taylor  ", "London");

        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("Oliver.Taylor");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getAddress()).isEqualTo("London");

        Trainee savedEntity = findTraineeEntity(response.getUsername());
        assertThat(savedEntity.getPassword()).hasSize(10);
        assertThat(savedEntity.getPassword()).matches("[A-Za-z0-9]{10}");
    }

    @Test
    void shouldUpdateTraineeWithoutChangingUsernameOrPassword() {
        TraineeResponseDTO saved = createTrainee("Emily", "Johnson", "Old Address");
        CredentialsDTO credentials = traineeCredentials(saved);
        String originalPassword = credentials.getPassword();

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setId(saved.getId());
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
        assertThat(traineeCredentials(saved).getPassword()).isEqualTo(originalPassword);
    }

    @Test
    void shouldGetTraineeByIdAndUsernameWhenCredentialsAreValid() {
        TraineeResponseDTO saved = createTrainee("Mia", "Clark", "Istanbul");
        CredentialsDTO credentials = traineeCredentials(saved);

        TraineeResponseDTO byId = gymFacade.getTrainee(credentials, saved.getId());
        TraineeResponseDTO byUsername = gymFacade.getTraineeByUsername(credentials, saved.getUsername());

        assertThat(byId.getId()).isEqualTo(saved.getId());
        assertThat(byUsername.getUsername()).isEqualTo(saved.getUsername());
    }

    @Test
    void shouldChangeTraineePasswordAndRejectOldPassword() {
        TraineeResponseDTO saved = createTrainee("Noah", "Hill", null);
        CredentialsDTO oldCredentials = traineeCredentials(saved);

        gymFacade.changeTraineePassword(oldCredentials, "NewPass123");

        CredentialsDTO newCredentials = credentials(saved.getUsername(), "NewPass123");
        assertThat(gymFacade.getTrainee(newCredentials, saved.getId()).getId()).isEqualTo(saved.getId());
        assertThatThrownBy(() -> gymFacade.getTrainee(oldCredentials, saved.getId()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldToggleTraineeActivationStatus() {
        TraineeResponseDTO saved = createTrainee("Ava", "King", null);
        CredentialsDTO credentials = traineeCredentials(saved);

        TraineeResponseDTO deactivated = gymFacade.changeTraineeActivationStatus(credentials);
        TraineeResponseDTO activated = gymFacade.changeTraineeActivationStatus(credentials);

        assertThat(deactivated.getIsActive()).isFalse();
        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    void shouldDeleteTraineeByIdAndByUsername() {
        TraineeResponseDTO first = createTrainee("Delete", "ById", null);
        CredentialsDTO firstCredentials = traineeCredentials(first);
        gymFacade.deleteTrainee(firstCredentials, first.getId());
        assertThatThrownBy(() -> gymFacade.getTrainee(firstCredentials, first.getId()))
                .isInstanceOf(AuthenticationException.class);

        TraineeResponseDTO second = createTrainee("Delete", "ByUsername", null);
        CredentialsDTO secondCredentials = traineeCredentials(second);
        gymFacade.deleteTraineeByUsername(secondCredentials, second.getUsername());
        assertThatThrownBy(() -> gymFacade.getTraineeByUsername(secondCredentials, second.getUsername()))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TraineeResponseDTO trainee = createTrainee("Trainer", "Owner", null);
        TrainerResponseDTO firstTrainer = createTrainer("Assigned", "One", yoga);
        TrainerResponseDTO secondTrainer = createTrainer("Assigned", "Two", yoga);

        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername(trainee.getUsername());
        request.setTrainerIds(Set.of(firstTrainer.getId(), secondTrainer.getId()));

        TraineeResponseDTO updated = gymFacade.updateTraineeTrainers(traineeCredentials(trainee), request);

        assertThat(updated.getId()).isEqualTo(trainee.getId());
        assertThat(countAssignedTrainers(trainee.getUsername())).isEqualTo(2);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTraineeTrainersWithMissingTrainer() {
        TraineeResponseDTO trainee = createTrainee("Missing", "Trainer", null);
        UpdateTraineeTrainersRequestDTO request = new UpdateTraineeTrainersRequestDTO();
        request.setTraineeUsername(trainee.getUsername());
        request.setTrainerIds(Set.of(999L));

        assertThatThrownBy(() -> gymFacade.updateTraineeTrainers(traineeCredentials(trainee), request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("One or more trainers not found");
    }

    @Test
    void shouldThrowExpectedExceptionsForMissingTraineeAndWrongCredentials() {
        TraineeResponseDTO saved = createTrainee("Wrong", "Password", null);
        CredentialsDTO validCredentials = traineeCredentials(saved);
        CredentialsDTO wrongCredentials = credentials(saved.getUsername(), "bad-password");

        assertThatThrownBy(() -> gymFacade.getTrainee(wrongCredentials, saved.getId()))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> gymFacade.getTrainee(validCredentials, 999L))
                .isInstanceOf(EntityNotFoundException.class);

        UpdateTraineeRequestDTO update = new UpdateTraineeRequestDTO();
        update.setId(999L);
        update.setFirstName("Missing");
        update.setLastName("Trainee");
        update.setDateOfBirth(LocalDate.of(2000, 1, 1));
        update.setAddress("Nowhere");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainee(validCredentials, update))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> gymFacade.deleteTrainee(validCredentials, 999L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> gymFacade.deleteTraineeByUsername(validCredentials, "missing.username"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private TraineeResponseDTO createTrainee(String firstName, String lastName, String address) {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setAddress(address);
        return gymFacade.createTrainee(dto);
    }

    private TrainerResponseDTO createTrainer(String firstName,
                                             String lastName,
                                             TrainingType specialization) {
        NewTrainerRequestDTO dto = new NewTrainerRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setSpecializationName(specialization.getTrainingTypeName());
        return gymFacade.createTrainer(dto);
    }

    private CredentialsDTO traineeCredentials(TraineeResponseDTO trainee) {
        return transactionTemplate.execute(status -> {
            Trainee entity = traineeDao.findByUsername(trainee.getUsername()).orElseThrow();
            return credentials(entity.getUsername(), entity.getPassword());
        });
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private Trainee findTraineeEntity(String username) {
        return transactionTemplate.execute(status -> traineeDao.findByUsername(username).orElseThrow());
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
            trainee.getTrainers().size();
            return (long) trainee.getTrainers().size();
        });
    }
}
