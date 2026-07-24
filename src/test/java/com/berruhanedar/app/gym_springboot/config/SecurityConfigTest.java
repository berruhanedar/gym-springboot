package com.berruhanedar.app.gym_springboot.config;

import com.berruhanedar.app.gym_springboot.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig config =
            new SecurityConfig(
                    mock(JwtAuthenticationFilter.class)
            );

    @Test
    void shouldCreateBcryptPasswordEncoderWithSaltedHashes() {
        PasswordEncoder encoder = config.passwordEncoder();

        String firstHash =
                encoder.encode("same-password");

        String secondHash =
                encoder.encode("same-password");

        assertThat(firstHash)
                .isNotEqualTo("same-password");

        assertThat(firstHash)
                .isNotEqualTo(secondHash);

        assertThat(
                encoder.matches(
                        "same-password",
                        firstHash
                )
        ).isTrue();

        assertThat(
                encoder.matches(
                        "wrong-password",
                        firstHash
                )
        ).isFalse();
    }

    @Test
    void shouldConfigureCorsForFrontendAndBearerHeader() {
        var source =
                config.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/login"
                );

        var cors =
                source.getCorsConfiguration(request);

        assertThat(cors).isNotNull();

        assertThat(cors.getAllowedOrigins())
                .containsExactly(
                        "http://localhost:3000"
                );

        assertThat(cors.getAllowedMethods())
                .containsExactly(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                );

        assertThat(cors.getAllowedHeaders())
                .containsExactly(
                        "Authorization",
                        "Content-Type"
                );

        assertThat(cors.getExposedHeaders())
                .containsExactly(
                        "Authorization"
                );

        assertThat(cors.getAllowCredentials())
                .isTrue();

        assertThat(cors.getMaxAge())
                .isEqualTo(3600L);
    }
}