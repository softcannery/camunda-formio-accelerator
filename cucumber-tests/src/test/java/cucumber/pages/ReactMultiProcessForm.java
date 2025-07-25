package cucumber.pages;

import static net.serenitybdd.core.Serenity.getDriver;

import java.util.concurrent.TimeUnit;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class ReactMultiProcessForm {

    private static final By startProcessBtn = By.xpath("//a[contains(text(),'Start Process')]");
    private static final By processName = By.xpath("//a[contains(text(), 'Multi-Instance Sub-Process Demo')]");
    private static final By submitBtn = By.xpath("//button[text()='Submit']");
    private static final By tasksList = By.xpath("//a[contains(text(),'Tasklist')]");
    private static final By approveCheckBox = By.xpath("//input[@name='data[approved]']");
    private static final By submitForm = By.xpath("//button[@type='submit']");
    private static final By showResults = By.xpath("//div[text()='Show Result']");

    public static Performable startMultiProcess() {
        WebDriver driver = getDriver();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(() -> driver.findElements(startProcessBtn).size() > 0);
        driver.findElement(startProcessBtn).click();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).until(() -> driver.findElements(processName).size() > 0);

        driver.findElement(processName).click();
        driver.findElement(submitBtn).click();
        return Task.where("{0} Start Process");
    }

    public static Performable approveMultiTasks(Actor actor) {
        Actions actions = new Actions(getDriver());
        String[] multiTaskUsers = { "Chris", "Tyler", "Edward" };
        for (String user : multiTaskUsers) {
            actor.wasAbleTo(NavigateTo.theReactMainPage());
            WebDriver driver = getDriver();
            driver.findElement(tasksList).click();
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
                Assert.assertEquals("Wrong employee name", "Chris", emplName);
                Assert.assertEquals("Wrong employee Email", "chris@example.com", emplEmail);
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-02"));
                Assert.assertEquals("Wrong Position", "Position3", position);
                Assert.assertEquals("Wrong supervisor name", "Joe Doe", supervisorName);
                Assert.assertEquals("Wrong supervisor Email", "joe@example.com", supervisorEmail);
            }
            if (user.equals("Tyler")) {
                Assert.assertEquals("Wrong employee name", "Tyler", emplName);
                Assert.assertEquals("Wrong employee Email", "tyler@example.com", emplEmail);
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-04"));
                Assert.assertEquals("Wrong Position", "Position2", position);
                Assert.assertEquals("Wrong supervisor name", "Joe Doe", supervisorName);
                Assert.assertEquals("Wrong supervisor Email", "joe@example.com", supervisorEmail);
            }
            if (user.equals("Edward")) {
                Assert.assertEquals("Wrong employee name", "Edward", emplName);
                Assert.assertEquals("Wrong employee Email", "edward@example.com", emplEmail);
                Assert.assertTrue("Wrong DOH", doh.contains("2023-04-01"));
                Assert.assertEquals("Wrong Position", "Position1 ", position);
                Assert.assertEquals("Wrong supervisor name", "Joe Doe", supervisorName);
                Assert.assertEquals("Wrong supervisor Email", "joe@example.com", supervisorEmail);
            }
            WebElement approvedField = driver.findElement(approveCheckBox);
            actions.moveToElement(approvedField).click().build().perform();
            WebElement submitButton = driver.findElement(submitForm);
            actions.moveToElement(submitButton).click().build().perform();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Task.where("{0} Approve MultiTasks");
    }

    public static Performable completeShowResults() {
        WebDriver driver = getDriver();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> driver.findElements(tasksList).size() > 0);
        driver.findElement(tasksList).click();
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> driver.findElements(showResults).size() > 1);
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
            driver.findElement(submitForm).click();
        } catch (ElementClickInterceptedException e) {
            driver.findElement(submitForm).click();
        }
        return Task.where("{0} Complete MultiTasks");
    }
}
