package cucumber.stepdefinitions;

import cucumber.actions.*;
import cucumber.actions.api.Auth;
import cucumber.actions.api.FileUpload;
import cucumber.actions.api.InvoiceProcess;
import cucumber.actions.api.Methods;
import cucumber.navigation.NavigateTo;
import cucumber.navigation.TaskListPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class SearchStepDefinitions {

    private static int processCount = 0;
    private static int tasksCountUI = 0;
    private static int processVersion;
    private static Map<String, Cookie> cookiesMap;
    private static Methods methods;
    private static InvoiceProcess invoiceProcess;
    private static String fileURL;
    private static String processDefinitionId;
    private static String processInstanceId;

    @Given("{actor} is logged in to Camunda")
    public void loginToCamunda(Actor actor) {
        actor.wasAbleTo(NavigateTo.theCamundaLoginPage());
        actor.attemptsTo(Login.loginToCamunda(actor.toString(), "password"));
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
        this.processDefinitionId = methods.getProcessDefinitionId();
        this.processInstanceId = invoiceProcess.submitForm(fileURL, processDefinitionId);
    }

    @When("{actor} completes Invoice Process via API")
    public void completeProcessInvoicePOST(Actor actor) {
        String taskId = this.methods.getTaskId(this.processInstanceId);
        this.invoiceProcess.claim(taskId);
        Map<String, String> submitAndActivityIds = this.methods.getSubmitAndActivityIds(taskId);
        int respCodeGetProcessInstance = this.methods.getProcessInstance(this.processInstanceId);
        Assertions.assertEquals(200, respCodeGetProcessInstance, "process instance was not created");
        this.invoiceProcess.completeProcess(this.fileURL, taskId, submitAndActivityIds, this.processDefinitionId);
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
        actor.attemptsTo(TasksList.startProcessByName(processName));
    }

    @When("{actor} fill all required fields and start")
    public void fillInvoiceForm(Actor actor) {
        actor.attemptsTo(FillInvoiceForm.setCreditor("test auto"));
        actor.attemptsTo(FillInvoiceForm.fillFormAndStart("test@test.com", "111", "222"));
    }

    @When("{actor} fill all required fields with attachment and start")
    public void fillInvoiceFormWithAttachment(Actor actor) {
        actor.attemptsTo(FillInvoiceForm.setCreditor("test auto"));
        actor.attemptsTo(FillInvoiceForm.fillFormWithAttachmentAndStart("test@test.com", "111", "222"));
    }

    @When("{actor} fills all required fields and start for Simple task")
    public void fillSimpleForm(Actor actor) {
        actor.attemptsTo(FillSimpleForm.fillFormAndStartSimple("test auto", "111"));
    }

    @Then("{actor} should see that process {string} is started and present in the tasks list")
    public void checkThatTaskInList(Actor actor, String processName) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.attemptsTo(Ensure.that(TaskListPage.TASK_LIST_FIRST_ITEM_PROCESS).hasText(processName));
        tasksCountUI = TasksList.getTasksCount();
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TasksList.getCountProcesses();
        System.out.println();
    }

    @When("{actor} select the first process {string} in the tasks list")
    public void selectFirstProcessByName(Actor actor, String processName) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        actor.attemptsTo(Ensure.that(TaskListPage.TASK_LIST_FIRST_ITEM_PROCESS).hasText(processName));
        actor.attemptsTo(TasksList.selectFirstProcessFromList());
    }

    @Then("{actor} can see attached file")
    public void checkThatFileIsAttached(Actor actor) {
        actor.attemptsTo(Ensure.that(TasksList.isElementPresent("//span[contains(text(), 'Press to open')]")).isTrue());
    }

    @When("{actor} claim process")
    public void climeProcess(Actor actor) {
        actor.attemptsTo(TasksList.claim());
    }

    @When("{actor} complete form")
    public void completeForm(Actor actor) {
        actor.attemptsTo(TasksList.completeForm());
    }

    @When("{actor} completes form - Simple - {string}")
    public void completeFormSimple(Actor actor, String action) {
        actor.attemptsTo(TasksList.completeFormSimple(action));
    }

    @When("{actor} reject form")
    public void rejectForm(Actor actor) {
        actor.attemptsTo(TasksList.rejectForm());
    }

    @Then("{actor} should see that task is disappeared")
    public void taskIsDisappeared(Actor actor) {
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int currentTasksCount = TasksList.getTasksCount();
        if (currentTasksCount == 0) {
            boolean isTaskPresent = TasksList.isElementPresent("(//ol[contains(@class, 'tasks-list')]/li)[1]/.//h6");
            actor.attemptsTo(Ensure.that(isTaskPresent).isFalse());
        } else {
            actor.attemptsTo(Ensure.that(currentTasksCount).isEqualTo(tasksCountUI - 1));
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        int processCountAfterComplete = TasksList.getCountProcesses();
        actor.attemptsTo(Ensure.that(processCountAfterComplete).isEqualTo(processCount - 1));
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

        actor.attemptsTo(ReactSimpleProcess.uploadFiles(uploadFiles));
    }

    @When("{actor} gets current simple process version")
    public void heGetsCurrentSimpleProcessVersion(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processVersion = ReactSimpleProcess.getSimpleProcessVersion();
    }

    @Then("{actor} see that version is incremented")
    public void checkThatVersionIncremented(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        int newVersion = ReactSimpleProcess.getSimpleProcessVersion();
        Assert.assertEquals("version was not incremented", processVersion + 1, newVersion);
    }

    @When("{actor} starts simple process")
    public void startSimpleProcess(Actor actor) {
        actor.attemptsTo(ReactSimpleProcess.startSimpleProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TasksList.getCountProcesses();
    }

    @When("{actor} starts PDF process")
    public void startPDFProcess(Actor actor) {
        actor.attemptsTo(PDFFormProcess.startProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TasksList.getCountProcesses();
    }

    @When("{actor} starts Multi process")
    public void startMultiProcess(Actor actor) {
        actor.attemptsTo(ReactMultiProcess.startMultiProcess());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        processCount = TasksList.getCountProcesses();
    }

    @When("{actor} approve multi tasks")
    public void approveMultiTasks(Actor actor) {
        actor.attemptsTo(ReactMultiProcess.approveMultiTasks(actor));
    }

    @When("{actor} open Show Results and complete")
    public void completeMultiResults(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.attemptsTo(ReactMultiProcess.completeShowResults());
    }

    @When("{actor} complete PDF process")
    public void completePDFProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.attemptsTo(PDFFormProcess.submitPdfProcess());
    }

    @When("{actor} complete process")
    public void completeSimpleProcess(Actor actor) {
        actor.wasAbleTo(NavigateTo.theReactMainPage());
        actor.attemptsTo(ReactSimpleProcess.submitSimpleProcess());
    }

    @Then("{actor} process is closed")
    public void checkThatProcessIsClosed(Actor actor) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        int newProcessCount = TasksList.getCountProcesses();
        Assert.assertEquals("process was not closed", processCount - 1, newProcessCount);
    }
}
