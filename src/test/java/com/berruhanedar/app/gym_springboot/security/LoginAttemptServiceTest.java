package com.berruhanedar.app.gym_springboot.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    @Test
    void shouldBlockUserAfterThreeFailedAttempts() {
        LoginAttemptService service = new LoginAttemptService();

        service.loginFailed(" Test.User ");
        service.loginFailed("test.user");

        assertThat(service.isBlocked("TEST.USER")).isFalse();

        service.loginFailed("test.user");

        assertThat(service.isBlocked("test.user")).isTrue();
        assertThat(service.getRemainingBlockSeconds("test.user"))
                .isBetween(298L, 300L);
    }

    @Test
    void shouldResetFailedAttemptsAfterSuccessfulLogin() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("user");
        service.loginFailed("user");

        service.loginSucceeded("user");
        service.loginFailed("user");

        assertThat(service.isBlocked("user")).isFalse();
        assertThat(service.getRemainingBlockSeconds("user")).isZero();
    }

    @Test
    void shouldHandleNullUsernameWithoutFailure() {
        LoginAttemptService service = new LoginAttemptService();

        service.loginFailed(null);
        service.loginSucceeded(null);

        assertThat(service.isBlocked(null)).isFalse();
    }
}
