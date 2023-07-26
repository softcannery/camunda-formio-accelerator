package cucumber.navigation;

import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;

public class NavigateTo {

    public static Performable theTaskListPage() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Task.where("{0} opens the Tasklist page", Open.browserOn().the(TaskListPage.class));
    }

    public static Performable theCamundaLoginPage() {
        return Task.where("{0} opens the Camunda Login page", Open.browserOn().the(CamundaLoginPage.class));
    }

    public static Performable theProcessCountAPIPage() {
        return Task.where(
            "{0} opens the started processes count page",
            Open.browserOn().the(ProcessInstanceCountApiPage.class)
        );
    }

    public static Performable theReactMainPage() {
        return Task.where("{0} opens the React main page", Open.browserOn().the(MainReactPage.class));
    }

    public static Performable theReactStartProcess() {
        return Task.where("{0} opens the React Start Process page", Open.browserOn().the(StartProcessReactPage.class));
    }
}
