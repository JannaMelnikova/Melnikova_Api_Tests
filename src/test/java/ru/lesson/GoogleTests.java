package ru.lesson;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.JavascriptExecutor;
import java.time.Duration;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GoogleTests {
    private WebDriver driver;
    private JavascriptExecutor js;
    private WebDriverWait wait;

    static Stream<Arguments> formData() {
        return Stream.of(
                Arguments.of("JannaMelnikova", "jannet87list.ru", "Service 1", "Testing.", false),
                Arguments.of("JannaMelnikova", "jannet87@list.ru", "Service 1", "Testing.", true)
        );
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized");
        driver = new ChromeDriver(chromeOptions);
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @ParameterizedTest
    @MethodSource("formData")
    public void checkFormRegistration(String name, String email, String service, String message, boolean expectedSuccess) {
        driver.get("https://qatest.datasub.com/quote.html");

        try {
            // Прокрутка к кнопке "Request A Quote"
            WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            js.executeScript("arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", submitButton);

            // Заполнение формы
            WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("q_name")));
            nameField.sendKeys(name);
            WebElement qEmail = driver.findElement(By.id("q_email"));
            qEmail.sendKeys(email);

            Select qService = new Select(driver.findElement(By.id("q_service")));
            qService.selectByVisibleText(service);

            WebElement qMessage = driver.findElement(By.id("q_message"));
            qMessage.sendKeys(message);
            // Отправка формы
            new Actions(driver)
                    .moveToElement(submitButton)
                    .click()
                    .perform();

            // Проверка результата
            if (expectedSuccess) {
                // Ожидание появления сообщения об успехе
                WebElement statusMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("quoteStatus")));
                assertNotNull(statusMessage, "Форма отправлена успешно!");
                assertTrue(statusMessage.isDisplayed(), "Форма отправлена успешно!");
            } else {
                // Ожидание, что `#quoteStatus` не появился
                boolean isStatusInvisible = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("quoteStatus")));
                assertTrue(isStatusInvisible, "Сообщение об успехе не должно отображаться при невалидных данных");
            }

        } catch (TimeoutException e) {
            fail("Элемент не найден за 10 секунд");
            throw e;
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}