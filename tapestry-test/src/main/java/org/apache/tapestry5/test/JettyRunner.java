// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SunJsseListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;
import static java.lang.String.format;

/**
 * Used to start up an instance of the Jetty servlet container in-process, as part of an integration test suite. The
 * started Jetty is reliant on the file <code>src/test/conf/webdefault.xml</code>.
 *
 * @see AbstractIntegrationTestSuite
 */
public class JettyRunner
{
    public static final String DEFAULT_CONTEXT_PATH = "/";

    public static final int DEFAULT_PORT = 8080;

    public static final int DEFAULT_SECURE_PORT = 8443;

    private final File workingDir;

    private final String contextPath;

    private final int port;

    private final String warPath;

    private final Server jetty;
    
    private final String[] virtualHosts;

    /**
     * Creates and starts a new instance of Jetty. This should be done from a test case setup method.
     *
     * @param workingDir  current directory (used for any relative files)
     * @param contextPath the context path for the deployed application
     * @param port        the port number used to access the application
     * @param warPath     the path to the exploded web application (typically, "src/main/webapp")
     * @param virtualHosts an array with virtual hosts
     */
    public JettyRunner(File workingDir, String contextPath, int port, String warPath,
    		String ... virtualHosts)
    {
        this.workingDir = workingDir;
        this.contextPath = contextPath;
        this.port = port;
        this.warPath = warPath;
        this.virtualHosts = virtualHosts;

        jetty = createAndStart();
    }

    /**
     * Stops the Jetty instance. This should be called from a test case tear down method.
     */
    public void stop()
    {
        System.out.printf("Stopping Jetty instance on port %d\n", port);

        try
        {
            // Stop immediately and not gracefully.
            jetty.stop(false);

            while (jetty.isStarted())
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
        return format("<JettyRunner %s:%d (%s)>", contextPath, port, warPath);
    }


    private Server createAndStart()
    {
        try
        {

        	File warPathFile = new File(warPath);
        	
            String webappPath = warPathFile.isAbsolute()
                                ? warPath
                                : new File(workingDir, this.warPath).getPath();
            String webDefaults = new File(workingDir, "src/test/conf/webdefault.xml").getPath();

            File keystoreFile = new File(workingDir, "src/test/conf/keystore");
            String keystore = keystoreFile.getPath();

            System.out.printf("Starting Jetty instance on port %d (%s mapped to %s)\n", port, contextPath, webappPath);

            Server server = new Server();


            SocketListener socketListener = new SocketListener();
            socketListener.setPort(port);
            server.addListener(socketListener);

            if (keystoreFile.exists())
            {
                SunJsseListener secureListener = new SunJsseListener();
                secureListener.setPort(DEFAULT_SECURE_PORT);
                secureListener.setKeystore(keystore);
                secureListener.setPassword("tapestry");
                secureListener.setKeyPassword("tapestry");

                server.addListener(secureListener);
            }

            NCSARequestLog log = new NCSARequestLog();
            server.setRequestLog(log);

            WebApplicationContext context = server.addWebApplication(contextPath, webappPath);
            
            for (String virtualHost : virtualHosts) {
				context.addVirtualHost(virtualHost);
			}

            context.setDefaultsDescriptor(webDefaults);

            server.start();

            return server;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Failure starting Jetty instance: " + ex.toString(), ex);
        }
    }
}
