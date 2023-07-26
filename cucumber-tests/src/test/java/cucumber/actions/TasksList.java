package cucumber.actions;

import static net.serenitybdd.core.Serenity.getDriver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.navigation.TaskListPage;
import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TasksList {

    public static Performable clickStartProcess() {
        return Task.where("{0} starts process", Click.on(TaskListPage.START_PROCESS));
    }

    public static Performable selectProcessByName(String processName) {
        Target processByName = Target
            .the("Start Process button")
            .locatedBy("//*[contains(text(), '" + processName + "')]");
        return Task.where("{0} select process by name", Click.on(processByName));
    }

    public static Performable startProcessByName(String processName) {
        return Task.where("{0} starts process", clickStartProcess(), selectProcessByName(processName));
    }

    public static Performable selectFirstProcessFromList() {
        return Task.where("{0} select first process", Click.on(TaskListPage.TASK_LIST_FIRST_ITEM_PROCESS));
    }

    public static Performable hideTaskList() {
        return Task.where("{0} hide tasks list", Click.on(TaskListPage.HIDE_TASK_LIST_BUTTON));
    }

    public static Performable clickClaimButton() {
        return Task.where("{0} click Claim button", Click.on(TaskListPage.CLAIM_BUTTON));
    }

    public static Performable checkApproveCheckBox() {
        return Task.where("{0} click approve", Click.on(TaskListPage.APPROVE_CHECKBOX));
    }

    public static Performable selectActionFromDropDown(String value) {
        TaskListPage.DROPDOWN_ACTION.selectByVisibleText(value);
        return Task.where("{0} selects " + value);
    }

    public static Performable clickCompleteButton() {
        return Task.where("{0} click Complete", Click.on(TaskListPage.COMPLETE_BUTTON));
    }

    public static Performable completeForm() {
        return Task.where("{0} complete form", checkApproveCheckBox(), clickCompleteButton());
    }

    public static Performable claim() {
        return Task.where("{0} complete form", hideTaskList(), clickClaimButton());
    }

    public static Performable completeFormSimple(String action) {
        return Task.where(
            "{0} complete form",
            hideTaskList(),
            clickClaimButton(),
            selectActionFromDropDown(action),
            clickCompleteButton()
        );
    }

    public static Performable rejectForm() {
        return Task.where("{0} complete form", clickCompleteButton());
    }

    public static boolean isElementPresent(String xPath) {
        WebDriver driver = getDriver();
        try {
            driver.findElement(By.xpath(xPath));
            return true;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public static int getTasksCount() {
        WebDriver driver = getDriver();
        WebElement el = driver.findElement(
            By.xpath("//div[@ng-show='totalItems']/.//span[@class='counter ng-binding']")
        );
        String countTasksStr = el.getText();
        return Integer.parseInt(countTasksStr);
    }

    public static int getCountProcesses() {
        WebDriver driver = getDriver();
        WebElement responseWE = driver.findElement(By.xpath("//pre"));
        String jsonString = responseWE.getText();
        int count = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonString);
            count = jsonNode.get("count").asInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
