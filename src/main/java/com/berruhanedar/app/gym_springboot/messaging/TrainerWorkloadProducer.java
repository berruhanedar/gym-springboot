package com.berruhanedar.app.gym_springboot.messaging;

import com.berruhanedar.app.gym_springboot.dto.TrainerWorkloadRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadProducer {
    private static final String TRANSACTION_ID = "transactionId";
    private final JmsTemplate jmsTemplate;

    @Value("${app.messaging.trainer-workload-queue}")
    private String trainerWorkloadQueue;

    public void sendWorkload(TrainerWorkloadRequestDTO request) {
        String transactionId = MDC.get(TRANSACTION_ID);
        log.info("Sending trainer workload message. trainerUsername={}, actionType={}", request.getTrainerUsername(), request.getActionType());
        jmsTemplate.convertAndSend(trainerWorkloadQueue, request, message -> {
            if (transactionId != null && !transactionId.isBlank()) {
                message.setStringProperty(
                        TRANSACTION_ID,
                        transactionId);
            }
            return message;
        });
    }
}