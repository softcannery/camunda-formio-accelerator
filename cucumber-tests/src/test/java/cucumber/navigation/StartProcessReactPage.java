package cucumber.navigation;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("http://localhost/startProcess/list")
public class StartProcessReactPage extends PageObject {

    public static Target SIMPLE_PROCESS_LINK = Target
        .the("Link to start simple process")
        .locatedBy("//a[contains(text(), 'Simple Formio Task Action')]");
}
