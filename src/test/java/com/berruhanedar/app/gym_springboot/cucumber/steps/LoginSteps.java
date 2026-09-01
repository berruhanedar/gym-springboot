package com.berruhanedar.app.gym_springboot.cucumber.steps;

import com.berruhanedar.app.gym_springboot.controller.AuthenticationController;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.handler.BaseExceptionHandler;
import com.berruhanedar.app.gym_springboot.exception.handler.MethodArgumentNotValidExceptionHandler;
import com.berruhanedar.app.gym_springboot.service.AuthenticationService;
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

public class LoginSteps {

    private AuthenticationService authenticationService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CredentialsDTO credentials;
    private MvcResult result;

    @Before
    public void setUp() {
        authenticationService = mock(AuthenticationService.class);
        AuthenticationController authenticationController = new AuthenticationController(authenticationService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(authenticationController).setValidator(validator).setControllerAdvice(new BaseExceptionHandler(), new MethodArgumentNotValidExceptionHandler()).build();
    }

    @Given("valid login credentials")
    public void validLoginCredentials() {
        credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");
        credentials.setPassword("password123");
        when(authenticationService.login(any(CredentialsDTO.class))).thenReturn("jwt-token");
    }

    @Given("login credentials with a wrong password")
    public void loginCredentialsWithAWrongPassword() {
        credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");
        credentials.setPassword("wrongPassword");
        doThrow(new AuthenticationException("Invalid username or password.")).when(authenticationService).login(any(CredentialsDTO.class));
    }

    @Given("login credentials with a missing password")
    public void loginCredentialsWithAMissingPassword() {
        credentials = new CredentialsDTO();
        credentials.setUsername("John.Doe");
    }

    @When("the login request is sent")
    public void theLoginRequestIsSent() throws Exception {
        result = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(credentials))).andReturn();
    }

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int expectedStatus) {
        assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
    }

    @Then("a JWT token should be returned")
    public void aJwtTokenShouldBeReturned() throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("token").asText()).isEqualTo("jwt-token");
        verify(authenticationService).login(any(CredentialsDTO.class));
    }

    @Then("authentication service should not be called")
    public void authenticationServiceShouldNotBeCalled() {
        verify(authenticationService, never()).login(any(CredentialsDTO.class));
    }
}