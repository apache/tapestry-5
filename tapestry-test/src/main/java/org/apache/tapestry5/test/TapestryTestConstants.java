// Copyright 2007, 2010, 2013 The Apache Software Foundation
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

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.Selenium;
import org.testng.ITestContext;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;

/**
 * Defins {@link ITestContext} attributes meaninful to Tapestry for controlling application startup and shutdown.
 */
public class TapestryTestConstants
{

    /**
     * {@link ITestContext} attribute holding an instance of {@link Selenium}.
     *
     * @see SeleniumTestCase#testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)
     * @since 5.2.0
     */
    public static final String SELENIUM_ATTRIBUTE = "tapestry.selenium";

    /**
     * {@link ITestContext} attribute holding an instance of {@link ErrorReporter}.
     *
     * @see SeleniumTestCase#testStartup(org.testng.ITestContext, org.testng.xml.XmlTest)
     * @since 5.2.0
     */
    public static final String ERROR_REPORTER_ATTRIBUTE = "tapestry.error-reporter";

    /**
     * {@link ITestContext} attribute holding an instance of {@link ErrorReporter}, used
     * to shutdown Selenium and the Web Server at the end of the test.
     *
     * @since 5.2.2
     */
    public static final String SHUTDOWN_ATTRIBUTE = "tapestry.shutdown";

    /**
     * The {@link ITestContext} attribute holding an instance of {@link CommandProcessor}, with
     * enhanced exception reporting control. This allows tests that wish to, to bypass the {@link Selenium} interface
     * and execute commands directly on the Selenium RC server.
     *
     * @since 5.2.0
     */
    public static final String COMMAND_PROCESSOR_ATTRIBUTE = "tapestry.command-processor";

    /**
     * {@link ITestContext} attribute holding the application's base URL.
     *
     * @since 5.2.0
     */
    public static final String BASE_URL_ATTRIBUTE = "tapestry.base-url";

    /**
     * {@link ITestContext} attribute updated to store the current test method
     * (as a {@link Method} instance).
     */
    public static final String CURRENT_TEST_METHOD_ATTRIBUTE = "tapestry.current-test-method";

    /**
     * {@link XmlTest} parameter holding an absolute or relative path to a web app
     * folder.
     */
    public static final String WEB_APP_FOLDER_PARAMETER = "tapestry.web-app-folder";

    /**
     * {@link XmlTest} parameter holding the context path.
     */
    public static final String CONTEXT_PATH_PARAMETER = "tapestry.context-path";

    /**
     * {@link XmlTest} parameter holding the web server port.
     */
    public static final String PORT_PARAMETER = "tapestry.port";

    /**
     * {@link XmlTest} parameter holding the web server ssl port.
     */
    public static final String SSL_PORT_PARAMETER = "tapestry.ssl-port";

    /**
     * {@link XmlTest} parameter holding the browser command to pass to Selenium.
     */
    public static final String BROWSER_START_COMMAND_PARAMETER = "tapestry.browser-start-command";

    /**
     * {@link XmlTest} parameter holding the servlet container to run for the integration tests.
     *
     * @since 5.3
     */
    public static final String SERVLET_CONTAINER_PARAMETER = "tapestry.servlet-container";
}
