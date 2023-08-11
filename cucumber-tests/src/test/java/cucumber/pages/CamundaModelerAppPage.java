package cucumber.pages;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CamundaModelerAppPage {

    private WebDriver driver;

    private By formIoImportBtn = By.xpath("//button[text()='Form.io Import']");
    private By saveBtn = By.xpath("//button[text()='Save']");
    private By bpmnDiagramBtn = By.xpath(
        "//*[@id='welcome-page-platform']/.//button[contains(text(), 'BPMN diagram')]"
    );
    private By apiKeyField = By.xpath("//label[@for='apiKey']/../input");
    private By endpointField = By.xpath("//label[@for='endpoint']/../input");
    private By versionField = By.xpath("//label[@for='tag']/../input");

    public CamundaModelerAppPage() {
        Properties prop = new Properties();
        String confName = "camunda-modeler.conf";
        URL confFileUrl = getClass().getClassLoader().getResource(confName);
        String confPath = confFileUrl.getPath();
        try (FileInputStream fis = new FileInputStream(confPath)) {
            prop.load(fis);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String camundaModelerPath = prop.getProperty("camundaModelerPath");
        String userDirPath = prop.getProperty("userDirPath");
        String browserVersion = prop.getProperty("browserVersion");
        boolean headless = Boolean.parseBoolean(prop.getProperty("headless"));

        String configJosnName = "config.json";
        URL configJSONUrl = getClass().getClassLoader().getResource(configJosnName);
        String configJSONPath = configJSONUrl.getPath();
        InputStream is = null;
        OutputStream os = null;
        File source = new File(configJSONPath);
        File dest = new File(userDirPath + "/config.json");
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ChromeOptions opt = new ChromeOptions();
        opt.setBinary(camundaModelerPath);
        opt.setBrowserVersion(browserVersion);
        opt.addArguments("--user-data-dir=" + userDirPath);
        if (headless) {
            opt.addArguments("--headless=new");
        }
        driver = new ChromeDriver(opt);
    }

    private void clickSaveBtn() {
        try {
            driver.findElement(saveBtn).click();
        } catch (NoSuchElementException ex) {}
    }

    private void clickBPMNdiagramBtn() {
        driver.findElement(bpmnDiagramBtn).click();
    }

    public void openNewDiagram() {
        //clickSaveBtn();
        clickBPMNdiagramBtn();
    }

    public boolean isFormIoImportPresent() {
        try {
            WebElement formIoImportBtnEl = driver.findElement(formIoImportBtn);
            return formIoImportBtnEl.isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    public void openFormIoImportMenu() {
        driver.findElement(formIoImportBtn).click();
        Assert.assertTrue("Api Key field is not present", driver.findElement(apiKeyField).isDisplayed());
        Assert.assertTrue("Endpoint field is not present", driver.findElement(endpointField).isDisplayed());
        Assert.assertTrue("Version field is not present", driver.findElement(versionField).isDisplayed());
    }

    public void closeDriver() {
        driver.quit();
    }
}
