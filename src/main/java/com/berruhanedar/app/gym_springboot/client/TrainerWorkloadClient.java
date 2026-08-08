package com.berruhanedar.app.gym_springboot.client;

import com.berruhanedar.app.gym_springboot.dto.TrainerWorkloadRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TrainerWorkloadClient {

    private final RestClient restClient;

    public TrainerWorkloadClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://trainer-workload-service")
                .build();
    }

    public void sendWorkload(TrainerWorkloadRequestDTO request) {
        restClient.post()
                .uri("/api/workloads")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}