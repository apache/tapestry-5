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

package org.apache.tapestry.services;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.StreamResponse;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.Translator;
import org.apache.tapestry.Validator;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.AfterRenderBody;
import org.apache.tapestry.annotations.AfterRenderTemplate;
import org.apache.tapestry.annotations.BeforeRenderBody;
import org.apache.tapestry.annotations.BeforeRenderTemplate;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.CleanupRender;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.PageAttached;
import org.apache.tapestry.annotations.PageDetached;
import org.apache.tapestry.annotations.PageLoaded;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.beaneditor.Validate;
import org.apache.tapestry.corelib.data.GridPagerPosition;
import org.apache.tapestry.dom.DefaultMarkupModel;
import org.apache.tapestry.dom.Document;
import org.apache.tapestry.grid.GridDataSource;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry.internal.bindings.BlockBindingFactory;
import org.apache.tapestry.internal.bindings.ComponentBindingFactory;
import org.apache.tapestry.internal.bindings.LiteralBindingFactory;
import org.apache.tapestry.internal.bindings.MessageBindingFactory;
import org.apache.tapestry.internal.bindings.TranslateBindingFactory;
import org.apache.tapestry.internal.bindings.ValidateBindingFactory;
import org.apache.tapestry.internal.grid.ListGridDataSource;
import org.apache.tapestry.internal.grid.NullDataSource;
import org.apache.tapestry.internal.services.ActionLinkHandler;
import org.apache.tapestry.internal.services.ApplicationGlobalsImpl;
import org.apache.tapestry.internal.services.ApplicationStateManagerImpl;
import org.apache.tapestry.internal.services.ApplicationStatePersistenceStrategySourceImpl;
import org.apache.tapestry.internal.services.ApplicationStateWorker;
import org.apache.tapestry.internal.services.AssetDispatcher;
import org.apache.tapestry.internal.services.AssetSourceImpl;
import org.apache.tapestry.internal.services.BeanModelSourceImpl;
import org.apache.tapestry.internal.services.BindingSourceImpl;
import org.apache.tapestry.internal.services.ClasspathAssetAliasManagerImpl;
import org.apache.tapestry.internal.services.CommonResourcesInjectionProvider;
import org.apache.tapestry.internal.services.ComponentActionDispatcher;
import org.apache.tapestry.internal.services.ComponentClassLocatorImpl;
import org.apache.tapestry.internal.services.ComponentClassResolverImpl;
import org.apache.tapestry.internal.services.ComponentInstanceResultProcessor;
import org.apache.tapestry.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.internal.services.ComponentLifecycleMethodWorker;
import org.apache.tapestry.internal.services.ComponentMessagesSourceImpl;
import org.apache.tapestry.internal.services.ComponentResourcesInjectionProvider;
import org.apache.tapestry.internal.services.ComponentSourceImpl;
import org.apache.tapestry.internal.services.ComponentWorker;
import org.apache.tapestry.internal.services.ContextImpl;
import org.apache.tapestry.internal.services.ContextPathSource;
import org.apache.tapestry.internal.services.CookieSink;
import org.apache.tapestry.internal.services.CookieSource;
import org.apache.tapestry.internal.services.CookiesImpl;
import org.apache.tapestry.internal.services.DefaultInjectionProvider;
import org.apache.tapestry.internal.services.DefaultValidationDelegateCommand;
import org.apache.tapestry.internal.services.EnvironmentImpl;
import org.apache.tapestry.internal.services.EnvironmentalWorker;
import org.apache.tapestry.internal.services.FieldValidatorDefaultSourceImpl;
import org.apache.tapestry.internal.services.FieldValidatorSourceImpl;
import org.apache.tapestry.internal.services.FlashPersistentFieldStrategy;
import org.apache.tapestry.internal.services.HeartbeatImpl;
import org.apache.tapestry.internal.services.InfrastructureImpl;
import org.apache.tapestry.internal.services.InfrastructureManagerImpl;
import org.apache.tapestry.internal.services.InjectAnonymousWorker;
import org.apache.tapestry.internal.services.InjectAssetWorker;
import org.apache.tapestry.internal.services.InjectBlockWorker;
import org.apache.tapestry.internal.services.InjectComponentWorker;
import org.apache.tapestry.internal.services.InjectNamedWorker;
import org.apache.tapestry.internal.services.InjectPageWorker;
import org.apache.tapestry.internal.services.InjectStandardStylesheetCommand;
import org.apache.tapestry.internal.services.InternalModule;
import org.apache.tapestry.internal.services.LinkActionResponseGenerator;
import org.apache.tapestry.internal.services.LinkFactory;
import org.apache.tapestry.internal.services.LocationRenderer;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.internal.services.MetaDataLocatorImpl;
import org.apache.tapestry.internal.services.MetaWorker;
import org.apache.tapestry.internal.services.MixinAfterWorker;
import org.apache.tapestry.internal.services.MixinWorker;
import org.apache.tapestry.internal.services.ObjectComponentEventResultProcessor;
import org.apache.tapestry.internal.services.OnEventWorker;
import org.apache.tapestry.internal.services.PageLifecycleAnnotationWorker;
import org.apache.tapestry.internal.services.PageLinkHandler;
import org.apache.tapestry.internal.services.PageRenderDispatcher;
import org.apache.tapestry.internal.services.PageRenderSupportImpl;
import org.apache.tapestry.internal.services.PageResponseRenderer;
import org.apache.tapestry.internal.services.ParameterWorker;
import org.apache.tapestry.internal.services.PersistWorker;
import org.apache.tapestry.internal.services.PersistentFieldManagerImpl;
import org.apache.tapestry.internal.services.PropertyConduitSourceImpl;
import org.apache.tapestry.internal.services.RenderCommandWorker;
import org.apache.tapestry.internal.services.RequestGlobalsImpl;
import org.apache.tapestry.internal.services.RequestImpl;
import org.apache.tapestry.internal.services.RequestPageCache;
import org.apache.tapestry.internal.services.RequestRenderer;
import org.apache.tapestry.internal.services.ResourceCache;
import org.apache.tapestry.internal.services.ResourceDigestGeneratorImpl;
import org.apache.tapestry.internal.services.ResourceStreamer;
import org.apache.tapestry.internal.services.ResponseImpl;
import org.apache.tapestry.internal.services.RetainWorker;
import org.apache.tapestry.internal.services.RootPathDispatcher;
import org.apache.tapestry.internal.services.SessionApplicationStatePersistenceStrategy;
import org.apache.tapestry.internal.services.SessionHolder;
import org.apache.tapestry.internal.services.SessionPersistentFieldStrategy;
import org.apache.tapestry.internal.services.StaticFilesFilter;
import org.apache.tapestry.internal.services.StreamResponseResultProcessor;
import org.apache.tapestry.internal.services.StringResultProcessor;
import org.apache.tapestry.internal.services.SupportsInformalParametersWorker;
import org.apache.tapestry.internal.services.TranslatorDefaultSourceImpl;
import org.apache.tapestry.internal.services.TranslatorSourceImpl;
import org.apache.tapestry.internal.services.UnclaimedFieldWorker;
import org.apache.tapestry.internal.services.UpdateListenerHub;
import org.apache.tapestry.internal.services.ValidationConstraintGeneratorImpl;
import org.apache.tapestry.internal.services.ValidationMessagesSourceImpl;
import org.apache.tapestry.internal.structure.DefaultComponentParameterBindingSourceImpl;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Lifecycle;
import org.apache.tapestry.ioc.annotations.SubModule;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ChainBuilder;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.Coercion;
import org.apache.tapestry.ioc.services.CoercionTuple;
import org.apache.tapestry.ioc.services.PipelineBuilder;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry.ioc.services.StrategyBuilder;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.translator.DoubleTranslator;
import org.apache.tapestry.translator.IntegerTranslator;
import org.apache.tapestry.translator.LongTranslator;
import org.apache.tapestry.translator.StringTranslator;
import org.apache.tapestry.util.StringToEnumCoercion;
import org.apache.tapestry.validator.Max;
import org.apache.tapestry.validator.MaxLength;
import org.apache.tapestry.validator.Min;
import org.apache.tapestry.validator.MinLength;
import org.apache.tapestry.validator.Required;

