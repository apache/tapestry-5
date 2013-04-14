// Copyright 2009, 2010, 2012 The Apache Software Foundation
//
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

import java.io.File;
import java.lang.reflect.Method;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.xml.XmlTest;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.Selenium;

/**
 * Base class for creating Selenium-based integration test cases. This class implements all the
 * methods of {@link Selenium} and delegates to an instance (setup once per test by
 * {@link #testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)}.
 *
 * @since 5.2.0
 */
public abstract class SeleniumTestCase extends Assert implements Selenium
{
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
    protected Selenium selenium;

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
     * </table>
     * <p/>
     * Tests in the <em>beforeStartup</em> group will be run before the start of Selenium. This can be used to
     * programmatically override the above parameter values.
     * <p/>
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

        System.err.println("Starting SeleniumTestCase:");
        System.err.println("    currentDir: " + System.getProperty("user.dir"));
        System.err.println("  webAppFolder: " + webAppFolder);
        System.err.println("     container: " + container);
        System.err.println("   contextPath: " + contextPath);
        System.err.printf("         ports: %d / %d%n", port, sslPort);
        System.err.println("  browserStart: " + browserStartCommand);
        System.err.println("       baseURL: " + baseURL);

        final Runnable stopWebServer = launchWebServer(container, webAppFolder, contextPath, port, sslPort);

        final SeleniumServer seleniumServer = new SeleniumServer();

        File ffProfileTemplate = new File(TapestryTestConstants.MODULE_BASE_DIR, "src/test/conf/ff_profile_template");

        if (ffProfileTemplate.isDirectory())
        {
            seleniumServer.getConfiguration().setFirefoxProfileTemplate(ffProfileTemplate);
        }

        seleniumServer.start();


        CommandProcessor httpCommandProcessor = new HttpCommandProcessor("localhost",
                RemoteControlConfiguration.DEFAULT_PORT, browserStartCommand, baseURL);

        final ErrorReporterImpl errorReporter = new ErrorReporterImpl(httpCommandProcessor, testContext);

        ErrorReportingCommandProcessor commandProcessor = new ErrorReportingCommandProcessor(httpCommandProcessor,
                errorReporter);

        final Selenium selenium = new DefaultSelenium(commandProcessor);

        selenium.start();

