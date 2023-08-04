package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

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
        for (int i = 0; i<20; i++) {
            try {
                driver.findElement(By.xpath("//a[contains(text(),'Start Process')]")).click();
                driver.findElement(By.xpath("//a[contains(text(), 'Simple Formio Task Action')]")).getText();
                break;
            } catch (NoSuchElementException e){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
        driver.findElement(By.xpath("//a[contains(text(), 'Simple Formio Task Action')]")).click();
        driver.findElement(By.xpath("//input[@name='data[textField]']")).sendKeys("test");
        driver.findElement(By.xpath("//input[@name='data[number]']")).sendKeys("11111");
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
