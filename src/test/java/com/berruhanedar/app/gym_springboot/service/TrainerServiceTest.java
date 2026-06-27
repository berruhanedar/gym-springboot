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
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TrainerServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void shouldCreateTrainerWithGeneratedUsernamePasswordAndActiveStatus() {
        TrainingType boxing = ensureTrainingType("Boxing");

        TrainerResponseDTO response = createTrainer("Daniel", "Anderson", boxing);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getSpecializationName())
                .isEqualTo("Boxing");
        assertThat(findTrainerEntity(response.getUsername()).getPassword()).hasSize(10);
    }

    @Test
    void shouldGenerateUniqueUsernameAcrossTraineesAndTrainersWithIncrementedSuffix() {
        TrainingType fitness = ensureTrainingType("Fitness");

        TrainerResponseDTO first = createTrainer("Michael", "Brown", fitness);
        TraineeResponseDTO second = createTrainee("Michael", "Brown");
        TrainerResponseDTO third = createTrainer("Michael", "Brown", fitness);

        assertThat(first.getUsername()).isEqualTo("Michael.Brown");
        assertThat(second.getUsername()).isEqualTo("Michael.Brown1");
        assertThat(third.getUsername()).isEqualTo("Michael.Brown2");
    }

    @Test
    void shouldUpdateTrainerWithoutChangingUsernameOrPassword() {
        TrainingType fitness = ensureTrainingType("Fitness");
        TrainingType pilates = ensureTrainingType("Pilates");
        TrainerResponseDTO saved = createTrainer("Thomas", "White", fitness);
        CredentialsDTO credentials = trainerCredentials(saved);
        String oldPassword = credentials.getPassword();

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setId(saved.getId());
        update.setFirstName("Tom");
        update.setLastName("Black");
        update.setSpecializationName("Pilates");
        update.setIsActive(false);

        TrainerResponseDTO updated = gymFacade.updateTrainer(credentials, update);

        assertThat(updated.getFirstName()).isEqualTo("Tom");
        assertThat(updated.getLastName()).isEqualTo("Black");
        assertThat(updated.getUsername()).isEqualTo(saved.getUsername());
        assertThat(updated.getSpecializationName())
                .isEqualTo("Pilates");
        assertThat(updated.getIsActive()).isFalse();
        assertThat(trainerCredentials(saved).getPassword()).isEqualTo(oldPassword);
    }

    @Test
    void shouldGetTrainerByIdAndUsernameWhenCredentialsAreValid() {
        TrainerResponseDTO saved = createTrainer("Laura", "Green", ensureTrainingType("Pilates"));
        CredentialsDTO credentials = trainerCredentials(saved);

        TrainerResponseDTO byId = gymFacade.getTrainer(credentials, saved.getId());
        TrainerResponseDTO byUsername = gymFacade.getTrainerByUsername(credentials, saved.getUsername());

        assertThat(byId.getId()).isEqualTo(saved.getId());
        assertThat(byUsername.getUsername()).isEqualTo(saved.getUsername());
    }

    @Test
    void shouldChangeTrainerPasswordAndToggleActivationStatus() {
        TrainerResponseDTO saved = createTrainer("Password", "Trainer", ensureTrainingType("Cardio"));
        CredentialsDTO oldCredentials = trainerCredentials(saved);

        gymFacade.changeTrainerPassword(oldCredentials, "TrainerPass123");
        CredentialsDTO newCredentials = credentials(saved.getUsername(), "TrainerPass123");

        assertThat(gymFacade.getTrainer(newCredentials, saved.getId()).getId()).isEqualTo(saved.getId());
        assertThatThrownBy(() -> gymFacade.getTrainer(oldCredentials, saved.getId()))
                .isInstanceOf(AuthenticationException.class);

        TrainerResponseDTO deactivated = gymFacade.changeTrainerActivationStatus(newCredentials);
        TrainerResponseDTO activated = gymFacade.changeTrainerActivationStatus(newCredentials);

        assertThat(deactivated.getIsActive()).isFalse();
        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    void shouldReturnOnlyTrainersNotAssignedToTrainee() {
        TrainingType yoga = ensureTrainingType("Yoga");
        TraineeResponseDTO trainee = createTrainee("Assigned", "Trainee");
        TrainerResponseDTO assigned = createTrainer("Assigned", "Trainer", yoga);
        TrainerResponseDTO available = createTrainer("Available", "Trainer", yoga);
        TrainerResponseDTO requester = createTrainer("Requester", "Trainer", yoga);

        UpdateTraineeTrainersRequestDTO update = new UpdateTraineeTrainersRequestDTO();
        update.setTraineeUsername(trainee.getUsername());
        update.setTrainerIds(Set.of(assigned.getId()));
        gymFacade.updateTraineeTrainers(traineeCredentials(trainee), update);

        assertThat(gymFacade.getTrainersNotAssignedToTrainee(trainerCredentials(requester), trainee.getUsername()))
                .extracting(TrainerResponseDTO::getUsername)
                .contains(available.getUsername(), requester.getUsername())
                .doesNotContain(assigned.getUsername());
    }

    @Test
    void shouldThrowExpectedExceptionsForMissingTrainerAndWrongCredentials() {
        TrainerResponseDTO saved = createTrainer("Wrong", "Trainer", ensureTrainingType("Yoga"));
        CredentialsDTO validCredentials = trainerCredentials(saved);
        CredentialsDTO wrongCredentials = credentials(saved.getUsername(), "bad-password");

        assertThatThrownBy(() -> gymFacade.getTrainer(wrongCredentials, saved.getId()))
                .isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> gymFacade.getTrainer(validCredentials, 999L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> gymFacade.getTrainerByUsername(validCredentials, "missing.username"))
                .isInstanceOf(EntityNotFoundException.class);

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setId(999L);
        update.setFirstName("Missing");
        update.setLastName("Trainer");
        update.setSpecializationName("Fitness");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainer(validCredentials, update))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private TraineeResponseDTO createTrainee(String firstName, String lastName) {
        NewTraineeRequestDTO dto = new NewTraineeRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return gymFacade.createTrainee(dto);
    }

    private TrainerResponseDTO createTrainer(
            String firstName,
            String lastName,
            TrainingType specialization) {

        NewTrainerRequestDTO dto = new NewTrainerRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setSpecializationName(specialization.getTrainingTypeName());

        return gymFacade.createTrainer(dto);
    }

    private CredentialsDTO trainerCredentials(TrainerResponseDTO trainer) {
        return transactionTemplate.execute(status -> {
            Trainer entity = trainerDao.findByUsername(trainer.getUsername()).orElseThrow();
            return credentials(entity.getUsername(), entity.getPassword());
        });
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

    private Trainer findTrainerEntity(String username) {
        return transactionTemplate.execute(status -> trainerDao.findByUsername(username).orElseThrow());
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
