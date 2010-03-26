// Copyright 2009, 2010 The Apache Software Foundation
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

import java.io.File;
import java.util.Map;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.xml.XmlTest;

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
     * 
     * Tests in the <em>beforeStartup</em> group will be run before the start of Selenium. This
     * can be used to programmatically override the above parameter values. For an example see
     * {@link org.apache.tapestry5.integration.reload.ReloadTests#beforeStartup}. 
     * 
     * @param webAppFolder
     * @param contextPath
     * @param port
     * @param browserStartCommand
     * @param testContext
     *            Used to share objects between the launcher and the test suites
     * @throws Exception
     */
    // Note: Made this method synchronized since there's no guarantee the same thread that invokes
    // setup() will invoke
    // shutdown(). Best to be safe!
    @Parameters(
    { TapestryTestConstants.WEB_APP_FOLDER_PARAMETER, TapestryTestConstants.CONTEXT_PATH_PARAMTER,
            TapestryTestConstants.PORT_PARAMETER, TapestryTestConstants.SSL_PORT_PARAMETER,
            TapestryTestConstants.BROWSER_START_COMMAND_PARAMETER })
    @BeforeTest(dependsOnGroups = { "beforeStartup" })
    public synchronized void startup(

    @Optional("src/main/webapp")
    String webAppFolder,

    @Optional("")
    String contextPath,

    @Optional("9090")
    int port,
    
    @Optional("8443")
    int sslPort,

    @Optional("*firefox")
    String browserStartCommand, ITestContext testContext, XmlTest xmlTest) throws Exception
    {
        // If a parameter is overridden in another test method, TestNG won't pass the
        // updated value but the original (coming from testng.xml or the default).
        Map<String, String> testParameters = xmlTest.getParameters();
        
        if(testParameters.containsKey(TapestryTestConstants.WEB_APP_FOLDER_PARAMETER))
            webAppFolder = testParameters.get(TapestryTestConstants.WEB_APP_FOLDER_PARAMETER);
        
        if(testParameters.containsKey(TapestryTestConstants.CONTEXT_PATH_PARAMTER))
            contextPath = testParameters.get(TapestryTestConstants.CONTEXT_PATH_PARAMTER);
        
        if(testParameters.containsKey(TapestryTestConstants.PORT_PARAMETER))
            port = Integer.parseInt(testParameters.get(TapestryTestConstants.PORT_PARAMETER));
        
        if(testParameters.containsKey(TapestryTestConstants.BROWSER_START_COMMAND_PARAMETER))
            browserStartCommand = testParameters.get(TapestryTestConstants.BROWSER_START_COMMAND_PARAMETER);
        
        stopWebServer = launchWebServer(webAppFolder, contextPath, port, sslPort);

        seleniumServer = new SeleniumServer();
        
        File ffProfileTemplate = new File(TapestryTestConstants.MODULE_BASE_DIR, "src/test/conf/ff_profile_template");
        
        if(ffProfileTemplate.isDirectory())
            seleniumServer.getConfiguration().setFirefoxProfileTemplate(ffProfileTemplate);

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
     * @param sslPort
     *            the port number on which the server should handle secure requests
     * @return Runnable used to shut down the server
     * @throws Exception
     */
    protected Runnable launchWebServer(String webAppFolder, String contextPath, int port, int sslPort)
            throws Exception
    {
        final Jetty7Runner runner = new Jetty7Runner(webAppFolder, contextPath, port, sslPort);

        return new Runnable()
        {
            public void run()
            {
                runner.stop();
            }
        };
    }
}
