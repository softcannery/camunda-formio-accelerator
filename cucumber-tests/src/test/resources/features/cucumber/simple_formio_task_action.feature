@web
Feature: Simple Formio Task Action
  @reset
  Scenario: Start and complete process API test
    Given kermit is logged in to Camunda via POST
    When he starts an Simple Process via API
    When he completes Simple Process via API
    Then he should see that process not in the list via API
  @reset
  Scenario: Start and complete process - Approved
    Given kermit is logged in to Camunda
    When he selects start process "Simple Formio Task Action"
    When he fills all required fields and start for Simple task
    Then he should see that process "Simple Formio Task Action" is started and present in the tasks list
    When he select the first process "Simple Formio Task Action" in the tasks list
    When he completes form - Simple - "Approve"
    Then he should see that task is disappeared
  @reset
  Scenario: Start and complete process - Rejected
    Given kermit is logged in to Camunda
    When he selects start process "Simple Formio Task Action"
    When he fills all required fields and start for Simple task
    Then he should see that process "Simple Formio Task Action" is started and present in the tasks list
    When he select the first process "Simple Formio Task Action" in the tasks list
    When he completes form - Simple - "Reject"
    When he select the first process "Simple Formio Task Action" in the tasks list
    When he completes form - Simple - "Approve"
    Then he should see that task is disappeared
