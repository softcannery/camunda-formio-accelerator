package cucumber.actions;

import static net.serenitybdd.core.Serenity.getDriver;

import cucumber.navigation.StartProcessInvoiceForm;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class FillInvoiceForm {

    public static Performable setCreditor(String creditorName) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//input[@name='data[creditor]']")).sendKeys(creditorName);
        return Task.where("{0} set creditor name");
    }

    public static Performable setNumber(String number) {
        return Task.where("{0} set number", Enter.theValue(number).into(StartProcessInvoiceForm.NUMBER_FIELD));
    }

    public static Performable setEmail(String email) {
        return Task.where("{0} set number", Enter.theValue(email).into(StartProcessInvoiceForm.EMAIL_FIELD));
    }

    public static Performable setInvoiceId(String invoiceId) {
        return Task.where(
            "{0} set invoice id",
            Enter.theValue(invoiceId).into(StartProcessInvoiceForm.INVOICE_ID_FIELD)
        );
    }

    public static Performable setSignature() {
        WebDriver driver = getDriver();
        Actions actions = new Actions(driver);
        WebElement signatureField = driver.findElement(By.xpath("//*[@class='signature-pad-canvas']"));
        actions.moveToElement(signatureField, 10, 10).clickAndHold().moveByOffset(20, 20).release().perform();
        return Task.where("{0} set signature");
    }

    public static Performable uploadFile(String filePath) {
        String testFilePath = null;
        URL resource = FillInvoiceForm.class.getClassLoader().getResource(filePath);
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

        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//a[@class='browse']")).click();
        driver.findElement(By.xpath("//input[@type='file']")).sendKeys(testFilePath);
        return Task.where("{0} upload file");
    }

    public static Performable clickStart() {
        return Task.where("{0} click start", Click.on(StartProcessInvoiceForm.START_BUTTON));
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
}
