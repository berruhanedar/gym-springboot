package com.berruhanedar.app.gym_springboot.cucumber.steps;

import com.berruhanedar.app.gym_springboot.controller.TraineeController;
import com.berruhanedar.app.gym_springboot.dto.NewTraineeRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.RegistrationResponseDTO;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.TraineeService;
import com.berruhanedar.app.gym_springboot.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class TraineeRegistrationSteps {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private NewTraineeRequestDTO request;
    private MvcResult result;

    @Before
    public void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        TraineeController traineeController = new TraineeController(traineeService, trainerService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(traineeController).setValidator(validator).setControllerAdvice(new BaseExceptionHandler(), new MethodArgumentNotValidExceptionHandler()).build();
    }

    @Given("valid trainee registration data")
    public void validTraineeRegistrationData() {
        request = new NewTraineeRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Istanbul");
        RegistrationResponseDTO response = new RegistrationResponseDTO("John.Doe", "password123");
        when(traineeService.createTrainee(any(NewTraineeRequestDTO.class))).thenReturn(response);
    }

    @Given("invalid trainee registration data")
    public void invalidTraineeRegistrationData() {
        request = new NewTraineeRequestDTO();
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        request.setAddress("Istanbul");
    }

    @When("the trainee registration request is sent")
    public void theTraineeRegistrationRequestIsSent() throws Exception {
        result = mockMvc.perform(post("/api/trainees").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andReturn();
    }

    @Then("the trainee registration response status should be {int}")
    public void theTraineeRegistrationResponseStatusShouldBe(int expectedStatus) {
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @Then("generated trainee credentials should be returned")
    public void generatedTraineeCredentialsShouldBeReturned() throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("username").asText()).isEqualTo("John.Doe");
        assertThat(response.get("password").asText()).isEqualTo("password123");
        verify(traineeService).createTrainee(any(NewTraineeRequestDTO.class));
    }

    @Then("trainee registration service should not be called")
    public void traineeRegistrationServiceShouldNotBeCalled() {
        verify(traineeService, never()).createTrainee(any(NewTraineeRequestDTO.class));
    }
}