        testContext.setAttribute(TapestryTestConstants.BASE_URL_ATTRIBUTE, baseURL);
        testContext.setAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE, selenium);
        testContext.setAttribute(TapestryTestConstants.ERROR_REPORTER_ATTRIBUTE, errorReporter);
        testContext.setAttribute(TapestryTestConstants.COMMAND_PROCESSOR_ATTRIBUTE, commandProcessor);

        testContext.setAttribute(TapestryTestConstants.SHUTDOWN_ATTRIBUTE, new Runnable()
        {
            public void run()
            {
                try
                {
                    selenium.stop();
                    seleniumServer.stop();
                    stopWebServer.run();

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
            return new Tomcat6Runner(webAppFolder, contextPath, port, sslPort);
        }

        if (JETTY_7.equals(container))
        {
            return new Jetty7Runner(webAppFolder, contextPath, port, sslPort);
        }

        throw new RuntimeException("Unknown servlet container: " + container);
    }

    @BeforeClass
    public void setup(ITestContext context)
    {
        this.testContext = context;

        selenium = (Selenium) context.getAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE);
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
        errorReporter.writeErrorReport();
    }

    /**
     * Returns the base URL for the application. This is of the typically <code>http://localhost:9999/</code> (i.e., it
     * includes a trailing slash).
     * <p/>
     * Generally, you should use {@link #openLinks(String...)} to start from your application's home page.
     */
    public String getBaseURL()
    {
        return baseURL;
    }

    @BeforeMethod
    public void indicateTestMethodName(Method testMethod)
    {
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

    public void addCustomRequestHeader(String key, String value)
    {
        selenium.addCustomRequestHeader(key, value);
    }

    public void addLocationStrategy(String strategyName, String functionDefinition)
    {
        selenium.addLocationStrategy(strategyName, functionDefinition);
    }

    public void addScript(String scriptContent, String scriptTagId)
    {
        selenium.addScript(scriptContent, scriptTagId);
    }

    public void addSelection(String locator, String optionLocator)
    {
        selenium.addSelection(locator, optionLocator);
    }

    public void allowNativeXpath(String allow)
    {
        selenium.allowNativeXpath(allow);
    }

    public void altKeyDown()
    {
        selenium.altKeyDown();
    }

    public void altKeyUp()
    {
        selenium.altKeyUp();
    }

    public void answerOnNextPrompt(String answer)
    {
        selenium.answerOnNextPrompt(answer);
    }

    public void assignId(String locator, String identifier)
    {
        selenium.assignId(locator, identifier);
    }

    public void attachFile(String fieldLocator, String fileLocator)
    {
        selenium.attachFile(fieldLocator, fileLocator);
    }

    public void captureEntirePageScreenshot(String filename, String kwargs)
    {
        selenium.captureEntirePageScreenshot(filename, kwargs);
    }

    public String captureEntirePageScreenshotToString(String kwargs)
    {
        return selenium.captureEntirePageScreenshotToString(kwargs);
    }

    public String captureNetworkTraffic(String type)
    {
        return selenium.captureNetworkTraffic(type);
    }

    public void captureScreenshot(String filename)
    {
        selenium.captureScreenshot(filename);
    }

    public String captureScreenshotToString()
    {
        return selenium.captureScreenshotToString();
    }

    public void check(String locator)
    {
        selenium.check(locator);
    }

    public void chooseCancelOnNextConfirmation()
    {
        selenium.chooseCancelOnNextConfirmation();
    }

    public void chooseOkOnNextConfirmation()
    {
        selenium.chooseOkOnNextConfirmation();
    }

    public void click(String locator)
    {
        selenium.click(locator);
    }

    public void clickAt(String locator, String coordString)
    {
        selenium.clickAt(locator, coordString);
    }

    public void close()
    {
        selenium.close();
    }

    public void contextMenu(String locator)
    {
        selenium.contextMenu(locator);
    }

    public void contextMenuAt(String locator, String coordString)
    {
        selenium.contextMenuAt(locator, coordString);
    }

    public void controlKeyDown()
    {
        selenium.controlKeyDown();
    }

    public void controlKeyUp()
    {
        selenium.controlKeyUp();
    }

    public void createCookie(String nameValuePair, String optionsString)
    {
        selenium.createCookie(nameValuePair, optionsString);
    }

    public void deleteAllVisibleCookies()
    {
        selenium.deleteAllVisibleCookies();
    }

    public void deleteCookie(String name, String optionsString)
    {
        selenium.deleteCookie(name, optionsString);
    }

    public void deselectPopUp()
    {
        selenium.deselectPopUp();
    }

    public void doubleClick(String locator)
    {
        selenium.doubleClick(locator);
    }

    public void doubleClickAt(String locator, String coordString)
    {
        selenium.doubleClickAt(locator, coordString);
    }

    public void dragAndDrop(String locator, String movementsString)
    {
        selenium.dragAndDrop(locator, movementsString);
    }

    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject)
    {
        selenium.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
    }

    public void dragdrop(String locator, String movementsString)
    {
        selenium.dragdrop(locator, movementsString);
    }

    public void fireEvent(String locator, String eventName)
    {
        selenium.fireEvent(locator, eventName);
    }

    public void focus(String locator)
    {
        selenium.focus(locator);
    }

    public String getAlert()
    {
        return selenium.getAlert();
    }

    public String[] getAllButtons()
    {
        return selenium.getAllButtons();
    }

    public String[] getAllFields()
    {
        return selenium.getAllFields();
    }

    public String[] getAllLinks()
    {
        return selenium.getAllLinks();
    }

    public String[] getAllWindowIds()
    {
        return selenium.getAllWindowIds();
    }

    public String[] getAllWindowNames()
    {
        return selenium.getAllWindowNames();
    }

    public String[] getAllWindowTitles()
    {
        return selenium.getAllWindowTitles();
    }

    public String getAttribute(String attributeLocator)
    {
        return selenium.getAttribute(attributeLocator);
    }

    public String[] getAttributeFromAllWindows(String attributeName)
    {
        return selenium.getAttributeFromAllWindows(attributeName);
    }

    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    public String getConfirmation()
    {
        return selenium.getConfirmation();
    }

    public String getCookie()
    {
        return selenium.getCookie();
    }

    public String getCookieByName(String name)
    {
        return selenium.getCookieByName(name);
    }

    public Number getCursorPosition(String locator)
    {
        return selenium.getCursorPosition(locator);
    }

    public Number getElementHeight(String locator)
    {
        return selenium.getElementHeight(locator);
    }

    public Number getElementIndex(String locator)
    {
        return selenium.getElementIndex(locator);
    }

    public Number getElementPositionLeft(String locator)
    {
        return selenium.getElementPositionLeft(locator);
    }

    public Number getElementPositionTop(String locator)
    {
        return selenium.getElementPositionTop(locator);
    }

    public Number getElementWidth(String locator)
    {
        return selenium.getElementWidth(locator);
    }

    public String getEval(String script)
    {
        return selenium.getEval(script);
    }

    public String getExpression(String expression)
    {
        return selenium.getExpression(expression);
    }

    public String getHtmlSource()
    {
        return selenium.getHtmlSource();
    }

    public String getLocation()
    {
        return selenium.getLocation();
    }

    public String getLog()
    {
        return selenium.getLog();
    }

    public Number getMouseSpeed()
    {
        return selenium.getMouseSpeed();
    }

    public String getPrompt()
    {
        return selenium.getPrompt();
    }

    public String getSelectedId(String selectLocator)
    {
        return selenium.getSelectedId(selectLocator);
    }

    public String[] getSelectedIds(String selectLocator)
    {
        return selenium.getSelectedIds(selectLocator);
    }

    public String getSelectedIndex(String selectLocator)
    {
        return selenium.getSelectedIndex(selectLocator);
    }

    public String[] getSelectedIndexes(String selectLocator)
    {
        return selenium.getSelectedIndexes(selectLocator);
    }

    public String getSelectedLabel(String selectLocator)
    {
        return selenium.getSelectedLabel(selectLocator);
    }

    public String[] getSelectedLabels(String selectLocator)
    {
        return selenium.getSelectedLabels(selectLocator);
    }

    public String getSelectedValue(String selectLocator)
    {
        return selenium.getSelectedValue(selectLocator);
    }

    public String[] getSelectedValues(String selectLocator)
    {
        return selenium.getSelectedValues(selectLocator);
    }

    public String[] getSelectOptions(String selectLocator)
    {
        return selenium.getSelectOptions(selectLocator);
    }

    public String getSpeed()
    {
        return selenium.getSpeed();
    }

    public String getTable(String tableCellAddress)
    {
        return selenium.getTable(tableCellAddress);
    }

    public String getText(String locator)
    {
        return selenium.getText(locator);
    }

    public String getTitle()
    {
        return selenium.getTitle();
    }

    public String getValue(String locator)
    {
        return selenium.getValue(locator);
    }

    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target)
    {
        return selenium.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
    }

    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target)
    {
        return selenium.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
    }

    public Number getXpathCount(String xpath)
    {
        return selenium.getXpathCount(xpath);
    }

    public void goBack()
    {
        selenium.goBack();
    }

    public void highlight(String locator)
    {
        selenium.highlight(locator);
    }

    public void ignoreAttributesWithoutValue(String ignore)
    {
        selenium.ignoreAttributesWithoutValue(ignore);
    }

    public boolean isAlertPresent()
    {
        return selenium.isAlertPresent();
    }

    public boolean isChecked(String locator)
    {
        return selenium.isChecked(locator);
    }

    public boolean isConfirmationPresent()
    {
        return selenium.isConfirmationPresent();
    }

    public boolean isCookiePresent(String name)
    {
        return selenium.isCookiePresent(name);
    }

    public boolean isEditable(String locator)
    {
        return selenium.isEditable(locator);
    }

    public boolean isElementPresent(String locator)
    {
        return selenium.isElementPresent(locator);
    }

    public boolean isOrdered(String locator1, String locator2)
    {
        return selenium.isOrdered(locator1, locator2);
    }

    public boolean isPromptPresent()
    {
        return selenium.isPromptPresent();
    }

    public boolean isSomethingSelected(String selectLocator)
    {
        return selenium.isSomethingSelected(selectLocator);
    }

    public boolean isTextPresent(String pattern)
    {
        return selenium.isTextPresent(pattern);
    }

    public boolean isVisible(String locator)
    {
        return selenium.isVisible(locator);
    }

    public void keyDown(String locator, String keySequence)
    {
        selenium.keyDown(locator, keySequence);
    }

    public void keyDownNative(String keycode)
    {
        selenium.keyDownNative(keycode);
    }

    public void keyPress(String locator, String keySequence)
    {
        selenium.keyPress(locator, keySequence);
    }

    public void keyPressNative(String keycode)
    {
        selenium.keyPressNative(keycode);
    }

    public void keyUp(String locator, String keySequence)
    {
        selenium.keyUp(locator, keySequence);
    }

    public void keyUpNative(String keycode)
    {
        selenium.keyUpNative(keycode);
    }

    public void metaKeyDown()
    {
        selenium.metaKeyDown();
    }

    public void metaKeyUp()
    {
        selenium.metaKeyUp();
    }

    public void mouseDown(String locator)
    {
        selenium.mouseDown(locator);
    }

    public void mouseDownAt(String locator, String coordString)
    {
        selenium.mouseDownAt(locator, coordString);
    }

    public void mouseDownRight(String locator)
    {
        selenium.mouseDownRight(locator);
    }

    public void mouseDownRightAt(String locator, String coordString)
    {
        selenium.mouseDownRightAt(locator, coordString);
    }

    public void mouseMove(String locator)
    {
        selenium.mouseMove(locator);
    }

    public void mouseMoveAt(String locator, String coordString)
    {
        selenium.mouseMoveAt(locator, coordString);
    }

    public void mouseOut(String locator)
    {
        selenium.mouseOut(locator);
    }

    public void mouseOver(String locator)
    {
        selenium.mouseOver(locator);
    }

    public void mouseUp(String locator)
    {
        selenium.mouseUp(locator);
    }

    public void mouseUpAt(String locator, String coordString)
    {
        selenium.mouseUpAt(locator, coordString);
    }

    public void mouseUpRight(String locator)
    {
        selenium.mouseUpRight(locator);
    }

    public void mouseUpRightAt(String locator, String coordString)
    {
        selenium.mouseUpRightAt(locator, coordString);
    }

    public void open(String url)
    {
        selenium.open(url);
    }

    public void open(String url, String ignoreResponseCode)
    {
        selenium.open(url, ignoreResponseCode);
    }

    public void openWindow(String url, String windowID)
    {
        selenium.openWindow(url, windowID);
    }

    public void refresh()
    {
        selenium.refresh();
    }

    public void removeAllSelections(String locator)
    {
        selenium.removeAllSelections(locator);
    }

    public void removeScript(String scriptTagId)
    {
        selenium.removeScript(scriptTagId);
    }

    public void removeSelection(String locator, String optionLocator)
    {
        selenium.removeSelection(locator, optionLocator);
    }

    public String retrieveLastRemoteControlLogs()
    {
        return selenium.retrieveLastRemoteControlLogs();
    }

    public void rollup(String rollupName, String kwargs)
    {
        selenium.rollup(rollupName, kwargs);
    }

    public void runScript(String script)
    {
        selenium.runScript(script);
    }

    public void select(String selectLocator, String optionLocator)
    {
        selenium.select(selectLocator, optionLocator);
    }

    public void selectFrame(String locator)
    {
        selenium.selectFrame(locator);
    }

    public void selectPopUp(String windowID)
    {
        selenium.selectPopUp(windowID);
    }

    public void selectWindow(String windowID)
    {
        selenium.selectWindow(windowID);
    }

    public void setBrowserLogLevel(String logLevel)
    {
        selenium.setBrowserLogLevel(logLevel);
    }

    public void setContext(String context)
    {
        selenium.setContext(context);
    }

    public void setCursorPosition(String locator, String position)
    {
        selenium.setCursorPosition(locator, position);
    }

    public void setExtensionJs(String extensionJs)
    {
        selenium.setExtensionJs(extensionJs);
    }

    public void setMouseSpeed(String pixels)
    {
        selenium.setMouseSpeed(pixels);
    }

    public void setSpeed(String value)
    {
        selenium.setSpeed(value);
    }

    public void setTimeout(String timeout)
    {
        selenium.setTimeout(timeout);
    }

    public void shiftKeyDown()
    {
        selenium.shiftKeyDown();
    }

    public void shiftKeyUp()
    {
        selenium.shiftKeyUp();
    }

    public void showContextualBanner()
    {
        selenium.showContextualBanner();
    }

    public void showContextualBanner(String className, String methodName)
    {
        selenium.showContextualBanner(className, methodName);
    }

    public void shutDownSeleniumServer()
    {
        selenium.shutDownSeleniumServer();
    }

    public void start()
    {
        selenium.start();
    }

    public void start(Object optionsObject)
    {
        selenium.start(optionsObject);
    }

    public void start(String optionsString)
    {
        selenium.start(optionsString);
    }

    public void stop()
    {
        selenium.stop();
    }

    public void submit(String formLocator)
    {
        selenium.submit(formLocator);
    }

    public void type(String locator, String value)
    {
        selenium.type(locator, value);
    }

    public void typeKeys(String locator, String value)
    {
        selenium.typeKeys(locator, value);
    }

    public void uncheck(String locator)
    {
        selenium.uncheck(locator);
    }

    public void useXpathLibrary(String libraryName)
    {
        selenium.useXpathLibrary(libraryName);
    }

    public void waitForCondition(String script, String timeout)
    {
        selenium.waitForCondition(script, timeout);
    }

    public void waitForFrameToLoad(String frameAddress, String timeout)
    {
        selenium.waitForFrameToLoad(frameAddress, timeout);
    }

    public void waitForPageToLoad(String timeout)
    {
        selenium.waitForPageToLoad(timeout);
    }

    public void waitForPopUp(String windowID, String timeout)
    {
        selenium.waitForPopUp(windowID, timeout);
    }

    public void windowFocus()
    {
        selenium.windowFocus();
    }

    public void windowMaximize()
    {
        selenium.windowMaximize();
    }

    // ---------------------------------------------------------------------
    // End of delegate methods
    // ---------------------------------------------------------------------

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
     * Waits, up to the page load limit for an element (identified by a CSS rule) to exist
     * (it is not assured that the element will be visible).
     *
     * @param cssRule
     *         used to locate the element
     * @since 5.3
     */
    protected void waitForCSSSelectedElementToAppear(String cssRule)
    {
        String condition = String.format("window.$$ && window.$$(\"%s\").size() > 0", cssRule);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
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

        String condition = String.format("window.$(\"%s\")", elementId);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
    }

    /**
     * Waits for the element to be removed from the DOM.
     *
     * @param elementId
     *         client-side id of element
     * @since 5.3
     */
    protected final void waitForElementToDisappear(String elementId)
    {
        String condition = String.format("window.$(\"%s\").hide()", elementId);

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
        String condition = String.format("selenium.isVisible(\"%s\")", selector);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
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
        String condition = String.format("!selenium.isVisible(\"%s\")", selector);

        waitForCondition(condition, PAGE_LOAD_TIMEOUT);
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
     * Waits until all active XHR requests are completed. However, this is Prototype-based.
     *
     * @param timeout
     *         timeout to wait for
     * @since 5.3
     * @deprecated Deprecated in 5.4 as it is tied to Prototype.
     */
    protected final void waitForAjaxRequestsToComplete(String timeout)
    {
        waitForCondition("selenium.browserbot.getCurrentWindow().Ajax.activeRequestCount == 0", timeout);
    }

    public Number getCssCount(String str)
    {
        return selenium.getCssCount(str);
    }

    /**
     * Waits for page initialization to finish, which is recognized by the {@code data-page-initialized} attribute
     * being added to the HTML element. Polls at 20ms intervals for 200ms.
     *
     * @since 5.4
     */
    protected final void waitForPageInitialized()
    {
        for (int i = 0; i < 10; i++)
        {
            if (isElementPresent("css=html[data-page-initialized]"))
            {
                return;
            }

            sleep(20);
        }

        reportAndThrowAssertionError("Page did not finish initializing.");
    }
}
