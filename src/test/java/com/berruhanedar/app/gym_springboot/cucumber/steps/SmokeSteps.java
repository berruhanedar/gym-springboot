package com.berruhanedar.app.gym_springboot.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmokeSteps {

    private boolean applicationAvailable;

    @Given("the Gym application test environment is available")
    public void theGymApplicationTestEnvironmentIsAvailable() {
        applicationAvailable = true;
    }

    @Then("Cucumber should run successfully")
    public void cucumberShouldRunSuccessfully() {
        assertTrue(applicationAvailable);
    }
}