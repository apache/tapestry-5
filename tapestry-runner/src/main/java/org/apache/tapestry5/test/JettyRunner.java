// Copyright 2009, 2010, 2011, 2013, 20026 The Apache Software Foundation
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
import org.apache.tapestry5.test.constants.TapestryRunnerConstants;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Launches an instance of Jetty.
 */
public class JettyRunner implements ServletContainerRunner
{
    /**
     * Number of attempts made to acquire the server's ports before giving up.
     *
     * @since 5.10
     */
    private static final int BIND_ATTEMPTS = 5;

    /**
     * Delay, in milliseconds, between attempts to acquire the server's ports.
     *
     * @since 5.10
     */
    private static final long BIND_RETRY_DELAY = 500;

    private Server jettyServer;

    private String description;

    private int port;

    private int sslPort;

    private boolean sslEnabled;

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

        sslEnabled = keystoreFile.exists();

        if (sslEnabled)
        {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
            sslContextFactory.setKeyStorePassword("tapestry");
            sslContextFactory.setKeyManagerPassword("tapestry");

            HttpConfiguration httpConfiguration = new HttpConfiguration();
            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(sslPort);

            ServerConnector sslConnector = new ServerConnector(getServer(),
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpConfiguration));

            sslConnector.setPort(sslPort);

            jettyServer.addConnector(sslConnector);
        }

        jettyServer.setHandler(webapp);
        return this;
    }

    public void start() throws Exception
    {
        awaitPortAvailable(port);

        if (sslEnabled)
        {
            awaitPortAvailable(sslPort);
        }

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

            // Release the connectors and the thread pool, rather than leaving them to be
            // collected at some later point.
            jettyServer.destroy();
        } catch (Exception ex)
        {
            throw new RuntimeException("Error stopping Jetty instance: " + ex.toString(), ex);
        }

        System.out.println("Jetty instance has stopped.");
    }

    /**
     * Waits, for a bounded amount of time, until the given port can be acquired.
     *
     * <p>A port might not always be available the instant the server holding it is stopped,
     * so a suite that runs several test applications in sequence inside a single JVM could
     * otherwise fail with "Failed to bind" IOException.
     *
     * @param portToAcquire
     *         the port the server is about to bind
     * @throws IOException
     *         if the port is still held after {@link #BIND_ATTEMPTS} attempts
     * @since 5.10
     */
    private static void awaitPortAvailable(int portToAcquire) throws IOException, InterruptedException
    {
        IOException failure = null;

        for (int attempt = 0; attempt < BIND_ATTEMPTS; attempt++)
        {
            if (attempt > 0)
            {
                Thread.sleep(BIND_RETRY_DELAY);
            }

            // Jetty's connectors set SO_REUSEADDR, so this probe has to as well for the result to
            // mean the same thing.
            try (ServerSocket probe = new ServerSocket())
            {
                probe.setReuseAddress(true);
                probe.bind(new InetSocketAddress(portToAcquire));

                return;
            } catch (IOException ex)
            {
                failure = ex;

                System.out.printf("Port %d is not available yet (attempt %d of %d).%n",
                        portToAcquire, attempt + 1, BIND_ATTEMPTS);
            }
        }

        throw new IOException(String.format(
                "Port %d is still in use after %d attempts over %d ms; a previous server instance, or another process, has not released it.",
                portToAcquire, BIND_ATTEMPTS, (BIND_ATTEMPTS - 1) * BIND_RETRY_DELAY), failure);
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
