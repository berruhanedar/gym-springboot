Feature: Trainee registration

  Scenario: Successfully register a trainee
    Given valid trainee registration data
    When the trainee registration request is sent
    Then the trainee registration response status should be 201
    And generated trainee credentials should be returned

  Scenario: Fail to register a trainee with invalid data
    Given invalid trainee registration data
    When the trainee registration request is sent
    Then the trainee registration response status should be 400
    And trainee registration service should not be called