package cucumber.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

public class ParameterDefinitions {

    @ParameterType(".*")
    public Actor actor(String actorName) {
        return OnStage.theActorCalled(actorName);
    }

    @Before
    public void setTheStage() {
        OnStage.setTheStage(new OnlineCast());
    }

    @Before(value = "@reset", order = 1)
    public void restartBrowser() {
        if (Serenity.getWebdriverManager().getCurrentDriver() != null) {
            Serenity.getWebdriverManager().getCurrentDriver().close();
            Serenity.getWebdriverManager().getCurrentDriver().quit();
        }
    }
}
