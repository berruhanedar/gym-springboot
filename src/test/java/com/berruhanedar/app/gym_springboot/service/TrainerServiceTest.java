package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
class TrainerServiceTest {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateTrainerWithGeneratedUsernamePasswordAndActiveStatus() {
        TrainingType boxing = ensureTrainingType("Boxing");

        RegistrationResponseDTO response = createTrainer("Daniel", "Anderson", boxing);

        assertThat(response.getUsername()).isEqualTo("Daniel.Anderson");
        assertThat(response.getPassword()).hasSize(10);

        authenticateAs(response.getUsername());

        TrainerResponseDTO profile = gymFacade.getTrainerByUsername(response.getUsername());

        assertThat(profile.getIsActive()).isTrue();
        assertThat(profile.getSpecializationName()).isEqualTo("Boxing");
    }

    @Test
    void shouldGenerateUniqueUsernameAcrossTraineesAndTrainersWithIncrementedSuffix() {
        TrainingType fitness = ensureTrainingType("Fitness");

        RegistrationResponseDTO first = createTrainer("Michael", "Brown", fitness);
        RegistrationResponseDTO second = createTrainee("Michael", "Brown");
        RegistrationResponseDTO third = createTrainer("Michael", "Brown", fitness);

        assertThat(first.getUsername()).isEqualTo("Michael.Brown");
        assertThat(second.getUsername()).isEqualTo("Michael.Brown1");
        assertThat(third.getUsername()).isEqualTo("Michael.Brown2");
    }

    @Test
    void shouldUpdateTrainerWithoutChangingUsernamePasswordOrSpecialization() {
        TrainingType fitness = ensureTrainingType("Fitness");
        RegistrationResponseDTO saved = createTrainer("Thomas", "White", fitness);

        authenticateAs(saved.getUsername());

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setUsername(saved.getUsername());
        update.setFirstName("Tom");
        update.setLastName("Black");
        update.setSpecializationName("Fitness");
        update.setIsActive(false);

        TrainerResponseDTO updated = gymFacade.updateTrainer(update);

        assertThat(updated.getFirstName()).isEqualTo("Tom");
        assertThat(updated.getLastName()).isEqualTo("Black");
        assertThat(updated.getUsername()).isEqualTo(saved.getUsername());
        assertThat(updated.getSpecializationName()).isEqualTo("Fitness");
        assertThat(updated.getIsActive()).isFalse();

        Trainer entity = findTrainerEntity(saved.getUsername());
        assertThat(entity.getPassword()).isEqualTo(saved.getPassword());
    }

    @Test
    void shouldGetTrainerByUsernameWhenAuthenticatedUserOwnsProfile() {
        RegistrationResponseDTO saved = createTrainer("Laura", "Green", ensureTrainingType("Pilates"));

        authenticateAs(saved.getUsername());

        TrainerResponseDTO response = gymFacade.getTrainerByUsername(saved.getUsername());

        assertThat(response.getUsername()).isEqualTo(saved.getUsername());
        assertThat(response.getFirstName()).isEqualTo("Laura");
        assertThat(response.getLastName()).isEqualTo("Green");
        assertThat(response.getSpecializationName()).isEqualTo("Pilates");
    }

    @Test
    void shouldChangeTrainerPasswordAndActivationStatus() {
        RegistrationResponseDTO saved = createTrainer("Password", "Trainer", ensureTrainingType("Cardio"));

        ChangePasswordRequestDTO passwordRequest = new ChangePasswordRequestDTO();
        passwordRequest.setUsername(saved.getUsername());
        passwordRequest.setOldPassword(saved.getPassword());
        passwordRequest.setNewPassword("TrainerPass123");

        gymFacade.changePassword(passwordRequest);

        assertThatCode(() -> gymFacade.authenticate(credentials(saved.getUsername(), "TrainerPass123")))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> gymFacade.authenticate(credentials(saved.getUsername(), saved.getPassword())))
                .isInstanceOf(AuthenticationException.class);

        authenticateAs(saved.getUsername());

