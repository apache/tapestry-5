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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.bindings.LiteralBinding;
import org.apache.tapestry.internal.bindings.PropBindingFactory;
import org.apache.tapestry.internal.util.IntegerRange;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.ServiceBinder;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ChainBuilder;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.AliasContribution;
import org.apache.tapestry.services.ApplicationGlobals;
import org.apache.tapestry.services.ApplicationInitializer;
import org.apache.tapestry.services.ApplicationInitializerFilter;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.ClasspathAssetAliasManager;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.ObjectRenderer;
import org.apache.tapestry.services.PropertyConduitSource;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestExceptionHandler;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestGlobals;
import org.apache.tapestry.services.ResourceDigestGenerator;
import org.apache.tapestry.services.Response;

public final class InternalModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(TemplateParser.class, TemplateParserImpl.class);
        binder.bind(PageResponseRenderer.class, PageResponseRendererImpl.class);
        binder.bind(PageMarkupRenderer.class, PageMarkupRendererImpl.class);
        binder.bind(ComponentInvocationMap.class, NoOpComponentInvocationMap.class);
        binder.bind(ObjectRenderer.class, LocationRenderer.class).withId("LocationRenderer");
        binder.bind(UpdateListenerHub.class, UpdateListenerHubImpl.class);
        binder.bind(ObjectProvider.class, AssetObjectProvider.class).withId("AssetObjectProvider");
        binder.bind(LinkFactory.class, LinkFactoryImpl.class);
        binder.bind(LocalizationSetter.class, LocalizationSetterImpl.class);
        binder.bind(PageElementFactory.class, PageElementFactoryImpl.class);
        binder.bind(ActionLinkHandler.class, ActionLinkHandlerImpl.class);
        binder.bind(ClassNameLocator.class, ClassNameLocatorImpl.class);
        binder.bind(RequestExceptionHandler.class, DefaultRequestExceptionHandler.class);
        binder.bind(PageLinkHandler.class, PageLinkHandlerImpl.class);
        binder.bind(ResourceStreamer.class, ResourceStreamerImpl.class);
    }

    public static void contributeAlias(Configuration<AliasContribution> configuration,
            ObjectLocator locator)
    {
        add(
                configuration,
                locator,
                FormParameterLookup.class,
                ContextPathSource.class,
                URLEncoder.class);
    }

    @SuppressWarnings("unchecked")
    private static void add(Configuration<AliasContribution> configuration, ObjectLocator locator,
            Class... serviceInterfaces)
    {
        for (Class serviceInterface : serviceInterfaces)
        {
            String name = serviceInterface.getName();
            String serviceId = InternalUtils.lastTerm(name);

            AliasContribution contribution = AliasContribution.create(serviceInterface, locator
                    .getService(serviceId, serviceInterface));

            configuration.add(contribution);
        }
    }

    public static void contributeTemplateParser(MappedConfiguration<String, URL> configuration)
    {
        Class c = InternalModule.class;
        configuration.add("-//W3C//DTD XHTML 1.0 Strict//EN", c.getResource("xhtml1-strict.dtd"));
        configuration.add("-//W3C//DTD XHTML 1.0 Transitional//EN", c
                .getResource("xhtml1-transitional.dtd"));
        configuration.add("-//W3C//DTD XHTML 1.0 Frameset//EN", c
                .getResource("xhtml1-frameset.dtd"));
        configuration
                .add("-//W3C//ENTITIES Latin 1 for XHTML//EN", c.getResource("xhtml-lat1.ent"));
        configuration.add("-//W3C//ENTITIES Symbols for XHTML//EN", c
                .getResource("xhtml-symbol.ent"));
        configuration.add("-//W3C//ENTITIES Special for XHTML//EN", c
                .getResource("xhtml-special.ent"));
    }

    /**
     * Contributes factory defaults that map be overridden.
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        // Remember this is request-to-request time, presumably it'll take the developer more than
        // one second to make a change, save it, and switch back to the browser.

        configuration.add("tapestry.file-check-interval", "1000"); // 1 second
        configuration.add("tapestry.supported-locales", "en");
        configuration.add("tapestry.default-cookie-max-age", "604800"); // One week

        configuration.add("tapestry.start-page-name", "start");

        // This is designed to make it easy to keep syncrhonized with script.aculo.ous. As we
        // support a new version, we create a new folder, and update the path entry. We can then
        // delete the old version folder (or keep it around). This should be more manageable than
        // ovewriting the local copy with updates. There's also a ClasspathAliasManager
        // contribution based on the path.

        configuration.add("tapestry.scriptaculous", "classpath:${tapestry.scriptaculous.path}");
        configuration.add("tapestry.scriptaculous.path", "org/apache/tapestry/scriptaculous_1_7_0");
    }

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

    private final ContextPathSource _contextPathSource;

    public InternalModule(@InjectService("ComponentInstantiatorSource")
    ComponentInstantiatorSource componentInstantiatorSource,

    @InjectService("UpdateListenerHub")
    UpdateListenerHub updateListenerHub,

    @InjectService("ThreadCleanupHub")
    ThreadCleanupHub threadCleanupHub,

    @InjectService("ComponentTemplateSource")
    ComponentTemplateSource componentTemplateSource,

    @InjectService("ComponentClassResolver")
    ComponentClassResolver componentClassResolver,

    @InjectService("ChainBuilder")
    ChainBuilder chainBuilder,

    Request request,

    Response response,

    @InjectService("ContextPathSource")
    ContextPathSource contextPathSource,

    @InjectService("ThreadLocale")
    ThreadLocale threadLocale,

    RequestGlobals requestGlobals)
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
        _contextPathSource = contextPathSource;
    }

    public PageTemplateLocator build(@InjectService("ContextAssetFactory")
    AssetFactory contextAssetFactory,

    ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(),
                componentClassResolver);
    }

    public ComponentInstantiatorSource build(@InjectService("ClassFactory")
    ClassFactory classFactory,

    ComponentClassTransformer transformer,

    Log log)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(classFactory
                .getClassLoader(), transformer, log);

        _updateListenerHub.addUpdateListener(source);

        return source;
    }

    public ComponentClassTransformer buildComponentClassTransformer(ServiceResources resources)
    {
        ComponentClassTransformerImpl transformer = resources
                .autobuild(ComponentClassTransformerImpl.class);

        _componentInstantiatorSource.addInvalidationListener(transformer);

        return transformer;
    }

    public PagePool build(Log log, PageLoader pageLoader,
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

    public PageLoader buildPageLoader(ServiceResources resources)
    {
        PageLoaderImpl service = resources.autobuild(PageLoaderImpl.class);

        // Recieve invalidations when the class loader is discarded (due to a component class
        // change). The notification is forwarded to the page loader's listeners.

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Scope(PERTHREAD_SCOPE)
    public RequestPageCache build(PagePool pagePool)
    {
        RequestPageCacheImpl service = new RequestPageCacheImpl(_componentClassResolver, pagePool);

        _threadCleanupHub.addThreadCleanupListener(service);

        return service;
    }

    public ResourceCache build(ResourceDigestGenerator digestGenerator)
    {
        ResourceCacheImpl service = new ResourceCacheImpl(digestGenerator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentTemplateSource build(@InjectService("TemplateParser")
    TemplateParser parser, @InjectService("PageTemplateLocator")
    PageTemplateLocator locator)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    public AssetFactory buildClasspathAssetFactory(@InjectService("ResourceCache")
    ResourceCache resourceCache,

    ClasspathAssetAliasManager aliasManager)
    {
        ClasspathAssetFactory factory = new ClasspathAssetFactory(resourceCache, aliasManager);

        resourceCache.addInvalidationListener(factory);

        return factory;
    }

    public AssetFactory buildContextAssetFactory(ApplicationGlobals globals)
    {
        return new ContextAssetFactory(_contextPathSource, globals.getContext());
    }

    public ContextPathSource buildContextPathSource()
    {
        return _request;
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

    public FormParameterLookup buildFormParameterLookup()
    {
        return _request;
    }

    /**
     * Builds the PropBindingFactory as a chain of command. The terminator of the chain is
     * responsible for ordinary property names (and property paths). Contributions to the service
     * cover additional special cases, such as simple literal values.
     * 
     * @param configuration
     *            contributions of special factories for some constants, each contributed factory
     *            may return a binding if applicable, or null otherwise
     */
    public BindingFactory buildPropBindingFactory(List<BindingFactory> configuration,
            PropertyConduitSource propertyConduitSource)
    {
        PropBindingFactory service = new PropBindingFactory(propertyConduitSource);

        configuration.add(service);

        return _chainBuilder.build(BindingFactory.class, configuration);
    }

    /**
     * Adds content types for "css" and "js" file extensions.
     */
    public void contributeResourceStreamer(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
    }

    public SessionHolder buildSessionHolder()
    {
        return _request;
    }

    public URLEncoder buildURLEncoder()
    {
        return _response;
    }

    /**
     * Adds a filter that sets the application package (for class loading purposes). The filter is
     * ordered before:*.*".
     */
    public void contributeApplicationInitializer(
            OrderedConfiguration<ApplicationInitializerFilter> configuration,
            final ApplicationGlobals applicationGlobals, final PropertyAccess propertyAccess,
            final TypeCoercer typeCoercer)
    {
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

    public void contributePropBindingFactory(OrderedConfiguration<BindingFactory> configuration)
    {
        BindingFactory keywordFactory = new BindingFactory()
        {
            private final Map<String, Object> _keywords = newCaseInsensitiveMap();

            {
                _keywords.put("true", Boolean.TRUE);
                _keywords.put("false", Boolean.FALSE);
                _keywords.put("null", null);
            }

            public Binding newBinding(String description, ComponentResources container,
                    ComponentResources component, String expression, Location location)
            {
                String key = expression.trim();

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

    /**
     * Adds a filter that checks for updates to classes and other resources. It is ordered before:*.
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("RequestGlobals")
            final RequestGlobals requestGlobals,

            // @Inject not needed because its a long, not a String
            @Symbol("tapestry.file-check-interval")
            long checkInterval,

            @InjectService("LocalizationSetter")
            LocalizationSetter localizationSetter)
    {
        configuration.add("CheckForUpdates", new CheckForUpdatesFilter(_updateListenerHub,
                checkInterval), "before:*");

        configuration.add("Localization", new LocalizationFilter(localizationSetter));
    }
}
