package com.berruhanedar.app.gym_springboot.cucumber.steps;

import com.berruhanedar.app.gym_springboot.dto.TrainerWorkloadRequestDTO;
import com.berruhanedar.app.gym_springboot.enums.ActionType;
import com.berruhanedar.app.gym_springboot.messaging.TrainerWorkloadProducer;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.Message;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWorkloadIntegrationSteps {

    private static final String QUEUE_NAME = "trainer.workload.queue";
    private static final String TRANSACTION_ID = "transactionId";

    @Autowired
    @Qualifier("cucumberJmsTemplate")
    private JmsTemplate jmsTemplate;

    private TrainerWorkloadProducer trainerWorkloadProducer;
    private TrainerWorkloadRequestDTO request;

    private Validator validator;

    private Set<ConstraintViolation<TrainerWorkloadRequestDTO>>
            violations;

    @Before
    public void setUpIntegration() {

        trainerWorkloadProducer =
                new TrainerWorkloadProducer(jmsTemplate);

        ReflectionTestUtils.setField(
                trainerWorkloadProducer,
                "trainerWorkloadQueue",
                QUEUE_NAME
        );

        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        MDC.clear();

        clearQueue();
    }

    private void clearQueue() {

        long originalTimeout =
                jmsTemplate.getReceiveTimeout();

        jmsTemplate.setReceiveTimeout(100);

        while (jmsTemplate.receive(QUEUE_NAME) != null) {
            // Remove messages left by previous test runs
        }

        jmsTemplate.setReceiveTimeout(originalTimeout);
    }

    @Given("a valid trainer workload request for integration")
    public void aValidTrainerWorkloadRequestForIntegration() {

        request = new TrainerWorkloadRequestDTO();

        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(
                LocalDate.of(2026, 8, 30)
        );
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);
    }

    @Given("an invalid trainer workload request for integration")
    public void anInvalidTrainerWorkloadRequestForIntegration() {

        request = new TrainerWorkloadRequestDTO();

        request.setTrainerUsername("john.smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(
                LocalDate.of(2026, 8, 30)
        );

        // Invalid because trainingDuration must be positive
        request.setTrainingDuration(0);

        request.setActionType(ActionType.ADD);
    }

    @And("a transaction id exists")
    public void aTransactionIdExists() {

        MDC.put(
                TRANSACTION_ID,
                "integration-test-id"
        );
    }

    @When("the gym microservice sends the trainer workload message")
    public void theGymMicroserviceSendsTheTrainerWorkloadMessage() {

        trainerWorkloadProducer.sendWorkload(request);
    }

    @When("the trainer workload request is validated for integration")
    public void theTrainerWorkloadRequestIsValidatedForIntegration() {

        violations =
                validator.validate(request);
    }

    @Then("the trainer workload message should be received from the queue")
    public void theTrainerWorkloadMessageShouldBeReceivedFromTheQueue() {

        Object receivedMessage =
                jmsTemplate.receiveAndConvert(QUEUE_NAME);

        assertThat(receivedMessage)
                .isNotNull();
    }

    @Then("the trainer workload message should contain the transaction id")
    public void theTrainerWorkloadMessageShouldContainTheTransactionId()
            throws Exception {

        Message receivedJmsMessage =
                jmsTemplate.receive(QUEUE_NAME);

        assertThat(receivedJmsMessage)
                .isNotNull();

        assertThat(
                receivedJmsMessage.getStringProperty(
                        TRANSACTION_ID
                )
        ).isEqualTo("integration-test-id");

        MDC.clear();
    }

    @Then("the trainer workload request should be invalid")
    public void theTrainerWorkloadRequestShouldBeInvalid() {

        assertThat(violations)
                .isNotEmpty();

        assertThat(
                violations.stream()
                        .anyMatch(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                                        .equals("trainingDuration")
                        )
        ).isTrue();
    }

    @And("no trainer workload message should be sent to the queue")
    public void noTrainerWorkloadMessageShouldBeSentToTheQueue() {

        jmsTemplate.setReceiveTimeout(500);

        Message message =
                jmsTemplate.receive(QUEUE_NAME);

        assertThat(message)
                .isNull();
    }
}