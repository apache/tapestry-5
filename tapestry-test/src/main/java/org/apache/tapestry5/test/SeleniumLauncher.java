// Copyright 2009, 2010 The Apache Software Foundation
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

import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;

/**
 * This class is no longer necessary (as part of the TAP5-1315 fix) and the reference to it inside testng.xml
 * may be removed.
 * 
 * @since 5.2.0
 * @deprecated To be removed in Tapestry 5.3.
 */
@Deprecated
public class SeleniumLauncher
{
    @BeforeTest(dependsOnGroups =
    { "beforeStartup" })
    public void startup() throws Exception
    {
        System.err.println("***\n*** Class org.apache.tapestry5.test.SeleniumLauncher is not longer used, and may be\n"
                + "*** removed from testng.xml.\n***");
    }

    /**
     * Launches the web server to be tested. The return value is a Runnable that
     * will shut down the launched server at the end of the test (it is coded
     * this way so that the default Jetty web server can be more easily replaced).
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
    protected Runnable launchWebServer(String webAppFolder, String contextPath, int port, int sslPort) throws Exception
    {
        return null;
    }
}
