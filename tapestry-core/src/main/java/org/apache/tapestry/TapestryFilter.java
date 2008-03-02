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

package org.apache.tapestry;

import org.apache.tapestry.internal.ServletContextSymbolProvider;
import org.apache.tapestry.internal.TapestryAppInitializer;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.services.ServiceActivity;
import org.apache.tapestry.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry.ioc.services.Status;
import org.apache.tapestry.ioc.services.SymbolProvider;
import org.apache.tapestry.services.HttpServletRequestHandler;
import org.apache.tapestry.services.ServletApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;

/**
 * The TapestryFilter is responsible for intercepting all requests into the web application. It identifies the requests
 * that are relevant to Tapestry, and lets the servlet container handle the rest. It is also responsible for
 * initializing Tapestry.
 */
public class TapestryFilter implements Filter
{
    private final Logger _logger = LoggerFactory.getLogger(TapestryFilter.class);

    private FilterConfig _config;

    private Registry _registry;

    private HttpServletRequestHandler _handler;

    /**
     * Initializes the filter using the {@link TapestryAppInitializer}. The application package is defined by the
     * <code>tapestry.app-package</code> context init parameter and the application name is the capitalization of the
     * filter name (as specified in web.xml).
     */
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        _config = filterConfig;

        ServletContext context = _config.getServletContext();

        String filterName = _config.getFilterName();

        SymbolProvider provider = new ServletContextSymbolProvider(context);

        TapestryAppInitializer appInitializer = new TapestryAppInitializer(provider, filterName, "servlet");

        appInitializer.addModules(provideExtraModuleDefs(context));

        _registry = appInitializer.getRegistry();

        long start = appInitializer.getStartTime();

        long toRegistry = appInitializer.getRegistryCreatedTime();

        ServletApplicationInitializer ai = _registry.getService("ServletApplicationInitializer",
                                                                ServletApplicationInitializer.class);

        ai.initializeApplication(filterConfig.getServletContext());

        _registry.performRegistryStartup();

        _handler = _registry.getService("HttpServletRequestHandler", HttpServletRequestHandler.class);

        init(_registry);

        long toFinish = System.currentTimeMillis();

        StringBuilder buffer = new StringBuilder("Startup status:\n\n");
        Formatter f = new Formatter(buffer);

        f.format("Startup time: %,d ms to build IoC Registry, %,d ms overall." + "\n\nStartup services status:\n",
                 toRegistry - start, toFinish - start);

        int unrealized = 0;

        ServiceActivityScoreboard scoreboard = _registry
                .getService(ServiceActivityScoreboard.class);

        List<ServiceActivity> serviceActivity = scoreboard.getServiceActivity();

        int longest = 0;

        // One pass to find the longest name, and to count the unrealized services.

        for (ServiceActivity activity : serviceActivity)
        {
            Status status = activity.getStatus();

            longest = Math.max(longest, activity.getServiceId().length());

            if (status == Status.DEFINED || status == Status.VIRTUAL) unrealized++;

        }

        String formatString = "%" + longest + "s: %s\n";

        // A second pass to output all the services

        for (ServiceActivity activity : serviceActivity)
        {
            f.format(formatString, activity.getServiceId(), activity.getStatus().name());
        }

        f.format("\n%4.2f%% unrealized services (%d/%d)\n", 100. * unrealized / serviceActivity.size(), unrealized,
                 serviceActivity.size());

        _logger.info(buffer.toString());
    }

    protected final FilterConfig getFilterConfig()
    {
        return _config;
    }

    /**
     * Invoked from {@link #init(FilterConfig)} after the Registry has been created, to allow any additional
     * initialization to occur. This implementation does nothing, and my be overriden in subclasses.
     *
     * @param registry from which services may be extracted
     * @throws ServletException
     */
    protected void init(Registry registry) throws ServletException
    {

    }

    /**
     * Overridden in subclasses to provide additional module definitions beyond those normally located. This
     * implementation returns an empty array.
     */
    protected ModuleDef[] provideExtraModuleDefs(ServletContext context)
    {
        return new ModuleDef[0];
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            boolean handled = _handler.service((HttpServletRequest) request, (HttpServletResponse) response);

            if (!handled) chain.doFilter(request, response);
        }
        finally
        {
            _registry.cleanupThread();
        }
    }

    /**
     * Shuts down and discards the registry.
     */
    public final void destroy()
    {
        destroy(_registry);

        _registry.shutdown();

        _registry = null;
        _config = null;
        _handler = null;
    }

    /**
     * Invoked from {@link #destroy()} to allow subclasses to add additional shutdown logic to the filter. The Registry
     * will be shutdown after this call. This implementation does nothing, and may be overridden in subclasses.
     *
     * @param registry
     */
    protected void destroy(Registry registry)
    {

    }

}
