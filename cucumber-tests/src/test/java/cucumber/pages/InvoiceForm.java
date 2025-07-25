package cucumber.pages;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.targets.Target;
import org.awaitility.Awaitility;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class InvoiceForm extends PageObject {

    public static final Target APPROVE_CHECKBOX = Target
        .the("Check approve")
        .locatedBy("//input[@name='data[approved]']");
    public static final Target COMPLETE_BUTTON = Target
        .the("Complete")
        .locatedBy("//button[contains(text(), 'Complete')]");
    private static final Target EMAIL_FIELD = Target.the("Email field").locatedBy("//input[@name='data[email]']");
    private static final Target NUMBER_FIELD = Target.the("Number field").locatedBy("//input[@name='data[number]']");
    private static final Target INVOICE_ID_FIELD = Target
        .the("Invoice id field")
        .locatedBy("//input[@name='data[invoiceID]']");
    private static final Target START_BUTTON = Target
        .the("Start button")
        .locatedBy("//button[contains(text(), 'Start')]");

    private static final By creditor = By.xpath("//input[@name='data[creditor]']");
    public static final By signature = By.xpath("//*[@class='signature-pad-canvas']");
    public static final By browse = By.xpath("//a[@class='browse']");
    public static final By file = By.xpath("//input[@type='file']");

    public static Performable setCreditor(String creditorName) {
        WebDriver driver = Serenity.getDriver();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> driver.findElements(creditor).size() > 0);
        driver.findElement(creditor).sendKeys(creditorName);
        return Task.where("{0} set creditor name");
    }

    public static Performable setNumber(String number) {
        return Task.where("{0} set number", Enter.theValue(number).into(InvoiceForm.NUMBER_FIELD));
    }

    public static Performable setEmail(String email) {
        return Task.where("{0} set number", Enter.theValue(email).into(InvoiceForm.EMAIL_FIELD));
    }

    public static Performable setInvoiceId(String invoiceId) {
        return Task.where("{0} set invoice id", Enter.theValue(invoiceId).into(InvoiceForm.INVOICE_ID_FIELD));
    }

    public static Performable setSignature() {
        WebDriver driver = Serenity.getDriver();
        Actions actions = new Actions(driver);
        WebElement signatureField = driver.findElement(signature);
        actions.moveToElement(signatureField, 10, 3).clickAndHold().moveByOffset(20, 6).release().perform();
        return Task.where("{0} set signature");
    }

    public static Performable uploadFile(String filePath) {
        String testFilePath = null;
        URL resource = InvoiceForm.class.getClassLoader().getResource(filePath);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            try {
                File testFile = new File(resource.toURI());
                testFilePath = testFile.getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        WebDriver driver = Serenity.getDriver();
        driver.findElement(browse).click();
        driver.findElement(file).sendKeys(testFilePath);
        return Task.where("{0} upload file");
    }

    public static Performable clickStart() {
        return Task.where("{0} click start", Click.on(InvoiceForm.START_BUTTON));
    }

    public static Performable fillFormAndStart(String email, String number, String invoiceId) {
        return Task.where(
            "{0} set invoice id",
            setEmail(email),
            setNumber(number),
            setInvoiceId(invoiceId),
            setSignature(),
            clickStart()
        );
    }

    public static Performable fillFormWithAttachmentAndStart(String email, String number, String invoiceId) {
        return Task.where(
            "{0} set invoice id",
            uploadFile("files/bot.png"),
            setEmail(email),
            setNumber(number),
            setInvoiceId(invoiceId),
            setSignature(),
            clickStart()
        );
    }

    public static Performable clickCompleteButton() {
        return Task.where("{0} click Complete", Scroll.to(COMPLETE_BUTTON), Click.on(COMPLETE_BUTTON));
    }

    public static Performable completeForm() {
        return Task.where("{0} complete form", checkApproveCheckBox(), clickCompleteButton());
    }

    public static Performable rejectForm() {
        return Task.where("{0} complete form", clickCompleteButton());
    }

    public static Performable checkApproveCheckBox() {
        return Task.where("{0} click approve", Scroll.to(APPROVE_CHECKBOX), Click.on(APPROVE_CHECKBOX));
    }
}
