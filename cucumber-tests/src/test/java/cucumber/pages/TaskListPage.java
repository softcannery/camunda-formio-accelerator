package cucumber.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@DefaultUrl("http://localhost/camunda/app/tasklist/default/")
public class TaskListPage extends PageObject {

    private static final Target START_PROCESS = Target
        .the("Start Process button")
        .locatedBy("//*[@class='ng-scope start-process-action']");
    public static final Target TASK_LIST_FIRST_ITEM_PROCESS = Target
        .the("First process in the list")
        .locatedBy("(//ol[contains(@class, 'tasks-list')]/li)[1]/.//h6");
    private static final Target CLAIM_BUTTON = Target.the("Click Claim button").locatedBy("//button[text()='Claim']");
    private static final Target HIDE_TASK_LIST_BUTTON = Target
        .the("Hide")
        .locatedBy("//section[contains(@class,'tasks-list')]/.//button[@ng-click='toggleRegion($event)']");
    private static final Target START_BUTTON = Target
        .the("Start button")
        .locatedBy("//button[contains(text(), 'Start')]");

    @FindBy(name = "data[action]")
    public static WebElementFacade DROPDOWN_ACTION;

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

    public static Performable claim() {
        return Task.where("{0} complete form", hideTaskList(), clickClaimButton());
    }

    public static boolean isElementPresent(String xPath) {
        WebDriver driver = Serenity.getDriver();
        return driver.findElements(By.xpath(xPath)).size() > 0;
    }

    public static int getTasksCount() {
        WebDriver driver = Serenity.getDriver();
        String countTasksStr = "";
        for (int i = 0; i < 10; i++) {
            try {
                WebElement el = driver.findElement(
                    By.xpath("//div[@ng-show='totalItems']/.//span[@class='counter ng-binding']")
                );
                countTasksStr = el.getText();
                if (!countTasksStr.equals("")) {
                    break;
                }
            } catch (NoSuchElementException ex) {}
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Integer.parseInt(countTasksStr);
    }

    public static int getCountProcesses(Actor actor) {
        actor.wasAbleTo(NavigateTo.theProcessCountAPIPage());
        WebDriver driver = Serenity.getDriver();
        WebElement responseWE = driver.findElement(By.xpath("//pre"));
        String jsonString = responseWE.getText();
        int count = 0;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (jsonNode.has("count")) {
            count = jsonNode.get("count").asInt();
        }

        return count;
    }

    public static Performable clickStartProcessButton() {
        return Task.where("{0} click Claim button", Click.on(START_BUTTON));
    }
}
