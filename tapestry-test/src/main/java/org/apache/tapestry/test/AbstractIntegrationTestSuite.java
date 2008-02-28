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

package org.apache.tapestry.test;

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
 * @see org.apache.tapestry.test.JettyRunner
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

    private final String _webappRoot;

    private final String _seleniumBrowserCommand;

    private JettyRunner _jettyRunner;

    private Selenium _selenium;

    private SeleniumServer _server;

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
        _webappRoot = webAppRoot;
        _seleniumBrowserCommand = browserCommand;
    }

    protected final void assertSourcePresent(String... expected)
    {
        String source = _selenium.getHtmlSource();

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
                              _selenium.getHtmlSource());

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
                              _selenium.getHtmlSource());

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

            System.err.printf("Text pattern '%s' not found in:\n%s\n\n", item, _selenium
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
        _selenium.stop();
        _selenium = null;

        _server.stop();
        _server = null;

        _jettyRunner.stop();
        _jettyRunner = null;
    }

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        _jettyRunner = new JettyRunner(TapestryTestConstants.MODULE_BASE_DIR, "/", JETTY_PORT, _webappRoot);

        _server = new SeleniumServer();

        _server.start();

        CommandProcessor cp = new HttpCommandProcessor("localhost", SeleniumServer.DEFAULT_PORT,
                                                       _seleniumBrowserCommand, BASE_URL);

        _selenium = new DefaultSelenium(new ErrorReportingCommandProcessor(cp));

        _selenium.start();
    }

    public void addSelection(String locator, String optionLocator)
    {
        _selenium.addSelection(locator, optionLocator);
    }

    public void answerOnNextPrompt(String answer)
    {
        _selenium.answerOnNextPrompt(answer);
    }

    public void check(String locator)
    {
        _selenium.check(locator);
    }

    public void chooseCancelOnNextConfirmation()
    {
        _selenium.chooseCancelOnNextConfirmation();
    }

    public void chooseOkOnNextConfirmation()
    {
        _selenium.chooseOkOnNextConfirmation();
    }

    public void click(String locator)
    {
        _selenium.click(locator);
    }

    public void doubleClick(String locator)
    {
        _selenium.doubleClick(locator);
    }

    public void clickAt(String locator, String coordString)
    {
        _selenium.clickAt(locator, coordString);
    }

    public void doubleClickAt(String locator, String coordString)
    {
        _selenium.doubleClickAt(locator, coordString);
    }

    public void close()
    {
        _selenium.close();
    }

    public void fireEvent(String locator, String eventName)
    {
        _selenium.fireEvent(locator, eventName);
    }

    public String getAlert()
    {
        return _selenium.getAlert();
    }

    public String[] getAllButtons()
    {
        return _selenium.getAllButtons();
    }

    public String[] getAllFields()
    {
        return _selenium.getAllFields();
    }

    public String[] getAttributeFromAllWindows(String attributeName)
    {
        return _selenium.getAttributeFromAllWindows(attributeName);
    }

    public void dragdrop(String locator, String movementsString)
    {
        _selenium.dragdrop(locator, movementsString);
    }

    public void setMouseSpeed(String pixels)
    {
        _selenium.setMouseSpeed(pixels);
    }

    public Number getMouseSpeed()
    {
        return _selenium.getMouseSpeed();
    }

    public void dragAndDrop(String locator, String movementsString)
    {
        _selenium.dragAndDrop(locator, movementsString);
    }

    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject)
    {
        _selenium.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
    }

    public void windowFocus()
    {
        _selenium.windowFocus();
    }

    public void windowMaximize()
    {
        _selenium.windowMaximize();
    }

    public String[] getAllWindowIds()
    {
        return _selenium.getAllWindowIds();
    }

    public String[] getAllWindowNames()
    {
        return _selenium.getAllWindowNames();
    }

    public String[] getAllWindowTitles()
    {
        return _selenium.getAllWindowTitles();
    }

    public String[] getAllLinks()
    {
        return _selenium.getAllLinks();
    }

    public String getAttribute(String attributeLocator)
    {
        return _selenium.getAttribute(attributeLocator);
    }

    public String getBodyText()
    {
        return _selenium.getBodyText();
    }

    public String getConfirmation()
    {
        return _selenium.getConfirmation();
    }

    public Number getCursorPosition(String locator)
    {
        return _selenium.getCursorPosition(locator);
    }

    public String getEval(String script)
    {
        return _selenium.getEval(script);
    }

    public String getExpression(String expression)
    {
        return _selenium.getExpression(expression);
    }

    public Number getXpathCount(String xpath)
    {
        return _selenium.getXpathCount(xpath);
    }

    public void assignId(String locator, String identifier)
    {
        _selenium.assignId(locator, identifier);
    }

    public void allowNativeXpath(String allow)
    {
        _selenium.allowNativeXpath(allow);
    }

    public String getHtmlSource()
    {
        return _selenium.getHtmlSource();
    }

    public String getLocation()
    {
        return _selenium.getLocation();
    }

    public String getPrompt()
    {
        return _selenium.getPrompt();
    }

    public String getSelectedId(String selectLocator)
    {
        return _selenium.getSelectedId(selectLocator);
    }

    public String[] getSelectedIds(String selectLocator)
    {
        return _selenium.getSelectedIds(selectLocator);
    }

    public String getSelectedIndex(String selectLocator)
    {
        return _selenium.getSelectedIndex(selectLocator);
    }

    public String[] getSelectedIndexes(String selectLocator)
    {
        return _selenium.getSelectedIndexes(selectLocator);
    }

    public String getSelectedLabel(String selectLocator)
    {
        return _selenium.getSelectedLabel(selectLocator);
    }

    public String[] getSelectedLabels(String selectLocator)
    {
        return _selenium.getSelectedLabels(selectLocator);
    }

    public String getSelectedValue(String selectLocator)
    {
        return _selenium.getSelectedValue(selectLocator);
    }

    public String[] getSelectedValues(String selectLocator)
    {
        return _selenium.getSelectedValues(selectLocator);
    }

    public String[] getSelectOptions(String selectLocator)
    {
        return _selenium.getSelectOptions(selectLocator);
    }

    public String getTable(String tableCellAddress)
    {
        return _selenium.getTable(tableCellAddress);
    }

    public String getText(String locator)
    {
        return _selenium.getText(locator);
    }

    public void highlight(String locator)
    {
        _selenium.highlight(locator);
    }

    public String getTitle()
    {
        return _selenium.getTitle();
    }

    public String getValue(String locator)
    {
        return _selenium.getValue(locator);
    }

    public void goBack()
    {
        _selenium.goBack();
    }

    public boolean isAlertPresent()
    {
        return _selenium.isAlertPresent();
    }

    public boolean isChecked(String locator)
    {
        return _selenium.isChecked(locator);
    }

    public boolean isConfirmationPresent()
    {
        return _selenium.isConfirmationPresent();
    }

    public boolean isEditable(String locator)
    {
        return _selenium.isEditable(locator);
    }

    public boolean isElementPresent(String locator)
    {
        return _selenium.isElementPresent(locator);
    }

    public boolean isPromptPresent()
    {
        return _selenium.isPromptPresent();
    }

    public boolean isSomethingSelected(String selectLocator)
    {
        return _selenium.isSomethingSelected(selectLocator);
    }

    public boolean isTextPresent(String pattern)
    {
        return _selenium.isTextPresent(pattern);
    }

    public boolean isVisible(String locator)
    {
        return _selenium.isVisible(locator);
    }

    public void keyDown(String locator, String keycode)
    {
        _selenium.keyDown(locator, keycode);
    }

    public void keyPress(String locator, String keycode)
    {
        _selenium.keyPress(locator, keycode);
    }

    public void shiftKeyDown()
    {
        _selenium.shiftKeyDown();
    }

    public void shiftKeyUp()
    {
        _selenium.shiftKeyUp();
    }

    public void metaKeyDown()
    {
        _selenium.metaKeyDown();
    }

    public void metaKeyUp()
    {
        _selenium.metaKeyUp();
    }

    public void altKeyDown()
    {
        _selenium.altKeyDown();
    }

    public void altKeyUp()
    {
        _selenium.altKeyUp();
    }

    public void controlKeyDown()
    {
        _selenium.controlKeyDown();
    }

    public void controlKeyUp()
    {
        _selenium.controlKeyUp();
    }

    public void keyUp(String locator, String keycode)
    {
        _selenium.keyUp(locator, keycode);
    }

    public void mouseDown(String locator)
    {
        _selenium.mouseDown(locator);
    }

    public void mouseDownAt(String locator, String coordString)
    {
        _selenium.mouseDownAt(locator, coordString);
    }

    public void mouseUp(String locator)
    {
        _selenium.mouseUp(locator);
    }

    public void mouseUpAt(String locator, String coordString)
    {
        _selenium.mouseUpAt(locator, coordString);
    }

    public void mouseMove(String locator)
    {
        _selenium.mouseMove(locator);
    }

    public void mouseMoveAt(String locator, String coordString)
    {
        _selenium.mouseMoveAt(locator, coordString);
    }

    public void mouseOver(String locator)
    {
        _selenium.mouseOver(locator);
    }

    public void mouseOut(String locator)
    {
        _selenium.mouseOut(locator);
    }

    public void open(String url)
    {
        _selenium.open(url);

        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    public void openWindow(String url, String windowID)
    {
        _selenium.openWindow(url, windowID);
    }

    public void refresh()
    {
        _selenium.refresh();
    }

    public void removeSelection(String locator, String optionLocator)
    {
        _selenium.removeSelection(locator, optionLocator);
    }

    public void removeAllSelections(String locator)
    {
        _selenium.removeAllSelections(locator);
    }

    public void select(String selectLocator, String optionLocator)
    {
        _selenium.select(selectLocator, optionLocator);
    }

    public void selectWindow(String windowID)
    {
        _selenium.selectWindow(windowID);
    }

    public void selectFrame(String locator)
    {
        _selenium.selectFrame(locator);
    }

    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target)
    {
        return _selenium.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
    }

    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target)
    {
        return _selenium.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
    }

    public void setCursorPosition(String locator, String position)
    {
        _selenium.setCursorPosition(locator, position);
    }

    public Number getElementIndex(String locator)
    {
        return _selenium.getElementIndex(locator);
    }

    public boolean isOrdered(String locator1, String locator2)
    {
        return _selenium.isOrdered(locator1, locator2);
    }

    public Number getElementPositionLeft(String locator)
    {
        return _selenium.getElementPositionLeft(locator);
    }

    public Number getElementPositionTop(String locator)
    {
        return _selenium.getElementPositionTop(locator);
    }

    public Number getElementWidth(String locator)
    {
        return _selenium.getElementWidth(locator);
    }

    public Number getElementHeight(String locator)
    {
        return _selenium.getElementHeight(locator);
    }

    public void setTimeout(String timeout)
    {
        _selenium.setTimeout(timeout);
    }

    public void start()
    {
        _selenium.start();
    }

    public void stop()
    {
        _selenium.stop();
    }

    public void submit(String formLocator)
    {
        _selenium.submit(formLocator);
    }

    public void type(String locator, String value)
    {
        _selenium.type(locator, value);
    }

    public void typeKeys(String locator, String value)
    {
        _selenium.typeKeys(locator, value);
    }

    public void setSpeed(String value)
    {
        _selenium.setSpeed(value);
    }

    public void getSpeed()
    {
        _selenium.getSpeed();
    }

    public void uncheck(String locator)
    {
        _selenium.uncheck(locator);
    }

    public void waitForCondition(String script, String timeout)
    {
        _selenium.waitForCondition(script, timeout);
    }

    public void waitForPageToLoad(String timeout)
    {
        _selenium.waitForPageToLoad(timeout);
    }

    public void waitForFrameToLoad(String frameAddress, String timeout)
    {
        _selenium.waitForFrameToLoad(frameAddress, timeout);
    }

    public String getCookie()
    {
        return _selenium.getCookie();
    }

    public void createCookie(String nameValuePair, String optionsString)
    {
        _selenium.createCookie(nameValuePair, optionsString);
    }

    public void deleteCookie(String name, String path)
    {
        _selenium.deleteCookie(name, path);
    }

    public void setBrowserLogLevel(String logLevel)
    {
        _selenium.setBrowserLogLevel(logLevel);
    }

    public void runScript(String script)
    {
        _selenium.runScript(script);
    }

    public void addLocationStrategy(String strategyName, String functionDefinition)
    {
        _selenium.addLocationStrategy(strategyName, functionDefinition);
    }

    public void setContext(String context)
    {
        _selenium.setContext(context);
    }

    public void captureScreenshot(String filename)
    {
        _selenium.captureScreenshot(filename);
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
        _selenium.waitForPopUp(windowID, timeout);
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
