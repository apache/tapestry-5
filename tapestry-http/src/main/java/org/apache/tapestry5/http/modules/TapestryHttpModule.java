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

package org.apache.tapestry5.http.modules;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.OptimizedSessionPersistedObject;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.internal.gzip.GZipFilter;
import org.apache.tapestry5.http.internal.services.ApplicationGlobalsImpl;
import org.apache.tapestry5.http.internal.services.BaseURLSourceImpl;
import org.apache.tapestry5.http.internal.services.ContextImpl;
import org.apache.tapestry5.http.internal.services.DefaultSessionPersistedObjectAnalyzer;
import org.apache.tapestry5.http.internal.services.OptimizedSessionPersistedObjectAnalyzer;
import org.apache.tapestry5.http.internal.services.RequestGlobalsImpl;
import org.apache.tapestry5.http.internal.services.RequestImpl;
import org.apache.tapestry5.http.internal.services.ResponseCompressionAnalyzerImpl;
import org.apache.tapestry5.http.internal.services.ResponseImpl;
import org.apache.tapestry5.http.internal.services.TapestrySessionFactory;
import org.apache.tapestry5.http.internal.services.TapestrySessionFactoryImpl;
import org.apache.tapestry5.http.services.ApplicationGlobals;
import org.apache.tapestry5.http.services.ApplicationInitializer;
import org.apache.tapestry5.http.services.ApplicationInitializerFilter;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestHandler;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.http.services.ServletApplicationInitializer;
import org.apache.tapestry5.http.services.ServletApplicationInitializerFilter;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.PipelineBuilder;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.StrategyBuilder;
import org.slf4j.Logger;

/**
 * The Tapestry module for HTTP handling classes.
 */
public final class TapestryHttpModule {
    
    final private PropertyShadowBuilder shadowBuilder;
    final private RequestGlobals requestGlobals;
    final private PipelineBuilder pipelineBuilder;
    final private ApplicationGlobals applicationGlobals;
    
    public TapestryHttpModule(PropertyShadowBuilder shadowBuilder, 
            RequestGlobals requestGlobals, PipelineBuilder pipelineBuilder,
            ApplicationGlobals applicationGlobals) 
    {
        this.shadowBuilder = shadowBuilder;
        this.requestGlobals = requestGlobals;
        this.pipelineBuilder = pipelineBuilder;
        this.applicationGlobals = applicationGlobals;
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(RequestGlobals.class, RequestGlobalsImpl.class);
        binder.bind(ApplicationGlobals.class, ApplicationGlobalsImpl.class);
        binder.bind(TapestrySessionFactory.class, TapestrySessionFactoryImpl.class);
        binder.bind(BaseURLSource.class, BaseURLSourceImpl.class);
        binder.bind(ResponseCompressionAnalyzer.class, ResponseCompressionAnalyzerImpl.class);
    }
    
    /**
     * Contributes factory defaults that may be overridden.
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.SESSION_LOCKING_ENABLED, true);
        configuration.add(TapestryHttpSymbolConstants.CLUSTERED_SESSIONS, true);
        configuration.add(TapestryHttpSymbolConstants.CHARSET, "UTF-8");
        configuration.add(TapestryHttpSymbolConstants.APPLICATION_VERSION, "0.0.1");
        configuration.add(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED, true);
        configuration.add(TapestryHttpSymbolConstants.MIN_GZIP_SIZE, 100);
        
        // The default values denote "use values from request"
        configuration.add(TapestryHttpSymbolConstants.HOSTNAME, "");
        configuration.add(TapestryHttpSymbolConstants.HOSTPORT, 0);
        configuration.add(TapestryHttpSymbolConstants.HOSTPORT_SECURE, 0);
    }
    
    /**
     * Builds a shadow of the RequestGlobals.request property. Note again that
     * the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Request buildRequest(PropertyShadowBuilder shadowBuilder)
    {
        return shadowBuilder.build(requestGlobals, "request", Request.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.HTTPServletRequest property.
     * Generally, you should inject the {@link Request} service instead, as
     * future version of Tapestry may operate beyond just the servlet API.
     */
    public HttpServletRequest buildHttpServletRequest()
    {
        return shadowBuilder.build(requestGlobals, "HTTPServletRequest", HttpServletRequest.class);
    }

