package cucumber.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.screenplay.targets.Target;

@DefaultUrl("http://localhost/")
public class MainReactPage extends PageObject {

    @FindBy(name = "data[action]")
    public static WebElementFacade DROPDOWN_ACTION;
}
