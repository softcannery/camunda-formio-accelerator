package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class MultiProcessForm {

    public static Performable approveMultiTasks(Actor actor) {
        Actions actions = new Actions(getDriver());
        actor.wasAbleTo(NavigateTo.theTaskListPage());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String[] multiTaskUsers = { "Chris", "Tyler", "Edward" };
        for (String user : multiTaskUsers) {
            WebDriver driver = getDriver();
            driver.findElement(By.xpath("//a[contains(text(),'Evaluate " + user + "')]")).click();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String emplName = (String) js.executeScript(
                "return document.getElementsByName('data[employeeName]')[0].value"
            );
            String emplEmail = (String) js.executeScript(
                "return document.getElementsByName('data[employeeEmail]')[0].value"
            );
            String doh = (String) js.executeScript("return document.getElementsByName('data[doh]')[0].value");
            String position = (String) js.executeScript("return document.getElementsByName('data[position]')[0].value");
            String supervisorName = (String) js.executeScript(
                "return document.getElementsByName('data[supervisorName]')[0].value"
            );
            String supervisorEmail = (String) js.executeScript(
                "return document.getElementsByName('data[supervisorEmail]')[0].value"
            );
            if (user.equals("Chris")) {
                Assert.assertEquals("Wrong employee name", emplName, "Chris");
                Assert.assertEquals("Wrong employee Email", emplEmail, "chris@example.com");
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-02"));
                Assert.assertEquals("Wrong Position", position, "Position3");
                Assert.assertEquals("Wrong supervisor name", supervisorName, "Joe Doe");
                Assert.assertEquals("Wrong supervisor Email", supervisorEmail, "joe@example.com");
            }
            if (user.equals("Tyler")) {
                Assert.assertEquals("Wrong employee name", emplName, "Tyler");
                Assert.assertEquals("Wrong employee Email", emplEmail, "tyler@example.com");
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-04"));
                Assert.assertEquals("Wrong Position", position, "Position2");
                Assert.assertEquals("Wrong supervisor name", supervisorName, "Joe Doe");
                Assert.assertEquals("Wrong supervisor Email", supervisorEmail, "joe@example.com");
            }
            if (user.equals("Edward")) {
                Assert.assertEquals("Wrong employee name", emplName, "Edward");
                Assert.assertEquals("Wrong employee Email", emplEmail, "edward@example.com");
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-01"));
                Assert.assertEquals("Wrong Position", position, "Position1 ");
                Assert.assertEquals("Wrong supervisor name", supervisorName, "Joe Doe");
                Assert.assertEquals("Wrong supervisor Email", supervisorEmail, "joe@example.com");
            }
            WebElement approvedField = driver.findElement(By.xpath("//input[@name='data[approved]']"));
            actions.moveToElement(approvedField).click().build().perform();
            WebElement submitButton = driver.findElement(By.xpath("//button[@ng-click='complete()']"));
            js.executeScript("arguments[0].click();", submitButton);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Task.where("{0} Approve MultiTasks");
    }

    public static Performable completeShowResults() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebDriver driver = getDriver();
        driver.findElement(By.xpath("//a[contains(text(),'Show Result')]")).click();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement row1 = driver.findElement(By.xpath("//tbody/.//tr/.//input[@value='Chris']/../../../.."));
        Assert.assertNotNull("Wrong employee email", row1.findElement(By.xpath("//input[@value='chris@example.com']")));
        Assert.assertNotNull(
            "Wrong employee DOH",
            row1.findElement(By.xpath("//input[contains(@value, '2023-04-02')]"))
        );
        Assert.assertNotNull("Wrong employee Position", row1.findElement(By.xpath("//input[@value='Position3']")));
        Assert.assertNotNull("Wrong employee Supervisor", row1.findElement(By.xpath("//input[@value='Joe Doe']")));
        Assert.assertNotNull(
            "Wrong employee Supervisor Email",
            row1.findElement(By.xpath("//input[@value='joe@example.com']"))
        );
        Assert.assertNotNull("Wrong Form status", row1.findElement(By.xpath("//input[@value='Reviewed']")));
        Assert.assertNotNull("Task must be approved", row1.findElement(By.xpath("//input[@checked='true']")));

        WebElement row2 = driver.findElement(By.xpath("//tbody/.//tr/.//input[@value='Tyler']/../../../.."));
        Assert.assertNotNull("Wrong employee email", row2.findElement(By.xpath("//input[@value='tyler@example.com']")));
        Assert.assertNotNull(
            "Wrong employee DOH",
            row2.findElement(By.xpath("//input[contains(@value, '2023-04-04')]"))
        );
        Assert.assertNotNull("Wrong employee Position", row2.findElement(By.xpath("//input[@value='Position2']")));
        Assert.assertNotNull("Wrong employee Supervisor", row2.findElement(By.xpath("//input[@value='Joe Doe']")));
        Assert.assertNotNull(
            "Wrong employee Supervisor Email",
            row2.findElement(By.xpath("//input[@value='joe@example.com']"))
        );
        Assert.assertNotNull("Wrong Form status", row2.findElement(By.xpath("//input[@value='Reviewed']")));
        Assert.assertNotNull("Task must be approved", row2.findElement(By.xpath("//input[@checked='true']")));

        WebElement row3 = driver.findElement(By.xpath("//tbody/.//tr/.//input[@value='Edward']/../../../.."));
        Assert.assertNotNull(
            "Wrong employee email",
            row3.findElement(By.xpath("//input[@value='edward@example.com']"))
        );
        Assert.assertNotNull(
            "Wrong employee DOH",
            row3.findElement(By.xpath("//input[contains(@value,'2023-04-01')]"))
        );
        Assert.assertNotNull("Wrong employee Position", row3.findElement(By.xpath("//input[@value='Position1 ']")));
        Assert.assertNotNull("Wrong employee Supervisor", row3.findElement(By.xpath("//input[@value='Joe Doe']")));
        Assert.assertNotNull(
            "Wrong employee Supervisor Email",
            row3.findElement(By.xpath("//input[@value='joe@example.com']"))
        );
        Assert.assertNotNull("Wrong Form status", row3.findElement(By.xpath("//input[@value='Reviewed']")));
        Assert.assertNotNull("Task must be approved", row3.findElement(By.xpath("//input[@checked='true']")));

        driver.findElement(By.xpath("//button[@ng-click='claim()']")).click();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement submitButton = driver.findElement(By.xpath("//button[@ng-click='complete()']"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", submitButton);
        return Task.where("{0} Complete MultiTasks");
    }
}
