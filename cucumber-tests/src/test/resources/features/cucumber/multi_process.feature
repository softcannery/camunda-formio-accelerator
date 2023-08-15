@web
Feature: Parallel tasks process
  Scenario: Start and complete process
    Given kermit goes to the react main page
    When he starts Multi process
    When he approve multi tasks
    Then he open Show Results and complete
    Then he process is closed
