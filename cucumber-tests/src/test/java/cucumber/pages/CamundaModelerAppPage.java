package cucumber.pages;

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

    public CamundaModelerAppPage() {
        ChromeOptions opt = new ChromeOptions();
        opt.setBinary("/Applications/Camunda Modeler.app/Contents/MacOS/Camunda Modeler");
        opt.setBrowserVersion("108");
        opt.addArguments("--user-data-dir=/Users/Mike/Library/Application Support/camunda-modeler");
        //opt.addArguments("--headless=new");
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
        clickSaveBtn();
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

    public void closeDriver() {
        driver.quit();
    }
}
