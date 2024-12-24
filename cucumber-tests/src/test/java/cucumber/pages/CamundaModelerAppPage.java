package cucumber.pages;

import static java.lang.Thread.sleep;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CamundaModelerAppPage {

    private WebDriver driver;

    private By formIoImportBtn = By.xpath("//button[text()='Form.io Import']");

    private By deployToCamundaBtn = By.xpath("//button[text()='Deploy to Camunda']");
    private By saveBtn = By.xpath("//button[text()='Save']");
    private By bpmnDiagramBtn = By.xpath(
        "//*[@id='welcome-page-platform']/.//button[contains(text(), 'BPMN diagram')]"
    );
    private By apiKeyField = By.xpath("//label[@for='apiKey']/../input");
    private By endpointField = By.xpath("//label[@for='endpoint']/../input");
    private By versionField = By.xpath("//label[@for='tag']/../input");
    private By deploymentNameField = By.xpath("//label[@for='deployment.name']/../input");
    private By deploymentTenantId = By.xpath("//label[@for='deployment.tenantId']/../input");
    private By deploymentRestEndpoint = By.xpath("//label[@for='endpoint.url']/../input");
    private By deploymentUsername = By.xpath("//label[@for='endpoint.username']/../input");
    private By deploymentPassword = By.xpath("//label[@for='endpoint.password']/../input");
    private By deploymentRBHttp = By.xpath("//label[@for='radio-element-http-basic']/../input");
    private By deploymentRBToken = By.xpath("//label[@for='radio-element-bearer-token']/../input");
    private By deploymentAddAttachments = By.xpath("//label[@for='deployment.attachments']/../input");

    public CamundaModelerAppPage() {
        Properties prop = new Properties();
        String confName = "modeler/camunda-modeler.conf";
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

        String configJosnName = "modeler/config.json";
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

        System.setProperty(
            "webdriver.chrome.driver",
            getClass().getClassLoader().getResource("files/chromedriver").getPath()
        );
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

    public boolean isDeployToCamundaPresent() {
        try {
            WebElement deployToCamundaBtnEl = driver.findElement(deployToCamundaBtn);
            return deployToCamundaBtnEl.isDisplayed();
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

    public void openDeployToCamundaMenu() {
        driver.findElement(deployToCamundaBtn).click();
        Assert.assertTrue(
            "Deployment name field is not present",
            driver.findElement(deploymentNameField).isDisplayed()
        );
        Assert.assertTrue("Tenant ID field is not present", driver.findElement(deploymentTenantId).isDisplayed());
        Assert.assertTrue(
            "REST Endpoint field is not present",
            driver.findElement(deploymentRestEndpoint).isDisplayed()
        );
        Assert.assertTrue("HTTP Basic radio button is not present", driver.findElement(deploymentRBHttp).isDisplayed());
        Assert.assertTrue(
            "Bearer token radio button field is not present",
            driver.findElement(deploymentRBToken).isDisplayed()
        );
        Assert.assertTrue("Username field is not present", driver.findElement(deploymentUsername).isDisplayed());
        Assert.assertTrue("Password field is not present", driver.findElement(deploymentPassword).isDisplayed());
        Assert.assertTrue("Password field is not present", driver.findElement(deploymentPassword).isDisplayed());
        Assert.assertTrue(
            "Include additional files is not present",
            driver.findElement(deploymentAddAttachments).isDisplayed()
        );
        driver.findElement(deployToCamundaBtn).click();
    }

    public void deployBpmnProcess(String processName) throws URISyntaxException, InterruptedException {
        driver.findElement(deployToCamundaBtn).click();
        driver.findElement(By.xpath("//input[@name='deployment.name']")).sendKeys("process");
        for (int i = 0; i < 35; i++) {
            driver.findElement(By.xpath("//input[@name='endpoint.url']")).sendKeys(Keys.BACK_SPACE);
        }
        driver.findElement(By.xpath("//input[@name='endpoint.url']")).sendKeys("http://localhost/bpm/engine-rest");
        driver.findElement(By.xpath("//input[@name='endpoint.username']")).sendKeys("test");
        driver.findElement(By.xpath("//input[@name='endpoint.password']")).sendKeys("test");

        URL resource = InvoiceForm.class.getClassLoader().getResource("files/" + processName + ".bpmn");
        URL review = InvoiceForm.class.getClassLoader().getResource("files/" + processName + "-review.formio");
        URL submit = InvoiceForm.class.getClassLoader().getResource("files/" + processName + "-submit.formio");
        driver
            .findElement(net.serenitybdd.core.annotations.findby.By.xpath("//input[@type='file']"))
            .sendKeys(new String(new File(resource.toURI()).getAbsolutePath()));
        driver
            .findElement(net.serenitybdd.core.annotations.findby.By.xpath("//input[@type='file']"))
            .sendKeys(new String(new File(review.toURI()).getAbsolutePath()));
        driver
            .findElement(net.serenitybdd.core.annotations.findby.By.xpath("//input[@type='file']"))
            .sendKeys(new String(new File(submit.toURI()).getAbsolutePath()));
        driver.findElement(By.xpath("//button[text()='Deploy']")).click();
        sleep(4000);
        driver.findElement(By.xpath("//h3[text()='Deployment succeeded']"));
    }

    public void closeDriver() {
        driver.quit();
    }
}
