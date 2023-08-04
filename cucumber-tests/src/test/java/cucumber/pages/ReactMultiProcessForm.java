package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import net.serenitybdd.core.annotations.findby.By;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.junit.Assert;
import org.openqa.selenium.*;

public class ReactMultiProcessForm {

    public static Performable startMultiProcess() {
        WebDriver driver = getDriver();
        for (int i = 0; i < 20; i++) {
            try {
                driver.findElement(By.xpath("//a[contains(text(),'Start Process')]")).click();
                driver.findElement(By.xpath("//a[contains(text(), 'Multi-Instance Sub-Process Demo')]"));
                break;
            } catch (NoSuchElementException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        driver.findElement(By.xpath("//a[contains(text(), 'Multi-Instance Sub-Process Demo')]")).click();
        driver.findElement(By.xpath("//button[text()='Submit']")).click();
        return Task.where("{0} Start Process");
    }

    public static Performable approveMultiTasks(Actor actor) {
        String[] multiTaskUsers = { "Chris", "Tyler", "Edward" };
        for (String user : multiTaskUsers) {
            actor.wasAbleTo(NavigateTo.theReactMainPage());
            WebDriver driver = getDriver();
            driver.findElement(By.xpath("//a[contains(text(),'Tasklist')]")).click();
            try {
                driver.findElement(By.xpath("(//div[text()='Evaluate " + user + "'])[1]")).click();
            } catch (ElementClickInterceptedException e) {
                driver.findElement(By.xpath("(//div[text()='Evaluate " + user + "'])[1]")).click();
            }
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
            try {
                driver.findElement(By.xpath("//input[@name='data[approved]']")).click();
            } catch (ElementClickInterceptedException e) {
                driver.findElement(By.xpath("//input[@name='data[approved]']")).click();
            }

            try {
                driver.findElement(By.xpath("//button[@type='submit']")).click();
            } catch (ElementClickInterceptedException e) {
                driver.findElement(By.xpath("//button[@type='submit']")).click();
            }
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
        driver.findElement(By.xpath("//a[contains(text(),'Tasklist')]")).click();
        driver.findElement(By.xpath("(//div[text()='Show Result'])[1]")).click();
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

        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (ElementClickInterceptedException e) {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        }
        return Task.where("{0} Complete MultiTasks");
    }
}
