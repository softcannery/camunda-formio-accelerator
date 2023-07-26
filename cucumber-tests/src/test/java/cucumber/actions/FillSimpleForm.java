package cucumber.actions;

import cucumber.navigation.StartProcessSimpleForm;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

public class FillSimpleForm {

    public static Performable setTextField(String textField) {
        return Task.where("{0} set creditor name", Enter.theValue(textField).into(StartProcessSimpleForm.TEXT_FIELD));
    }

    public static Performable setNumber(String number) {
        return Task.where("{0} set number", Enter.theValue(number).into(StartProcessSimpleForm.NUMBER_FIELD));
    }

    public static Performable clickStart() {
        return Task.where("{0} click start", Click.on(StartProcessSimpleForm.START_BUTTON));
    }

    public static Performable fillFormAndStartSimple(String textField, String number) {
        return Task.where("{0} fill and start form", setTextField(textField), setNumber(number), clickStart());
    }
}
