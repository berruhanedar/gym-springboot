@component @login
Feature: User login

  Scenario: Successfully login with valid credentials
    Given valid login credentials
    When the login request is sent
    Then the login response status should be 200
    And a JWT token should be returned

  Scenario: Fail to login with wrong password
    Given login credentials with a wrong password
    When the login request is sent
    Then the login response status should be 401

  Scenario: Fail to login with invalid request data
    Given login credentials with a missing password
    When the login request is sent
    Then the login response status should be 400
    And authentication service should not be called