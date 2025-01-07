@web
Feature: Process with Emails integration
  Background:
    Given delete mails for user "from@greenmail.com"
    Given delete mails for user "camunda@greenmail.com"

  Scenario: Start process without email
    When kermit is logged in to Camunda via POST
    When he starts an Pizza Process via API
    Then he should see that process not in the list via API

  Scenario: Start process with email
    When mail for pizza order
    And kermit is logged in to Camunda via POST
    When he starts an Pizza Process via API
    Then kermit claim and complete Pizza forms for make and deliver via API
    Then Mail about order is received from user "from@greenmail.com"
    Then he should see that process not in the list via API
