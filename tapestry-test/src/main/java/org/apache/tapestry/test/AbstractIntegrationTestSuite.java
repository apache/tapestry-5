// Copyright 2007 The Apache Software Foundation
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
 * A base class for creating integration tests. Ths encapsulates starting up an in-process copy of
 * Jetty, and in-process copy of {@link SeleniumServer}, and a Selenium client.
 * <p/>
 * Unless you are <em>very, very clever</em>, you will want to run the tests sequentially. TestNG
 * tends to run them in an arbitrary order unless you explicitly set the order. If you have managed
 * to get TestNG to run tests in parallel, you may see further problems caused by a single client
 * jumping all over your web application in an unpredictable order.
 * <p/>
 * This class implements the {@link Selenium} interface, and delegates all those methods to the
 * {@link DefaultSelenium} instance it creates. It also extends the normal exception reporting for
 * any failed command or query to produce a more detailed report to the main console.
 *
 * @see JettyRunner
 */
public abstract class AbstractIntegrationTestSuite extends Assert implements Selenium
{
    /**
     * Default directory containing the web application to be tested (this conforms to Maven's
     * default folder).
     */
    public static final String DEFAULT_WEB_APP_ROOT = "src/main/webapp";

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

    private JettyRunner _jettyRunner;

    private Selenium _selenium;

    private SeleniumServer _server;

    /**
     * Initializes the suite using {@link #DEFAULT_WEB_APP_ROOT}.
     */
    public AbstractIntegrationTestSuite()
    {
        this(DEFAULT_WEB_APP_ROOT);
    }

    /**
     * @param webAppRoot the directory containing the web application to be tested.
     */
    protected AbstractIntegrationTestSuite(String webAppRoot)
    {
        _webappRoot = webAppRoot;
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
        _jettyRunner = new JettyRunner("/", JETTY_PORT, _webappRoot);

        _server = new SeleniumServer();

        _server.start();

        CommandProcessor cp = new HttpCommandProcessor("localhost", SeleniumServer.DEFAULT_PORT, "*firefox", BASE_URL);

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

    public void click(String locator)
    {
        _selenium.click(locator);
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

    public void keyUp(String locator, String keycode)
    {
        _selenium.keyUp(locator, keycode);
    }

    public void mouseDown(String locator)
    {
        _selenium.mouseDown(locator);
    }

    public void mouseOver(String locator)
    {
        _selenium.mouseOver(locator);
    }

    public void open(String url)
    {
        _selenium.open(url);

        waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    public void refresh()
    {
        _selenium.refresh();
    }

    public void removeSelection(String locator, String optionLocator)
    {
        _selenium.removeSelection(locator, optionLocator);
    }

    public void select(String selectLocator, String optionLocator)
    {
        _selenium.select(selectLocator, optionLocator);
    }

    public void selectWindow(String windowID)
    {
        _selenium.selectWindow(windowID);
    }

    public void setContext(String context, String logLevelThreshold)
    {
        _selenium.setContext(context, logLevelThreshold);
    }

    public void setCursorPosition(String locator, String position)
    {
        _selenium.setCursorPosition(locator, position);
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
     * Used to start a typical test, by opening to the base URL and clicking through a series of
     * links.
     */
    protected final void start(String... linkText)
    {
        open(BASE_URL);

        for (String s : linkText)
            clickAndWait(String.format("link=%s", s));
    }

}
