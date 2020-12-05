// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.test;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.xml.XmlTest;

import io.github.bonigarcia.wdm.FirefoxDriverManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Base class for creating Selenium-based integration test cases. This class implements all the
 * methods of {@link Selenium} and delegates to an instance (setup once per test by
 * {@link #testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)}.
 *
 * @since 5.2.0
 */
public abstract class SeleniumTestCase extends Assert implements Selenium
{
    public final static Logger LOGGER = LoggerFactory.getLogger(SeleniumTestCase.class);

    /**
     * 15 seconds
     */
    public static final String PAGE_LOAD_TIMEOUT = "15000";

    public static final String TOMCAT_6 = "tomcat6";

    public static final String JETTY_7 = "jetty7";

    /**
     * An XPath expression for locating a submit element (very commonly used
     * with {@link #clickAndWait(String)}.
     *
     * @since 5.3
     */
    public static final String SUBMIT = "//input[@type='submit']";

    /**
     * The underlying {@link Selenium} instance that all the methods of this class delegate to;
     * this can be useful when attempting to use SeleniumTestCase with a newer version of Selenium which
     * has added some methods to the interface. This field will not be set until the test case instance
     * has gone through its full initialization.
     *
     * @since 5.3
     */
    @Deprecated
    protected Selenium selenium;

    protected WebDriver webDriver;

    private String baseURL;

    private ErrorReporter errorReporter;

    private ITestContext testContext;

    /**
     * Starts up the servers for the entire test (i.e., for multiple TestCases). By placing &lt;parameter&gt; elements
     * inside the appropriate &lt;test&gt; (of your testng.xml configuration
     * file), you can change the configuration or behavior of the servers. It is common to have two
     * or more identical tests that differ only in terms of the <code>tapestry.browser-start-command</code> parameter,
     * to run tests against multiple browsers.
     * <table>
     * <tr>
     * <th>Parameter</th>
     * <th>Name</th>
     * <th>Default</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>container</td>
     * <td>tapestry.servlet-container</td>
     * <td>JETTY_7</td>
     * <td>The Servlet container to use for the tests. Currently {@link #JETTY_7} or {@link #TOMCAT_6}</td>
     * </tr>
     * <tr>
     * <td>webAppFolder</td>
     * <td>tapestry.web-app-folder</td>
     * <td>src/main/webapp</td>
     * <td>Location of web application context</td>
     * </tr>
     * <tr>
     * <td>contextPath</td>
     * <td>tapestry.context-path</td>
     * <td><em>empty string</em></td>
     * <td>Context path (defaults to root). As elsewhere, the context path should be blank, or start with a slash (but
     * not end with one).</td>
     * </tr>
     * <tr>
     * <td>port</td>
     * <td>tapestry.port</td>
     * <td>9090</td>
     * <td>Port number for web server to listen to</td>
     * </tr>
     * <tr>
     * <td>sslPort</td>
     * <td>tapestry.ssl-port</td>
     * <td>8443</td>
     * <td>Port number for web server to listen to for secure requests</td>
     * </tr>
     * <tr>
     * <td>browserStartCommand</td>
     * <td>tapestry.browser-start-command</td>
     * <td>*firefox</td>
     * <td>Command string used to launch the browser, as defined by Selenium</td>
     * </tr>
     * <caption>Options and defaults</caption>
     * </table>
     *
     * Tests in the <em>beforeStartup</em> group will be run before the start of Selenium. This can be used to
     * programmatically override the above parameter values.
     *
     * This method will be invoked in <em>each</em> subclass, but is set up to only startup the servers once (it checks
     * the {@link ITestContext} to see if the necessary keys are already present).
     *
     * @param testContext
     *         Used to share objects between the launcher and the test suites
     * @throws Exception
     */
    @BeforeTest(dependsOnGroups =
            {"beforeStartup"})
    public void testStartup(final ITestContext testContext, XmlTest xmlTest) throws Exception
    {
        // This is not actually necessary, because TestNG will only invoke this method once
        // even when multiple test cases within the test extend from SeleniumTestCase. TestNG
        // just invokes it on the "first" TestCase instance it has test methods for.

        if (testContext.getAttribute(TapestryTestConstants.SHUTDOWN_ATTRIBUTE) != null)
        {
            return;
        }

        // If a parameter is overridden in another test method, TestNG won't pass the
        // updated value via a parameter, but still passes the original (coming from testng.xml or the default).
        // Seems like a TestNG bug.

        // Map<String, String> testParameters = xmlTest.getParameters();

        TapestryTestConfiguration annotation = this.getClass().getAnnotation(TapestryTestConfiguration.class);
        if (annotation == null)
        {
            @TapestryTestConfiguration
            final class EmptyInnerClass
            {
            }

            annotation = EmptyInnerClass.class.getAnnotation(TapestryTestConfiguration.class);
        }

        String webAppFolder = getParameter(xmlTest, TapestryTestConstants.WEB_APP_FOLDER_PARAMETER,
                annotation.webAppFolder());
        String container = getParameter(xmlTest, TapestryTestConstants.SERVLET_CONTAINER_PARAMETER,
                annotation.container());
        String contextPath = getParameter(xmlTest, TapestryTestConstants.CONTEXT_PATH_PARAMETER,
                annotation.contextPath());
        int port = getIntParameter(xmlTest, TapestryTestConstants.PORT_PARAMETER, annotation.port());
        int sslPort = getIntParameter(xmlTest, TapestryTestConstants.SSL_PORT_PARAMETER, annotation.sslPort());
        String browserStartCommand = getParameter(xmlTest, TapestryTestConstants.BROWSER_START_COMMAND_PARAMETER,
                annotation.browserStartCommand());

        String baseURL = String.format("http://localhost:%d%s/", port, contextPath);

        String sep = System.getProperty("line.separator");

        LOGGER.info("Starting SeleniumTestCase:" + sep +
                "    currentDir: " + System.getProperty("user.dir") + sep +
                "  webAppFolder: " + webAppFolder + sep +
                "     container: " + container + sep +
                "   contextPath: " + contextPath + sep +
                String.format("         ports: %d / %d", port, sslPort) + sep +
                "  browserStart: " + browserStartCommand + sep +
                "       baseURL: " + baseURL);

        final Runnable stopWebServer = launchWebServer(container, webAppFolder, contextPath, port, sslPort);

        FirefoxDriverManager.getInstance().setup();

        File ffProfileTemplate = new File(TapestryRunnerConstants.MODULE_BASE_DIR, "src/test/conf/ff_profile_template");
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
        desiredCapabilities.setCapability(FirefoxDriver.MARIONETTE, true);

        FirefoxOptions options = new FirefoxOptions(desiredCapabilities);

        if (ffProfileTemplate.isDirectory())
        {
            FirefoxProfile profile = new FirefoxProfile(ffProfileTemplate);
            options.setProfile(profile);
        }

        FirefoxDriver driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        CommandProcessor webDriverCommandProcessor = new WebDriverCommandProcessor(baseURL, driver);


        final ErrorReporterImpl errorReporter = new ErrorReporterImpl(driver, testContext);

        ErrorReportingCommandProcessor commandProcessor = new ErrorReportingCommandProcessor(webDriverCommandProcessor,
                errorReporter);

        Selenium selenium = new WebDriverBackedSelenium(driver, baseURL);

        testContext.setAttribute(TapestryTestConstants.BASE_URL_ATTRIBUTE, baseURL);
        testContext.setAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE, selenium);
        testContext.setAttribute(TapestryTestConstants.ERROR_REPORTER_ATTRIBUTE, errorReporter);
        testContext.setAttribute(TapestryTestConstants.COMMAND_PROCESSOR_ATTRIBUTE, commandProcessor);

        testContext.setAttribute(TapestryTestConstants.SHUTDOWN_ATTRIBUTE, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    LOGGER.info("Shutting down selenium client ...");

                    try
                    {
                        selenium.stop();
                    } catch (RuntimeException e)
                    {
                        LOGGER.error("Selenium client shutdown failure.", e);
                    }

                    LOGGER.info("Shutting down webdriver ...");

                    try
                    {
                        if (webDriver != null) { // is sometimes null... but why?
                            webDriver.quit();
                        }
                    } catch (RuntimeException e)
                    {
                        LOGGER.error("Webdriver shutdown failure.", e);
                    }

                    LOGGER.info("Shutting down selenium server ...");

                    LOGGER.info("Shutting web server ...");

                    try
                    {
                        stopWebServer.run();
                    } catch (RuntimeException e)
                    {
                        LOGGER.error("Web server shutdown failure.", e);
                    }

                    // Output, at the end of the Test, any html capture or screen shots (this makes it much easier
                    // to locate them at the end of the run; there's such a variance on where they end up based
                    // on whether the tests are running from inside an IDE or via one of the command line
                    // builds.

                    errorReporter.writeOutputPaths();
                } finally
                {
                    testContext.removeAttribute(TapestryTestConstants.BASE_URL_ATTRIBUTE);
                    testContext.removeAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE);
                    testContext.removeAttribute(TapestryTestConstants.ERROR_REPORTER_ATTRIBUTE);
                    testContext.removeAttribute(TapestryTestConstants.COMMAND_PROCESSOR_ATTRIBUTE);
                    testContext.removeAttribute(TapestryTestConstants.SHUTDOWN_ATTRIBUTE);
                }
            }
        });
    }

    private final String getParameter(XmlTest xmlTest, String key, String defaultValue)
    {
        String value = xmlTest.getParameter(key);

        return value != null ? value : defaultValue;
    }

    private final int getIntParameter(XmlTest xmlTest, String key, int defaultValue)
    {
        String value = xmlTest.getParameter(key);

        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Like {@link #testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)} , this may
     * be called multiple times against multiple instances, but only does work the first time.
     */
    @AfterTest
    public void testShutdown(ITestContext context)
    {
        // Likewise, this method should only be invoked once.
        Runnable r = (Runnable) context.getAttribute(TapestryTestConstants.SHUTDOWN_ATTRIBUTE);

        // This test is still useful, however, because testStartup() may not have completed properly,
        // and the runnable is the last thing it puts into the test context.

        if (r != null)
        {
            LOGGER.info("Shutting down integration test support ...");
            r.run();
        }
    }

    /**
     * Invoked from {@link #testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)} to launch the web
     * server to be tested. The return value is a Runnable that can be invoked later to cleanly shut down the launched
     * server at the end of the test.
     *
     * @param container
     *         identifies which web server should be launched
     * @param webAppFolder
     *         path to the web application context
     * @param contextPath
     *         the path the context is mapped to, usually the empty string
     * @param port
     *         the port number the server should handle
     * @param sslPort
     *         the port number on which the server should handle secure requests
     * @return Runnable used to shut down the server
     * @throws Exception
     */
    protected Runnable launchWebServer(String container, String webAppFolder, String contextPath, int port, int sslPort)
            throws Exception
    {
        final ServletContainerRunner runner = createWebServer(container, webAppFolder, contextPath, port, sslPort);

        return new Runnable()
        {
            @Override
            public void run()
            {
                runner.stop();
            }
        };
    }

    private ServletContainerRunner createWebServer(String container, String webAppFolder, String contextPath, int port, int sslPort) throws Exception
    {
        if (TOMCAT_6.equals(container))
        {
            return new TomcatRunner(webAppFolder, contextPath, port, sslPort);
        }

        if (JETTY_7.equals(container))
        {
            return new JettyRunner(webAppFolder, contextPath, port, sslPort);
        }

        throw new RuntimeException("Unknown servlet container: " + container);
    }

    @BeforeClass
    public void setup(ITestContext context)
    {
        this.testContext = context;

        selenium = (Selenium) context.getAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE);
        webDriver = ((WebDriverBackedSelenium) selenium).getWrappedDriver();
        baseURL = (String) context.getAttribute(TapestryTestConstants.BASE_URL_ATTRIBUTE);
        errorReporter = (ErrorReporter) context.getAttribute(TapestryTestConstants.ERROR_REPORTER_ATTRIBUTE);
    }

    @AfterClass
    public void cleanup()
    {
        selenium = null;
        baseURL = null;
        errorReporter = null;
        testContext = null;
    }

    /**
     * Delegates to {@link ErrorReporter#writeErrorReport(String)} to capture the current page markup in a
     * file for later analysis.
     */
    protected void writeErrorReport(String reportText)
    {
        errorReporter.writeErrorReport(reportText);
    }

    /**
     * Returns the base URL for the application. This is of the typically <code>http://localhost:9999/</code> (i.e., it
     * includes a trailing slash).
     *
     * Generally, you should use {@link #openLinks(String...)} to start from your application's home page.
     */
    public String getBaseURL()
    {
        return baseURL;
    }

    @BeforeMethod
    public void indicateTestMethodName(Method testMethod)
    {
        LOGGER.info("Executing " + testMethod);

        testContext.setAttribute(TapestryTestConstants.CURRENT_TEST_METHOD_ATTRIBUTE, testMethod);

        String className = testMethod.getDeclaringClass().getSimpleName();
        String testName = testMethod.getName().replace("_", " ");

        selenium.setContext(className + ": " + testName);
    }

    @AfterMethod
    public void cleanupTestMethod()
    {
        testContext.setAttribute(TapestryTestConstants.CURRENT_TEST_METHOD_ATTRIBUTE, null);
    }

    // ---------------------------------------------------------------------
    // Start of delegate methods
    //
    // When upgrading to a new version of Selenium, it is probably easiest
    // to delete all these methods and use the Generate Delegate Methods
    // refactoring.
    // ---------------------------------------------------------------------

    @Override
    public void addCustomRequestHeader(String key, String value)
    {
        selenium.addCustomRequestHeader(key, value);
    }

    @Override
    public void addLocationStrategy(String strategyName, String functionDefinition)
    {
        selenium.addLocationStrategy(strategyName, functionDefinition);
    }

    @Override
    public void addScript(String scriptContent, String scriptTagId)
    {
        selenium.addScript(scriptContent, scriptTagId);
    }

    @Override
    public void addSelection(String locator, String optionLocator)
    {
        selenium.addSelection(locator, optionLocator);
    }

    @Override
    public void allowNativeXpath(String allow)
    {
        selenium.allowNativeXpath(allow);
    }

    @Override
    public void altKeyDown()
    {
        selenium.altKeyDown();
    }

    @Override
    public void altKeyUp()
    {
        selenium.altKeyUp();
    }

    @Override
    public void answerOnNextPrompt(String answer)
    {
        selenium.answerOnNextPrompt(answer);
    }

    @Override
    public void assignId(String locator, String identifier)
    {
        selenium.assignId(locator, identifier);
    }

    @Override
    public void attachFile(String fieldLocator, String fileLocator)
    {
        selenium.attachFile(fieldLocator, fileLocator);
    }

    @Override
    public void captureEntirePageScreenshot(String filename, String kwargs)
    {
        selenium.captureEntirePageScreenshot(filename, kwargs);
    }

    @Override
    public String captureEntirePageScreenshotToString(String kwargs)
    {
        return selenium.captureEntirePageScreenshotToString(kwargs);
    }

    @Override
    public String captureNetworkTraffic(String type)
    {
        return selenium.captureNetworkTraffic(type);
    }

    @Override
    public void captureScreenshot(String filename)
    {
        selenium.captureScreenshot(filename);
    }

    @Override
    public String captureScreenshotToString()
    {
        return selenium.captureScreenshotToString();
    }

    @Override
    public void check(String locator)
    {
        WebElement element = webDriver.findElement(convertLocator(locator));
        if (!element.isSelected())
        {
            scrollIntoView(element);
            element.click();
        }
    }

    @Override
    public void chooseCancelOnNextConfirmation()
    {
        selenium.chooseCancelOnNextConfirmation();
    }

    @Override
    public void chooseOkOnNextConfirmation()
    {
        selenium.chooseOkOnNextConfirmation();
    }

    @Override
    public void click(String locator)
    {
        WebElement element = webDriver.findElement(convertLocator(locator));
        scrollIntoView(element);
        JavascriptExecutor executor = (JavascriptExecutor)webDriver;
        executor.executeScript("arguments[0].click();", element);
//      element.click(); // failing as of Aug 2018
    }

    @Override
    public void clickAt(String locator, String coordString)
    {
        selenium.clickAt(locator, coordString);
    }

    @Override
    public void close()
    {
        selenium.close();
    }

    @Override
    public void contextMenu(String locator)
    {
        selenium.contextMenu(locator);
    }

    @Override
    public void contextMenuAt(String locator, String coordString)
    {
        selenium.contextMenuAt(locator, coordString);
    }

    @Override
    public void controlKeyDown()
    {
        selenium.controlKeyDown();
    }

    @Override
    public void controlKeyUp()
    {
        selenium.controlKeyUp();
    }

    @Override
    public void createCookie(String nameValuePair, String optionsString)
    {
        selenium.createCookie(nameValuePair, optionsString);
    }

    @Override
    public void deleteAllVisibleCookies()
    {
        selenium.deleteAllVisibleCookies();
    }

    @Override
    public void deleteCookie(String name, String optionsString)
    {
        selenium.deleteCookie(name, optionsString);
    }

    @Override
    public void deselectPopUp()
    {
        selenium.deselectPopUp();
    }

    @Override
    public void doubleClick(String locator)
    {
        selenium.doubleClick(locator);
    }

    @Override
    public void doubleClickAt(String locator, String coordString)
    {
        selenium.doubleClickAt(locator, coordString);
    }

    @Override
    public void dragAndDrop(String locator, String movementsString)
    {
        selenium.dragAndDrop(locator, movementsString);
    }

    @Override
    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject)
    {
        selenium.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
    }

    @Override
    public void dragdrop(String locator, String movementsString)
    {
        selenium.dragdrop(locator, movementsString);
    }

    @Override
    public void fireEvent(String locator, String eventName)
    {
        selenium.fireEvent(locator, eventName);
    }

    @Override
    public void focus(String locator)
    {
        selenium.focus(locator);
    }

    @Override
    public String getAlert()
    {
        return selenium.getAlert();
    }

    @Override
    public String[] getAllButtons()
    {
        return selenium.getAllButtons();
    }

    @Override
    public String[] getAllFields()
    {
        return selenium.getAllFields();
    }

    @Override
    public String[] getAllLinks()
    {
        return selenium.getAllLinks();
    }

    @Override
    public String[] getAllWindowIds()
    {
        return selenium.getAllWindowIds();
    }

    @Override
    public String[] getAllWindowNames()
    {
        return selenium.getAllWindowNames();
    }

    @Override
    public String[] getAllWindowTitles()
    {
        return selenium.getAllWindowTitles();
    }

    @Override
    public String getAttribute(String attributeLocator)
    {
        return selenium.getAttribute(attributeLocator);
    }

    @Override
    public String[] getAttributeFromAllWindows(String attributeName)
    {
        return selenium.getAttributeFromAllWindows(attributeName);
    }

    @Override
    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    @Override
    public String getConfirmation()
    {
        return selenium.getConfirmation();
    }

    @Override
    public String getCookie()
    {
        return selenium.getCookie();
    }

    @Override
    public String getCookieByName(String name)
    {
        return selenium.getCookieByName(name);
    }

    @Override
    public Number getCursorPosition(String locator)
    {
        return selenium.getCursorPosition(locator);
    }

    @Override
    public Number getElementHeight(String locator)
    {
        return selenium.getElementHeight(locator);
    }

    @Override
    public Number getElementIndex(String locator)
    {
        return selenium.getElementIndex(locator);
    }

    @Override
    public Number getElementPositionLeft(String locator)
    {
        return selenium.getElementPositionLeft(locator);
    }

    @Override
    public Number getElementPositionTop(String locator)
    {
        return selenium.getElementPositionTop(locator);
    }

    @Override
    public Number getElementWidth(String locator)
    {
        return selenium.getElementWidth(locator);
    }

    @Override
    public String getEval(String script)
    {
        return selenium.getEval(script);
    }

    @Override
    public String getExpression(String expression)
    {
        return selenium.getExpression(expression);
    }

    @Override
    public String getHtmlSource()
    {
        return selenium.getHtmlSource();
    }

    @Override
    public String getLocation()
    {
        return selenium.getLocation();
    }

    @Override
    public String getLog()
    {
        return selenium.getLog();
    }

    @Override
    public Number getMouseSpeed()
    {
        return selenium.getMouseSpeed();
    }

    @Override
    public String getPrompt()
    {
        return selenium.getPrompt();
    }

    @Override
    public String getSelectedId(String selectLocator)
    {
        return selenium.getSelectedId(selectLocator);
    }

    @Override
    public String[] getSelectedIds(String selectLocator)
    {
        return selenium.getSelectedIds(selectLocator);
    }

    @Override
    public String getSelectedIndex(String selectLocator)
    {
        return selenium.getSelectedIndex(selectLocator);
    }

    @Override
    public String[] getSelectedIndexes(String selectLocator)
    {
        return selenium.getSelectedIndexes(selectLocator);
    }

    @Override
    public String getSelectedLabel(String selectLocator)
    {
        return selenium.getSelectedLabel(selectLocator);
    }

    @Override
    public String[] getSelectedLabels(String selectLocator)
    {
        return selenium.getSelectedLabels(selectLocator);
    }

    @Override
    public String getSelectedValue(String selectLocator)
    {
        return selenium.getSelectedValue(selectLocator);
    }

    @Override
    public String[] getSelectedValues(String selectLocator)
    {
        return selenium.getSelectedValues(selectLocator);
    }

    @Override
    public String[] getSelectOptions(String selectLocator)
    {
        return selenium.getSelectOptions(selectLocator);
    }

    @Override
    public String getSpeed()
    {
        return selenium.getSpeed();
    }

    @Override
    public String getTable(String tableCellAddress)
    {
        return selenium.getTable(tableCellAddress);
    }

    @Override
    public String getText(String locator)
    {
        return selenium.getText(locator);
    }

    @Override
    public String getTitle()
    {
        return selenium.getTitle();
    }

    @Override
    public String getValue(String locator)
    {
        return selenium.getValue(locator);
    }

    @Override
    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target)
    {
        return selenium.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
    }

    @Override
    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target)
    {
        return selenium.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
    }

    @Override
    public Number getXpathCount(String xpath)
    {
        return selenium.getXpathCount(xpath);
    }

    @Override
    public void goBack()
    {
        selenium.goBack();
    }

    @Override
    public void highlight(String locator)
    {
        selenium.highlight(locator);
    }

    @Override
    public void ignoreAttributesWithoutValue(String ignore)
    {
        selenium.ignoreAttributesWithoutValue(ignore);
    }

    @Override
    public boolean isAlertPresent()
    {
        return selenium.isAlertPresent();
    }

    @Override
    public boolean isChecked(String locator)
    {
        return selenium.isChecked(locator);
    }

    @Override
    public boolean isConfirmationPresent()
    {
        return selenium.isConfirmationPresent();
    }

    @Override
    public boolean isCookiePresent(String name)
    {
        return selenium.isCookiePresent(name);
    }

    @Override
    public boolean isEditable(String locator)
    {
        return selenium.isEditable(locator);
    }

    @Override
    public boolean isElementPresent(String locator)
    {
        return !webDriver.findElements(convertLocator(locator)).isEmpty();
    }

    @Override
    public boolean isOrdered(String locator1, String locator2)
    {
        return selenium.isOrdered(locator1, locator2);
    }

    @Override
    public boolean isPromptPresent()
    {
        return selenium.isPromptPresent();
    }

    @Override
    public boolean isSomethingSelected(String selectLocator)
    {
        return selenium.isSomethingSelected(selectLocator);
    }

    @Override
    public boolean isTextPresent(String pattern)
    {
        return selenium.isTextPresent(pattern);
    }

    @Override
    public boolean isVisible(String locator)
    {
        return selenium.isVisible(locator);
    }

    @Override
    public void keyDown(String locator, String keySequence)
    {
        selenium.keyDown(locator, keySequence);
    }

    @Override
    public void keyDownNative(String keycode)
    {
        selenium.keyDownNative(keycode);
    }

    @Override
    public void keyPress(String locator, String keySequence)
    {
        selenium.keyPress(locator, keySequence);
    }

    @Override
    public void keyPressNative(String keycode)
    {
        selenium.keyPressNative(keycode);
    }

    @Override
    public void keyUp(String locator, String keySequence)
    {
        selenium.keyUp(locator, keySequence);
    }

    @Override
    public void keyUpNative(String keycode)
    {
        selenium.keyUpNative(keycode);
    }

    @Override
    public void metaKeyDown()
    {
        selenium.metaKeyDown();
    }

    @Override
    public void metaKeyUp()
    {
        selenium.metaKeyUp();
    }

    @Override
    public void mouseDown(String locator)
    {
        selenium.mouseDown(locator);
    }

    @Override
    public void mouseDownAt(String locator, String coordString)
    {
        selenium.mouseDownAt(locator, coordString);
    }

    @Override
    public void mouseDownRight(String locator)
    {
        selenium.mouseDownRight(locator);
    }

    @Override
    public void mouseDownRightAt(String locator, String coordString)
    {
        selenium.mouseDownRightAt(locator, coordString);
    }

    @Override
    public void mouseMove(String locator)
    {
        selenium.mouseMove(locator);
    }

    @Override
    public void mouseMoveAt(String locator, String coordString)
    {
        selenium.mouseMoveAt(locator, coordString);
    }

    @Override
    public void mouseOut(String locator)
    {
        selenium.mouseOut(locator);
    }

    @Override
    public void mouseOver(String locator)
    {
        selenium.mouseOver(locator);
    }

    @Override
    public void mouseUp(String locator)
    {
        selenium.mouseUp(locator);
    }

    @Override
    public void mouseUpAt(String locator, String coordString)
    {
        selenium.mouseUpAt(locator, coordString);
    }

    @Override
    public void mouseUpRight(String locator)
    {
        selenium.mouseUpRight(locator);
    }

    @Override
    public void mouseUpRightAt(String locator, String coordString)
    {
        selenium.mouseUpRightAt(locator, coordString);
    }

    @Override
    public void open(String url)
    {
        selenium.open(url);
    }

    @Override
    public void open(String url, String ignoreResponseCode)
    {
        selenium.open(url, ignoreResponseCode);
    }

    @Override
    public void openWindow(String url, String windowID)
    {
        selenium.openWindow(url, windowID);
    }

    @Override
    public void refresh()
    {
        selenium.refresh();
    }

    @Override
    public void removeAllSelections(String locator)
    {
        selenium.removeAllSelections(locator);
    }

    @Override
    public void removeScript(String scriptTagId)
    {
        selenium.removeScript(scriptTagId);
    }

    @Override
    public void removeSelection(String locator, String optionLocator)
    {
        selenium.removeSelection(locator, optionLocator);
    }

    @Override
    public String retrieveLastRemoteControlLogs()
    {
        return selenium.retrieveLastRemoteControlLogs();
    }

    @Override
    public void rollup(String rollupName, String kwargs)
    {
        selenium.rollup(rollupName, kwargs);
    }

    @Override
    public void runScript(String script)
    {
        selenium.runScript(script);
    }

    @Override
    public void select(String selectLocator, String optionLocator)
    {
        selenium.select(selectLocator, optionLocator);
    }

    @Override
    public void selectFrame(String locator)
    {
        selenium.selectFrame(locator);
    }

    @Override
    public void selectPopUp(String windowID)
    {
        selenium.selectPopUp(windowID);
    }

    @Override
    public void selectWindow(String windowID)
    {
        selenium.selectWindow(windowID);
    }

    @Override
    public void setBrowserLogLevel(String logLevel)
    {
        selenium.setBrowserLogLevel(logLevel);
    }

    @Override
    public void setContext(String context)
    {
        selenium.setContext(context);
    }

    @Override
    public void setCursorPosition(String locator, String position)
    {
        selenium.setCursorPosition(locator, position);
    }

    @Override
    public void setExtensionJs(String extensionJs)
    {
        selenium.setExtensionJs(extensionJs);
    }

    @Override
    public void setMouseSpeed(String pixels)
    {
        selenium.setMouseSpeed(pixels);
    }

    @Override
    public void setSpeed(String value)
    {
        selenium.setSpeed(value);
    }

    @Override
    public void setTimeout(String timeout)
    {
        selenium.setTimeout(timeout);
    }

    @Override
    public void shiftKeyDown()
    {
        selenium.shiftKeyDown();
    }

    @Override
    public void shiftKeyUp()
    {
        selenium.shiftKeyUp();
    }

    @Override
    public void showContextualBanner()
    {
        selenium.showContextualBanner();
    }

    @Override
    public void showContextualBanner(String className, String methodName)
    {
        selenium.showContextualBanner(className, methodName);
    }

    @Override
    public void shutDownSeleniumServer()
    {
        selenium.shutDownSeleniumServer();
    }

    @Override
    public void start()
    {
        selenium.start();
    }

    @Override
    public void start(Object optionsObject)
    {
        selenium.start(optionsObject);
    }

    @Override
    public void start(String optionsString)
    {
        selenium.start(optionsString);
    }

    @Override
    public void stop()
    {
        selenium.stop();
    }

    @Override
    public void submit(String formLocator)
    {
        selenium.submit(formLocator);
    }

    @Override
    public void type(String locator, String value)
    {
        WebElement element = webDriver.findElement(convertLocator(locator));
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].value = arguments[1];", element, value);
    }

    @Override
    public void typeKeys(String locator, String value)
    {
        WebElement element = webDriver.findElement(convertLocator(locator));
        element.sendKeys(value);
    }

    @Override
    public void uncheck(String locator)
    {
        selenium.uncheck(locator);
    }

    @Override
    public void useXpathLibrary(String libraryName)
    {
        selenium.useXpathLibrary(libraryName);
    }

    @Override
    public void waitForCondition(String script, String timeout)
    {
        selenium.waitForCondition(script, timeout);
    }

    protected void waitForCondition(ExpectedCondition condition)
    {
      waitForCondition(condition, 10l);
    }

    protected void waitForCondition(ExpectedCondition condition, long timeoutSeconds)
    {
      WebDriverWait wait = new WebDriverWait(webDriver, timeoutSeconds);
      wait.until(condition);
    }

    @Override
    public void waitForFrameToLoad(String frameAddress, String timeout)
    {
        selenium.waitForFrameToLoad(frameAddress, timeout);
    }

    /**
     * Waits for page  to load, then waits for initialization to finish, which is recognized by the {@code data-page-initialized} attribute
     * being set to true on the body element. Polls at increasing intervals, for up-to 30 seconds (that's extraordinarily long, but helps sometimes
     * when manually debugging a page that doesn't have the floating console enabled)..
     */
    @Override
    public void waitForPageToLoad(String timeout)
    {
        selenium.waitForPageToLoad(timeout);

        // In a limited number of cases, a "page" is an container error page or raw HTML content
        // that does not include the body element and data-page-initialized element. In those cases,
        // there will never be page initialization in the Tapestry sense and we return immediately.
        try
        {
            WebElement body = webDriver.findElement(By.cssSelector("body"));

            if (body.getAttribute("data-page-initialized") == null)
            {
                return;
            }
            
            // Attempt to fix StaleElementReferenceException: The element reference of <body> is stale; either the element is no longer attached to the DOM, it is not in the current frame context, or the document has been refreshed
            // waitForCondition(ExpectedConditions.attributeToBe(body, "data-page-initialized", "true"), 30);
            waitForCssSelectorToAppear("body[data-page-initialized='true']");
        } catch (NoSuchElementException e)
        {
            // no body element found, there's nothing to wait for
        } catch (StaleElementReferenceException e) {
            e.printStackTrace();
            System.out.println("Continuing execution after exception above.");
        }
        
    }

    @Override
    public void waitForPopUp(String windowID, String timeout)
    {
        selenium.waitForPopUp(windowID, timeout);
    }

    @Override
    public void windowFocus()
    {
        selenium.windowFocus();
    }

    @Override
    public void windowMaximize()
    {
        selenium.windowMaximize();
    }

    // ---------------------------------------------------------------------
    // End of delegate methods
    // ---------------------------------------------------------------------


    public void scrollIntoView(WebElement element)
    {
        ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Formats a message from the provided arguments, which is written to System.err. In addition,
     * captures the AUT's markup, screenshot, and a report to the output directory.
     *
     * @param message
     * @param arguments
     * @since 5.4
     */
    protected final void reportAndThrowAssertionError(String message, Object... arguments)
    {
        StringBuilder builder = new StringBuilder(5000);

        String formatted = String.format(message, arguments);

        builder.append(formatted);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StringBuilder buffer = new StringBuilder(5000);

        boolean enabled = false;

        for (StackTraceElement e : stackTrace)
        {
            if (enabled)
            {
                buffer.append("\n- ");
                buffer.append(e);
                continue;
            }

            if (e.getMethodName().equals("reportAndThrowAssertionError"))
            {
                enabled = true;
            }
        }

        writeErrorReport(builder.toString());

        throw new AssertionError(formatted);
    }

    protected final void unreachable()
    {
        reportAndThrowAssertionError("An unreachable statement was reached.");
    }

    /**
     * Open the {@linkplain #getBaseURL()}, and waits for the page to load.
     */
    protected final void openBaseURL()
    {
        open(baseURL);

        waitForPageToLoad();
    }

    /**
     * Asserts the text of an element, identified by the locator.
     *
     * @param locator
     *         identifies the element whose text value is to be asserted
     * @param expected
     *         expected value for the element's text
     */
    protected final void assertText(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = getText(locator);
        } catch (RuntimeException ex)
        {
            System.err.printf("Error accessing %s: %s, in:\n\n%s\n\n", locator, ex.getMessage(), getHtmlSource());

            throw ex;
        }

        if (actual.equals(expected))
        {
            return;
        }

        reportAndThrowAssertionError("%s was '%s' not '%s'", locator, actual, expected);
    }

    protected final void assertTextPresent(String... text)
    {
        for (String item : text)
        {
            if (isTextPresent(item))
            {
                continue;
            }

            reportAndThrowAssertionError("Page did not contain '" + item + "'.");
        }
    }

    /**
     * Assets that each string provided is present somewhere in the current document.
     *
     * @param expected
     *         string expected to be present
     */
    protected final void assertSourcePresent(String... expected)
    {
        String source = getHtmlSource();

        for (String snippet : expected)
        {
            if (source.contains(snippet))
            {
                continue;
            }

            reportAndThrowAssertionError("Page did not contain source '" + snippet + "'.");
        }
    }

    /**
     * Click a link identified by a locator, then wait for the resulting page to load.
     * This is not useful for Ajax updates, just normal full-page refreshes.
     *
     * @param locator
     *         identifies the link to click
     */
    protected final void clickAndWait(String locator)
    {
        click(locator);
        waitForPageToLoad();
    }

    /**
     * Waits for the page to load (up to 15 seconds). This is invoked after clicking on an element
     * that forces a full page refresh.
     */
    protected final void waitForPageToLoad()
    {
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    /**
     * Used when the locator identifies an attribute, not an element.
     *
     * @param locator
     *         identifies the attribute whose value is to be asserted
     * @param expected
     *         expected value for the attribute
     */
    protected final void assertAttribute(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = getAttribute(locator);
        } catch (RuntimeException ex)
        {

            reportAndThrowAssertionError("Error accessing %s: %s", locator, ex.getMessage());
        }

        if (actual.equals(expected))
        {
            return;
        }

        reportAndThrowAssertionError("%s was '%s' not '%s'", locator, actual, expected);
    }

    /**
     * Assets that the value in the field matches the expectation
     *
     * @param locator
     *         identifies the field
     * @param expected
     *         expected value for the field
     * @since 5.3
     */
    protected final void assertFieldValue(String locator, String expected)
    {
        try
        {
            assertEquals(getValue(locator), expected);
        } catch (AssertionError ex)
        {
            reportAndThrowAssertionError("Failure accessing %s: %s", locator, ex);
        }
    }

    /**
     * Opens the base URL, then clicks through a series of links to get to a desired application
     * state.
     *
     * @since 5.3
     */
    protected final void openLinks(String... linkText)
    {
        openBaseURL();
        
        if (getTitle().toLowerCase().contains("service unavailable")) {
            throw new RuntimeException("Webapp didn't start correctly. HTML contents: " + getHtmlSource());
        }

        for (String text : linkText)
        {
            clickAndWait("link=" + text);
        }
    }

    /**
     * Sleeps for the indicated number of seconds.
     *
     * @since 5.3
     */
    protected final void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            // Ignore.
        }
    }

    /**
     * Waits for the element with the given client-side id to be present in the DOM (
     * does not assure that the element is visible).
     *
     * @param elementId
     *         identifies the element
     * @since 5.3
     */
    protected final void waitForElementToAppear(String elementId)
    {

        String condition = String.format("selenium.browserbot.getCurrentWindow().document.getElementById(\"%s\")", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }
    
    /**
     * Waits for an element with a given CSS selector to appear.
     *
     * @param selector
     *         the CSS selector to wait.
     * @since 5.5
     */
    protected final void waitForCssSelectorToAppear(String selector)
    {

        String condition = String.format("selenium.browserbot.getCurrentWindow().document.querySelector(\"%s\")", selector);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    /**
     * Waits for the element to be removed from the DOM.
     *
     *
     * This implementation depends on window being extended with testSupport.isNotVisible().
     *
     * @param elementId
     *         client-side id of element
     * @since 5.3
     * @deprecated Deprecated in 5.4 with no replacement
     */
    protected final void waitForElementToDisappear(String elementId)
    {
        String condition = String.format("selenium.browserbot.getCurrentWindow().testSupport.doesNotExist(\"%s\")", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    /**
     * Waits for the element specified by the selector to become visible
     * Note that waitForElementToAppear waits for the element to be present in the dom, visible or not. waitForVisible
     * waits for an element that already exists in the dom to become visible.
     *
     * @param selector
     *         element selector
     * @since 5.3
     */
    protected final void waitForVisible(String selector)
    {
        waitForCondition(ExpectedConditions.visibilityOfElementLocated(convertLocator(selector)));
    }

    /**
     * Waits for the element specified by the selector to become invisible
     * Note that waitForElementToDisappear waits for the element to be absent from the dom, visible or not. waitForInvisible
     * waits for an existing element to become invisible.
     *
     * @param selector
     *         element selector
     * @since 5.3
     */
    protected final void waitForInvisible(String selector)
    {
        waitForCondition(ExpectedConditions.invisibilityOfElementLocated(convertLocator(selector)));
    }

    /**
     * Asserts that the current page's title matches the expected value.
     *
     * @param expected
     *         value for title
     * @since 5.3
     */
    protected final void assertTitle(String expected)
    {
        try
        {
            assertEquals(getTitle(), expected);
        } catch (AssertionError ex)
        {
            reportAndThrowAssertionError("Unexpected title: %s", ex);

            throw ex;
        }
    }

    /**
     * Waits until all active XHR requests are completed.
     *
     * @param timeout
     *         timeout to wait for (no longer used)
     * @since 5.3
     * @deprecated Deprecated in 5.4 in favor of the version without a timeout
     */
    protected final void waitForAjaxRequestsToComplete(String timeout)
    {
        waitForAjaxRequestsToComplete();
    }


    /**
     * Waits until all active XHR requests (as noted by the t5/core/dom module)
     * have completed.
     *
     * @since 5.4
     */
    protected final void waitForAjaxRequestsToComplete()
    {
        // Ugly but necessary. Give the Ajax operation sufficient time to execute normally, then start
        // polling to see if it has complete.
        sleep(250);

        // The t5/core/dom module tracks how many Ajax requests are active
        // and body[data-ajax-active] as appropriate.

        for (int i = 0; i < 10; i++)
        {
            if (i > 0)
            {
                sleep(100);
            }

            if (getCssCount("body[data-ajax-active='0']").equals(1))
            {
                return;
            }
        }

        reportAndThrowAssertionError("Body 'data-ajax-active' attribute never reverted to '0'.");
    }

    @Override
    public Number getCssCount(String str)
    {
        return selenium.getCssCount(str);
    }

    protected static By convertLocator(String locator)
    {
        if (locator.startsWith("link="))
        {
            return By.linkText(locator.substring(5));
        }
        else if (locator.startsWith("css="))
        {
            return By.cssSelector(locator.substring(4));
        }
        else if (locator.startsWith("xpath="))
        {
            return By.xpath(locator.substring(6));
        }
        else if (locator.startsWith("id="))
        {
            return By.id(locator.substring(3));
        }
        else if (locator.startsWith("//"))
        {
            return By.xpath(locator);
        }
        else
        {
            return By.id(locator);
        }
    }
}