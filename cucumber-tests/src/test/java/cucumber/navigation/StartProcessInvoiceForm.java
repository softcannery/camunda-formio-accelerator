package cucumber.navigation;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.targets.Target;

public class StartProcessInvoiceForm extends PageObject {

    public static Target CREDITOR_FIELD = Target.the("Creditor field").locatedBy("//input[@name='data[creditor]']");
    public static Target EMAIL_FIELD = Target.the("Email field").locatedBy("//input[@name='data[email]']");
    public static Target NUMBER_FIELD = Target.the("Number field").locatedBy("//input[@name='data[number]']");
    public static Target CATEGORY_SELECT_FIELD = Target
        .the("Category select field")
        .locatedBy("//select[@name='data[category]']");
    public static Target INVOICE_ID_FIELD = Target
        .the("Invoice id field")
        .locatedBy("//input[@name='data[invoiceID]']");
    public static Target SIGNATURE_FIELD = Target
        .the("Signature field")
        .locatedBy("//*[@class='signature-pad-canvas']");
    public static Target START_BUTTON = Target.the("Start button").locatedBy("//button[contains(text(), 'Start')]");
}
