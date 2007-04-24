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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.bindings.LiteralBinding;
import org.apache.tapestry.internal.bindings.PropBindingFactory;
import org.apache.tapestry.internal.util.IntegerRange;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.LogSource;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Lifecycle;
import org.apache.tapestry.ioc.annotations.Match;
import org.apache.tapestry.ioc.annotations.Order;
import org.apache.tapestry.ioc.services.ChainBuilder;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.LoggingDecorator;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.ApplicationGlobals;
import org.apache.tapestry.services.ApplicationInitializer;
import org.apache.tapestry.services.ApplicationInitializerFilter;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.ClasspathAssetAliasManager;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.PageRenderInitializer;
import org.apache.tapestry.services.PersistentFieldManager;
import org.apache.tapestry.services.PersistentLocale;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestExceptionHandler;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestGlobals;
import org.apache.tapestry.services.ResourceDigestGenerator;
import org.apache.tapestry.services.Response;

@Id("tapestry.internal")
public final class InternalModule
{
    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ComponentTemplateSource _componentTemplateSource;

    private final UpdateListenerHub _updateListenerHub;

    private final ThreadCleanupHub _threadCleanupHub;

    private final ComponentClassResolver _componentClassResolver;

    private final ChainBuilder _chainBuilder;

    private final Request _request;

    private final Response _response;

    private final ThreadLocale _threadLocale;

    private final RequestGlobals _requestGlobals;

    private final RequestPageCache _pageCache;

    public InternalModule(@InjectService("ComponentInstantiatorSource")
    ComponentInstantiatorSource componentInstantiatorSource, @InjectService("UpdateListenerHub")
    UpdateListenerHub updateListenerHub, @InjectService("tapestry.ioc.ThreadCleanupHub")
    ThreadCleanupHub threadCleanupHub, @InjectService("ComponentTemplateSource")
    ComponentTemplateSource componentTemplateSource,
            @InjectService("tapestry.ComponentClassResolver")
            ComponentClassResolver componentClassResolver,
            @InjectService("tapestry.ioc.ChainBuilder")
            ChainBuilder chainBuilder, @Inject("infrastructure:Request")
            Request request, @Inject("infrastructure:Response")
            Response response, @InjectService("tapestry.ioc.ThreadLocale")
            ThreadLocale threadLocale, @Inject("infrastructure:RequestGlobals")
            RequestGlobals requestGlobals, @InjectService("RequestPageCache")
            RequestPageCache pageCache)
    {
        _componentInstantiatorSource = componentInstantiatorSource;
        _updateListenerHub = updateListenerHub;
        _threadCleanupHub = threadCleanupHub;
        _componentTemplateSource = componentTemplateSource;
        _componentClassResolver = componentClassResolver;
        _chainBuilder = chainBuilder;
        _request = request;
        _response = response;
        _threadLocale = threadLocale;
        _requestGlobals = requestGlobals;
        _pageCache = pageCache;
    }

    public ComponentClassTransformer buildComponentClassTransformer(
            @InjectService("tapestry.ComponentClassTransformWorker")
            ComponentClassTransformWorker workerChain, @InjectService("tapestry.ioc.LogSource")
            LogSource logSource)
    {
        ComponentClassTransformerImpl transformer = new ComponentClassTransformerImpl(workerChain,
                logSource);

        _componentInstantiatorSource.addInvalidationListener(transformer);

        return transformer;
    }

    public ComponentInstantiatorSource buildComponentInstantiatorSource(
            @InjectService("tapestry.ioc.ClassFactory")
            ClassFactory classFactory, @InjectService("ComponentClassTransformer")
            ComponentClassTransformer transformer, Log log)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(classFactory
                .getClassLoader(), transformer, log);

        _updateListenerHub.addUpdateListener(source);

