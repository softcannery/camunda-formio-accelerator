package cucumber.navigation;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.targets.Target;
import net.thucydides.core.annotations.DefaultUrl;

@DefaultUrl("http://localhost/bpm/engine-rest/process-instance/count")
public class ProcessInstanceCountApiPage extends PageObject {

    public static Target API_RESPONCE = Target.the("Responce API").locatedBy("//pre");
}
