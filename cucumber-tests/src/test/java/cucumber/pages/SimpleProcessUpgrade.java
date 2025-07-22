package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import java.util.concurrent.TimeUnit;
import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.awaitility.Awaitility;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class SimpleProcessUpgrade {

    public static Performable uploadFiles(String[] filePaths) {
        String testFilePath = "";
        int filesCount = filePaths.length;
        for (String file : filePaths) {
            filesCount = filesCount - 1;
            testFilePath = testFilePath + file;
            if (filesCount != 0) {
                testFilePath = testFilePath + "\n";
            }
        }

        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//input[@type='file']")).sendKeys(testFilePath);
        return Task.where("{0} upload file");
    }

    public static int getSimpleProcessVersion() {
        WebDriver driver = getDriver();
        Awaitility
            .await()
            .atMost(20, TimeUnit.SECONDS)
            .until(() -> driver.findElements(By.xpath("//a[contains(text(),'Start Process')]")).size() > 0);
        driver.findElement(By.xpath("//a[contains(text(),'Start Process')]")).click();
        Awaitility
            .await()
            .atMost(20, TimeUnit.SECONDS)
            .until(() -> driver.findElements(By.xpath("//a[contains(text(), 'Simple Formio Task Action')]")).size() > 0
            );
        String textInList = driver
            .findElement(By.xpath("//a[contains(text(), 'Simple Formio Task Action')]"))
            .getText();
        String[] strVersionArr = textInList.split(" ");
        String strVersion = strVersionArr[strVersionArr.length - 1];
        return Integer.parseInt(strVersion);
    }

    public static Performable startSimpleProcess() {
        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//a[contains(text(),'Start Process')]")).click();
        Actions actions = new Actions(driver);
        WebElement processName = driver.findElement(By.xpath("//a[contains(text(), 'Simple Formio Task Action')]"));
        actions.moveToElement(processName).click().build().perform();
        driver.findElement(By.xpath("//input[@name='data[textField]']")).sendKeys("test");
        driver.findElement(By.xpath("//input[@name='data[number]']")).sendKeys("11111");
        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> driver.findElements(By.xpath("//button[@type='submit']")).size() > 0);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        return Task.where("{0} Start Process");
    }

    public static Performable submitSimpleProcess() {
        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//a[contains(text(),'Tasklist')]")).click();
        driver.findElement(By.xpath("//div[text()='Review']")).click();
        MainReactPage.DROPDOWN_ACTION.selectByVisibleText("Approve");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        return Task.where("{0} Approve Process");
    }
}
