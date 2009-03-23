// Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.pageload.PageLoaderImpl;
import org.apache.tapestry5.internal.structure.ComponentPageElementResourcesSource;
import org.apache.tapestry5.internal.structure.ComponentPageElementResourcesSourceImpl;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.services.*;
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

    private final RequestGlobals requestGlobals;

    private final InvalidationEventHub classesInvalidationEventHub;

    public InternalModule(UpdateListenerHub updateListenerHub,
                          RequestGlobals requestGlobals,

                          @ComponentClasses
                          InvalidationEventHub classesInvalidationEventHub)
    {
        this.updateListenerHub = updateListenerHub;
        this.requestGlobals = requestGlobals;
        this.classesInvalidationEventHub = classesInvalidationEventHub;
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
        binder.bind(LinkSource.class, LinkSourceImpl.class);
        binder.bind(LocalizationSetter.class, LocalizationSetterImpl.class);
        binder.bind(PageElementFactory.class, PageElementFactoryImpl.class);
        binder.bind(ResourceStreamer.class, ResourceStreamerImpl.class);
        binder.bind(ClientPersistentFieldStorage.class, ClientPersistentFieldStorageImpl.class);
        binder.bind(PageRenderQueue.class, PageRenderQueueImpl.class);
        binder.bind(AjaxPartialResponseRenderer.class, AjaxPartialResponseRendererImpl.class);
        binder.bind(PageContentTypeAnalyzer.class, PageContentTypeAnalyzerImpl.class);
        binder.bind(RequestPathOptimizer.class, RequestPathOptimizerImpl.class);
        binder.bind(ComponentPageElementResourcesSource.class, ComponentPageElementResourcesSourceImpl.class);
        binder.bind(RequestSecurityManager.class, RequestSecurityManagerImpl.class);
        binder.bind(InternalRequestGlobals.class, InternalRequestGlobalsImpl.class);
        binder.bind(EndOfRequestEventHub.class);
        binder.bind(ResponseCompressionAnalyzer.class, ResponseCompressionAnalyzerImpl.class);
        binder.bind(ComponentModelSource.class);
        binder.bind(AssetResourceLocator.class);
    }

    public static VirtualAssetStreamer buildVirtualAssetStreamer(@Autobuild VirtualAssetStreamerImpl service, ResourceCache cache)
    {
        cache.addInvalidationListener(service);

        return service;
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
    public static RequestPageCache buildRequestPageCache(@Autobuild RequestPageCacheImpl service,
                                                         PerthreadManager perthreadManager)
    {
        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    public static PageTemplateLocator buildPageTemplateLocator(@ContextProvider AssetFactory contextAssetFactory,

                                                               ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(), componentClassResolver);
    }


    public ComponentMessagesSource buildComponentMessagesSource(@Autobuild ComponentMessagesSourceImpl service)
    {
        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentInstantiatorSource buildComponentInstantiatorSource(@Builtin ClassFactory classFactory,

                                                                        ComponentClassTransformer transformer,

                                                                        Logger logger,

                                                                        InternalRequestGlobals internalRequestGlobals,

                                                                        ClasspathURLConverter classpathURLConverter)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(logger, classFactory
                .getClassLoader(), transformer, internalRequestGlobals, classpathURLConverter);

        updateListenerHub.addUpdateListener(source);

        return source;
    }

    public static ComponentClassTransformer buildComponentClassTransformer(
            @Autobuild ComponentClassTransformerImpl transformer, @ComponentClasses InvalidationEventHub hub)
    {
        hub.addInvalidationListener(transformer);

        return transformer;
    }

    public PageLoader buildPageLoader(@Autobuild PageLoaderImpl service,

                                      @ComponentTemplates
                                      InvalidationEventHub templatesHub,

                                      @ComponentMessages
                                      InvalidationEventHub messagesHub)
    {
        // TODO: We could combine these three using chain-of-command.

        classesInvalidationEventHub.addInvalidationListener(service);
        templatesHub.addInvalidationListener(service);
        messagesHub.addInvalidationListener(service);

        return service;
    }


    public PagePool buildPagePool(@Autobuild PagePoolImpl service,

                                  @ComponentTemplates
                                  InvalidationEventHub templatesHub,

                                  @ComponentMessages
                                  InvalidationEventHub messagesHub)
    {
        // This covers invalidations due to changes to classes

        classesInvalidationEventHub.addInvalidationListener(service);

        // This covers invalidation due to changes to message catalogs (properties files)

        messagesHub.addInvalidationListener(service);

        // ... and this covers invalidations due to changes to templates

        templatesHub.addInvalidationListener(service);

        // Give the service a chance to clean up its own cache periodically as well

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentClassCache buildComponentClassCache(@Autobuild ComponentClassCacheImpl service)
    {
        classesInvalidationEventHub.addInvalidationListener(service);

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

    public ResourceCache buildResourceCache(@Autobuild ResourceCacheImpl service)
    {
        updateListenerHub.addUpdateListener(service);

        return service;
    }


    public ComponentTemplateSource buildComponentTemplateSource(TemplateParser parser,
                                                                PageTemplateLocator locator,
                                                                ClasspathURLConverter classpathURLConverter)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator, classpathURLConverter);

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    @Marker(ComponentLayer.class)
    public static CtClassSource buildCtClassSource(PropertyShadowBuilder builder,
                                                   ComponentInstantiatorSource componentInstantiatorSource)
    {
        return builder.build(componentInstantiatorSource, "classSource", CtClassSource.class);
    }

    public PageActivationContextCollector buildPageActivationContextCollector(
            @Autobuild PageActivationContextCollectorImpl service)
    {
        classesInvalidationEventHub.addInvalidationListener(service);

        return service;
    }

    /**
     * @since 5.1.0.0
     */
    public StringInterner buildStringInterner(@Autobuild StringInternerImpl service)
    {
        classesInvalidationEventHub.addInvalidationListener(service);

        return service;
    }
}
