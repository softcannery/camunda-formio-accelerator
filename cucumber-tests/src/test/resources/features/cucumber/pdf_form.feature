@web
Feature: test PDF form
  Scenario: Start and Complete PDF form
    Given kermit is logged in to Camunda
    When he selects start process "Example PDF Form"
    When he starts PDF process
    When he complete PDF process
    Then he checks that process is closed