        UpdateActivationStatusDTO deactivate = new UpdateActivationStatusDTO();
        deactivate.setUsername(saved.getUsername());
        deactivate.setIsActive(false);

        gymFacade.changeTrainerActivationStatus(deactivate);

        TrainerResponseDTO deactivated = gymFacade.getTrainerByUsername(saved.getUsername());

        assertThat(deactivated.getIsActive()).isFalse();

        UpdateActivationStatusDTO activate = new UpdateActivationStatusDTO();
        activate.setUsername(saved.getUsername());
        activate.setIsActive(true);

        gymFacade.changeTrainerActivationStatus(activate);

        TrainerResponseDTO activated = gymFacade.getTrainerByUsername(saved.getUsername());

        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    void shouldReturnOnlyActiveTrainersNotAssignedToTrainee() {
        TrainingType yoga = ensureTrainingType("Yoga");

        RegistrationResponseDTO trainee = createTrainee("Assigned", "Trainee");
        RegistrationResponseDTO assigned = createTrainer("Assigned", "Trainer", yoga);
        RegistrationResponseDTO available = createTrainer("Available", "Trainer", yoga);
        RegistrationResponseDTO requester = createTrainer("Requester", "Trainer", yoga);

        authenticateAs(trainee.getUsername());

        TrainerUsernameDTO assignedTrainer = new TrainerUsernameDTO();
        assignedTrainer.setUsername(assigned.getUsername());

        UpdateTraineeTrainersRequestDTO update = new UpdateTraineeTrainersRequestDTO();
        update.setTraineeUsername(trainee.getUsername());
        update.setTrainers(List.of(assignedTrainer));

        gymFacade.updateTraineeTrainers(update);

        authenticateAs(requester.getUsername());

        List<TrainerSummaryDTO> result =
                gymFacade.getTrainersNotAssignedToTrainee(trainee.getUsername());

        assertThat(result)
                .extracting(TrainerSummaryDTO::getUsername)
                .contains(available.getUsername(), requester.getUsername())
                .doesNotContain(assigned.getUsername());
    }

    @Test
    void shouldThrowExpectedExceptionsForMissingTrainerAndUnauthorizedUser() {
        RegistrationResponseDTO saved = createTrainer("Wrong", "Trainer", ensureTrainingType("Yoga"));

        authenticateAs("another.user");

        assertThatThrownBy(() -> gymFacade.getTrainerByUsername(saved.getUsername()))
                .isInstanceOf(AuthenticationException.class);

        authenticateAs(saved.getUsername());

        assertThatThrownBy(() -> gymFacade.getTrainerByUsername("missing.username"))
                .isInstanceOf(EntityNotFoundException.class);

        UpdateTrainerRequestDTO update = new UpdateTrainerRequestDTO();
        update.setUsername("missing.username");
        update.setFirstName("Missing");
        update.setLastName("Trainer");
        update.setSpecializationName("Yoga");
        update.setIsActive(true);

        assertThatThrownBy(() -> gymFacade.updateTrainer(update))
                .isInstanceOf(EntityNotFoundException.class);
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

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private Trainer findTrainerEntity(String username) {
        return transactionTemplate.execute(status ->
                trainerDao.findByUsername(username).orElseThrow());
    }

    private void authenticateAs(String username) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private TrainingType ensureTrainingType(String name) {
        return transactionTemplate.execute(status -> {
            List<TrainingType> existingTypes = entityManager.createQuery(
                            """
                            SELECT t
                            FROM TrainingType t
                            WHERE LOWER(t.trainingTypeName) = LOWER(:name)
                            """,
                            TrainingType.class
                    )
                    .setParameter("name", name)
                    .setMaxResults(1)
                    .getResultList();

            if (!existingTypes.isEmpty()) {
                return existingTypes.get(0);
            }

            TrainingType trainingType = new TrainingType();
            trainingType.setTrainingTypeName(name);

            entityManager.persist(trainingType);
            entityManager.flush();

            return trainingType;
        });
    }
}