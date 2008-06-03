// Copyright 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.test;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * A base class for creating integration tests. Ths encapsulates starting up an in-process copy of Jetty, and in-process
 * copy of {@link SeleniumServer}, and a Selenium client.
 * <p/>
 * Unless you are <em>very, very clever</em>, you will want to run the tests sequentially. TestNG tends to run them in
 * an arbitrary order unless you explicitly set the order. If you have managed to get TestNG to run tests in parallel,
 * you may see further problems caused by a single client jumping all over your web application in an unpredictable
 * order.
 * <p/>
 * This class implements the {@link Selenium} interface, and delegates all those methods to the {@link DefaultSelenium}
 * instance it creates. It also extends the normal exception reporting for any failed command or query to produce a more
 * detailed report to the main console.
 *
 * @see org.apache.tapestry5.test.JettyRunner
 */
public class AbstractIntegrationTestSuite extends Assert implements Selenium
{
    /**
     * Default directory containing the web application to be tested (this conforms to Maven's default folder).
     */
    public static final String DEFAULT_WEB_APP_ROOT = "src/main/webapp";

    /**
     * Default browser in which to run tests - firefox
     */
    public static final String DEFAULT_WEB_BROWSER_COMMAND = "*firefox";

    /**
     * 15 seconds
     */
    public static final String PAGE_LOAD_TIMEOUT = "15000";

    /**
     * The port on which the internal copy of Jetty is executed.
     */
    public static final int JETTY_PORT = 9999;

    // This is likely to be a problem, since may want to test with a context path, rather than as
    // root.
    public static final String BASE_URL = String.format("http://localhost:%d/", JETTY_PORT);

    public static final String SUBMIT = "//input[@type='submit']";

    private final String webappRoot;

    private final String seleniumBrowserCommand;

    private JettyRunner jettyRunner;

    private Selenium selenium;

    private SeleniumServer server;

    /**
     * Initializes the suite using {@link #DEFAULT_WEB_APP_ROOT}.
     */
    public AbstractIntegrationTestSuite()
    {
        this(DEFAULT_WEB_APP_ROOT, DEFAULT_WEB_BROWSER_COMMAND);
    }

    /**
     * @param webAppRoot the directory containing the web application to be tested.
     */
    protected AbstractIntegrationTestSuite(String webAppRoot)
    {
        this(webAppRoot, DEFAULT_WEB_BROWSER_COMMAND);
    }

    /**
     * @param webAppRoot     web application root (default src/main/webapp)
     * @param browserCommand browser command to pass to selenium. Default is *firefox, syntax for custom browsers is
     *                       *custom &lt;path_to_browser&gt;, e.g. *custom /usr/lib/mozilla-firefox/firefox
     */
    protected AbstractIntegrationTestSuite(String webAppRoot, String browserCommand)
    {
        webappRoot = webAppRoot;
        seleniumBrowserCommand = browserCommand;
    }

    protected final void assertSourcePresent(String... expected)
    {
        String source = selenium.getHtmlSource();

        for (String snippet : expected)
        {
            if (source.contains(snippet)) continue;

            System.err.printf("Source content '%s' not found in:\n%s\n\n", snippet, source);

            throw new AssertionError("Page did not contain source '" + snippet + "'.");
        }
    }

    /**
     * Used when the locator identifies an attribute, not an element.
     *
     * @param locator  identifies the attribute whose value is to be asserted
     * @param expected expected value for the attribute
     */
    protected final void assertAttribute(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = getAttribute(locator);
        }
        catch (RuntimeException ex)
        {
            System.err.printf("Error accessing %s: %s, in:\n\n%s\n\n", locator, ex.getMessage(),
                              selenium.getHtmlSource());

            throw ex;
        }

        if (actual.equals(expected)) return;

        System.err.printf("Text for attribute %s should be '%s' but is '%s', in:\n\n%s\n\n", locator, expected, actual,
                          getHtmlSource());

