package com.berruhanedar.app.gym_springboot.security;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    @Test
    void shouldReportTokenAsBlacklistedUntilExpiration() {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklist("token", Instant.now().plusSeconds(60));

        assertThat(service.isBlacklisted("token")).isTrue();
        assertThat(service.isBlacklisted("different-token")).isFalse();
    }

    @Test
    void shouldRemoveExpiredTokenFromBlacklist() {
        TokenBlacklistService service = new TokenBlacklistService();
        service.blacklist("expired-token", Instant.now().minusSeconds(1));

        assertThat(service.isBlacklisted("expired-token")).isFalse();
        assertThat(service.isBlacklisted("expired-token")).isFalse();
    }
}
