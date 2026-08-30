package com.berruhanedar.app.gym_springboot.cucumber.steps;

import com.berruhanedar.app.gym_springboot.controller.TrainerController;
import com.berruhanedar.app.gym_springboot.dto.NewTrainerRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.RegistrationResponseDTO;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class TrainerRegistrationSteps {

    private TrainerService trainerService;
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NewTrainerRequestDTO request;
    private MvcResult result;

    @Before
    public void setUp() {

        trainerService = mock(TrainerService.class);
        TrainerController trainerController = new TrainerController(trainerService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(trainerController)
                .setValidator(validator)
                .setControllerAdvice(
                        new BaseExceptionHandler(),
                        new MethodArgumentNotValidExceptionHandler()
                )
                .build();
    }

    @Given("valid trainer registration data")
    public void validTrainerRegistrationData() {

        request = new NewTrainerRequestDTO();
        request.setFirstName("Daniel");
        request.setLastName("Anderson");
        request.setSpecializationName("Boxing");

        RegistrationResponseDTO response =
                new RegistrationResponseDTO(
                        "Daniel.Anderson",
                        "password123"
                );

        when(
                trainerService.createTrainer(
                        any(NewTrainerRequestDTO.class)
                )
        ).thenReturn(response);
    }

    @Given("invalid trainer registration data")
    public void invalidTrainerRegistrationData() {

        request = new NewTrainerRequestDTO();

        request.setFirstName("D");
        request.setLastName("Anderson");

    }

    @When("the trainer registration request is sent")
    public void theTrainerRegistrationRequestIsSent()
            throws Exception {

        result = mockMvc.perform(
                        post("/api/trainers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andReturn();
    }

    @Then("the trainer registration response status should be {int}")
    public void theTrainerRegistrationResponseStatusShouldBe(
            int expectedStatus) {

        assertThat(
                result.getResponse().getStatus()
        ).isEqualTo(expectedStatus);
    }

    @Then("generated trainer credentials should be returned")
    public void generatedTrainerCredentialsShouldBeReturned()
            throws Exception {

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse().getContentAsString()
                );

        assertThat(response.get("username").asText())
                .isEqualTo("Daniel.Anderson");

        assertThat(response.get("password").asText())
                .isEqualTo("password123");

        verify(trainerService)
                .createTrainer(
                        any(NewTrainerRequestDTO.class)
                );
    }

    @Then("trainer registration service should not be called")
    public void trainerRegistrationServiceShouldNotBeCalled() {

        verify(
                trainerService,
                never()
        ).createTrainer(
                any(NewTrainerRequestDTO.class)
        );
    }
}