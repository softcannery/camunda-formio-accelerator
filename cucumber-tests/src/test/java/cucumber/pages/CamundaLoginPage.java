package cucumber.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.targets.Target;

@DefaultUrl("http://localhost/")
public class CamundaLoginPage extends PageObject {

    private static final Target USER_LOGIN = Target.the("login field").locatedBy("//input[@id='username']");
    private static final Target USER_PASSWORD = Target.the("password field").locatedBy("//input[@id='password']");
    private static final Target LOGIN_BUTTON = Target.the("login button").locatedBy("//button[@type='submit']");

    private static Performable setLogin(String userLogin) {
        return Task.where("{0} searches for '" + userLogin + "'", Enter.theValue(userLogin).into(USER_LOGIN));
    }

    private static Performable setPassword(String password) {
        return Task.where("{0} password", Enter.theValue(password).into(CamundaLoginPage.USER_PASSWORD));
    }

    private static Performable clickLogin() {
        return Task.where("{0} password", Click.on(CamundaLoginPage.LOGIN_BUTTON));
    }

    public static Performable loginToCamunda(String userLogin, String password) {
        return Task.where("User logged in", setLogin(userLogin), setPassword(password), clickLogin());
    }
}
