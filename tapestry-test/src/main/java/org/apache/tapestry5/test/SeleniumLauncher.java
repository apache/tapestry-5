// Copyright 2009 The Apache Software Foundation
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

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.Selenium;

/**
 * Base class for launching the Selenium test stack, which consists of an
 * instance of {@link SeleniumServer}, and instance of {@link Selenium} RC (the
 * client), and a web server (by default, an instance of Jetty 7).
 * <p>
 * This class contains configuration methods to launch the servers (before <em>test</em>) and shut
 * them down (after <em>test</em>). In addition, the Selenium instance is stored as an attribute of
 * the {@link ITestContext} where it can be pulled out by {@link SeleniumTestCase}.
 * <p>
 * To create selenium tests, include SeleniumLauncher as part of your TestNG configuration and
 * (optionally) configure properties. Create a subclass of {@link SeleniumTestCase} to contain your
 * tests.
 * <p>
 * It is rarely necessary to subclass SeleniumLauncher, except if the default Jetty server instance
 * needs to be configured specially (or replaced with an alternate server such as Tomcat or Resin).
 * <p>
 * To use this as part of a set of tests, you must configure the class inside your testng.xml:
 * 
 * <pre>
 * &lt;test name="My Integration Tests"&gt;
 *   &lt;!-- parameters go here, if needed --&gt;
 *   &lt;packages&gt;
 *     &lt;!-- list of packages containing test suites goes here --&gt;
 *   &lt;/packages&gt;
 *   &lt;classes&gt;
 *     &lt;class name="org.apache.tapestry5.test.SeleniumLauncher"/&gt;
 *   &lt;/classes&gt;
 * &lt;/test&gt;
 * </pre>
 * 
 * @since 5.2.0
 */
public class SeleniumLauncher
{
    private SeleniumServer seleniumServer;

    private Selenium selenium;

    private Runnable stopWebServer;

    /**
     * Starts up the servers. By placing &lt;parameter&gt; elements inside the appropriate
     * &lt;test&gt; (of your testng.xml configuration
     * file), you can change the configuration or behavior of the servers. It is common to have two
     * or more identical tests that differ only in terms of the
     * <code>tapestry.browser-start-command</code> parameter, to run tests
     * against multiple browsers.
     * <table>
     * <tr>
     * <th>Parameter</th>
     * <th>Name</th>
     * <th>Default</th>
     * <th>Description</th>
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
     * <td>Context path (defaults to root). As elsewhere, the context path should be blank, or start
     * with a slash (but not end with one).</td>
     * </tr>
     * <tr>
     * <td>port</td>
     * <td>tapestry.port</td>
     * <td>9999</td>
     * <td>Port number for web server to listen to</td>
     * </tr>
     * <tr>
     * <td>browserStartCommand</td>
     * <td>tapestry.browser-start-command</td>
     * <td>*firefox</td>
     * <td>Command string used to launch the browser, as defined by Selenium</td>
     * </tr>
     * </table>
     * 
     * @param webAppFolder
     * @param contextPath
     * @param port
     * @param browserStartCommand
     * @param testContext
     *            TODO
     * @throws Exception
     */
    // Note: Made this method synchronized since there's no guarantee the same thread that invokes
    // setup() will invoke
    // shutdown(). Best to be safe!
    @Parameters(
    { "tapestry.web-app-folder", "tapestry.context-path", "tapestry.port",
            "tapestry.browser-start-command" })
    @BeforeTest
    public synchronized void startup(

    @Optional("src/main/webapp")
    String webAppFolder,

    @Optional("")
    String contextPath,

    @Optional("9999")
    int port,

    @Optional("*firefox")
    String browserStartCommand, ITestContext testContext) throws Exception
    {
        stopWebServer = launchWebServer(webAppFolder, contextPath, port);

        seleniumServer = new SeleniumServer();

        seleniumServer.start();

        String baseURL = String.format("http://localhost:%d%s/", port, contextPath);

        CommandProcessor cp = new HttpCommandProcessor("localhost",
                RemoteControlConfiguration.DEFAULT_PORT, browserStartCommand, baseURL);

        ErrorReporter errorReporter = new ErrorReporterImpl(cp, testContext);

        selenium = new DefaultSelenium(new ErrorReportingCommandProcessor(cp, errorReporter));

        selenium.start();

        testContext.setAttribute(TapestryTestConstants.BASE_URL_ATTRIBUTE, baseURL);
        testContext.setAttribute(TapestryTestConstants.SELENIUM_ATTRIBUTE, selenium);
        testContext.setAttribute(TapestryTestConstants.ERROR_REPORTER_ATTRIBUTE, errorReporter);
    }

    /** Shuts down the stack at the end of the test. */
    @AfterTest
    public synchronized void cleanup()
    {
        if (selenium != null)
        {
            selenium.stop();
            selenium = null;
        }

        if (seleniumServer != null)
        {
            seleniumServer.stop();
            seleniumServer = null;
        }

        if (stopWebServer != null)
        {
            stopWebServer.run();
            stopWebServer = null;
        }

    }

    /**
     * Invoked from {@link #startup(String, String, int, String, ITestContext)} to launch the web
     * server to be
     * tested. The return value is a Runnable that will shut down the launched server at the end of
     * the test (it is coded this way so that the default Jetty web server can be more easily
     * replaced).
     * 
     * @param webAppFolder
     *            path to the web application context
     * @param contextPath
     *            the path the context is mapped to, usually the empty string
     * @param port
     *            the port number the server should handle
     * @return Runnable used to shut down the server
     * @throws Exception
     */
    protected Runnable launchWebServer(String webAppFolder, String contextPath, int port)
            throws Exception
    {
        final Jetty7Runner runner = new Jetty7Runner(webAppFolder, contextPath, port);

        return new Runnable()
        {
            public void run()
            {
                runner.stop();
            }
        };
    }
}
