driver = "firefox"

baseUrl = "http://localhost:8080"

waiting {
  // Long timeout since we have to wait for Rhino & friends to spin up
  timeout = 240
}