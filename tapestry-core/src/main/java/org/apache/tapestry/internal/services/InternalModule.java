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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.PageResourcesSource;
import org.apache.tapestry.internal.structure.PageResourcesSourceImpl;
import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ServiceBinder;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.annotations.Marker;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.services.Builtin;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.PerthreadManager;
import org.apache.tapestry.services.*;
import org.slf4j.Logger;

import javax.servlet.http.Cookie;

/**
 * {@link org.apache.tapestry.services.TapestryModule} has gotten too complicated and it is nice to demarkate public
 * (and stable) from internal (and volatile).
 */
@Marker(Core.class)
public class InternalModule
{
    private final UpdateListenerHub _updateListenerHub;
    private final ComponentInstantiatorSource _componentInstantiatorSource;
    private final ComponentTemplateSource _componentTemplateSource;
    private final RequestGlobals _requestGlobals;

    public InternalModule(UpdateListenerHub updateListenerHub, ComponentInstantiatorSource componentInstantiatorSource,
                          ComponentTemplateSource componentTemplateSource, RequestGlobals requestGlobals)
    {
        _updateListenerHub = updateListenerHub;
        _componentInstantiatorSource = componentInstantiatorSource;
        _componentTemplateSource = componentTemplateSource;
        _requestGlobals = requestGlobals;
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
        binder.bind(RequestEncodingInitializer.class, RequestEncodingInitializerImpl.class);
        binder.bind(PageRenderQueue.class, PageRenderQueueImpl.class);
        binder.bind(AjaxPartialResponseRenderer.class, AjaxPartialResponseRendererImpl.class);
        binder.bind(PageContentTypeAnalyzer.class, PageContentTypeAnalyzerImpl.class);
        binder.bind(ResponseRenderer.class, ResponseRendererImpl.class);
        binder.bind(RequestPathOptimizer.class, RequestPathOptimizerImpl.class);
        binder.bind(PageResourcesSource.class, PageResourcesSourceImpl.class);
        binder.bind(RequestSecurityManager.class, RequestSecurityManagerImpl.class);
        binder.bind(InternalRequestGlobals.class, InternalRequestGlobalsImpl.class);
    }

    /**
     * Chooses one of two implementations, based on the configured mode.
     */
    public static ActionRenderResponseGenerator buildActionRenderResponseGenerator(

            @Symbol(TapestryConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL)
            boolean immediateMode,

            ObjectLocator locator)
    {
        if (immediateMode) return locator.autobuild(ImmediateActionRenderResponseGenerator.class);

        return locator.autobuild(ActionRenderResponseGeneratorImpl.class);
    }

    @Scope(PERTHREAD_SCOPE)
    public static RequestPageCache buildRequestPageCache(PagePool pagePool, PerthreadManager perthreadManager)
    {
        RequestPageCacheImpl service = new RequestPageCacheImpl(pagePool);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    public static PageTemplateLocator buildPageTemplateLocator(@ContextProvider AssetFactory contextAssetFactory,

                                                               ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(), componentClassResolver);
    }


    public ComponentInstantiatorSource buildComponentInstantiatorSource(@Builtin ClassFactory classFactory,

                                                                        ComponentClassTransformer transformer,

                                                                        Logger logger,

                                                                        InternalRequestGlobals internalRequestGlobals)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(logger, classFactory
                .getClassLoader(), transformer, internalRequestGlobals);

        _updateListenerHub.addUpdateListener(source);

        return source;
    }

    public ComponentClassTransformer buildComponentClassTransformer(ServiceResources resources)
    {
        ComponentClassTransformerImpl transformer = resources.autobuild(ComponentClassTransformerImpl.class);

        _componentInstantiatorSource.addInvalidationListener(transformer);

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

        _componentTemplateSource.addInvalidationListener(service);

        // Give the service a chance to clean up its own cache periodically as well

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentClassCache buildComponentClassCache(@ComponentLayer ClassFactory classFactory)
    {
        ComponentClassCacheImpl service = new ComponentClassCacheImpl(classFactory);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
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

    public ResourceCache buildResourceCache(ResourceDigestGenerator digestGenerator)
    {
        ResourceCacheImpl service = new ResourceCacheImpl(digestGenerator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }


    public ComponentTemplateSource buildComponentTemplateSource(TemplateParser parser, PageTemplateLocator locator)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator);

        _updateListenerHub.addUpdateListener(service);

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


}
