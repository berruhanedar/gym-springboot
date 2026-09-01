@integration @workload
Feature: Gym and Trainer Workload microservice integration

  Scenario: Send trainer workload message successfully
    Given a valid trainer workload request for integration
    When the gym microservice sends the trainer workload message
    Then the trainer workload message should be received from the queue

  Scenario: Send trainer workload message with transaction id
    Given a valid trainer workload request for integration
    And a transaction id exists
    When the gym microservice sends the trainer workload message
    Then the trainer workload message should contain the transaction id

  Scenario: Reject invalid trainer workload message
    Given an invalid trainer workload request for integration
    When the trainer workload request is validated for integration
    Then the trainer workload request should be invalid
    And no trainer workload message should be sent to the queue