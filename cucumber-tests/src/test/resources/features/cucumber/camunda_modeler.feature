@modeler
Feature: Camunda Modeler plugin
  Scenario: Check that Form.io plugin is added
    When Open Camunda modeler and new diagram
    Then Form.io Import button is displayed
    And All fields are present in 'Form.io Import' menu
