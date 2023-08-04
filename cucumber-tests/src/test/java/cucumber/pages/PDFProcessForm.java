package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.junit.Assert;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class PDFProcessForm {

    public static Performable startProcess() {
        WebDriver driver = getDriver();
        for (int i = 0; i < 20; i++) {
            try {
                driver.findElement(By.xpath("//a[contains(text(),'Start Process')]")).click();
                driver.findElement(By.xpath("//a[contains(text(), 'Example PDF Form')]")).click();
                break;
            } catch (NoSuchElementException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.switchTo().frame(0);
        driver.findElement(By.xpath("//input[@name='data[name]']")).sendKeys("test");
        driver.findElement(By.xpath("//input[@name='data[clientId]']")).sendKeys("11111");
        driver.findElement(By.xpath("//input[@name='data[facilitySiteProgram]']")).sendKeys("site test");
        driver.findElement(By.xpath("//input[@name='data[date2]']/../input[@type='text']")).sendKeys("2023-11-11");
        driver.findElement(By.xpath("//input[@name='data[printName1]']/../input[@type='text']")).sendKeys("print name");
        driver.switchTo().defaultContent();
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (ElementClickInterceptedException e) {
            e.getMessage();
        }
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (ElementClickInterceptedException e) {
            e.getMessage();
        }
        return Task.where("{0} Start Process");
    }

    public static Performable submitPdfProcess() {
        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//a[contains(text(),'Tasklist')]")).click();
        driver.findElement(By.xpath("//div[text()='Review PDF']")).click();
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
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (ElementClickInterceptedException e) {
            e.getMessage();
        }
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (ElementClickInterceptedException e) {
            e.getMessage();
        }
        return Task.where("{0} Submit Process");
    }
}
