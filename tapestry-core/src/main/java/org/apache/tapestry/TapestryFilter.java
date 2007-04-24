// Copyright 2006 The Apache Software Foundation
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

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.TapestryAppInitializer;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.services.HttpServletRequestHandler;
import org.apache.tapestry.services.ServletApplicationInitializer;
import org.apache.tapestry.services.TapestryModule;

/**
 * The TapestryFilter is responsible for intercepting all requests into the web application. It
 * identifies the requests that are relevant to Tapestry, and lets the servlet container handle the
 * rest. It is also responsible for initializating Tapestry.
 */
public class TapestryFilter implements Filter
{
    private final Log _log = LogFactory.getLog(TapestryFilter.class);

    private FilterConfig _config;

    private Registry _registry;

    private HttpServletRequestHandler _handler;

    /**
     * Initializes the filter using the {@link TapestryAppInitializer}. The application package is
     * defined by the <code>tapestry.app-package</code> context init parameter and the application
     * name is the capitalization of the filter name (as specified in web.xml).
     */
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        _config = filterConfig;

        // Note: configured as a <context-param>, not a filter <init-param>
        String appPackage = _config.getServletContext().getInitParameter(
                InternalConstants.TAPESTRY_APP_PACKAGE_PARAM);
        String filterName = _config.getFilterName();

        TapestryAppInitializer appInitializer = new TapestryAppInitializer(appPackage, filterName,
                "servlet");

        _registry = appInitializer.getRegistry();

        long start = appInitializer.getStartTime();

        long toRegistry = appInitializer.getRegistryCreatedTime();

        ServletApplicationInitializer ai = _registry.getService(
                "tapestry.ServletApplicationInitializer",
                ServletApplicationInitializer.class);

        ai.initializeApplication(filterConfig.getServletContext());

        _handler = _registry.getService(
                "tapestry.HttpServletRequestHandler",
                HttpServletRequestHandler.class);

        long toFinish = System.currentTimeMillis();

        _log.info(format("Startup time: %,d ms to build IoC Registry, %,d ms overall.", toRegistry
                - start, toFinish - start));
    }

    /**
     * Adds additional modules to the builder. This implementation adds any modules identified by
     * {@link IOCUtilities#addDefaultModules(RegistryBuilder)}. Most subclasses will invoke this
     * implementation, and add additional modules to the RegistryBuilder besides.
     * {@link org.apache.tapestry.ioc.services.TapestryIOCModule} and {@link TapestryModule} will
     * already have been added, as will an application module if present.
     * 
     * @param builder
     */
    protected void addModules(RegistryBuilder builder)
    {
        IOCUtilities.addDefaultModules(builder);
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            boolean handled = _handler.service(
                    (HttpServletRequest) request,
                    (HttpServletResponse) response);

            if (!handled)
                chain.doFilter(request, response);
        }
        finally
        {
            _registry.cleanupThread();
        }
    }

    /** Shuts down and discards the registry. */
    public final void destroy()
    {
        _registry.shutdown();

        _registry = null;
        _config = null;
        _handler = null;
    }

}
