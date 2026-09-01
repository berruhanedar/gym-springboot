@component @workload
Feature: Training workload publishing

  Scenario: Send workload message when training is created
    Given valid training data for workload publishing
    When a training is created
    Then a workload message should be sent to the mocked queue