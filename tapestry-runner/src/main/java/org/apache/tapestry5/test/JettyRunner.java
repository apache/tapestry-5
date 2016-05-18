// Copyright 2009, 2010, 2011, 2013 The Apache Software Foundation
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

import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * Launches an instance of Jetty.
 */
public class JettyRunner implements ServletContainerRunner
{
    private Server jettyServer;

    private String description;

    private int port;

    private int sslPort;

    public JettyRunner()
    {
        // un-configured runner
    }

    public JettyRunner(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        configure(webappFolder, contextPath, port, sslPort).start();
    }

    public JettyRunner configure(String webappFolder, String contextPath, int port, int sslPort) throws Exception
    {
        this.port = port;

        this.sslPort = sslPort;

        String expandedPath = expand(webappFolder);

        description = String.format("<JettyRunner: %s:%s/%s (%s)", contextPath, port, sslPort, expandedPath);

        jettyServer = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        webapp.setWar(expandedPath);

        // SSL support
        File keystoreFile = new File(TapestryRunnerConstants.MODULE_BASE_DIR, "src/test/conf/keystore");

        if (keystoreFile.exists())
        {
            SslContextFactory sslContextFactory = new SslContextFactory();

            sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());

            sslContextFactory.setKeyStorePassword("tapestry");

            sslContextFactory.setKeyManagerPassword("tapestry");

            SslSocketConnector sslConnector = new SslSocketConnector(sslContextFactory);

            sslConnector.setPort(sslPort);

            jettyServer.addConnector(sslConnector);
        }

        jettyServer.setHandler(webapp);
        return this;
    }

    public void start() throws Exception
    {
        jettyServer.start();
    }

    /**
     * Immediately shuts down the server instance.
     */
    @Override
    public void stop()
    {
        System.out.printf("Stopping Jetty instance on port %d/%d\n", port, sslPort);

        try
        {
            // Stop immediately and not gracefully.
            jettyServer.stop();
        } catch (Exception ex)
        {
            throw new RuntimeException("Error stopping Jetty instance: " + ex.toString(), ex);
        }

        System.out.println("Jetty instance has stopped.");
    }

    public Server getServer()
    {
        return jettyServer;
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

    /**
     * Main entrypoint used to run the Jetty instance from the command line.
     *
     * @since 5.4
     */
    public static void main(String[] args) throws Exception
    {
        String commandName = JettyRunner.class.getName();

        Options options = new Options();

        String webapp = "src/main/webapp";
        String context = "/";
        int httpPort = 8080;
        int sslPort = 8443;

        options.addOption(OptionBuilder.withLongOpt("directory")
                .withDescription("Root context directory (defaults to 'src/main/webapp')")
                .hasArg().withArgName("DIR")
                .create('d'))
                .addOption(OptionBuilder.withLongOpt("context")
                        .withDescription("Context path for application (defaults to '/')")
                        .hasArg().withArgName("CONTEXT")
                        .create('c'))
                .addOption(OptionBuilder.withLongOpt("port")
                        .withDescription("HTTP port (defaults to 8080)")
                        .hasArg().withArgName("PORT")
                        .create('p'))
                .addOption(OptionBuilder.withLongOpt("secure-port")
                        .withDescription("HTTPS port (defaults to 8443)")
                        .hasArg().withArgName("PORT")
                        .create('s'))
                .addOption("h", "help", false, "Display command usage");


        CommandLine line = new BasicParser().parse(options, args);

        boolean usage = line.hasOption('h');

        if (!usage)
        {
            if (line.hasOption('d'))
            {
                webapp = line.getOptionValue('d');
            }

            File folder = new File(webapp);

            if (!folder.exists())
            {
                System.err.printf("%s: Directory `%s' does not exist.%n", commandName, webapp);
                System.exit(-1);
            }

            if (line.hasOption('p'))
            {
                try
                {
                    httpPort = Integer.parseInt(line.getOptionValue('p'));
                } catch (NumberFormatException e)
                {
                    usage = true;
                }
            }

            if (line.hasOption('s'))
            {
                try
                {
                    sslPort = Integer.parseInt(line.getOptionValue('s'));
                } catch (NumberFormatException e)
                {
                    usage = true;
                }
            }

            if (line.hasOption('c'))
            {
                context = line.getOptionValue('c');
            }

        }

        if (usage)
        {
            new HelpFormatter().printHelp(commandName, options);
            System.exit(-1);
        }

        new JettyRunner(webapp, context, httpPort, sslPort);
    }
}