/**
 * The root module for Tapestry.
 */
@Id("tapestry")
@SubModule(InternalModule.class)
public final class TapestryModule
{
    private final ChainBuilder _chainBuilder;

    private final PipelineBuilder _pipelineBuilder;

    private final RequestGlobals _requestGlobals;

    private final ApplicationGlobals _applicationGlobals;

    private final PropertyShadowBuilder _shadowBuilder;

    private final RequestPageCache _requestPageCache;

    private final PageResponseRenderer _pageResponseRenderer;

    private final Request _request;

    private final Environment _environment;

    private final StrategyBuilder _strategyBuilder;

    // Primarily used as a InvalidationEventHub for service implementations
    // that should clear their cache when the underlying component class loader
    // is discarded.

    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final LinkFactory _linkFactory;

    private final PropertyAccess _propertyAccess;

    private final PropertyConduitSource _propertyConduitSource;

    private final ClassFactory _componentClassFactory;

    // Yes, you can inject services defined by this module into this module. The service proxy is
    // created without instantiating the module itself. We're careful about making as many
    // service builder and contributor methods static as possible to avoid recursive build
    // exceptions.

    public TapestryModule(@InjectService("tapestry.ioc.PipelineBuilder")
    PipelineBuilder pipelineBuilder,

    @InjectService("tapestry.ioc.PropertyShadowBuilder")
    PropertyShadowBuilder shadowBuilder,

    @Inject("infrastructure:RequestGlobals")
    RequestGlobals requestGlobals,

    @Inject("infrastructure:ApplicationGlobals")
    ApplicationGlobals applicationGlobals,

    @InjectService("tapestry.ioc.ChainBuilder")
    ChainBuilder chainBuilder,

    @InjectService("tapestry.internal.RequestPageCache")
    RequestPageCache requestPageCache,

    @InjectService("tapestry.internal.PageResponseRenderer")
    PageResponseRenderer pageResponseRenderer,

    @Inject("infrastructure:Request")
    Request request,

    @Inject("infrastructure:Environment")
    Environment environment,

    @InjectService("tapestry.ioc.StrategyBuilder")
    StrategyBuilder strategyBuilder,

    @InjectService("tapestry.internal.ComponentInstantiatorSource")
    ComponentInstantiatorSource componentInstantiatorSource,

    @InjectService("tapestry.internal.LinkFactory")
    LinkFactory linkFactory,

    @Inject("infrastructure:PropertyConduitSource")
    PropertyConduitSource propertyConduitSource,

    @Inject("infrastructure:PropertyAccess")
    PropertyAccess propertyAccess,

    @InjectService("ComponentClassFactory")
    ClassFactory componentClassFactory)
    {
        _pipelineBuilder = pipelineBuilder;
        _shadowBuilder = shadowBuilder;
        _requestGlobals = requestGlobals;
        _applicationGlobals = applicationGlobals;
        _chainBuilder = chainBuilder;
        _requestPageCache = requestPageCache;
        _pageResponseRenderer = pageResponseRenderer;
        _request = request;
        _environment = environment;
        _strategyBuilder = strategyBuilder;
        _componentInstantiatorSource = componentInstantiatorSource;
        _linkFactory = linkFactory;
        _propertyAccess = propertyAccess;
        _propertyConduitSource = propertyConduitSource;
        _componentClassFactory = componentClassFactory;
    }

    /**
     * Invoked from
     * {@link #contributeInfrastructure(Configuration, ServiceLocator, Request, Response, TypeCoercer)}
     * to contribute services from the tapestry module where the unqualified class name of the
     * service interface matches the unqualified service id. This unqualified name is used as the
     * infrastructure alias.
     */
    @SuppressWarnings("unchecked")
    private static void add(Configuration<InfrastructureContribution> configuration,
            ServiceLocator locator, Class... serviceInterfaces)
    {
        for (Class serviceInterface : serviceInterfaces)
        {
            String serviceId = IOCUtilities.toSimpleId(serviceInterface.getName());

            Object service = locator.getService(serviceId, serviceInterface);

            InfrastructureContribution contribution = new InfrastructureContribution(serviceId,
                    service);

            configuration.add(contribution);
        }
    }

