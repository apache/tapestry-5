// Copyright 2011 The Apache Software Foundation
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

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Embedded;

import java.io.File;

/**
 * Launches an instance of Tomcat 6.
 * 
 * @since 5.3
 */
public class Tomcat6Runner implements ServletContainerRunner
{
    private final String description;
    private final int port;
    private final int sslPort;
    private Embedded tomcatServer;

    public Tomcat6Runner(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        this.port = port;

        this.sslPort = sslPort;

        final String expandedPath = expand(webappFolder);

        description = String.format("<Tomcat6Runner:%s:%s/%s (%s)", contextPath, port, sslPort, expandedPath);

        tomcatServer = new Embedded();

        // Tomcat creates a folder, try to put it in an OS agnostic tmp dir
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileSeparator = System.getProperty("file.separator");
        if (!tmpDir.endsWith(fileSeparator))
            tmpDir = tmpDir + fileSeparator;
        tomcatServer.setCatalinaHome(tmpDir + "tomcat");

        final Engine engine = tomcatServer.createEngine();
        engine.setDefaultHost("localhost");

        final Host host = tomcatServer.createHost("localhost", expandedPath);
        engine.addChild(host);

        final Context context = tomcatServer.createContext(contextPath, expandedPath);

        // Without a servlet the filter will not get run.
        final Wrapper wrapper = context.createWrapper();
        final String name = "DefaultServlet";
        wrapper.setName(name);
        wrapper.setServletClass(DefaultServlet.class.getName());
        context.addChild(wrapper);
        context.addServletMapping("/", name);

        File contextConfigFile = new File(webappFolder, "META-INF/context.xml");

        if (contextConfigFile.exists())
            context.setConfigFile(contextConfigFile.getAbsolutePath());

        context.setLoader(new WebappLoader(this.getClass().getClassLoader()));

        host.addChild(context);

        tomcatServer.addEngine(engine);

        Connector http = tomcatServer.createConnector("localhost", port, false);
        http.setAllowTrace(true);
        tomcatServer.addConnector(http);

        // SSL support
        final File keystoreFile = new File(TapestryTestConstants.MODULE_BASE_DIR, "src/test/conf/keystore");

        if (keystoreFile.exists())
        {
            final Connector https = tomcatServer.createConnector("localhost", sslPort, true);
            https.setProperty("keystore", keystoreFile.getPath());
            https.setProperty("keypass", "tapestry");
            tomcatServer.addConnector(https);
        }

        tomcatServer.start();
    }

    /**
     * Immediately shuts down the server instance.
     */
    public void stop()
    {
        System.out.printf("Stopping Tomcat instance on port %d/%d\n", port, sslPort);

        try
        {
            // Stop immediately and not gracefully.
            tomcatServer.stop();
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error stopping Tomcat6 instance: " + ex.toString(), ex);
        }

        System.out.println("Tomcat instance has stopped.");
    }

    @Override
    public String toString()
    {
        return description;
    }

    /**
     * Needed inside Maven multi-projects to expand a path relative to the module to a complete
     * path. If the path already is absolute and points to an existing directory, it will be used
     * unchanged.
     * 
     * @param moduleLocalPath
     * @return expanded path
     * @see TapestryTestConstants#MODULE_BASE_DIR
     */
    protected String expand(String moduleLocalPath)
    {
        File path = new File(moduleLocalPath);

        // Don't expand if the path provided already exists.
        if (path.isAbsolute() && path.isDirectory())
            return moduleLocalPath;

        return new File(TapestryTestConstants.MODULE_BASE_DIR, moduleLocalPath).getPath();
    }

}
