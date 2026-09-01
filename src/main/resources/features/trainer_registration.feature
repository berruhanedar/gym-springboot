@component @trainer
Feature: Trainer registration

  Scenario: Successfully register a trainer
    Given valid trainer registration data
    When the trainer registration request is sent
    Then the trainer registration response status should be 201
    And generated trainer credentials should be returned

  Scenario: Fail to register a trainer with invalid data
    Given invalid trainer registration data
    When the trainer registration request is sent
    Then the trainer registration response status should be 400
    And trainer registration service should not be called