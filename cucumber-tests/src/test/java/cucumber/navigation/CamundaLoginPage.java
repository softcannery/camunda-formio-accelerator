package cucumber.navigation;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("http://localhost/bpm/")
public class CamundaLoginPage extends PageObject {

    public static Target USER_LOGIN = Target.the("login field").locatedBy("//*[@placeholder='Username']");
    public static Target USER_PASSWORD = Target.the("password field").locatedBy("//*[@placeholder='Password']");
    public static Target LOGIN_BUTTON = Target.the("login button").locatedBy("//*[@type='submit']");
}