    /**
     * @since 5.1.0.0
     */
    public HttpServletResponse buildHttpServletResponse()
    {
        return shadowBuilder.build(requestGlobals, "HTTPServletResponse", HttpServletResponse.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.response property. Note again that
     * the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Response buildResponse()
    {
        return shadowBuilder.build(requestGlobals, "response", Response.class);
    }

    /**
     * Ordered contributions to the MasterDispatcher service allow different URL
     * matching strategies to occur.
     */
    @Marker(Primary.class)
    public Dispatcher buildMasterDispatcher(List<Dispatcher> configuration,
            ChainBuilder chainBuilder)
    {
        return chainBuilder.build(Dispatcher.class, configuration);
    }
    
    /**
     * The master SessionPersistedObjectAnalyzer.
     *
     * @since 5.1.0.0
     */
    @SuppressWarnings("rawtypes")
    @Marker(Primary.class)
    public SessionPersistedObjectAnalyzer buildSessionPersistedObjectAnalyzer(
            Map<Class, SessionPersistedObjectAnalyzer> configuration,
            StrategyBuilder strategyBuilder)
    {
        return strategyBuilder.build(SessionPersistedObjectAnalyzer.class, configuration);
    }

    /**
     * Identifies String, Number and Boolean as immutable objects, a catch-all
     * handler for Object (that understands
     * the {@link org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject} annotation),
     * and a handler for {@link org.apache.tapestry5.http.OptimizedSessionPersistedObject}.
     *
     * @since 5.1.0.0
     */
    @SuppressWarnings("rawtypes")
    public static void contributeSessionPersistedObjectAnalyzer(
            MappedConfiguration<Class, SessionPersistedObjectAnalyzer> configuration)
    {
        configuration.add(Object.class, new DefaultSessionPersistedObjectAnalyzer());

        SessionPersistedObjectAnalyzer<Object> immutable = new SessionPersistedObjectAnalyzer<Object>()
        {
            public boolean checkAndResetDirtyState(Object sessionPersistedObject)
            {
                return false;
            }
        };

        configuration.add(String.class, immutable);
        configuration.add(Number.class, immutable);
        configuration.add(Boolean.class, immutable);

        configuration.add(OptimizedSessionPersistedObject.class, new OptimizedSessionPersistedObjectAnalyzer());
    }

    /**
     * Initializes the application, using a pipeline of {@link org.apache.tapestry5.http.services.ApplicationInitializer}s.
     */
    @Marker(Primary.class)
    public ApplicationInitializer buildApplicationInitializer(Logger logger,
                                                              List<ApplicationInitializerFilter> configuration)
    {
        ApplicationInitializer terminator = new ApplicationInitializerTerminator();

        return pipelineBuilder.build(logger, ApplicationInitializer.class, ApplicationInitializerFilter.class,
                configuration, terminator);
    }

    public HttpServletRequestHandler buildHttpServletRequestHandler(Logger logger,

                                                                    List<HttpServletRequestFilter> configuration,

                                                                    @Primary
                                                                    RequestHandler handler,

                                                                    @Symbol(TapestryHttpSymbolConstants.CHARSET)
                                                                    String applicationCharset,

                                                                    TapestrySessionFactory sessionFactory)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandlerTerminator(handler, applicationCharset,
                sessionFactory);

        return pipelineBuilder.build(logger, HttpServletRequestHandler.class, HttpServletRequestFilter.class,
                configuration, terminator);
    }

    @Marker(Primary.class)
    public RequestHandler buildRequestHandler(Logger logger, List<RequestFilter> configuration,

                                              @Primary
                                              Dispatcher masterDispatcher)
    {
        RequestHandler terminator = new RequestHandlerTerminator(masterDispatcher);

        return pipelineBuilder.build(logger, RequestHandler.class, RequestFilter.class, configuration, terminator);
    }

    public ServletApplicationInitializer buildServletApplicationInitializer(Logger logger,
                                                                            List<ServletApplicationInitializerFilter> configuration,

                                                                            @Primary
                                                                            ApplicationInitializer initializer)
    {
        ServletApplicationInitializer terminator = new ServletApplicationInitializerTerminator(initializer);

        return pipelineBuilder.build(logger, ServletApplicationInitializer.class,
                ServletApplicationInitializerFilter.class, configuration, terminator);
    }
    
    /**
     * <dl>
     * <dt>StoreIntoGlobals</dt>
     * <dd>Stores the request and response into {@link org.apache.tapestry5.http.services.RequestGlobals} at the start of the
     * pipeline</dd>
     * <dt>IgnoredPaths</dt>
     * <dd>Identifies requests that are known (via the IgnoredPathsFilter service's configuration) to be mapped to other
     * applications</dd>
     * <dt>GZip</dt>
     * <dd>Handles GZIP compression of response streams (if supported by client)</dd>
     * </dl>
     */
    public void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,                         
            @Symbol(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED) boolean gzipCompressionEnabled, 
            @Autobuild GZipFilter gzipFilter)
    {
        
        HttpServletRequestFilter storeIntoGlobals = new HttpServletRequestFilter()
        {
            public boolean service(HttpServletRequest request, HttpServletResponse response,
                                   HttpServletRequestHandler handler) throws IOException
            {
                requestGlobals.storeServletRequestResponse(request, response);

                return handler.service(request, response);
            }
        };

        configuration.add("StoreIntoGlobals", storeIntoGlobals, "before:*");
        
        configuration.add("GZIP", gzipCompressionEnabled ? gzipFilter : null);
        
    }
    
    
    // A bunch of classes "promoted" from inline inner class to nested classes,
    // just so that the stack trace would be more readable. Most of these
    // are terminators for pipeline services.

