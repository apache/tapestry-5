// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.internal.ServletContextSymbolProvider;
import org.apache.tapestry5.internal.TapestryAppInitializer;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.services.SystemPropertiesSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.HttpServletRequestHandler;
import org.apache.tapestry5.services.ServletApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The TapestryFilter is responsible for intercepting all requests into the web application. It
 * identifies the requests
 * that are relevant to Tapestry, and lets the servlet container handle the rest. It is also
 * responsible for
 * initializing Tapestry.
 * <p/>
 * The application is primarily configured via context-level init parameters.
 * <p/>
 * <dl>
 * <dt>tapestry.app-package</dt>
 * <dd>The application package (used to search for pages, components, etc.)</dd>
 * </dl>
 * <p/>
 * In addition, a JVM system property affects configuration: <code>tapestry.execution-mode</code>
 * (with default value "production"). This property is a comma-separated list of execution modes.
 * For each mode, an additional init parameter is checked for:
 * <code>tapestry.<em>mode</em>-modules</code>; this is a comma-separated list of module class names
 * to load. In this way, more precise control over the available modules can be obtained which is
 * often needed during testing.
 */
public class TapestryFilter implements Filter
{
    private final Logger logger = LoggerFactory.getLogger(TapestryFilter.class);

    private FilterConfig config;

    private Registry registry;

    private HttpServletRequestHandler handler;

    /**
     * Key under which that Tapestry IoC {@link org.apache.tapestry5.ioc.Registry} is stored in the
     * ServletContext. This
     * allows other code, beyond Tapestry, to obtain the Registry and, from it, any Tapestry
     * services. Such code should
     * be careful about invoking {@link org.apache.tapestry5.ioc.Registry#cleanupThread()}
     * appropriately.
     */
    public static final String REGISTRY_CONTEXT_NAME = "org.apache.tapestry5.application-registry";

    /**
     * Initializes the filter using the {@link TapestryAppInitializer}. The application name is the
     * capitalization of
     * the filter name (as specified in web.xml).
     */
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        config = filterConfig;

        final ServletContext context = config.getServletContext();

        String filterName = config.getFilterName();

        SymbolProvider provider = new SymbolProvider()
        {
            SymbolProvider contextProvider = new ServletContextSymbolProvider(context);
            SymbolProvider systemProvider = new SystemPropertiesSymbolProvider();

            public String valueForSymbol(String symbolName)
            {
                String contextValue = contextProvider.valueForSymbol(symbolName);
                if (contextValue != null) return contextValue;

                return systemProvider.valueForSymbol(symbolName);
            }
        };

        String executionMode = System.getProperty(SymbolConstants.EXECUTION_MODE, "production");

        TapestryAppInitializer appInitializer = new TapestryAppInitializer(logger, provider,
                filterName, executionMode);

        appInitializer.addModules(provideExtraModuleDefs(context));
        appInitializer.addModules(provideExtraModuleClasses(context));

        registry = appInitializer.createRegistry();

        context.setAttribute(REGISTRY_CONTEXT_NAME, registry);

        ServletApplicationInitializer ai = registry.getService("ServletApplicationInitializer",
                ServletApplicationInitializer.class);

        ai.initializeApplication(filterConfig.getServletContext());

        registry.performRegistryStartup();

        handler = registry.getService("HttpServletRequestHandler", HttpServletRequestHandler.class);

        init(registry);

        appInitializer.announceStartup();
    }

    protected final FilterConfig getFilterConfig()
    {
        return config;
    }

    /**
     * Invoked from {@link #init(FilterConfig)} after the Registry has been created, to allow any
     * additional
     * initialization to occur. This implementation does nothing, and my be overriden in subclasses.
     *
     * @param registry from which services may be extracted
     * @throws ServletException
     */
    protected void init(Registry registry) throws ServletException
    {

    }

    /**
     * Overridden in subclasses to provide additional module definitions beyond those normally
     * located. This
     * implementation returns an empty array.
     */
    protected ModuleDef[] provideExtraModuleDefs(ServletContext context)
    {
        return new ModuleDef[0];
    }

    /**
     * Overriden in subclasses to provide additional module classes beyond those normally located. This implementation
     * returns an empty array.
     *
     * @since 5.3
     */
    protected Class[] provideExtraModuleClasses(ServletContext context)
    {
        return new Class[0];
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            boolean handled = handler.service((HttpServletRequest) request,
                    (HttpServletResponse) response);

            if (!handled)
                chain.doFilter(request, response);
        } finally
        {
            registry.cleanupThread();
        }
    }

    /**
     * Shuts down and discards the registry. Invokes
     * {@link #destroy(org.apache.tapestry5.ioc.Registry)} to allow
     * subclasses to peform any shutdown logic, then shuts down the registry, and removes it from
     * the ServletContext.
     */
    public final void destroy()
    {
        destroy(registry);

        registry.shutdown();

        config.getServletContext().removeAttribute(REGISTRY_CONTEXT_NAME);

        registry = null;
        config = null;
        handler = null;
    }

    /**
     * Invoked from {@link #destroy()} to allow subclasses to add additional shutdown logic to the
     * filter. The Registry
     * will be shutdown after this call. This implementation does nothing, and may be overridden in
     * subclasses.
     *
     * @param registry
     */
    protected void destroy(Registry registry)
    {

    }
}
