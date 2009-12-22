// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.structure.PageResourcesSource;
import org.apache.tapestry5.internal.structure.PageResourcesSourceImpl;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

import javax.servlet.http.Cookie;

/**
 * {@link org.apache.tapestry5.services.TapestryModule} has gotten too complicated and it is nice to demarkate public
 * (and stable) from internal (and volatile).
 */
@Marker(Core.class)
public class InternalModule
{
    private final UpdateListenerHub updateListenerHub;

    private final ComponentInstantiatorSource componentInstantiatorSource;

    private final ComponentTemplateSource componentTemplateSource;

    private final RequestGlobals requestGlobals;

    public InternalModule(UpdateListenerHub updateListenerHub, ComponentInstantiatorSource componentInstantiatorSource,
                          ComponentTemplateSource componentTemplateSource, RequestGlobals requestGlobals)
    {
        this.updateListenerHub = updateListenerHub;
        this.componentInstantiatorSource = componentInstantiatorSource;
        this.componentTemplateSource = componentTemplateSource;
        this.requestGlobals = requestGlobals;
    }


    /**
     * Bind all the private/internal services of Tapestry.
     */
    public static void bind(ServiceBinder binder)
    {
        binder.bind(PersistentFieldManager.class, PersistentFieldManagerImpl.class);
        binder.bind(TemplateParser.class, TemplateParserImpl.class);
        binder.bind(PageResponseRenderer.class, PageResponseRendererImpl.class);
        binder.bind(PageMarkupRenderer.class, PageMarkupRendererImpl.class);
        binder.bind(ComponentInvocationMap.class, NoOpComponentInvocationMap.class);
        binder.bind(UpdateListenerHub.class, UpdateListenerHubImpl.class);
        binder.bind(LinkFactory.class, LinkFactoryImpl.class);
        binder.bind(LocalizationSetter.class, LocalizationSetterImpl.class);
        binder.bind(PageElementFactory.class, PageElementFactoryImpl.class);
        binder.bind(ResourceStreamer.class, ResourceStreamerImpl.class);
        binder.bind(ClientPersistentFieldStorage.class, ClientPersistentFieldStorageImpl.class);
        binder.bind(PageRenderQueue.class, PageRenderQueueImpl.class);
        binder.bind(AjaxPartialResponseRenderer.class, AjaxPartialResponseRendererImpl.class);
        binder.bind(PageContentTypeAnalyzer.class, PageContentTypeAnalyzerImpl.class);
        binder.bind(RequestPathOptimizer.class, RequestPathOptimizerImpl.class);
        binder.bind(PageResourcesSource.class, PageResourcesSourceImpl.class);
        binder.bind(RequestSecurityManager.class, RequestSecurityManagerImpl.class);
        binder.bind(InternalRequestGlobals.class, InternalRequestGlobalsImpl.class);
        binder.bind(EndOfRequestListenerHub.class);
        binder.bind(PageActivationContextCollector.class);
    }

    /**
     * Chooses one of two implementations, based on the configured mode.
     */
    public static ActionRenderResponseGenerator buildActionRenderResponseGenerator(

            @Symbol(SymbolConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS)
            boolean immediateMode,

            ObjectLocator locator)
    {
        if (immediateMode) return locator.autobuild(ImmediateActionRenderResponseGenerator.class);

        return locator.autobuild(ActionRenderResponseGeneratorImpl.class);
    }


    @Scope(ScopeConstants.PERTHREAD)
    public static RequestPageCache buildRequestPageCache(ObjectLocator locator, PerthreadManager perthreadManager)
    {
        RequestPageCacheImpl service = locator.autobuild(RequestPageCacheImpl.class);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    public static PageTemplateLocator buildPageTemplateLocator(@ContextProvider AssetFactory contextAssetFactory,

                                                               ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(), componentClassResolver);
    }


    public ComponentMessagesSource buildComponentMessagesSource(
            @ContextProvider
            AssetFactory contextAssetFactory,

            @Inject
            @Symbol(SymbolConstants.APPLICATION_CATALOG)
            String appCatalog)
    {
        ComponentMessagesSourceImpl service = new ComponentMessagesSourceImpl(contextAssetFactory
                .getRootResource(), appCatalog);

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentInstantiatorSource buildComponentInstantiatorSource(@Builtin ClassFactory classFactory,

                                                                        ComponentClassTransformer transformer,

                                                                        Logger logger,

                                                                        InternalRequestGlobals internalRequestGlobals)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(logger, classFactory
                .getClassLoader(), transformer, internalRequestGlobals);

        updateListenerHub.addUpdateListener(source);

        return source;
    }

    public ComponentClassTransformer buildComponentClassTransformer(ServiceResources resources)
    {
        ComponentClassTransformerImpl transformer = resources.autobuild(ComponentClassTransformerImpl.class);

        componentInstantiatorSource.addInvalidationListener(transformer);

        return transformer;
    }

    public PagePool buildPagePool(PageLoader pageLoader, ComponentMessagesSource componentMessagesSource,
                                  ServiceResources resources)
    {
        PagePoolImpl service = resources.autobuild(PagePoolImpl.class);

        // This covers invalidations due to changes to classes

        pageLoader.addInvalidationListener(service);

        // This covers invalidation due to changes to message catalogs (properties files)

        componentMessagesSource.addInvalidationListener(service);

        // ... and this covers invalidations due to changes to templates

        componentTemplateSource.addInvalidationListener(service);

        // Give the service a chance to clean up its own cache periodically as well

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentClassCache buildComponentClassCache(@ComponentLayer ClassFactory classFactory)
    {
        ComponentClassCacheImpl service = new ComponentClassCacheImpl(classFactory);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public CookieSource buildCookieSource()
    {
        return new CookieSource()
        {

            public Cookie[] getCookies()
            {
                return requestGlobals.getHTTPServletRequest().getCookies();
            }
        };
    }


    public CookieSink buildCookieSink()
    {
        return new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                requestGlobals.getHTTPServletResponse().addCookie(cookie);
            }
        };
    }

    public ResourceCache buildResourceCache(ResourceDigestGenerator digestGenerator)
    {
        ResourceCacheImpl service = new ResourceCacheImpl(digestGenerator);

        updateListenerHub.addUpdateListener(service);

        return service;
    }


    public ComponentTemplateSource buildComponentTemplateSource(TemplateParser parser, PageTemplateLocator locator)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator);

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public PageLoader buildPageLoader(ServiceResources resources)
    {
        PageLoaderImpl service = resources.autobuild(PageLoaderImpl.class);

        // Recieve invalidations when the class loader is discarded (due to a component class
        // change). The notification is forwarded to the page loader's listeners.

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Marker(ComponentLayer.class)
    public CtClassSource buildCtClassSource(PropertyShadowBuilder builder)
    {
        return builder.build(componentInstantiatorSource, "classSource", CtClassSource.class);
    }
}