    /**
     * @since 5.1.0.0
     */
    private class ApplicationInitializerTerminator implements ApplicationInitializer
    {
        public void initializeApplication(Context context)
        {
            applicationGlobals.storeContext(context);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class HttpServletRequestHandlerTerminator implements HttpServletRequestHandler
    {
        private final RequestHandler handler;
        private final String applicationCharset;
        private final TapestrySessionFactory sessionFactory;

        public HttpServletRequestHandlerTerminator(RequestHandler handler, String applicationCharset,
                                                   TapestrySessionFactory sessionFactory)
        {
            this.handler = handler;
            this.applicationCharset = applicationCharset;
            this.sessionFactory = sessionFactory;
        }

        public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
                throws IOException
        {
            requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

            // Should have started doing this a long time ago: recoding attributes into
            // the request for things that may be needed downstream, without having to extend
            // Request.

            servletRequest.setAttribute("servletAPI.protocol", servletRequest.getProtocol());
            servletRequest.setAttribute("servletAPI.characterEncoding", servletRequest.getCharacterEncoding());
            servletRequest.setAttribute("servletAPI.contentLength", servletRequest.getContentLength());
            servletRequest.setAttribute("servletAPI.authType", servletRequest.getAuthType());
            servletRequest.setAttribute("servletAPI.contentType", servletRequest.getContentType());
            servletRequest.setAttribute("servletAPI.scheme", servletRequest.getScheme());

            Request request = new RequestImpl(servletRequest, applicationCharset, sessionFactory);
            Response response = new ResponseImpl(servletRequest, servletResponse);

            // TAP5-257: Make sure that the "initial guess" for request/response
            // is available, even ifsome filter in the RequestHandler pipeline replaces them.
            // Which just goes to show that there should have been only one way to access the Request/Response:
            // either functionally (via parameters) or global (via ReqeuestGlobals) but not both.
            // That ship has sailed.

            requestGlobals.storeRequestResponse(request, response);

            // Transition from the Servlet API-based pipeline, to the
            // Tapestry-based pipeline.

            return handler.service(request, response);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class RequestHandlerTerminator implements RequestHandler
    {
        private final Dispatcher masterDispatcher;

        public RequestHandlerTerminator(Dispatcher masterDispatcher)
        {
            this.masterDispatcher = masterDispatcher;
        }

        public boolean service(Request request, Response response) throws IOException
        {
            // Update RequestGlobals with the current request/response (in case
            // some filter replaced the
            // normal set).
            requestGlobals.storeRequestResponse(request, response);

            return masterDispatcher.dispatch(request, response);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class ServletApplicationInitializerTerminator implements ServletApplicationInitializer
    {
        private final ApplicationInitializer initializer;

        public ServletApplicationInitializerTerminator(ApplicationInitializer initializer)
        {
            this.initializer = initializer;
        }

        public void initializeApplication(ServletContext servletContext)
        {
            applicationGlobals.storeServletContext(servletContext);

            // And now, down the (Web) ApplicationInitializer pipeline ...

            ContextImpl context = new ContextImpl(servletContext);

            applicationGlobals.storeContext(context);

            initializer.initializeApplication(context);
        }
    }

}
