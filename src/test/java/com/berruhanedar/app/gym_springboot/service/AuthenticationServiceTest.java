package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.AccountTemporarilyBlockedException;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.monitoring.GymMetrics;
import com.berruhanedar.app.gym_springboot.security.LoginAttemptService;
import com.berruhanedar.app.gym_springboot.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private final TraineeDao traineeDao = mock(TraineeDao.class);
    private final TrainerDao trainerDao = mock(TrainerDao.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
    private final TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
    private final GymMetrics gymMetrics = mock(GymMetrics.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService();
        service.setTraineeDao(traineeDao);
        service.setTrainerDao(trainerDao);
        service.setJwtService(jwtService);
        service.setPasswordEncoder(passwordEncoder);
        service.setAuthenticationManager(authenticationManager);
        service.setLoginAttemptService(loginAttemptService);
        service.setTokenBlacklistService(tokenBlacklistService);
        service.setGymMetrics(gymMetrics);
    }

    @Test
    void shouldReturnTokenAndResetAttemptsWhenLoginIsSuccessful() {
        CredentialsDTO credentials = credentials("trainee.user", "pass");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("trainee.user", null);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken("trainee.user")).thenReturn("jwt-token");

        assertThat(service.login(credentials)).isEqualTo("jwt-token");

        verify(loginAttemptService).loginSucceeded("trainee.user");
        verify(gymMetrics).recordSuccessfulLogin();
        verify(jwtService).generateToken("trainee.user");
    }

    @Test
    void shouldRecordFailedAttemptAndReturnUnauthorizedBeforeThirdFailure() {
        CredentialsDTO credentials = credentials("trainee.user", "wrong");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));
        when(loginAttemptService.isBlocked("trainee.user")).thenReturn(false, false);

        assertThatThrownBy(() -> service.login(credentials))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        verify(loginAttemptService).loginFailed("trainee.user");
        verify(gymMetrics).recordFailedLogin();
    }

    @Test
    void shouldBlockUserAfterThirdUnsuccessfulLogin() {
        CredentialsDTO credentials = credentials("trainee.user", "wrong");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));
        when(loginAttemptService.isBlocked("trainee.user")).thenReturn(false, true);

        assertThatThrownBy(() -> service.login(credentials))
                .isInstanceOf(AccountTemporarilyBlockedException.class)
                .hasMessageContaining("blocked for 5 minutes");
    }

    @Test
    void shouldRejectLoginImmediatelyWhenUserIsAlreadyBlocked() {
        when(loginAttemptService.isBlocked("blocked.user")).thenReturn(true);
        when(loginAttemptService.getRemainingBlockSeconds("blocked.user")).thenReturn(120L);

        assertThatThrownBy(() -> service.login(credentials("blocked.user", "pass")))
                .isInstanceOf(AccountTemporarilyBlockedException.class)
                .hasMessageContaining("120 seconds");

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void shouldChangeTraineePasswordUsingBcrypt() {
        Trainee trainee = trainee("trainee.user", passwordEncoder.encode("oldPass"));
        when(traineeDao.findByUsername("trainee.user")).thenReturn(Optional.of(trainee));

        service.changePassword(changePasswordRequest("trainee.user", "oldPass", "newPass"));

        assertThat(passwordEncoder.matches("newPass", trainee.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldPass", trainee.getPassword())).isFalse();
        verify(traineeDao).update(trainee);
    }

    @Test
    void shouldChangeTrainerPasswordUsingBcrypt() {
        Trainer trainer = trainer("trainer.user", passwordEncoder.encode("oldPass"));
        when(traineeDao.findByUsername("trainer.user")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("trainer.user")).thenReturn(Optional.of(trainer));

        service.changePassword(changePasswordRequest("trainer.user", "oldPass", "newPass"));

        assertThat(passwordEncoder.matches("newPass", trainer.getPassword())).isTrue();
        verify(trainerDao).update(trainer);
    }

    @Test
    void shouldRejectPasswordChangeForWrongOldPassword() {
        Trainee trainee = trainee("trainee.user", passwordEncoder.encode("oldPass"));
        when(traineeDao.findByUsername("trainee.user")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() ->
                service.changePassword(changePasswordRequest("trainee.user", "wrong", "newPass")))
                .isInstanceOf(AuthenticationException.class);

        assertThat(passwordEncoder.matches("oldPass", trainee.getPassword())).isTrue();
        verify(traineeDao, never()).update(any());
    }

    @Test
    void shouldRejectPasswordChangeWhenUserDoesNotExist() {
        when(traineeDao.findByUsername("missing")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.changePassword(changePasswordRequest("missing", "oldPass", "newPass")))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldBlacklistTokenDuringLogout() {
        Instant expiration = Instant.now().plusSeconds(600);
        when(jwtService.extractExpiration("valid-token")).thenReturn(expiration);

        service.logout("Bearer valid-token");

        verify(tokenBlacklistService).blacklist("valid-token", expiration);
    }

    @Test
    void shouldRejectLogoutWithInvalidAuthorizationHeader() {
        assertThatThrownBy(() -> service.logout("invalid-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid authorization header");

        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void shouldDetectExistingAndActiveUsers() {
        Trainee trainee = trainee("trainee.user", "encoded");
        trainee.setIsActive(true);
        when(traineeDao.findByUsername("trainee.user")).thenReturn(Optional.of(trainee));

        assertThat(service.userExists("trainee.user")).isTrue();
        assertThat(service.isUserActive("trainee.user")).isTrue();
    }

    private CredentialsDTO credentials(String username, String password) {
        CredentialsDTO dto = new CredentialsDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private ChangePasswordRequestDTO changePasswordRequest(
            String username,
            String oldPassword,
            String newPassword
    ) {
        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setUsername(username);
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        return dto;
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
}
