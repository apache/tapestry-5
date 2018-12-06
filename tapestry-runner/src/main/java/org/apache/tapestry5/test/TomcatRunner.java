// Copyright 2011, 2013 The Apache Software Foundation
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

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * Launches an instance of Tomcat.
 *
 * @since 5.3
 */
public class TomcatRunner implements ServletContainerRunner
{
    private final String description;
    private final int port;
    private final int sslPort;
    private Tomcat tomcatServer;

    public TomcatRunner(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        this.port = port;

        this.sslPort = sslPort;

        final String expandedPath = expand(webappFolder);

        description = String.format("<TomcatRunner:%s:%s/%s (%s)", contextPath, port, sslPort, expandedPath);

        tomcatServer = new Tomcat();

        // Tomcat creates a folder, try to put it in an OS agnostic tmp dir
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileSeparator = System.getProperty("file.separator");
        if (!tmpDir.endsWith(fileSeparator))
            tmpDir = tmpDir + fileSeparator;
        tomcatServer.setBaseDir(tmpDir + "tomcat");

        tomcatServer.addWebapp("/", expandedPath);

        tomcatServer.getConnector().setAllowTrace(true);

        // SSL support
        final File keystoreFile = new File(TapestryRunnerConstants.MODULE_BASE_DIR, "src/test/conf/keystore");

        if (keystoreFile.exists())
        {
            final Connector https = new Connector();
            https.setPort(sslPort);
            https.setProperty("keystore", keystoreFile.getPath());
            https.setProperty("keypass", "tapestry");
            tomcatServer.getService().addConnector(https);
        }

        tomcatServer.start();
    }

    /**
     * Immediately shuts down the server instance.
     */
    @Override
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
            throw new RuntimeException("Error stopping Tomcat instance: " + ex.toString(), ex);
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
     * @see TapestryRunnerConstants#MODULE_BASE_DIR
     */
    protected String expand(String moduleLocalPath)
    {
        File path = new File(moduleLocalPath);

        // Don't expand if the path provided already exists.
        if (path.isAbsolute() && path.isDirectory())
            return moduleLocalPath;

        return new File(TapestryRunnerConstants.MODULE_BASE_DIR, moduleLocalPath).getPath();
    }

}
