package com.berruhanedar.app.gym_springboot.exception.handler;

import com.berruhanedar.app.gym_springboot.exception.IBaseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import static org.assertj.core.api.Assertions.assertThat;

class BaseBindExceptionHandlerTest {

    private final BaseBindExceptionHandler handler = new TestHandler();

    @Test
    void shouldProcessFieldAndObjectErrors() {
        BeanPropertyBindingResult result =
                new BeanPropertyBindingResult(new Object(), "request");

        result.addError(new FieldError("request", "username", "Username is required"));
        result.addError(new ObjectError("request", "Request is invalid"));

        ResponseEntity<IBaseException> response = handler.processBindingResult(result);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrorsInternal()).hasSize(2);
    }

    private static class TestHandler extends BaseBindExceptionHandler {
    }
}