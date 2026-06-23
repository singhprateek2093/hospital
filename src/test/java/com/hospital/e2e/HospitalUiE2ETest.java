package com.hospital.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end UI tests driven through a real Chrome browser with Selenium.
 *
 * These verify the WHOLE stack (React → API → DB) the way a user would:
 * logging in, role-based navigation, role-filtered data, and error handling.
 *
 * REQUIRES THE APP TO BE RUNNING. Start it first (see README), then:
 *     mvn test -Pe2e
 *
 * Config via system properties:
 *     -De2e.baseUrl=http://localhost:5173   (the frontend URL; default below)
 *     -De2e.headless=false                  (watch the browser; default headless)
 *
 * Assumes the default dev (H2) seed data: 14 patients total, doctor Kamlesh has 7.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HospitalUiE2ETest {

    private static final String BASE_URL =
            System.getProperty("e2e.baseUrl", "http://localhost:5173");
    private static final boolean HEADLESS =
            Boolean.parseBoolean(System.getProperty("e2e.headless", "true"));

    private WebDriver driver;
    private WebDriverWait wait;

    /** Fresh browser per test → complete isolation (no shared cookies/localStorage/state). */
    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,1000");
        // Selenium Manager auto-downloads a matching ChromeDriver — no setup needed.
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /** Save a screenshot for debugging, then close the browser. */
    @AfterEach
    void stopBrowser(TestInfo info) {
        try {
            Path dir = Path.of("target", "e2e-screenshots");
            Files.createDirectories(dir);
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), dir.resolve(info.getTestMethod().get().getName() + ".png"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
        } finally {
            if (driver != null) driver.quit();
        }
    }

    // ---- helpers --------------------------------------------------------

    private void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        setReactInput(By.cssSelector("input[type=email]"), email);
        setReactInput(By.cssSelector("input[type=password]"), password);
        driver.findElement(By.cssSelector("button[type=submit]")).click();
    }

    /**
     * Reliably fill a React-controlled <input>. Plain sendKeys races React's onChange
     * (React can reset the value to its state before the handler attaches), so we set
     * the value via the native setter and fire a real "input" event React listens to,
     * then wait until the value sticks.
     */
    private void setReactInput(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0], v = arguments[1];" +
                "const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                "setter.call(el, v);" +
                "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                "el.dispatchEvent(new Event('change', { bubbles: true }));",
                el, value);
        // Confirm React kept the value (didn't reset it).
        wait.until(ExpectedConditions.attributeToBe(locator, "value", value));
    }

    private void waitForDashboard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Welcome')]")));
    }

    /** The distinct "Doctor" column values shown in the patients table (current page). */
    private Set<String> doctorColumnValues() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
        Set<String> doctors = new HashSet<>();
        for (WebElement row : driver.findElements(By.cssSelector("table tbody tr"))) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 5) doctors.add(cells.get(4).getText().trim()); // 5th col = Doctor
        }
        return doctors;
    }

    // ---- tests ----------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Admin sees patients across MULTIPLE doctors (full access)")
    void adminLogin_seesAllPatients() {
        login("rakesh@hospital.in", "admin123");
        waitForDashboard();

        driver.get(BASE_URL + "/patients");
        // Count-independent: an admin's list spans more than one doctor's patients.
        Set<String> doctors = doctorColumnValues();
        assertTrue(doctors.contains("Dr. Kamlesh Ramgiri") && doctors.contains("Dr. Ravali Ramgiri"),
                "Admin should see patients of BOTH doctors, but the page showed: " + doctors);
    }

    @Test
    @Order(2)
    @DisplayName("Doctor sees ONLY their own patients (\"My Patients\")")
    void doctorLogin_seesOnlyOwnPatients() {
        login("kamlesh@hospital.in", "doctor123");
        waitForDashboard();

        driver.get(BASE_URL + "/patients");
        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("My Patients", heading.getText().trim(),
                "Doctor's patient list should be titled 'My Patients'");

        // The real security invariant: EVERY visible patient is assigned to this doctor.
        Set<String> doctors = doctorColumnValues();
        assertEquals(Set.of("Dr. Kamlesh Ramgiri"), doctors,
                "Doctor must see only their own patients, but the page showed doctors: " + doctors);
    }

    @Test
    @Order(3)
    @DisplayName("Doctor does NOT see staff-only nav (New Patient / Add User)")
    void doctorLogin_hidesStaffNavigation() {
        login("kamlesh@hospital.in", "doctor123");
        waitForDashboard();

        assertTrue(driver.findElements(By.partialLinkText("New Patient")).isEmpty(),
                "Doctor must not see the 'New Patient' link");
        assertTrue(driver.findElements(By.partialLinkText("Add User")).isEmpty(),
                "Doctor must not see the 'Add User' link");
    }

    @Test
    @Order(4)
    @DisplayName("Admin sees staff-only nav (New Patient / Add User)")
    void adminLogin_showsStaffNavigation() {
        login("rakesh@hospital.in", "admin123");
        waitForDashboard();

        assertFalse(driver.findElements(By.partialLinkText("New Patient")).isEmpty(),
                "Admin should see the 'New Patient' link");
        assertFalse(driver.findElements(By.partialLinkText("Add User")).isEmpty(),
                "Admin should see the 'Add User' link");
    }

    @Test
    @Order(5)
    @DisplayName("Invalid credentials show an error and stay on the login page")
    void invalidLogin_showsError() {
        login("rakesh@hospital.in", "wrong-password");
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertFalse(error.getText().isBlank(), "An error message should be displayed");
        // Still on the login screen (email field present)
        assertFalse(driver.findElements(By.cssSelector("input[type=email]")).isEmpty(),
                "User should remain on the login page after a failed login");
    }
}
