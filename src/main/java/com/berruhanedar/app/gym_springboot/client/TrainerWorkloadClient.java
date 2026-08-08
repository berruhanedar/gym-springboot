package com.berruhanedar.app.gym_springboot.client;

import com.berruhanedar.app.gym_springboot.dto.TrainerWorkloadRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TrainerWorkloadClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public TrainerWorkloadClient(RestClient.Builder builder, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = builder
                .baseUrl("http://trainer-workload-service")
                .build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public void sendWorkload(
            TrainerWorkloadRequestDTO request,
            String authorizationHeader) {

        String transactionId = MDC.get("transactionId");

        circuitBreakerFactory
                .create("trainerWorkloadService")
                .run(
                        () -> {
                            sendRequest(
                                    request,
                                    authorizationHeader,
                                    transactionId
                            );
                            return null;
                        },
                        throwable -> {
                            fallback(request, throwable);
                            return null;
                        }
                );
    }

    private void sendRequest(TrainerWorkloadRequestDTO request, String authorizationHeader,String transactionId) {
        restClient.post()
                .uri("/api/workloads")
                .header(AUTHORIZATION_HEADER, authorizationHeader)
                .header(TRANSACTION_ID_HEADER, transactionId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private void fallback(TrainerWorkloadRequestDTO request, Throwable throwable) {
        log.error("Trainer workload service is unavailable. trainerUsername={}, actionType={}, error={}",
                request.getTrainerUsername(),
                request.getActionType(),
                throwable.getMessage());
    }
}