package com.berruhanedar.app.gym_springboot.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtService.generateToken("test.user");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token))
                .isEqualTo("test.user");
    }

    @Test
    void shouldGenerateTokenWithFutureExpiration() {
        String token = jwtService.generateToken("test.user");
        Instant expiration = jwtService.extractExpiration(token);
        Instant now = Instant.now();

        assertThat(expiration)
                .isAfter(now.plusSeconds(3500));

        assertThat(expiration)
                .isBefore(now.plusSeconds(3700));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("test.user");
        String tamperedToken = tamperSignature(token);

        assertThat(tamperedToken)
                .isNotEqualTo(token);

        assertThatThrownBy(
                () -> jwtService.extractUsername(tamperedToken)
        ).isInstanceOf(JwtException.class);
    }

    private String tamperSignature(String token) {
        String[] parts = token.split("\\.");

        String signature = parts[2];
        int index = signature.length() / 2;

        char originalCharacter = signature.charAt(index);
        char replacementCharacter =
                originalCharacter == 'a' ? 'b' : 'a';

        String tamperedSignature =
                signature.substring(0, index)
                        + replacementCharacter
                        + signature.substring(index + 1);

        return parts[0]
                + "."
                + parts[1]
                + "."
                + tamperedSignature;
    }
}