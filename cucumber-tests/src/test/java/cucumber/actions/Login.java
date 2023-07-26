package cucumber.actions;

import cucumber.navigation.CamundaLoginPage;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

public class Login {

    private static Performable setLogin(String userLogin) {
        return Task.where(
            "{0} searches for '" + userLogin + "'",
            Enter.theValue(userLogin).into(CamundaLoginPage.USER_LOGIN)
        );
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
