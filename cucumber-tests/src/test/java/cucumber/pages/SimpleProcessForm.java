package cucumber.pages;

import static cucumber.pages.TaskListPage.clickClaimButton;
import static cucumber.pages.TaskListPage.hideTaskList;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.targets.Target;

public class SimpleProcessForm extends PageObject {

    private static final Target COMPLETE_BUTTON = Target
        .the("Complete")
        .locatedBy("//button[contains(text(), 'Complete')]");

    private static final Target TEXT_FIELD = Target.the("Text field").locatedBy("//input[@name='data[textField]']");
    private static final Target NUMBER_FIELD = Target.the("Number field").locatedBy("//input[@name='data[number]']");
    private static final Target START_BUTTON = Target
        .the("Start button")
        .locatedBy("//button[contains(text(), 'Start')]");

    public static Performable selectActionFromDropDown(String value) {
        TaskListPage.DROPDOWN_ACTION.selectByVisibleText(value);
        return Task.where("{0} selects " + value);
    }

    public static Performable clickCompleteButton() {
        return Task.where("{0} click Complete", Scroll.to(COMPLETE_BUTTON), Click.on(COMPLETE_BUTTON));
    }

    public static Performable completeFormSimple(String action) {
        return Task.where(
            "{0} complete form",
            hideTaskList(),
            clickClaimButton(),
            selectActionFromDropDown(action),
            clickCompleteButton()
        );
    }

    public static Performable setTextField(String textField) {
        return Task.where("{0} set creditor name", Enter.theValue(textField).into(TEXT_FIELD));
    }

    public static Performable setNumber(String number) {
        return Task.where("{0} set number", Enter.theValue(number).into(NUMBER_FIELD));
    }

    public static Performable clickStart() {
        return Task.where("{0} click start", Click.on(START_BUTTON));
    }

    public static Performable fillFormAndStartSimple(String textField, String number) {
        return Task.where("{0} fill and start form", setTextField(textField), setNumber(number), clickStart());
    }
}
