@web
Feature: Parallel tasks process
  Scenario: Start and complete process
    Given kermit is logged in to Camunda
    When he selects start process "Multi-Instance Sub-Process Demo"
    When he clicks start process button
    When he approve multi tasks
    Then he open Show Results, claim and complete
    Then he checks that process is closed
