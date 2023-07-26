package cucumber.navigation;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("http://localhost/bpm/camunda/app/tasklist/default/")
public class TaskListPage extends PageObject {

    public static Target START_PROCESS = Target
        .the("Start Process button")
        .locatedBy("//*[@class='ng-scope start-process-action']");
    public static Target TASK_LIST_FIRST_ITEM_PROCESS = Target
        .the("First process in the list")
        .locatedBy("(//ol[contains(@class, 'tasks-list')]/li)[1]/.//h6");
    public static Target CLAIM_BUTTON = Target.the("Click Claim button").locatedBy("//button[text()='Claim']");
    public static Target APPROVE_CHECKBOX = Target.the("Check approve").locatedBy("//input[@name='data[approved]']");
    public static Target COMPLETE_BUTTON = Target.the("Complete").locatedBy("//button[contains(text(), 'Complete')]");
    public static Target HIDE_TASK_LIST_BUTTON = Target
        .the("Hide")
        .locatedBy("//section[contains(@class,'tasks-list')]/.//button[@ng-click='toggleRegion($event)']");
    public static Target ATTACH_LINK = Target.the("Attach link").locatedBy("//a[@ref='fileLink']");

    @FindBy(name = "data[action]")
    public static WebElementFacade DROPDOWN_ACTION;
}
