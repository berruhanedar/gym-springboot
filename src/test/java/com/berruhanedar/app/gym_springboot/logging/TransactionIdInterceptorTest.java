package com.berruhanedar.app.gym_springboot.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransactionIdInterceptorTest {

    private final TransactionIdInterceptor interceptor = new TransactionIdInterceptor();

    @Test
    void shouldGenerateTransactionIdAndSetResponseHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();

        verify(response).setHeader(eq("X-Transaction-Id"), anyString());
    }

    @Test
    void shouldClearMdcAfterCompletion() {
        MDC.put("transactionId", "test-id");

        interceptor.afterCompletion(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                new Object(),
                null
        );

        assertThat(MDC.get("transactionId")).isNull();
    }
}