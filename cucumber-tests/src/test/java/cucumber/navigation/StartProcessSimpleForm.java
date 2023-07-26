package cucumber.navigation;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.targets.Target;

public class StartProcessSimpleForm extends PageObject {

    public static Target TEXT_FIELD = Target.the("Text field").locatedBy("//input[@name='data[textField]']");
    public static Target NUMBER_FIELD = Target.the("Number field").locatedBy("//input[@name='data[number]']");
    public static Target START_BUTTON = Target.the("Start button").locatedBy("//button[contains(text(), 'Start')]");
}
