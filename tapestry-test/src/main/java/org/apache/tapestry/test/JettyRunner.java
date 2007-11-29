// Copyright 2006, 2007 The Apache Software Foundation
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

import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import static java.lang.String.format;

/**
 * Used to start up an instance of the Jetty servlet container in-process, as part of an integration
 * test suite. The started Jetty is reliant on the file <code>src/test/conf/webdefault.xml</code>.
 *
 * @see AbstractIntegrationTestSuite
 */
public class JettyRunner
{
    public static final String DEFAULT_CONTEXT_PATH = "/";

    public static final int DEFAULT_PORT = 80;

    private final String _contextPath;

    private final int _port;

    private final String _warPath;

    private final Server _jetty;

    /**
     * Defaults the context path to "/" and the port to 80.
     */
    public JettyRunner(String warPath)
    {
        this(DEFAULT_CONTEXT_PATH, DEFAULT_PORT, warPath);
    }

    /**
     * Creates and starts a new instance of Jetty. This should be done from a test case setup
     * method.
     *
     * @param contextPath the context path for the deployed application
     * @param port        the port number used to access the application
     * @param warPath     the path to the exploded web application (typically, "src/main/webapp")
     */
    public JettyRunner(String contextPath, int port, String warPath)
    {
        _contextPath = contextPath;
        _port = port;
        _warPath = warPath;

        _jetty = createAndStart();
    }

    /**
     * Stops the Jetty instance. This should be called from a test case tear down method.
     */
    public void stop()
    {
        System.out.printf("Stopping Jetty instance on port %d\n", _port);

        try
        {
            // Stop immediately and not gracefully.
            _jetty.stop(false);

            while (_jetty.isStarted())
            {
                Thread.sleep(100);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error stopping Jetty instance: " + ex.toString(), ex);
        }

        System.out.println("Jetty instance has stopped.");
    }

    @Override
    public String toString()
    {
        return format("<JettyRunner %s:%d (%s)>", _contextPath, _port, _warPath);
    }

    private Server createAndStart()
    {
        System.out.printf("Starting Jetty instance on port %d (%s mapped to %s)\n", _port, _contextPath, _warPath);

        try
        {
            Server server = new Server();

            SocketListener socketListener = new SocketListener();
            socketListener.setPort(_port);
            server.addListener(socketListener);

            NCSARequestLog log = new NCSARequestLog();
            server.setRequestLog(log);

            WebApplicationContext context = server.addWebApplication(_contextPath, _warPath);

            context.setDefaultsDescriptor("src/test/conf/webdefault.xml");

            server.start();

            return server;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failure starting Jetty instance: " + ex.toString(), ex);
        }
    }
}
