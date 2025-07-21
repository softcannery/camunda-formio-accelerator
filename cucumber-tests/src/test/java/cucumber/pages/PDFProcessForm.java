package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import java.util.concurrent.TimeUnit;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class PDFProcessForm {

    private static final By nameEl = By.xpath("//input[@name='data[name]']");
    private static final By clientIdEl = By.xpath("//input[@name='data[clientId]']");
    private static final By fasSitePrEl = By.xpath("//input[@name='data[facilitySiteProgram]']");
    private static final By dateEl = By.xpath("//input[@name='data[date2]']/../input[@type='text']");
    private static final By printNameEl = By.xpath("//input[@name='data[printName1]']/../input[@type='text']");
    private static final By submitBtnEl = By.xpath("//button[@ng-click='startProcessInstance()']");
    private static final By reviewPDFEl = By.xpath("//a[contains(text(),'Review PDF')]");
    private static final By claimBtnEl = By.xpath("//button[@ng-click='claim()']");
    private static final By completeBtnEl = By.xpath("//button[@ng-click='complete()']");

    public static Performable startProcess() {
        WebDriver driver = getDriver();
        Actions actions = new Actions(driver);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.switchTo().frame(0);
        driver.findElement(nameEl).sendKeys("test");
        driver.findElement(clientIdEl).sendKeys("11111");
        driver.findElement(fasSitePrEl).sendKeys("site test");
        driver.findElement(dateEl).sendKeys("2023-11-11");
        driver.findElement(printNameEl).sendKeys("print name");
        driver.switchTo().defaultContent();

        WebElement submitButton = driver.findElement(submitBtnEl);
        actions.moveToElement(submitButton).click().build().perform();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        actions.moveToElement(submitButton).click().build();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Task.where("{0} Start Process");
    }

    public static Performable submitPdfProcess() {
        WebDriver driver = getDriver();
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> driver.findElements(reviewPDFEl).size() > 0);
        driver.findElement(reviewPDFEl).click();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.switchTo().frame(0);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String actualName = (String) js.executeScript("return document.getElementsByName('data[name]')[0].value");
        String actualClientId = (String) js.executeScript(
            "return document.getElementsByName('data[clientId]')[0].value"
        );
        String actualFacilitySiteProgram = (String) js.executeScript(
            "return document.getElementsByName('data[facilitySiteProgram]')[0].value"
        );
        String actualDate = (String) js.executeScript("return document.getElementsByName('data[date2]')[0].value");

        Assert.assertEquals("The Name field is incorrect", "test", actualName);
        Assert.assertEquals("The Client Id field is incorrect", "11111", actualClientId);
        Assert.assertEquals("The Facility/Site/Program field is incorrect", "site test", actualFacilitySiteProgram);
        Assert.assertEquals("The Date field is incorrect", "2023-11-11T00:00:00", actualDate);
        driver.switchTo().defaultContent();
        driver.findElement(claimBtnEl).click();

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> driver.findElements(completeBtnEl).size() > 0);
        WebElement submitButton = driver.findElement(completeBtnEl);
        js.executeScript("arguments[0].click();", submitButton);
        return Task.where("{0} Submit Process");
    }
}
