// Copyright 2007, 2010 The Apache Software Foundation
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
import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.xml.XmlTest;

import com.thoughtworks.selenium.Selenium;

public class TapestryTestConstants
{
    /**
     * The current working directory (i.e., property "user.dir").
     */
    public static final String CURRENT_DIR_PATH = System.getProperty("user.dir");
    /**
     * The Surefire plugin sets basedir but DOES NOT change the current working directory. When
     * building across modules,
     * basedir changes for each module, but user.dir does not. This value should be used when
     * referecing local files.
     * Outside of surefire, the "basedir" property will not be set, and the current working
     * directory will be the
     * default.
     */
    public static final String MODULE_BASE_DIR_PATH = System.getProperty("basedir",
            CURRENT_DIR_PATH);

    /**
     * {@link #MODULE_BASE_DIR_PATH} as a file.
     */
    public static final File MODULE_BASE_DIR = new File(MODULE_BASE_DIR_PATH);

    /**
     * {@link ITestContext} attribute holding an instance of {@link Selenium}.
     * 
     * @see SeleniumLauncher#startup(String, String, int, String, ITestContext)
     * @since 5.2.0
     */
    public static final String SELENIUM_ATTRIBUTE = "tapestry.selenium";

    /**
     * {@link ITestContext} attribute holding an instance of {@link ErrorReporter}.
     * 
     * @see SeleniumLauncher#startup(String, String, int, String, ITestContext)
     * @since 5.2.0
     */
    public static final String ERROR_REPORTER_ATTRIBUTE = "tapestry.error-reporter";

    /**
     * {@link ITestContext} attribute holding the application's base URL.
     * 
     * @see SeleniumLauncher#startup(String, String, int, String, ITestContext)
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
    public static final String CONTEXT_PATH_PARAMTER = "tapestry.context-path";
    
    /**
     * {@link XmlTest} parameter holding the web server port.
     */
    public static final String PORT_PARAMETER = "tapestry.port";
    
    /**
     * {@link XmlTest} parameter holding the browser command to pass to Selenium.
     */
    public static final String BROWSER_START_COMMAND_PARAMETER = "tapestry.browser-start-command";
}
