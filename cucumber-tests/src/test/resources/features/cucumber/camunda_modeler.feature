@modeler
Feature: Camunda Modeler plugin
  Scenario: Check that Form.io plugin is added
    When Open Camunda modeler and new diagram
    Then Form.io Import button is displayed
    And All fields are present in 'Form.io Import' menu
  @reset
  Scenario: Check that Deploy plugin is added and works
    When Open Camunda modeler and new diagram
    Then Deploy to Camunda button is displayed
    And All fields are present in 'Deploy to Camunda' menu
    And Download bpmn project "simple-task-process"
    Given kermit is logged in to Camunda via POST
    Then Check process "simple-task-process" is present by ID
    When he goes to the react main page
    Then he starts process