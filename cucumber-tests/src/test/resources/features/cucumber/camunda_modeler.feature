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
    And Download bpmn project "simple-taskprocess"
    Given kermit is logged in to Camunda via POST
    Then Check process "simple-taskprocess" is present by ID