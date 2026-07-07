package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class AuthenticationServiceTest {

    @Test
    void shouldReturnTokenWhenLoginIsSuccessful() {
        AuthenticationService service = serviceWith(trainee("trainee.user", "pass"), null);

        String token = service.login(credentials("trainee.user", "pass"));

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldAuthenticateTraineeWhenUsernameAndPasswordMatch() {
        AuthenticationService service = serviceWith(trainee("trainee.user", "pass"), null);

        assertThatCode(() -> service.authenticate(credentials("trainee.user", "pass")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenTraineePasswordDoesNotMatchOrUserMissing() {
        AuthenticationService service = serviceWith(trainee("trainee.user", "pass"), null);

        assertThatThrownBy(() -> service.authenticate(credentials("trainee.user", "wrong")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        assertThatThrownBy(() -> service.authenticate(credentials("missing", "pass")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void shouldAuthenticateTrainerWhenUsernameAndPasswordMatch() {
        AuthenticationService service = serviceWith(null, trainer("trainer.user", "pass"));

        assertThatCode(() -> service.authenticate(credentials("trainer.user", "pass")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenTrainerPasswordDoesNotMatchOrUserMissing() {
        AuthenticationService service = serviceWith(null, trainer("trainer.user", "pass"));

        assertThatThrownBy(() -> service.authenticate(credentials("trainer.user", "wrong")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        assertThatThrownBy(() -> service.authenticate(credentials("missing", "pass")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void shouldChangeTraineePasswordWhenOldPasswordMatches() {
        Trainee trainee = trainee("trainee.user", "oldPass");
        AuthenticationService service = serviceWith(trainee, null);

        service.changePassword(changePasswordRequest("trainee.user", "oldPass", "newPass"));

        assertThat(trainee.getPassword()).isEqualTo("newPass");
    }

    @Test
    void shouldChangeTrainerPasswordWhenOldPasswordMatches() {
        Trainer trainer = trainer("trainer.user", "oldPass");
        AuthenticationService service = serviceWith(null, trainer);

        service.changePassword(changePasswordRequest("trainer.user", "oldPass", "newPass"));

        assertThat(trainer.getPassword()).isEqualTo("newPass");
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordDoesNotMatch() {
        Trainee trainee = trainee("trainee.user", "oldPass");
        AuthenticationService service = serviceWith(trainee, null);

        assertThatThrownBy(() ->
                service.changePassword(changePasswordRequest("trainee.user", "wrong", "newPass")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        assertThat(trainee.getPassword()).isEqualTo("oldPass");
    }

    @Test
    void shouldRejectPasswordChangeWhenUserDoesNotExist() {
        AuthenticationService service = serviceWith(null, null);

        assertThatThrownBy(() ->
                service.changePassword(changePasswordRequest("missing", "oldPass", "newPass")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }

    private AuthenticationService serviceWith(Trainee trainee, Trainer trainer) {
        AuthenticationService service = new AuthenticationService();
        service.setTraineeDao(new FakeTraineeDao(trainee));
        service.setTrainerDao(new FakeTrainerDao(trainer));
        service.setJwtService(new JwtService());
        return service;
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private ChangePasswordRequestDTO changePasswordRequest(
            String username,
            String oldPassword,
            String newPassword) {

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername(username);
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        return request;
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
            return Optional.ofNullable(trainee)
                    .filter(t -> t.getUsername().equals(username));
        }

        @Override
        public Trainee update(Trainee trainee) {
            return trainee;
        }
    }

    private static class FakeTrainerDao extends TrainerDao {
        private final Trainer trainer;

        private FakeTrainerDao(Trainer trainer) {
            this.trainer = trainer;
        }

        @Override
        public Optional<Trainer> findByUsername(String username) {
            return Optional.ofNullable(trainer)
                    .filter(t -> t.getUsername().equals(username));
        }

        @Override
        public Trainer update(Trainer trainer) {
            return trainer;
        }
    }
}