    public static ApplicationGlobals buildApplicationGlobals()
    {
        return new ApplicationGlobalsImpl();
    }

    public Context buildContext(@InjectService("ApplicationGlobals")
    ApplicationGlobals globals)
    {
        return _shadowBuilder.build(globals, "context", Context.class);
    }

    public ServletApplicationInitializer buildServletApplicationInitializer(Log log,
            List<ServletApplicationInitializerFilter> configuration,
            @InjectService("ApplicationInitializer")
            final ApplicationInitializer initializer)
    {
        ServletApplicationInitializer terminator = new ServletApplicationInitializer()
        {
            public void initializeApplication(ServletContext context)
            {
                _applicationGlobals.store(context);

                // And now, down the (Web) ApplicationInitializer pipeline ...

                initializer.initializeApplication(new ContextImpl(context));
            }
        };

        return _pipelineBuilder.build(
                log,
                ServletApplicationInitializer.class,
                ServletApplicationInitializerFilter.class,
                configuration,
                terminator);
    }

    /** Initializes the application. */
    public ApplicationInitializer buildApplicationInitializer(Log log,
            List<ApplicationInitializerFilter> configuration)
    {
        ApplicationInitializer terminator = new ApplicationInitializer()
        {
            public void initializeApplication(Context context)
            {
                _applicationGlobals.store(context);
            }
        };

        return _pipelineBuilder.build(
                log,
                ApplicationInitializer.class,
                ApplicationInitializerFilter.class,
                configuration,
                terminator);
    }

    /**
     * Allows the exact steps in the component class transformation process to be defined.
     */
    public ComponentClassTransformWorker buildComponentClassTransformWorker(
            List<ComponentClassTransformWorker> configuration)
    {
        return _chainBuilder.build(ComponentClassTransformWorker.class, configuration);
    }

    public HttpServletRequestHandler buildHttpServletRequestHandler(Log log,
            List<HttpServletRequestFilter> configuration,

            @InjectService("RequestHandler")
            final RequestHandler handler)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandler()
        {
            public boolean service(HttpServletRequest request, HttpServletResponse response)
                    throws IOException
            {
                _requestGlobals.store(request, response);

                return handler.service(new RequestImpl(request), new ResponseImpl(response));
            }
        };

