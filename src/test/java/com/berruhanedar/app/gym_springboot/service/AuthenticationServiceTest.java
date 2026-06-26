package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationServiceTest {

    @Test
    void shouldAuthenticateTraineeWhenUsernameAndPasswordMatch() {
        AuthenticationService service = serviceWith(trainee("trainee.user", "pass"), null);

        assertThatCode(() -> service.authenticateTrainee(credentials("trainee.user", "pass")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectTraineeWhenPasswordDoesNotMatchOrUserMissing() {
        AuthenticationService service = serviceWith(trainee("trainee.user", "pass"), null);

        assertThatThrownBy(() -> service.authenticateTrainee(credentials("trainee.user", "wrong")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid trainee username or password");
        assertThatThrownBy(() -> service.authenticateTrainee(credentials("missing", "pass")))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldAuthenticateTrainerWhenUsernameAndPasswordMatch() {
        AuthenticationService service = serviceWith(null, trainer("trainer.user", "pass"));

        assertThatCode(() -> service.authenticateTrainer(credentials("trainer.user", "pass")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectTrainerWhenPasswordDoesNotMatchOrUserMissing() {
        AuthenticationService service = serviceWith(null, trainer("trainer.user", "pass"));

        assertThatThrownBy(() -> service.authenticateTrainer(credentials("trainer.user", "wrong")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid trainer username or password");
        assertThatThrownBy(() -> service.authenticateTrainer(credentials("missing", "pass")))
                .isInstanceOf(AuthenticationException.class);
    }

    private AuthenticationService serviceWith(Trainee trainee, Trainer trainer) {
        AuthenticationService service = new AuthenticationService();
        service.setTraineeDao(new FakeTraineeDao(trainee));
        service.setTrainerDao(new FakeTrainerDao(trainer));
        return service;
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private Trainee trainee(String username, String password) {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setPassword(password);
        return trainee;
    }

    private Trainer trainer(String username, String password) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword(password);
        return trainer;
    }

    private static class FakeTraineeDao extends TraineeDao {
        private final Trainee trainee;

        private FakeTraineeDao(Trainee trainee) {
            this.trainee = trainee;
        }

        @Override
        public Optional<Trainee> findByUsername(String username) {
            return Optional.ofNullable(trainee).filter(t -> t.getUsername().equals(username));
        }
    }

    private static class FakeTrainerDao extends TrainerDao {
        private final Trainer trainer;

        private FakeTrainerDao(Trainer trainer) {
            this.trainer = trainer;
        }

        @Override
        public Optional<Trainer> findByUsername(String username) {
            return Optional.ofNullable(trainer).filter(t -> t.getUsername().equals(username));
        }
    }
}