        return source;
    }

    public ComponentTemplateSource buildComponentTemplateSource(@InjectService("TemplateParser")
    TemplateParser parser, @InjectService("PageTemplateLocator")
    PageTemplateLocator locator)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    @Lifecycle("perthread")
    public static TemplateParser buildTemplateParser(Log log)
    {
        return new TemplateParserImpl(log);
    }

    public PageElementFactory buildPageElementFactory(@Inject("infrastructure:TypeCoercer")
    TypeCoercer typeCoercer, @Inject("infrastructure:BindingSource")
    BindingSource bindingSource, @Inject("infrastructure:ComponentMessagesSource")
    ComponentMessagesSource componentMessagesSource)
    {
        return new PageElementFactoryImpl(_componentInstantiatorSource, _componentClassResolver,
                typeCoercer, bindingSource, componentMessagesSource);
    }

    public PageLoader buildPageLoader(@InjectService("PageElementFactory")
    PageElementFactory pageElementFactory, @InjectService("tapestry.BindingSource")
    BindingSource bindingSource, @InjectService("LinkFactory")
    LinkFactory linkFactory, @Inject("infrastructure:PersistentFieldManager")
    PersistentFieldManager persistentFieldManager)
    {
        PageLoaderImpl service = new PageLoaderImpl(_componentTemplateSource, pageElementFactory,
                bindingSource, linkFactory, persistentFieldManager);

        // Recieve invalidations when the class loader is discarded (due to a component class
        // change).
        // The notification is forwarded to the page loader's listeners.

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public PagePool buildPagePool(Log log, @InjectService("PageLoader")
    PageLoader pageLoader, @Inject("infrastructure:ComponentMessagesSource")
    ComponentMessagesSource componentMessagesSource)
    {
        PagePoolImpl service = new PagePoolImpl(log, pageLoader, _threadLocale);

        // This covers invalidations due to changes to classes

        pageLoader.addInvalidationListener(service);

        // This covers invalidation due to changes to message catalogs (properties files)

        componentMessagesSource.addInvalidationListener(service);

        // ... and this covers invalidations due to changes to templates

        _componentTemplateSource.addInvalidationListener(service);

        return service;
    }

    /**
     * The UpdateListenerHub provides events that other services use to check for invalidations.
     * Such services usually are {@link org.apache.tapestry.internal.event.InvalidationEventHub}s,
     * and fire invalidation events to their listeners.
     */
    public static UpdateListenerHub buildUpdateListenerHub()
    {
        return new UpdateListenerHubImpl();
    }

    /**
     * All public services in the tapestry module, and in any sub-module of tapestry will get
     * logging. This doesn't include the tapesry.ioc module since services of that module can not be
     * decorated.
     */
    @Match(
    { "tapestry.*", "tapestry.*.*" })
    @Order("before:*.*")
    public static <T> T decorateWithLogging(Class<T> serviceInterface, T delegate,
            String serviceId, Log log, @InjectService("tapestry.ioc.LoggingDecorator")
            LoggingDecorator loggingDecorator)
    {
        return loggingDecorator.build(serviceInterface, delegate, serviceId, log);
    }

    @Lifecycle("perthread")
    public RequestPageCache buildRequestPageCache(@InjectService("PagePool")
    PagePool pagePool)
    {
        RequestPageCacheImpl service = new RequestPageCacheImpl(_componentClassResolver, pagePool);

        _threadCleanupHub.addThreadCleanupListener(service);

        return service;
    }

    public static PageResponseRenderer buildPageResponseRenderer(
            @InjectService("PageMarkupRenderer")
            PageMarkupRenderer markupRenderer, @InjectService("tapestry.MarkupWriterFactory")
            MarkupWriterFactory markupWriterFactory)
    {
        return new PageResponseRendererImpl(markupWriterFactory, markupRenderer);
    }

    public static PageMarkupRenderer buildPageMarkupRenderer(
            @InjectService("tapestry.PageRenderInitializer")
            PageRenderInitializer pageRenderInitializer)
    {
        return new PageMarkupRendererImpl(pageRenderInitializer);
    }

    /**
     * Adds a filter that checks for updates to classes and other resources. It is ordered
     * before:*.*.
     */
    @Contribute("tapestry.RequestHandler")
    public void contributeRequestFilters(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("tapestry.RequestGlobals")
            final RequestGlobals requestGlobals, @Inject("${tapestry.file-check-interval}")
            long checkInterval, @InjectService("LocalizationSetter")
            LocalizationSetter localizationSetter)
    {
        configuration.add("CheckForUpdates", new CheckForUpdatesFilter(_updateListenerHub,
                checkInterval), "before:*.*");

        configuration.add("Localization", new LocalizationFilter(localizationSetter));
    }

    public LocalizationSetter buildLocalizationSetter(@InjectService("tapestry.PersistentLocale")
    PersistentLocale persistentLocale, @Inject("${tapestry.supported-locales}")
    String localeNames)
    {
        return new LocalizationSetterImpl(persistentLocale, _threadLocale, localeNames);
    }

    /**
     * Contributes factory defaults that map be overridden.
     */
    @Contribute("tapestry.ioc.FactoryDefaults")
    public void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add("tapestry.file-check-interval", "1000"); // 1 second
        configuration.add("tapestry.supported-locales", "en");
        configuration.add("tapestry.default-cookie-max-age", "604800"); // One week
    }

    /**
     * Adds a filter that sets the application package (for class loading purposes). The filter is
     * ordered before:*.*".
     */
    @Contribute("tapestry.ApplicationInitializer")
    public void contributeApplicationInitializerFilters(
            OrderedConfiguration<ApplicationInitializerFilter> configuration,
            @InjectService("tapestry.ioc.PropertyAccess")
            final PropertyAccess propertyAccess, @Inject("infrastructure:TypeCoercer")
            final TypeCoercer typeCoercer)
    {
        ApplicationInitializerFilter setApplicationPackage = new ApplicationInitializerFilter()
        {
            public void initializeApplication(Context context, ApplicationInitializer initializer)
            {
                String packageName = context
                        .getInitParameter(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM);

                _componentClassResolver.setApplicationPackage(packageName);

                initializer.initializeApplication(context);
            }
        };

        configuration.add("SetApplicationPackage", setApplicationPackage, "before:*.*");

        final InvalidationListener listener = new InvalidationListener()
        {
            public void objectWasInvalidated()
            {
                propertyAccess.clearCache();
                typeCoercer.clearCache();
            }
        };

        ApplicationInitializerFilter clearCaches = new ApplicationInitializerFilter()
        {
            public void initializeApplication(Context context, ApplicationInitializer initializer)
            {
                // Snuck in here is the logic to clear the PropertyAccess service's cache whenever
                // the component class loader is invalidated.

                _componentInstantiatorSource.addInvalidationListener(listener);

                initializer.initializeApplication(context);
            }
        };

        configuration.add("ClearCachesOnInvalidation", clearCaches);
    }

    /**
     * Builds the PropBindingFactory as a chain of command. The terminator of the chain is
     * responsible for ordinary property names (and property paths). Contributions to the service
     * cover additional special cases, such as simple literal values.
     * 
     * @param configuration
     *            contributions of special factories for some constants, each contributed factory
     *            may return a binding if applicable, or null otherwise
     * @param propertyAccess
     * @param classFactory
     * @return
     */
    public BindingFactory buildPropBindingFactory(List<BindingFactory> configuration,
            @InjectService("tapestry.ioc.PropertyAccess")
            PropertyAccess propertyAccess, @InjectService("tapestry.ComponentClassFactory")
            ClassFactory classFactory)
    {
        PropBindingFactory service = new PropBindingFactory(propertyAccess, classFactory);

        _componentInstantiatorSource.addInvalidationListener(service);

        configuration.add(service);

        return _chainBuilder.build(BindingFactory.class, configuration);
    }

    public void contributePropBindingFactory(OrderedConfiguration<BindingFactory> configuration)
    {
        BindingFactory keywordFactory = new BindingFactory()
        {
            private final Map<String, Object> _keywords = newMap();

            {
                _keywords.put("true", Boolean.TRUE);
                _keywords.put("false", Boolean.FALSE);
                _keywords.put("null", null);
            }

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                String key = expression.trim().toLowerCase();

                if (_keywords.containsKey(key))
                    return new LiteralBinding(description, _keywords.get(key), location);

                return null;
            }
        };

        BindingFactory thisFactory = new BindingFactory()
        {

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                if ("this".equalsIgnoreCase(expression.trim()))
                    return new LiteralBinding(description, container.getComponent(), location);

                return null;
            }
        };

        BindingFactory longFactory = new BindingFactory()
        {
            private final Pattern _pattern = Pattern.compile("^\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, new Long(value), location);
                }

                return null;
            }
        };

        BindingFactory intRangeFactory = new BindingFactory()
        {
            private final Pattern _pattern = Pattern
                    .compile("^\\s*(-?\\d+)\\s*\\.\\.\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

                if (matcher.matches())
                {
                    int start = Integer.parseInt(matcher.group(1));
                    int finish = Integer.parseInt(matcher.group(2));

                    IntegerRange range = new IntegerRange(start, finish);

                    return new LiteralBinding(description, range, location);
                }

                return null;
            }
        };

        BindingFactory doubleFactory = new BindingFactory()
        {
            // So, either 1234. or 1234.56 or .78
            private final Pattern _pattern = Pattern
                    .compile("^\\s*(\\-?((\\d+\\.)|(\\d*\\.\\d+)))\\s*$");

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, new Double(value), location);
                }

                return null;
            }
        };

        BindingFactory stringFactory = new BindingFactory()
        {
            // This will match embedded single quotes as-is, no escaping necessary.

            private final Pattern _pattern = Pattern.compile("^\\s*'(.*)'\\s*$");

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, value, location);
                }

                return null;
            }
        };

        // To be honest, order probably doesn't matter.

        configuration.add("Keyword", keywordFactory);
        configuration.add("This", thisFactory);
        configuration.add("Long", longFactory);
        configuration.add("IntRange", intRangeFactory);
        configuration.add("Double", doubleFactory);
        configuration.add("StringLiteral", stringFactory);
    }

    public RequestExceptionHandler buildDefaultRequestExceptionHandler(
            @InjectService("PageResponseRenderer")
            PageResponseRenderer renderer)
    {
        return new DefaultRequestExceptionHandler(_pageCache, renderer, _response);
    }

    /** Service used to create links for components and pages. */
    public LinkFactory buildLinkFactory(@InjectService("ContextPathSource")
    ContextPathSource contextPathSource, @InjectService("URLEncoder")
    URLEncoder encoder, @InjectService("ComponentInvocationMap")
    ComponentInvocationMap componentInvocationMap)
    {
        return new LinkFactoryImpl(contextPathSource, encoder, _componentClassResolver,
                componentInvocationMap, _pageCache);
    }

    public ContextPathSource buildContextPathSource()
    {
        return _request;
    }

    public URLEncoder buildURLEncoder()
    {
        return _response;
    }

    public ResourceStreamer buildResourceStreamer()
    {
        return new ResourceStreamerImpl(_response);
    }

    public AssetFactory buildContextAssetFactory(@Inject("infrastructure:ApplicationGlobals")
    ApplicationGlobals globals)
    {
        return new ContextAssetFactory(_request, globals.getContext());
    }

    public AssetFactory buildClasspathAssetFactory(@InjectService("ResourceCache")
    ResourceCache resourceCache, @Inject("infrastructure:ClasspathAssetAliasManager")
    ClasspathAssetAliasManager aliasManager)
    {
        ClasspathAssetFactory factory = new ClasspathAssetFactory(resourceCache, aliasManager);

        resourceCache.addInvalidationListener(factory);

        return factory;
    }

    public ResourceCache buildResourceCache(@Inject("infrastructure:ResourceDigestGenerator")
    ResourceDigestGenerator digestGenerator)
    {
        ResourceCacheImpl service = new ResourceCacheImpl(digestGenerator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    /**
     * A map from a {@link org.apache.tapestry.dom.Element} to a {@link ComponentInvocation}
     * Components rendering a link element that is intended to be "clickable" by the
     * {@link org.apache.tapestry.test.pagelevel.PageTester} must map that element to an
     * ComponentInvocation so that the PageTester can find it.
     * <p>
     * By default (production mode), the map does nothing.
     */
    public static ComponentInvocationMap buildComponentInvocationMap()
    {
        return new NoOpComponentInvocationMap();
    }

    public FormParameterLookup buildFormParameterLookup()
    {
        return _request;
    }

    public SessionHolder buildSessionHolder()
    {
        return _request;
    }

    public PageTemplateLocator buildPageTemplateLocator(@InjectService("ContextAssetFactory")
    AssetFactory contextAssetFactory, @Inject("infrastructure:ComponentClassResolver")
    ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(),
                componentClassResolver);
    }

    public CookieSource buildCookieSource()
    {
        return new CookieSource()
        {

            public Cookie[] getCookies()
            {
                return _requestGlobals.getHTTPServletRequest().getCookies();
            }

        };
    }

    public CookieSink buildCookieSink()
    {
        return new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                _requestGlobals.getHTTPServletResponse().addCookie(cookie);
            }

        };
    }
}