        throw new AssertionError(String.format("%s was '%s' not '%s'", locator, actual, expected));
    }

    /**
     * Asserts the text of an element, identified by the locator.
     *
     * @param locator  identifies the element whose text value is to be asserted
     * @param expected expected value for the element's text
     */
    protected final void assertText(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = getText(locator);
        }
        catch (RuntimeException ex)
        {
            System.err.printf("Error accessing %s: %s, in:\n\n%s\n\n", locator, ex.getMessage(),
                              selenium.getHtmlSource());

            throw ex;
        }

        if (actual.equals(expected)) return;

        System.err.printf("Text for %s should be '%s' but is '%s', in:\n\n%s\n\n", locator, expected, actual,
                          getHtmlSource());

        throw new AssertionError(String.format("%s was '%s' not '%s'", locator, actual, expected));
    }

    protected final void assertTextPresent(String... text)
    {
        for (String item : text)
        {
            if (isTextPresent(item)) return;

            System.err.printf("Text pattern '%s' not found in:\n%s\n\n", item, selenium
                    .getHtmlSource());

            throw new AssertionError("Page did not contain '" + item + "'.");
        }
    }

    protected final void assertFieldValue(String locator, String expected)
    {
        assertEquals(getValue(locator), expected);
    }

    protected final void clickAndWait(String link)
    {
        click(link);
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    protected final void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    protected final void assertAttributeSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertAttribute(id, values[i]);
        }
    }


    protected final void assertFieldValueSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertFieldValue(id, values[i]);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception
    {
        selenium.stop();
        selenium = null;

        server.stop();
        server = null;

        jettyRunner.stop();
        jettyRunner = null;
    }

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        jettyRunner = new JettyRunner(TapestryTestConstants.MODULE_BASE_DIR, "/", JETTY_PORT, webappRoot);

        server = new SeleniumServer();

        server.start();

        CommandProcessor cp = new HttpCommandProcessor("localhost", SeleniumServer.DEFAULT_PORT,
                                                       seleniumBrowserCommand, BASE_URL);

        selenium = new DefaultSelenium(new ErrorReportingCommandProcessor(cp));

        selenium.start();
    }

    public void addSelection(String locator, String optionLocator)
    {
        selenium.addSelection(locator, optionLocator);
    }

    public void answerOnNextPrompt(String answer)
    {
        selenium.answerOnNextPrompt(answer);
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

    public void doubleClick(String locator)
    {
        selenium.doubleClick(locator);
    }

    public void clickAt(String locator, String coordString)
    {
        selenium.clickAt(locator, coordString);
    }

    public void doubleClickAt(String locator, String coordString)
    {
        selenium.doubleClickAt(locator, coordString);
    }

    public void close()
    {
        selenium.close();
    }

    public void fireEvent(String locator, String eventName)
    {
        selenium.fireEvent(locator, eventName);
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

    public String[] getAttributeFromAllWindows(String attributeName)
    {
        return selenium.getAttributeFromAllWindows(attributeName);
    }

    public void dragdrop(String locator, String movementsString)
    {
        selenium.dragdrop(locator, movementsString);
    }

    public void setMouseSpeed(String pixels)
    {
        selenium.setMouseSpeed(pixels);
    }

    public Number getMouseSpeed()
    {
        return selenium.getMouseSpeed();
    }

    public void dragAndDrop(String locator, String movementsString)
    {
        selenium.dragAndDrop(locator, movementsString);
    }

    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject)
    {
        selenium.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
    }

    public void windowFocus()
    {
        selenium.windowFocus();
    }

    public void windowMaximize()
    {
        selenium.windowMaximize();
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

    public String[] getAllLinks()
    {
        return selenium.getAllLinks();
    }

    public String getAttribute(String attributeLocator)
    {
        return selenium.getAttribute(attributeLocator);
    }

    public String getBodyText()
    {
        return selenium.getBodyText();
    }

    public String getConfirmation()
    {
        return selenium.getConfirmation();
    }

    public Number getCursorPosition(String locator)
    {
        return selenium.getCursorPosition(locator);
    }

    public String getEval(String script)
    {
        return selenium.getEval(script);
    }

    public String getExpression(String expression)
    {
        return selenium.getExpression(expression);
    }

    public Number getXpathCount(String xpath)
    {
        return selenium.getXpathCount(xpath);
    }

    public void assignId(String locator, String identifier)
    {
        selenium.assignId(locator, identifier);
    }

    public void allowNativeXpath(String allow)
    {
        selenium.allowNativeXpath(allow);
    }

    public String getHtmlSource()
    {
        return selenium.getHtmlSource();
    }

    public String getLocation()
    {
        return selenium.getLocation();
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

    public String getTable(String tableCellAddress)
    {
        return selenium.getTable(tableCellAddress);
    }

    public String getText(String locator)
    {
        return selenium.getText(locator);
    }

    public void highlight(String locator)
    {
        selenium.highlight(locator);
    }

    public String getTitle()
    {
        return selenium.getTitle();
    }

    public String getValue(String locator)
    {
        return selenium.getValue(locator);
    }

    public void goBack()
    {
        selenium.goBack();
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

    public boolean isEditable(String locator)
    {
        return selenium.isEditable(locator);
    }

    public boolean isElementPresent(String locator)
    {
        return selenium.isElementPresent(locator);
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

    public void keyDown(String locator, String keycode)
    {
        selenium.keyDown(locator, keycode);
    }

    public void keyPress(String locator, String keycode)
    {
        selenium.keyPress(locator, keycode);
    }

    public void shiftKeyDown()
    {
        selenium.shiftKeyDown();
    }

    public void shiftKeyUp()
    {
        selenium.shiftKeyUp();
    }

    public void metaKeyDown()
    {
        selenium.metaKeyDown();
    }

    public void metaKeyUp()
    {
        selenium.metaKeyUp();
    }

    public void altKeyDown()
    {
        selenium.altKeyDown();
    }

    public void altKeyUp()
    {
        selenium.altKeyUp();
    }

    public void controlKeyDown()
    {
        selenium.controlKeyDown();
    }

    public void controlKeyUp()
    {
        selenium.controlKeyUp();
    }

    public void keyUp(String locator, String keycode)
    {
        selenium.keyUp(locator, keycode);
    }

    public void mouseDown(String locator)
    {
        selenium.mouseDown(locator);
    }

    public void mouseDownAt(String locator, String coordString)
    {
        selenium.mouseDownAt(locator, coordString);
    }

    public void mouseUp(String locator)
    {
        selenium.mouseUp(locator);
    }

    public void mouseUpAt(String locator, String coordString)
    {
        selenium.mouseUpAt(locator, coordString);
    }

    public void mouseMove(String locator)
    {
        selenium.mouseMove(locator);
    }

    public void mouseMoveAt(String locator, String coordString)
    {
        selenium.mouseMoveAt(locator, coordString);
    }

    public void mouseOver(String locator)
    {
        selenium.mouseOver(locator);
    }

    public void mouseOut(String locator)
    {
        selenium.mouseOut(locator);
    }

    public void open(String url)
    {
        selenium.open(url);

        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    public void openWindow(String url, String windowID)
    {
        selenium.openWindow(url, windowID);
    }

    public void refresh()
    {
        selenium.refresh();
    }

    public void removeSelection(String locator, String optionLocator)
    {
        selenium.removeSelection(locator, optionLocator);
    }

    public void removeAllSelections(String locator)
    {
        selenium.removeAllSelections(locator);
    }

    public void select(String selectLocator, String optionLocator)
    {
        selenium.select(selectLocator, optionLocator);
    }

    public void selectWindow(String windowID)
    {
        selenium.selectWindow(windowID);
    }

    public void selectFrame(String locator)
    {
        selenium.selectFrame(locator);
    }

    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target)
    {
        return selenium.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
    }

    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target)
    {
        return selenium.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
    }

    public void setCursorPosition(String locator, String position)
    {
        selenium.setCursorPosition(locator, position);
    }

    public Number getElementIndex(String locator)
    {
        return selenium.getElementIndex(locator);
    }

    public boolean isOrdered(String locator1, String locator2)
    {
        return selenium.isOrdered(locator1, locator2);
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

    public Number getElementHeight(String locator)
    {
        return selenium.getElementHeight(locator);
    }

    public void setTimeout(String timeout)
    {
        selenium.setTimeout(timeout);
    }

    public void start()
    {
        selenium.start();
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

    public void setSpeed(String value)
    {
        selenium.setSpeed(value);
    }

    public void getSpeed()
    {
        selenium.getSpeed();
    }

    public void uncheck(String locator)
    {
        selenium.uncheck(locator);
    }

    public void waitForCondition(String script, String timeout)
    {
        selenium.waitForCondition(script, timeout);
    }

    public void waitForPageToLoad(String timeout)
    {
        selenium.waitForPageToLoad(timeout);
    }

    public void waitForFrameToLoad(String frameAddress, String timeout)
    {
        selenium.waitForFrameToLoad(frameAddress, timeout);
    }

    public String getCookie()
    {
        return selenium.getCookie();
    }

    public void createCookie(String nameValuePair, String optionsString)
    {
        selenium.createCookie(nameValuePair, optionsString);
    }

    public void deleteCookie(String name, String path)
    {
        selenium.deleteCookie(name, path);
    }

    public void setBrowserLogLevel(String logLevel)
    {
        selenium.setBrowserLogLevel(logLevel);
    }

    public void runScript(String script)
    {
        selenium.runScript(script);
    }

    public void addLocationStrategy(String strategyName, String functionDefinition)
    {
        selenium.addLocationStrategy(strategyName, functionDefinition);
    }

    public void setContext(String context)
    {
        selenium.setContext(context);
    }

    public void captureScreenshot(String filename)
    {
        selenium.captureScreenshot(filename);
    }


    /**
     * Waits the default time for the page to load.
     */
    public void waitForPageToLoad()
    {
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    public void waitForPopUp(String windowID, String timeout)
    {
        selenium.waitForPopUp(windowID, timeout);
    }

    /**
     * Used to start a typical test, by opening to the base URL and clicking through a series of links.
     */
    protected final void start(String... linkText)
    {
        open(BASE_URL);

        for (String s : linkText)
            clickAndWait(String.format("link=%s", s));
    }

}
