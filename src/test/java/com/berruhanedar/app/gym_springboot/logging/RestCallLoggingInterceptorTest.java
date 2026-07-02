package com.berruhanedar.app.gym_springboot.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RestCallLoggingInterceptorTest {

    private final RestCallLoggingInterceptor interceptor = new RestCallLoggingInterceptor();

    @Test
    void shouldLogRequestAndReturnTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/trainees/test");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void shouldLogSuccessfulResponseWhenNoException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(response).getStatus();
    }

    @Test
    void shouldLogErrorResponseWhenExceptionExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(
                request,
                response,
                new Object(),
                new RuntimeException("Test error")
        );

        verify(response).getStatus();
    }
}