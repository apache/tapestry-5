import io.github.bonigarcia.wdm.managers.FirefoxDriverManager

driver = "firefox"

baseUrl = "http://localhost:8080"
FirefoxDriverManager.firefoxdriver().setup();

waiting {
  // Long timeout since we have to wait for Rhino & friends to spin up
  timeout = 60
}