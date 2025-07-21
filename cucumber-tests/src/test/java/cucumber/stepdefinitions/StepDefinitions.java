package cucumber.stepdefinitions;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
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
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class StepDefinitions {

    private static int processCount = 0;
    private static int tasksCountUI = 0;
    private static int processVersion;
    private static Map<String, Cookie> cookiesMap;
    private static Methods methods;
    private static InvoiceProcess invoiceProcess;
    private static PizzaProcess pizzaProcess;
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

    @Given("user is logged in to Camunda via POST")
    public void loginToCamundaPOST() {
        Auth auth = new Auth();
        this.cookiesMap = auth.loginPost();
        this.methods = new Methods(cookiesMap);
    }

    @Given("delete mails for user {string}")
    public void deleteMailsFromUser(String userEmail) {
        methods = new Methods();
        methods.getDeleteAllMails(userEmail);
    }

    @When("user starts an Invoice Process via API")
    public void startProcessInvoicePOST() {
        invoiceProcess = new InvoiceProcess(cookiesMap);
        FileUpload fileUpload = new FileUpload();
        fileURL = fileUpload.uploadForm("files/bot.png", cookiesMap);
        processDefinitionId = methods.getProcessDefinitionId("Submit Invoice for Approval");
        processInstanceId = invoiceProcess.submitForm(fileURL, processDefinitionId);
    }

    @When("user starts an Simple Process via API")
    public void startSimpleProcessPOST() {
        simpleProcess = new SimpleProcess(cookiesMap);
        processDefinitionId = methods.getProcessDefinitionId("Simple Formio Task Action");
        processInstanceId = simpleProcess.submitForm(processDefinitionId);
    }

    @When("user completes Invoice Process via API")
    public void completeProcessInvoicePOST() {
        String taskId = methods.getTaskId(processInstanceId);
        methods.claim(taskId);
        Map<String, String> submitAndActivityIds = methods.getFormVariables(taskId);
        int respCodeGetProcessInstance = methods.getProcessInstance(processInstanceId);
        Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
        invoiceProcess.completeProcess(fileURL, taskId, submitAndActivityIds, processDefinitionId);
    }

    @When("user completes Simple Process via API")
    public void completeSimpleProcessPOST() {
        String taskId = methods.getTaskId(processInstanceId);
        methods.claim(taskId);
        Map<String, String> formVariables = methods.getFormVariables(taskId);
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
        int respCodeGetProcessInstance = methods.getProcessInstance(processInstanceId);
        Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
        simpleProcess.completeProcess(taskId, formVariables, processDefinitionId);
    }

    @Then("user should see that process not in the list via API")
    public void ensureThatProcessCompleted() {
        Awaitility
            .await()
            .atMost(15, TimeUnit.SECONDS)
            .until(() -> methods.getProcessInstance(processInstanceId) == 404);
        int respCodeResult = methods.getProcessInstance(processInstanceId);
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
        processCount = TaskListPage.getCountProcesses(actor);
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
        int processCountAfterComplete = TaskListPage.getCountProcesses(actor);
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
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> TaskListPage.getCountProcesses(actor) > 0);
        processCount = TaskListPage.getCountProcesses(actor);
    }

    @When("{actor} starts PDF process")
    public void startPDFProcess(Actor actor) {
        actor.wasAbleTo(PDFProcessForm.startProcess());
        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> TaskListPage.getCountProcesses(actor) > processCount);
        processCount = TaskListPage.getCountProcesses(actor);
    }

    @When("{actor} starts Multi process")
    public void startMultiProcess(Actor actor) {
        actor.wasAbleTo(ReactMultiProcessForm.startMultiProcess());
        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> TaskListPage.getCountProcesses(actor) > processCount + 2);
        processCount = TaskListPage.getCountProcesses(actor);
    }

    @When("{actor} approve multi tasks")
    public void approveMultiTasks(Actor actor) {
        actor.wasAbleTo(MultiProcessForm.approveMultiTasks(actor));
    }

    @When("{actor} open Show Results, claim and complete")
    public void completeMultiResults(Actor actor) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        actor.wasAbleTo(MultiProcessForm.completeShowResults());
    }

    @When("{actor} complete PDF process")
    public void completePDFProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        actor.wasAbleTo(PDFProcessForm.submitPdfProcess());
    }

    @When("{actor} complete process")
    public void completeSimpleProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.wasAbleTo(SimpleProcessUpgrade.submitSimpleProcess());
    }

    @Then("{actor} checks that process is closed")
    public void checkThatProcessIsClosed(Actor actor) {
        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> TaskListPage.getCountProcesses(actor) < processCount);
        Assert.assertEquals("process was not closed", processCount - 1, TaskListPage.getCountProcesses(actor));
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
    }

    @And("Download bpmn project {string}")
    public void downloadBpmnProject(String processName) throws URISyntaxException, InterruptedException {
        camundaModelerAppPage.deployBpmnProcess(processName);
        camundaModelerAppPage.closeDriver();
    }

    @Then("Check process {string} is present by ID")
    public void processIsPresent(String processName) {
        processDefinitionId = methods.getProcessDefinitionId(processName);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> processDefinitionId.contains(processName));
        Assert.assertTrue(processDefinitionId.contains(processName));
    }

    @And("Check all formio files are present in process {string}")
    public void formioFilesAdded(String processName) {
        JsonPath resources = methods.checkFormioFiles(processName);
        Assertions.assertEquals(
            processName + "-review.formio",
            resources.getString("find{it.name == '" + processName + "-review.formio'}.name")
        );
        Assertions.assertEquals(
            processName + "-submit.formio",
            resources.getString("find{it.name == '" + processName + "-submit.formio'}.name")
        );
    }

    @When("user starts an Pizza Process via API")
    public void startProcessPizzaPOST() {
        pizzaProcess = new PizzaProcess(cookiesMap);
        processDefinitionId = methods.getProcessDefinitionId("Pizza Order Process");
        processInstanceId = pizzaProcess.submitForm(processDefinitionId);
    }

    @When("user claim and complete Pizza forms for make and deliver via API")
    public void makeThePizzaPOST() {
        for (int i = 0; i < 2; i++) {
            Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> methods.getTaskId(processInstanceId) != null);
            String taskId = methods.getTaskId(processInstanceId);
            Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> methods.claim(taskId) == 204);
            int respCodeGetProcessInstance = methods.getProcessInstance(processInstanceId);
            Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
            pizzaProcess.completeForm(taskId);
        }
    }

    @When("mail for pizza order")
    public void sendMail() {
        GreenMailUtil.sendTextEmail(
            "camunda@greenmail.com",
            "from@greenmail.com",
            "Pizza Order 1",
            "1 x Hawaii",
            ServerSetupTest.SMTP
        );
    }

    @Then("Mail about order is received from user {string}")
    public void checkThatMailIsReceived(String userEmail) {
        JsonPath mailJson = methods.getMailFromInbox(userEmail);
        Assert.assertTrue(mailJson.get("subject[0]").toString().contains("RE: Pizza Order 1"));
        Assert.assertTrue(mailJson.get("mimeMessage[0]").toString().contains("Cheers!"));
    }

    @When("{actor} clicks start process button")
    public void clickStartProcess(Actor actor) {
        actor.wasAbleTo(TaskListPage.clickStartProcessButton());
        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> TaskListPage.getCountProcesses(actor) > processCount);
        processCount = TaskListPage.getCountProcesses(actor);
    }
}
