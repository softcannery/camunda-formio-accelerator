@web
Feature: test PDF form
  Scenario: Start and Complete PDF form
    When kermit goes to the react main page
    When he starts PDF process
    When he complete PDF process
    Then he process is closed
