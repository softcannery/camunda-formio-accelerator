package cucumber.stepdefinitions;

import cucumber.api.*;
import cucumber.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Cookie;
import io.restassured.path.json.JsonPath;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class StepDefinitions {

    private static int processCount = 0;
    private static int tasksCountUI = 0;
    private static int processVersion;
    private static Map<String, Cookie> cookiesMap;
    private static Methods methods;
    private static InvoiceProcess invoiceProcess;
    private static SimpleProcess simpleProcess;
    private static String fileURL;
    private static String processDefinitionId;
    private static String processInstanceId;
    private static CamundaModelerAppPage camundaModelerAppPage;

    @Given("{actor} is logged in to Camunda")
    public void loginToCamunda(Actor actor) {
        actor.wasAbleTo(NavigateTo.theCamundaLoginPage());
        actor.wasAbleTo(CamundaLoginPage.loginToCamunda(actor.toString(), "password"));
    }

    @Given("{actor} is logged in to Camunda via POST")
    public void loginToCamundaPOST(Actor actor) {
        Auth auth = new Auth();
        this.cookiesMap = auth.loginPost();
        this.methods = new Methods(cookiesMap);
    }

    @When("{actor} starts an Invoice Process via API")
    public void startProcessInvoicePOST(Actor actor) {
        this.invoiceProcess = new InvoiceProcess(cookiesMap);
        FileUpload fileUpload = new FileUpload();
        this.fileURL = fileUpload.uploadForm("files/bot.png", cookiesMap);
        this.processDefinitionId = methods.getProcessDefinitionId("Submit Invoice for Approval");
        this.processInstanceId = invoiceProcess.submitForm(fileURL, processDefinitionId);
    }

    @When("{actor} starts an Simple Process via API")
    public void startSimpleProcessPOST(Actor actor) {
        this.simpleProcess = new SimpleProcess(cookiesMap);
        this.processDefinitionId = methods.getProcessDefinitionId("Simple Formio Task Action");
        this.processInstanceId = simpleProcess.submitForm(processDefinitionId);
    }

    @When("{actor} completes Invoice Process via API")
    public void completeProcessInvoicePOST(Actor actor) {
        String taskId = this.methods.getTaskId(this.processInstanceId);
        this.methods.claim(taskId);
        Map<String, String> submitAndActivityIds = this.methods.getFormVariables(taskId);
        int respCodeGetProcessInstance = this.methods.getProcessInstance(this.processInstanceId);
        Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
        this.invoiceProcess.completeProcess(this.fileURL, taskId, submitAndActivityIds, this.processDefinitionId);
    }

    @When("{actor} completes Simple Process via API")
    public void completeSimpleProcessPOST(Actor actor) {
        String taskId = this.methods.getTaskId(this.processInstanceId);
        this.methods.claim(taskId);
        Map<String, String> formVariables = this.methods.getFormVariables(taskId);
        JsonPath jpath = new JsonPath(formVariables.get("body"));
        Assertions.assertEquals(
            "1.11111112E8",
            jpath.getString("number.value"),
            "expected Number value - '1.11111112E8'"
        );
        Assertions.assertEquals(
            "mike test",
            jpath.getString("textField.value"),
            "expected textField value - 'mike test'"
        );
        int respCodeGetProcessInstance = this.methods.getProcessInstance(this.processInstanceId);
        Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
        this.simpleProcess.completeProcess(taskId, formVariables, this.processDefinitionId);
    }

    @Then("{actor} should see that process not in the list via API")
    public void ensureThatProcessCompleted(Actor actor) {
        int respCodeResult = 0;
        for (int i = 0; i < 10; i++) {
            respCodeResult = methods.getProcessInstance(processInstanceId);
            if (respCodeResult == 404) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(404, respCodeResult, "process instance was not completed");
    }

    @When("{actor} selects start process {string}")
    public void startProcess(Actor actor, String processName) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        TaskListPage.getTasksCount();
        actor.wasAbleTo(TaskListPage.startProcessByName(processName));
    }

    @When("{actor} fill all required fields and start")
    public void fillInvoiceForm(Actor actor) {
        actor.wasAbleTo(InvoiceForm.setCreditor("test auto"));
        actor.wasAbleTo(InvoiceForm.fillFormAndStart("test@test.com", "111", "222"));
    }

    @When("{actor} fill all required fields with attachment and start")
    public void fillInvoiceFormWithAttachment(Actor actor) {
        actor.wasAbleTo(InvoiceForm.setCreditor("test auto"));
        actor.wasAbleTo(InvoiceForm.fillFormWithAttachmentAndStart("test@test.com", "111", "222"));
    }

    @When("{actor} fills all required fields and start for Simple task")
    public void fillSimpleForm(Actor actor) {
        actor.wasAbleTo(SimpleProcessForm.fillFormAndStartSimple("test auto", "111"));
    }

    @Then("{actor} should see that process {string} is started and present in the tasks list")
    public void checkThatTaskInList(Actor actor, String processName) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        tasksCountUI = TaskListPage.getTasksCount();
        actor.wasAbleTo(Ensure.that(TaskListPage.TASK_LIST_FIRST_ITEM_PROCESS).hasText(processName));
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TaskListPage.getCountProcesses();
        System.out.println();
    }

    @When("{actor} select the first process {string} in the tasks list")
    public void selectFirstProcessByName(Actor actor, String processName) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        TaskListPage.getTasksCount();
        actor.wasAbleTo(Ensure.that(TaskListPage.TASK_LIST_FIRST_ITEM_PROCESS).hasText(processName));
        actor.wasAbleTo(TaskListPage.selectFirstProcessFromList());
    }

    @Then("{actor} can see attached file")
    public void checkThatFileIsAttached(Actor actor) {
        actor.wasAbleTo(
            Ensure.that(TaskListPage.isElementPresent("//span[contains(text(), 'Press to open')]")).isTrue()
        );
    }

    @When("{actor} claim process")
    public void climeProcess(Actor actor) {
        actor.wasAbleTo(TaskListPage.claim());
    }

    @When("{actor} complete form - invoice")
    public void completeFormInvoice(Actor actor) {
        actor.wasAbleTo(InvoiceForm.completeForm());
    }

    @When("{actor} completes form - Simple - {string}")
    public void completeFormSimple(Actor actor, String action) {
        actor.wasAbleTo(SimpleProcessForm.completeFormSimple(action));
    }

    @When("{actor} reject form - invoice")
    public void rejectForm(Actor actor) {
        actor.wasAbleTo(InvoiceForm.rejectForm());
    }

    @Then("{actor} should see that task is disappeared")
    public void taskIsDisappeared(Actor actor) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        int currentTasksCount = TaskListPage.getTasksCount();
        if (currentTasksCount == 0) {
            boolean isTaskPresent = TaskListPage.isElementPresent("(//ol[contains(@class, 'tasks-list')]/li)[1]/.//h6");
            actor.wasAbleTo(Ensure.that(isTaskPresent).isFalse());
        } else {
            actor.wasAbleTo(Ensure.that(currentTasksCount).isEqualTo(tasksCountUI - 1));
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        int processCountAfterComplete = TaskListPage.getCountProcesses();
        actor.wasAbleTo(Ensure.that(processCountAfterComplete).isEqualTo(processCount - 1));
    }

    @When("{actor} goes to the react main page")
    public void kermitGoesToTheMainPage(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
    }

    @When("{actor} upload simple process files")
    public void heUploadSimpleProcessFiles(Actor actor) {
        String[] uploadFiles = new String[3];
        File submitFormFile = new File("../camunda-formio-bpmn/Submit.formio");
        File reviewFormFile = new File("../camunda-formio-bpmn/Review.formio");
        File taskFile = new File("../camunda-formio-bpmn/simple-task.bpmn");
        try {
            uploadFiles[0] = submitFormFile.getCanonicalPath();
            uploadFiles[1] = reviewFormFile.getCanonicalPath();
            uploadFiles[2] = taskFile.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        actor.wasAbleTo(SimpleProcessUpgrade.uploadFiles(uploadFiles));
    }

    @When("{actor} gets current simple process version")
    public void heGetsCurrentSimpleProcessVersion(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processVersion = SimpleProcessUpgrade.getSimpleProcessVersion();
    }

    @Then("{actor} see that version is incremented")
    public void checkThatVersionIncremented(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        int newVersion = SimpleProcessUpgrade.getSimpleProcessVersion();
        Assert.assertEquals("version was not incremented", processVersion + 1, newVersion);
    }

    @When("{actor} starts simple process")
    public void startSimpleProcess(Actor actor) {
        actor.wasAbleTo(SimpleProcessUpgrade.startSimpleProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TaskListPage.getCountProcesses();
    }

    @When("{actor} starts PDF process")
    public void startPDFProcess(Actor actor) {
        actor.wasAbleTo(PDFProcessForm.startProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TaskListPage.getCountProcesses();
    }

    @When("{actor} starts Multi process")
    public void startMultiProcess(Actor actor) {
        actor.wasAbleTo(ReactMultiProcessForm.startMultiProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TaskListPage.getCountProcesses();
    }

    @When("{actor} approve multi tasks")
    public void approveMultiTasks(Actor actor) {
        actor.wasAbleTo(ReactMultiProcessForm.approveMultiTasks(actor));
    }

    @When("{actor} open Show Results and complete")
    public void completeMultiResults(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.wasAbleTo(ReactMultiProcessForm.completeShowResults());
    }

    @When("{actor} complete PDF process")
    public void completePDFProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.wasAbleTo(PDFProcessForm.submitPdfProcess());
    }

    @When("{actor} complete process")
    public void completeSimpleProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.wasAbleTo(SimpleProcessUpgrade.submitSimpleProcess());
    }

    @Then("{actor} process is closed")
    public void checkThatProcessIsClosed(Actor actor) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        int newProcessCount = TaskListPage.getCountProcesses();
        Assert.assertEquals("process was not closed", processCount - 1, newProcessCount);
    }

    @When("Open Camunda modeler and new diagram")
    public void camundaModelerIsOpened() {
        camundaModelerAppPage = new CamundaModelerAppPage();
        //camundaModelerAppPage.openNewDiagram();
    }

    @Then("Form.io Import button is displayed")
    public void checkThatFormIOPluginButtonPresent() {
        Assert.assertTrue("'Form.io Import' button is not present", camundaModelerAppPage.isFormIoImportPresent());
    }

    @Then("Deploy to Camunda button is displayed")
    public void checkThatDeployPluginButtonPresent() {
        Assert.assertTrue(
            "'Deploy to Camunda' button is not present",
            camundaModelerAppPage.isDeployToCamundaPresent()
        );
    }

    @And("All fields are present in 'Form.io Import' menu")
    public void allFieldsArePresentInFormIoImportMenu() {
        camundaModelerAppPage.openFormIoImportMenu();
        camundaModelerAppPage.closeDriver();
    }

    @And("All fields are present in 'Deploy to Camunda' menu")
    public void allFieldsArePresentInDeployToCamundaMenu() {
        camundaModelerAppPage.openDeployToCamundaMenu();
        camundaModelerAppPage.closeDriver();
    }
}
