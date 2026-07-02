package com.berruhanedar.app.gym_springboot.exception.handler;

import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.IBaseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class BaseExceptionHandlerTest {

    private final BaseExceptionHandler handler = new BaseExceptionHandler();

    @Test
    void shouldHandleBaseException() {
        AuthenticationException ex = new AuthenticationException("Invalid credentials");

        ResponseEntity<IBaseException> response = handler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(ex);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
        assertThat(response.getBody().getCode()).isEqualTo("AuthenticationException");
    }
}