        return _pipelineBuilder.build(
                log,
                HttpServletRequestHandler.class,
                HttpServletRequestFilter.class,
                configuration,
                terminator);
    }

    public static Infrastructure buildInfrastructure(Log log,
            Collection<InfrastructureContribution> configuration)
    {
        InfrastructureManager manager = new InfrastructureManagerImpl(log, configuration);

        return new InfrastructureImpl(manager);
    }

    public static MarkupWriterFactory buildMarkupWriterFactory(
            @InjectService("tapestry.internal.ComponentInvocationMap")
            final ComponentInvocationMap componentInvocationMap)
    {
        // Temporary ...
        return new MarkupWriterFactory()
        {
            public MarkupWriter newMarkupWriter()
            {
                return new MarkupWriterImpl(new DefaultMarkupModel(), componentInvocationMap);
            }
        };
    }

    /**
     * Ordered contributions to the MasterDispatcher service allow different URL matching strategies
     * to occur.
     */
    public Dispatcher buildMasterDispatcher(List<Dispatcher> configuration)
    {
        return _chainBuilder.build(Dispatcher.class, configuration);
    }

    @Lifecycle("perthread")
    public static RequestGlobals buildRequestGlobals()
    {
        return new RequestGlobalsImpl();
    }

    /**
     * Builds a shadow of the RequestGlobals.request property. Note again that the shadow can be an
     * ordinary singleton, even though RequestGlobals is perthread.
     */
    public Request buildRequest()
    {
        return _shadowBuilder.build(_requestGlobals, "request", Request.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.response property. Note again that the shadow can be an
     * ordinary singleton, even though RequestGlobals is perthread.
     */
    public Response buildResponse()
    {
        return _shadowBuilder.build(_requestGlobals, "response", Response.class);
    }

    public RequestHandler buildRequestHandler(Log log, List<RequestFilter> configuration,
            @InjectService("MasterDispatcher")
            final Dispatcher masterDispatcher)
    {
        RequestHandler terminator = new RequestHandler()
        {
            public boolean service(Request request, Response response) throws IOException
            {
                _requestGlobals.store(request, response);

                return masterDispatcher.dispatch(request, response);
            }
        };

        return _pipelineBuilder.build(
                log,
                RequestHandler.class,
                RequestFilter.class,
                configuration,
                terminator);
    }

    /**
     * Contributes filter "tapestry.StaticFilesFilter" that identifies requests for static resources
     * and terminates the pipeline by returning false. Generally, most filters should be ordered
     * after this filter.
     */
    public static void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("Context")
            Context context,

            @Inject("infrastructure:RequestExceptionHandler")
            final RequestExceptionHandler exceptionHandler)
    {
        RequestFilter staticFilesFilter = new StaticFilesFilter(context);

        configuration.add("StaticFiles", staticFilesFilter);

        RequestFilter errorFilter = new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler)
                    throws IOException
            {
                try
                {
                    return handler.service(request, response);
                }
                catch (IOException ex)
                {
                    // Pass it through.
                    throw ex;
                }
                catch (Throwable ex)
                {
                    exceptionHandler.handleRequestException(ex);

                    // We assume a reponse has been sent and there's no need to handle the request
                    // further.

                    return true;
                }
            }
        };

        configuration.add("ErrorFilter", errorFilter);
    }

    /**
     * Contributes pretty much everything in the tapestry module, plus PropertyAccess and
     * TypeCoercer.
     */
    public static void contributeInfrastructure(
            Configuration<InfrastructureContribution> configuration,

            ServiceLocator locator,

            @InjectService("tapestry.ioc.TypeCoercer")
            TypeCoercer typeCoercer,

            @InjectService("tapestry.ioc.PropertyAccess")
            PropertyAccess propertyAccess)
    {
        add(
                configuration,
                locator,
                ApplicationGlobals.class,
                ApplicationStateManager.class,
                ApplicationStatePersistenceStrategySource.class,
                AssetSource.class,
                BeanModelSource.class,
                BindingSource.class,
                ClasspathAssetAliasManager.class,
                ComponentClassResolver.class,
                ComponentEventResultProcessor.class,
                ComponentMessagesSource.class,
                ComponentSource.class,
                Context.class,
                DefaultComponentParameterBindingSource.class,
                Environment.class,
                FieldValidatorDefaultSource.class,
                FieldValidatorSource.class,
                MarkupWriterFactory.class,
                MetaDataLocator.class,
                ObjectRenderer.class,
                PersistentFieldManager.class,
                PropertyConduitSource.class,
                Request.class,
                RequestGlobals.class,
                ResourceDigestGenerator.class,
                Response.class,
                TranslatorDefaultSource.class,
                TranslatorSource.class,
                ValidationConstraintGenerator.class,
                ValidationMessagesSource.class);

        configuration.add(new InfrastructureContribution("TypeCoercer", typeCoercer));
        configuration.add(new InfrastructureContribution("PropertyAccess", propertyAccess));
    }

    /**
     * Contributes the {@link ObjectProvider} provided by {@link Infrastructure#getObjectProvider()}
     * mapped to the provider prefix "infrastructure".
     */
    @Contribute("tapestry.ioc.MasterObjectProvider")
    public static void contributeInfrastructureToMasterObjectProvider(
            MappedConfiguration<String, ObjectProvider> configuration,

            @InjectService("Infrastructure")
            final Infrastructure infrastructure)
    {
        // There's a nasty web of dependencies related to Infrastructure; this wrapper class lets us
        // defer instantiating the Infrastructure implementation just long enough to defuse those
        // dependencies.

        ObjectProvider wrapper = new ObjectProvider()
        {
            public <T> T provide(String expression, Class<T> objectType, ServiceLocator locator)
            {
                return infrastructure.getObjectProvider().provide(expression, objectType, locator);
            }
        };

        // Or you can defuse the dependency by using @InjectService("foo") instead of
        // @Inject("service:foo"). The latter requires the MasterObjectProvider, which requires
        // the Infrastructure, which then fails if any contribution
        // to infrastructure makes use of @Inject. However, since its likely that end users will try
        // to do this, the wrapper has been left in place (it does very little harm).

        configuration.add("infrastructure", wrapper);
    }

    public void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,
            @Inject("infrastructure:ClasspathAssetAliasManager")
            ClasspathAssetAliasManager aliasManager,

            @InjectService("tapestry.internal.ResourceCache")
            ResourceCache resourceCache,

            @InjectService("tapestry.internal.ResourceStreamer")
            ResourceStreamer streamer,

            @InjectService("tapestry.internal.PageLinkHandler")
            PageLinkHandler pageLinkHandler,

            @InjectService("tapestry.internal.ActionLinkHandler")
            ActionLinkHandler actionLinkHandler,

            @InjectService("tapestry.ComponentClassResolver")
            ComponentClassResolver componentClassResolver,

            @Inject("${tapestry.start-page-name}")
            String startPageName)
    {
        // Looks for the root path and renders the start page

        configuration.add("RootPath", new RootPathDispatcher(componentClassResolver,
                pageLinkHandler, _pageResponseRenderer, startPageName), "before:Asset");

        // This goes first because an asset to be streamed may have an file extension, such as
        // ".html", that will confuse the later dispatchers.

        configuration.add(
                "Asset",
                new AssetDispatcher(streamer, aliasManager, resourceCache),
                "before:PageRender");

        configuration.add("PageRender", new PageRenderDispatcher(componentClassResolver,
                pageLinkHandler, _pageResponseRenderer));

        configuration.add(
                "ComponentAction",
                new ComponentActionDispatcher(actionLinkHandler),
                "after:PageRender");
    }

    public ComponentClassResolver buildComponentClassResolver(
            Collection<LibraryMapping> configuration)
    {
        ComponentClassResolverImpl service = new ComponentClassResolverImpl(
                _componentInstantiatorSource, new ComponentClassLocatorImpl(), configuration);

        // Allow the resolver to clean its cache when the source is invalidated

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("core", "org.apache.tapestry.corelib"));
    }

    public static BindingSource buildBindingSource(Map<String, BindingFactory> configuration)
    {
        return new BindingSourceImpl(configuration);
    }

    /**
     * Contributes the factory for serveral built-in binding prefixes ("literal", prop", "block",
     * "component" "message", "validate", "translate").
     */
    public static void contributeBindingSource(
            MappedConfiguration<String, BindingFactory> configuration,

            @InjectService("tapestry.internal.PropBindingFactory")
            BindingFactory propBindingFactory,

            @Inject("infrastructure:FieldValidatorSource")
            FieldValidatorSource fieldValidatorSource,

            @Inject("infrastructure:TranslatorSource")
            TranslatorSource translatorSource)
    {
        configuration.add(TapestryConstants.LITERAL_BINDING_PREFIX, new LiteralBindingFactory());
        configuration.add(TapestryConstants.PROP_BINDING_PREFIX, propBindingFactory);
        configuration.add("component", new ComponentBindingFactory());
        configuration.add("message", new MessageBindingFactory());
        configuration.add("validate", new ValidateBindingFactory(fieldValidatorSource));
        configuration.add("translate", new TranslateBindingFactory(translatorSource));
        configuration.add("block", new BlockBindingFactory());
    }

    /**
     * Returns a {@link ClassFactory} that can be used to create extra classes around component
     * classes. This ClassFactory will be cleared whenever an underlying component class is
     * discovered to have changed. Use of this class factory implies that your code will become
     * aware of this (if necessary) to discard any cached object (alas, this currently involves
     * dipping into the internals side to register for the correct notifications). Failure to
     * properly clean up can result in really nasty PermGen space memory leaks.
     */
    public ClassFactory buildComponentClassFactory()
    {
        return _shadowBuilder.build(
                _componentInstantiatorSource,
                "classFactory",
                ClassFactory.class);
    }

    /**
     * A chain of command for providing values for {@link org.apache.tapestry.annotations.Inject}-ed
     * fields in component classes. The service's configuration can be extended to allow for
     * different automatic injections (based on some combination of field type and field name).
     */

    public InjectionProvider buildInjectionProvider(List<InjectionProvider> configuration)
    {
        return _chainBuilder.build(InjectionProvider.class, configuration);
    }

    /**
     * Contributes the elemental providers:
     * <ul>
     * <li>ComponentResources -- give component access to its resources</li>
     * <li>CommonResources -- access to properties of resources (log, messages, etc.)</li>
     * <li>Default -- looks for a unique IoC service that matches the field type</li>
     * </ul>
     */
    public static void contributeInjectionProvider(
            OrderedConfiguration<InjectionProvider> configuration)
    {
        configuration.add("ComponentResources", new ComponentResourcesInjectionProvider());
        configuration.add("CommonResources", new CommonResourcesInjectionProvider());
        configuration.add("Default", new DefaultInjectionProvider(), "after:*.*");
    }

    /**
     * Adds a number of standard component class transform workers:
     * <ul>
     * <li>Retain -- allows fields to retain their values between requests</li>
     * <li>Persist -- allows fields to store their their value persistently between requests</li>
     * <li>Parameter -- identifies parameters based on the
     * {@link org.apache.tapestry.annotations.Parameter} annotation</li>
     * <li>Component -- identifies embedded components based on the
     * {@link org.apache.tapestry.annotations.Component} annotation</li>
     * <li>Mixin -- adds a mixin as part of a component's implementation</li>
     * <li>Environment -- allows fields to contain values extracted from the {@link Environment}
     * service</li>
     * <li>InjectNamed -- used with the {@link Inject} annotation, when a value is supplied</li>
     * <li>InjectAnnonymous -- used with the {@link Inject} annotation, when no value is supplied</li>
     * <li>InjectPage -- adds code to allow access to other pages via the {@link InjectPage} field
     * annotation</li>
     * <li>InjectBlock -- allows a block from the template to be injected into a field</li>
     * <li>SupportsInformalParameters -- checks for the annotation</li>
     * <li>Meta -- checks for meta data and adds it to the component model
     * <li>ApplicationState -- converts fields that reference application state objects
     * <li>UnclaimedField -- identifies unclaimed fields and resets them to null/0/false at the end
     * of the request</li>
     * <li>RenderCommand -- ensures all components also implement {@link RenderCommand}</li>
     * <li>SetupRender, BeginRender, etc. -- correspond to component render phases and annotations</li>
     * </ul>
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration,

            ServiceLocator locator,

            @InjectService("tapestry.ioc.MasterObjectProvider")
            ObjectProvider objectProvider,

            @InjectService("InjectionProvider")
            InjectionProvider injectionProvider,

            @Inject("infrastructure:Environment")
            Environment environment,

            @Inject("infrastructure:ComponentClassResolver")
            ComponentClassResolver resolver,

            @InjectService("tapestry.internal.RequestPageCache")
            RequestPageCache requestPageCache,

            @Inject("infrastructure:AssetSource")
            AssetSource assetSource,

            @Inject("infrastructure:BindingSource")
            BindingSource bindingsource,

            @Inject("infrastructure:ApplicationStateManager")
            ApplicationStateManager applicationStateManager)
    {
        // TODO: Proper scheduling of all of this. Since a given field or method should
        // only have a single annotation, the order doesn't matter so much, as long as
        // UnclaimedField is last.

        configuration.add("Meta", new MetaWorker());
        configuration.add("ApplicationState", new ApplicationStateWorker(applicationStateManager));
        configuration.add("InjectNamed", new InjectNamedWorker(objectProvider, locator));
        configuration.add(
                "InjectAnonymous",
                new InjectAnonymousWorker(locator, injectionProvider),
                "after:InjectNamed");
        configuration.add("InjectAsset", new InjectAssetWorker(assetSource), "before:InjectNamed");
        configuration.add("InjectBlock", new InjectBlockWorker(), "before:InjectNamed");

        configuration.add("MixinAfter", new MixinAfterWorker());
        configuration.add("Component", new ComponentWorker(resolver));
        configuration.add("Environment", new EnvironmentalWorker(environment));
        configuration.add("Mixin", new MixinWorker(resolver));
        configuration.add("OnEvent", new OnEventWorker());
        configuration.add("SupportsInformalParameters", new SupportsInformalParametersWorker());
        configuration.add("InjectPage", new InjectPageWorker(requestPageCache));
        configuration.add("InjectComponent", new InjectComponentWorker());
        configuration.add("RenderCommand", new RenderCommandWorker());

        // Default values for parameters are often some form of injection, so make sure
        // that Parameter fields are processed after injections.

        configuration.add("Parameter", new ParameterWorker(bindingsource), "after:Inject*");

        // Workers for the component rendering state machine methods; this is in typical
        // execution order.

        add(configuration, TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);
        add(configuration, TransformConstants.BEGIN_RENDER_SIGNATURE, BeginRender.class, false);
        add(
                configuration,
                TransformConstants.BEFORE_RENDER_TEMPLATE_SIGNATURE,
                BeforeRenderTemplate.class,
                false);
        add(
                configuration,
                TransformConstants.BEFORE_RENDER_BODY_SIGNATURE,
                BeforeRenderBody.class,
                false);

        // These phases operate in reverse order.

        add(
                configuration,
                TransformConstants.AFTER_RENDER_BODY_SIGNATURE,
                AfterRenderBody.class,
                true);
        add(
                configuration,
                TransformConstants.AFTER_RENDER_TEMPLATE_SIGNATURE,
                AfterRenderTemplate.class,
                true);
        add(configuration, TransformConstants.AFTER_RENDER_SIGNATURE, AfterRender.class, true);
        add(configuration, TransformConstants.CLEANUP_RENDER_SIGNATURE, CleanupRender.class, true);

        // Ideally, these should be ordered pretty late in the process to make sure there are no
        // side effects
        // with other workers that do work inside the page lifecycle methods.

        add(
                configuration,
                PageLoaded.class,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "pageLoaded");
        add(
                configuration,
                PageAttached.class,
                TransformConstants.CONTAINING_PAGE_DID_ATTACH_SIGNATURE,
                "pageAttached");
        add(
                configuration,
                PageDetached.class,
                TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE,
                "pageDetached");

        configuration.add("Retain", new RetainWorker());
        configuration.add("Persist", new PersistWorker());
        configuration.add("UnclaimedField", new UnclaimedFieldWorker(), "after:*.*");
    }

    private static void add(OrderedConfiguration<ComponentClassTransformWorker> configuration,
            MethodSignature signature, Class<? extends Annotation> annotationClass, boolean reverse)
    {
        // make the name match the annotation class name.

        String name = IOCUtilities.toSimpleId(annotationClass.getName());

        configuration.add(name, new ComponentLifecycleMethodWorker(signature, annotationClass,
                reverse));
    }

    private static void add(OrderedConfiguration<ComponentClassTransformWorker> configuration,
            Class<? extends Annotation> annotationClass, MethodSignature lifecycleMethodSignature,
            String methodAlias)
    {
        ComponentClassTransformWorker worker = new PageLifecycleAnnotationWorker(annotationClass,
                lifecycleMethodSignature, methodAlias);

        String name = IOCUtilities.toSimpleId(annotationClass.getName());

        configuration.add(name, worker);
    }

    @Lifecycle("perthread")
    public static Environment buildEnvironment()
    {
        return new EnvironmentImpl();
    }

    /**
     * Controls setup and cleanup of the environment during page rendering (the generation of a
     * markup stream response for the client web browser).
     */
    public PageRenderInitializer buildPageRenderInitializer(
            final List<PageRenderCommand> configuration)
    {
        return new PageRenderInitializer()
        {
            public void setup(MarkupWriter writer)
            {
                _environment.clear();

                _environment.push(MarkupWriter.class, writer);
                _environment.push(Document.class, writer.getDocument());

                for (PageRenderCommand command : configuration)
                    command.setup(_environment);
            }

            public void cleanup(MarkupWriter writer)
            {
                Iterator<PageRenderCommand> i = InternalUtils.reverseIterator(configuration);

                while (i.hasNext())
                    i.next().cleanup(_environment);

                _environment.clear();
            }
        };
    }

    public static void contributePageRenderInitializer(
            OrderedConfiguration<PageRenderCommand> configuration,

            @InjectService("tapestry.ioc.ThreadLocale")
            ThreadLocale threadLocale,

            @Inject("infrastructure:AssetSource")
            AssetSource assetSource)
    {
        configuration.add("PageRenderSupport", new PageRenderCommand()
        {
            public void setup(Environment environment)
            {
                environment.push(PageRenderSupport.class, new PageRenderSupportImpl());
            }

            public void cleanup(Environment environment)
            {
                environment.pop(PageRenderSupport.class);
            }
        });

        configuration.add("Heartbeat", new PageRenderCommand()
        {
            public void setup(Environment environment)
            {
                HeartbeatImpl heartbeat = new HeartbeatImpl();

                heartbeat.begin();

                environment.push(Heartbeat.class, heartbeat);
            }

            public void cleanup(Environment environment)
            {
                environment.pop(Heartbeat.class).end();
            }
        });

        configuration.add("InjectStandardStylesheet", new InjectStandardStylesheetCommand(
                threadLocale, assetSource));
        configuration.add("DefaultValidationDelegate", new DefaultValidationDelegateCommand());
    }

    /** A public service since extensions may provide new persistent strategies. */
    public static PersistentFieldManager buildPersistentFieldManager(
            @Inject("infrastructure:MetaDataLocator")
            MetaDataLocator locator,

            Map<String, PersistentFieldStrategy> configuration)
    {
        return new PersistentFieldManagerImpl(locator, configuration);
    }

    /**
     * Contributes the "session" strategy.
     */
    public void contributePersistentFieldManager(
            MappedConfiguration<String, PersistentFieldStrategy> configuration,

            @InjectService("tapestry.internal.SessionHolder")
            SessionHolder sessionHolder)
    {
        configuration.add("session", new SessionPersistentFieldStrategy(sessionHolder));
        configuration.add("flash", new FlashPersistentFieldStrategy(sessionHolder));
    }

    public ComponentSource buildComponentSource(
            @InjectService("tapestry.internal.RequestPageCache")
            RequestPageCache pageCache)
    {
        return new ComponentSourceImpl(pageCache);
    }

    public ComponentMessagesSource buildComponentMessagesSource(
            @InjectService("tapestry.internal.UpdateListenerHub")
            UpdateListenerHub updateListenerHub)
    {
        ComponentMessagesSourceImpl service = new ComponentMessagesSourceImpl();

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    /**
     * Builds the source of {@link Messages} containing validation messages. The contributions are
     * paths to message bundles (resource paths within the classpath); the default contribution is
     * "org/apache/tapestry/internal/ValidationMessages".
     */
    public ValidationMessagesSource buildValidationMessagesSource(Collection<String> configuration,
            @InjectService("tapestry.internal.UpdateListenerHub")
            UpdateListenerHub updateListenerHub,

            @InjectService("tapestry.internal.ClasspathAssetFactory")
            AssetFactory classpathAssetFactory)
    {
        ValidationMessagesSourceImpl service = new ValidationMessagesSourceImpl(configuration,
                classpathAssetFactory.getRootResource());

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public void contributeValidationMessagesSource(Configuration<String> configuration)
    {
        configuration.add("org/apache/tapestry/internal/ValidationMessages");
    }

    public static AssetSource buildAssetSource(Map<String, AssetFactory> configuration)
    {
        return new AssetSourceImpl(configuration);
    }

    public void contributeAssetSource(MappedConfiguration<String, AssetFactory> configuration,
            @InjectService("tapestry.internal.ContextAssetFactory")
            AssetFactory contextAssetFactory,

            @InjectService("tapestry.internal.ClasspathAssetFactory")
            AssetFactory classpathAssetFactory)
    {
        configuration.add("context", contextAssetFactory);
        configuration.add("classpath", classpathAssetFactory);
    }

    public static ResourceDigestGenerator buildResourceDigestGenerator()
    {
        return new ResourceDigestGeneratorImpl();
    }

    public static ClasspathAssetAliasManager buildClasspathAssetAliasManager(
            @InjectService("tapestry.internal.ContextPathSource")
            ContextPathSource contextPathSource, Map<String, String> configuration)
    {
        return new ClasspathAssetAliasManagerImpl(contextPathSource, configuration);
    }

    public static void contributeClasspathAssetAliasManager(
            MappedConfiguration<String, String> configuration)
    {
        configuration.add("tapestry/", "org/apache/tapestry/");
    }

    public static FieldValidatorSource buildFieldValidatorSource(
            @Inject("infrastructure:ValidationMessagesSource")
            ValidationMessagesSource messagesSource,

            @Inject("infrastructure:TypeCoercer")
            TypeCoercer typeCoercer,

            Map<String, Validator> configuration)
    {
        return new FieldValidatorSourceImpl(messagesSource, typeCoercer, configuration);
    }

    /**
     * Contributes the basic set of validators:
     * <ul>
     * <li>required</li>
     * <li>minlength</li>
     * <li>maxlength</li>
     * <li>min</li>
     * <li>max</li>
     * </ul>
     */
    public static void contributeFieldValidatorSource(
            MappedConfiguration<String, Validator> configuration)
    {
        configuration.add("required", new Required());
        configuration.add("minlength", new MinLength());
        configuration.add("maxlength", new MaxLength());
        configuration.add("min", new Min());
        configuration.add("max", new Max());
    }

    public static TranslatorSource buildTranslatorSource(Map<String, Translator> configuration)
    {
        return new TranslatorSourceImpl(configuration);
    }

    public TranslatorDefaultSource buildTranslatorDefaultSource(Map<Class, Translator> configuration)
    {
        TranslatorDefaultSourceImpl service = new TranslatorDefaultSourceImpl(configuration);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Contributes the basic set of named translators:
     * <ul>
     * <li>integer</li>
     * <li>string</li>
     * <li>long</li>
     * <li>double</li>
     * </ul>
     */
    public static void contributeTranslatorSource(
            MappedConfiguration<String, Translator> configuration)
    {
        // Fortunately, the translators are tiny, so we don't have to worry about the slight
        // duplication between this and TranslatorDefaultSource, though it is a pain to keep the two
        // organized (perhaps they should be joined together into a single service, where we
        // identify a name and a match type).

        configuration.add("integer", new IntegerTranslator());
        configuration.add("string", new StringTranslator());
        configuration.add("long", new LongTranslator());
        configuration.add("double", new DoubleTranslator());
    }

    /**
     * Contributes the basic set of default translators:
     * <ul>
     * <li>Integer</li>
     * <li>String</li>
     * <li>Long</li>
     * <li>Double</li>
     * </li>
     */
    public static void contributeTranslatorDefaultSource(
            MappedConfiguration<Class, Translator> configuration)
    {
        configuration.add(Integer.class, new IntegerTranslator());
        configuration.add(String.class, new StringTranslator());
        configuration.add(Long.class, new LongTranslator());
        configuration.add(Double.class, new DoubleTranslator());
    }

    /**
     * Adds coercions:
     * <ul>
     * <li>String to {@link SelectModel}
     * <li>Map to {@link SelectModel}
     * <li>List to {@link GridDataSource}
     * <li>null to {@link GridDataSource}
     * <li>String to {@link GridPagerPosition}
     * </ul>
     */
    @Contribute("tapestry.ioc.TypeCoercer")
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
        add(configuration, String.class, SelectModel.class, new Coercion<String, SelectModel>()
        {
            public SelectModel coerce(String input)
            {
                return TapestryUtils.toSelectModel(input);
            }
        });

        add(configuration, Map.class, SelectModel.class, new Coercion<Map, SelectModel>()
        {
            public SelectModel coerce(Map input)
            {
                return TapestryUtils.toSelectModel(input);
            }
        });

        add(configuration, List.class, GridDataSource.class, new Coercion<List, GridDataSource>()
        {
            public GridDataSource coerce(List input)
            {
                return new ListGridDataSource(input);
            }
        });

        add(configuration, void.class, GridDataSource.class, new Coercion<Void, GridDataSource>()
        {
            private final GridDataSource _source = new NullDataSource();

            public GridDataSource coerce(Void input)
            {
                return _source;
            }
        });

        add(
                configuration,
                String.class,
                GridPagerPosition.class,
                new StringToEnumCoercion<GridPagerPosition>(GridPagerPosition.class));
    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType,
            Class<T> targetType, Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = new CoercionTuple<S, T>(sourceType, targetType, coercion);

        configuration.add(tuple);
    }

    public ComponentEventResultProcessor buildComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration)
    {
        // A slight hack!

        configuration.put(Object.class, new ObjectComponentEventResultProcessor(configuration
                .keySet()));

        StrategyRegistry<ComponentEventResultProcessor> registry = StrategyRegistry.newInstance(
                ComponentEventResultProcessor.class,
                configuration);

        return _strategyBuilder.build(registry);
    }

    /**
     * Contributes handlers for the following types:
     * <dl>
     * <dt>Object</dt>
     * <dd>Failure case, added to provide a more useful exception message</dd>
     * <dt>ActionResponseGenerator</dt>
     * <dd>Returns the ActionResponseGenerator; this sometimes occurs when a component generates
     * events whose return values are converted to ActionResponseGenerators (this handles that
     * bubble up case).</dd>
     * <dt>Link</dt>
     * <dd>Wraps the Link to send a redirect</dd>
     * <dt>String</dt>
     * <dd>The name of the page to render the response (after a redirect)</dd>
     * </dl>
     */
    public void contributeComponentEventResultProcessor(
            @InjectService("ComponentInstanceResultProcessor")
            ComponentEventResultProcessor componentInstanceProcessor,

            MappedConfiguration<Class, ComponentEventResultProcessor> configuration)
    {
        configuration.add(
                ActionResponseGenerator.class,
                new ComponentEventResultProcessor<ActionResponseGenerator>()
                {
                    public ActionResponseGenerator processComponentEvent(
                            ActionResponseGenerator value, Component component,
                            String methodDescripion)
                    {
                        return value;
                    }
                });

        configuration.add(Link.class, new ComponentEventResultProcessor<Link>()
        {

            public ActionResponseGenerator processComponentEvent(Link value, Component component,
                    String methodDescripion)
            {
                return new LinkActionResponseGenerator(value);
            }
        });

        configuration.add(String.class, new StringResultProcessor(_requestPageCache, _linkFactory));

        configuration.add(Component.class, componentInstanceProcessor);

        configuration.add(StreamResponse.class, new StreamResponseResultProcessor());
    }

    public ComponentEventResultProcessor buildComponentInstanceResultProcessor(Log log)
    {
        return new ComponentInstanceResultProcessor(_requestPageCache, _linkFactory, log);
    }

    public static PersistentLocale buildPersistentLocale(@InjectService("Cookies")
    Cookies cookies)
    {
        return new PersistentLocaleImpl(cookies);
    }

    public static Cookies buildCookies(@InjectService("tapestry.internal.ContextPathSource")
    ContextPathSource contextPathSource,

    @InjectService("tapestry.internal.CookieSource")
    CookieSource cookieSource,

    @InjectService("tapestry.internal.CookieSink")
    CookieSink cookieSink,

    @Inject("${tapestry.default-cookie-max-age}")
    int defaultMaxAge)
    {
        return new CookiesImpl(contextPathSource, cookieSource, cookieSink, defaultMaxAge);
    }

    public static ApplicationStatePersistenceStrategySource buildApplicationStatePersistenceStrategySource(
            Map<String, ApplicationStatePersistenceStrategy> configuration)
    {
        return new ApplicationStatePersistenceStrategySourceImpl(configuration);
    }

    /** Contributes the default "session" strategy. */
    public void contributeApplicationStatePersistenceStrategySource(
            MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration)
    {
        configuration.add("session", new SessionApplicationStatePersistenceStrategy(_request));
    }

    public static ApplicationStateManager buildApplicationStateManager(
            Map<Class, ApplicationStateContribution> configuration,
            @Inject("infrastructure:ApplicationStatePersistenceStrategySource")
            ApplicationStatePersistenceStrategySource source)
    {
        return new ApplicationStateManagerImpl(configuration, source);
    }

    /**
     * The configuration of the model source is a mapping from type to string. The types are
     * property types and the values, the strings, represent different type of editors.
     */
    public BeanModelSource buildBeanModelSource(

    @Inject("infrastructure:TypeCoercer")
    TypeCoercer typeCoercer,

    Map<Class, String> configuration)
    {
        BeanModelSourceImpl service = new BeanModelSourceImpl(typeCoercer, _propertyAccess,
                _propertyConduitSource, _componentClassFactory, configuration);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Maps types to corresponding property editor names:
     * <ul>
     * <li>String --&gt; text
     * <li>Number --&gt; text
     * <li>Enum --&gt; enum
     * <li>Boolean --&gt; checkbox
     * </ul>
     */
    public static void contributeBeanModelSource(MappedConfiguration<Class, String> configuration)
    {
        configuration.add(Object.class, "");
        configuration.add(String.class, "text");
        configuration.add(Number.class, "text");
        configuration.add(Enum.class, "enum");
        configuration.add(Boolean.class, "checkbox");
    }

    public static ValidationConstraintGenerator buildValidationConstraintGenerator(
            List<ValidationConstraintGenerator> configuration)
    {
        return new ValidationConstraintGeneratorImpl(configuration);
    }

    /**
     * Adds built-in constraint generators:
     * <ul>
     * <li>PrimtiveField -- primitive fields are always required
     * <li>ValidateAnnotation -- adds constraints from a {@link Validate} annotation
     * </ul>
     */
    public static void contributeValidationConstraintGenerator(
            OrderedConfiguration<ValidationConstraintGenerator> configuration)
    {
        configuration.add("PrimitiveField", new PrimitiveFieldConstraintGenerator());
        configuration.add("ValidateAnnotation", new ValidateAnnotationConstraintGenerator());
    }

    public static FieldValidatorDefaultSource buildFieldValidatorDefaultSource(
            @Inject("infrastructure:ValidationConstraintGenerator")
            ValidationConstraintGenerator validationConstraintGenerator,

            @Inject("infrastructure:FieldValidatorSource")
            FieldValidatorSource fieldValidatorSource)
    {
        return new FieldValidatorDefaultSourceImpl(validationConstraintGenerator,
                fieldValidatorSource);
    }

    public PropertyConduitSource buildPropertyConduitSource()
    {
        PropertyConduitSourceImpl service = new PropertyConduitSourceImpl(_propertyAccess,
                _componentClassFactory);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public MetaDataLocator buildMetaDataLocator(@Inject("infrastructure:ComponentClassResolver")
    ComponentClassResolver componentClassResolver,

    Map<String, String> configuration)
    {
        MetaDataLocatorImpl service = new MetaDataLocatorImpl(componentClassResolver, configuration);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public void contributeMetaDataLocator(MappedConfiguration<String, String> configuration)
    {
        configuration.add(
                PersistentFieldManagerImpl.META_KEY,
                PersistentFieldManagerImpl.DEFAULT_STRATEGY);
    }

    public DefaultComponentParameterBindingSource buildDefaultComponentParameterBindingSource(
            @Inject("infrastructure:BindingSource")
            BindingSource bindingSource)
    {
        return new DefaultComponentParameterBindingSourceImpl(_propertyAccess, bindingSource);
    }

    public ObjectRenderer buildObjectRenderer(@InjectService("tapestry.ioc.StrategyBuilder")
    StrategyBuilder strategyBuilder, Map<Class, ObjectRenderer> configuration)
    {
        StrategyRegistry<ObjectRenderer> registry = StrategyRegistry.newInstance(
                ObjectRenderer.class,
                configuration);

        return _strategyBuilder.build(registry);
    }

    /**
     * Contributes a default object renderer for type Object, plus specialized renderers for
     * {@link Request} and {@link Location}.
     */
    public void contributeObjectRenderer(MappedConfiguration<Class, ObjectRenderer> configuration)
    {
        configuration.add(Object.class, new ObjectRenderer()
        {
            public void render(Object object, MarkupWriter writer)
            {
                writer.write(String.valueOf(object));
            }
        });

        configuration.add(Request.class, new RequestRenderer());

        configuration.add(Location.class, new LocationRenderer());
    }
}
