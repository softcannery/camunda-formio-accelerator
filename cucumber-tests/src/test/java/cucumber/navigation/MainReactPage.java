package cucumber.navigation;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("http://localhost/")
public class MainReactPage extends PageObject {

    public static Target BROWS_BUTTON = Target
        .the("Brows files to Deploy")
        .locatedBy("//button[contains(text(),'Browse Files to Deploy BPMN and Formio forms')]");
    public static Target TASK_LIST_BUTTON = Target.the("Tasks List").locatedBy("//a[contains(text(),'Tasklist')]");
    public static Target START_PROCESS_BUTTON = Target
        .the("Start Process")
        .locatedBy("//a[contains(text(),'Start Process')]");
    public static Target DEPLOY_PROCESS_BUTTON = Target
        .the("Deploy Process")
        .locatedBy("//a[contains(text(),'Deploy Process')]");

    @FindBy(name = "data[action]")
    public static WebElementFacade DROPDOWN_ACTION;
}
