@web
Feature: Invoice for Approval
  @reset
  Scenario: Start and complete process with Attachments API test
    Given user is logged in to Camunda via POST
    When user starts an Invoice Process via API
    When user completes Invoice Process via API
    Then user should see that process not in the list via API
  @reset
  Scenario: Start and complete process
    Given kermit is logged in to Camunda
    When he selects start process "Submit Invoice for Approval"
    When he fill all required fields and start
    Then he should see that process "Submit Invoice for Approval" is started and present in the tasks list
    When he select the first process "Submit Invoice for Approval" in the tasks list
    When he claim process
    When he complete form - invoice
    Then he should see that task is disappeared
  @reset
  Scenario: Start and complete process with Attachment
    Given kermit is logged in to Camunda
    When he selects start process "Submit Invoice for Approval"
    When he fill all required fields with attachment and start
    Then he should see that process "Submit Invoice for Approval" is started and present in the tasks list
    When he select the first process "Submit Invoice for Approval" in the tasks list
    When he claim process
    Then he can see attached file
    When he complete form - invoice
    Then he should see that task is disappeared
  @reset
  Scenario: Start and reject process
    Given kermit is logged in to Camunda
    When he selects start process "Submit Invoice for Approval"
    When he fill all required fields and start
    Then he should see that process "Submit Invoice for Approval" is started and present in the tasks list
    When he select the first process "Submit Invoice for Approval" in the tasks list
    When he claim process
    When he reject form - invoice
    Then he should see that task is disappeared
