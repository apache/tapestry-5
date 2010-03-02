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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Launches an instance of Jetty7.
 */
public class Jetty7Runner
{
    private final Server jettyServer;

    private final String description;

    private final int port;

    public Jetty7Runner(String webappFolder, String contextPath, int port) throws Exception
    {
        this.port = port;

        String expandedPath = expand(webappFolder);

        description = String.format("<Jetty7Runner: %s:%s (%s)", contextPath, port, expandedPath);

        jettyServer = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setWar(expandedPath);

        // TODO: SSL support

        jettyServer.setHandler(webapp);

        jettyServer.start();
    }

    /** Immediately shuts down the server instance. */
    public void stop()
    {
        System.out.printf("Stopping Jetty instance on port %d\n", port);

        try
        {
            // Stop immediately and not gracefully.
            jettyServer.stop();
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
        if(path.isAbsolute() && path.isDirectory())
            return moduleLocalPath;
        
        return new File(TapestryTestConstants.MODULE_BASE_DIR, moduleLocalPath).getPath();
    }
}
