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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.beanmodel.internal.services.*;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.internal.services.ResponseCompressionAnalyzerImpl;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.pageload.PageLoaderImpl;
import org.apache.tapestry5.internal.services.AjaxPartialResponseRenderer;
import org.apache.tapestry5.internal.services.AjaxPartialResponseRendererImpl;
import org.apache.tapestry5.internal.services.ClientPersistentFieldStorage;
import org.apache.tapestry5.internal.services.ClientPersistentFieldStorageImpl;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.ComponentModelSource;
import org.apache.tapestry5.internal.services.CookieSink;
import org.apache.tapestry5.internal.services.CookieSource;
import org.apache.tapestry5.internal.services.EndOfRequestEventHub;
import org.apache.tapestry5.internal.services.FormControlNameManager;
import org.apache.tapestry5.internal.services.FormControlNameManagerImpl;
import org.apache.tapestry5.internal.services.InternalComponentInvalidationEventHub;
import org.apache.tapestry5.internal.services.InternalRequestGlobals;
import org.apache.tapestry5.internal.services.InternalRequestGlobalsImpl;
import org.apache.tapestry5.internal.services.LinkDecorationListener;
import org.apache.tapestry5.internal.services.LinkSource;
import org.apache.tapestry5.internal.services.LinkSourceImpl;
import org.apache.tapestry5.internal.services.LocalizationSetterImpl;
import org.apache.tapestry5.internal.services.PageContentTypeAnalyzer;
import org.apache.tapestry5.internal.services.PageContentTypeAnalyzerImpl;
import org.apache.tapestry5.internal.services.PageElementFactory;
import org.apache.tapestry5.internal.services.PageElementFactoryImpl;
import org.apache.tapestry5.internal.services.PageLoader;
import org.apache.tapestry5.internal.services.PageMarkupRenderer;
import org.apache.tapestry5.internal.services.PageMarkupRendererImpl;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.services.PageRenderQueueImpl;
import org.apache.tapestry5.internal.services.PageResponseRenderer;
import org.apache.tapestry5.internal.services.PageResponseRendererImpl;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.services.PageSourceImpl;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.internal.services.PersistentFieldManagerImpl;
import org.apache.tapestry5.internal.services.ReloadHelper;
import org.apache.tapestry5.internal.services.ReloadHelperImpl;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.internal.services.RequestPageCacheImpl;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.internal.services.RequestSecurityManagerImpl;
import org.apache.tapestry5.internal.services.ResourceDigestManager;
import org.apache.tapestry5.internal.services.ResourceDigestManagerImpl;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.ResourceStreamerImpl;
import org.apache.tapestry5.internal.services.TemplateParser;
import org.apache.tapestry5.internal.services.TemplateParserImpl;
import org.apache.tapestry5.internal.services.UnknownActivationContextHandler;
import org.apache.tapestry5.internal.services.UnknownActivationContextHandlerImpl;
import org.apache.tapestry5.internal.services.ajax.AjaxFormUpdateController;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor;
import org.apache.tapestry5.internal.structure.ComponentPageElementResourcesSource;
import org.apache.tapestry5.internal.structure.ComponentPageElementResourcesSourceImpl;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.LinkCreationListener2;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.transform.ControlledPackageType;

import javax.servlet.http.Cookie;
import java.util.Map;

/**
 * {@link org.apache.tapestry5.modules.TapestryModule} has gotten too complicated and it is nice to demarkate public
 * (and stable) from internal (and volatile).
 */
@Marker(Core.class)
public class InternalModule
{
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
        binder.bind(ComponentPageElementResourcesSource.class, ComponentPageElementResourcesSourceImpl.class);
        binder.bind(RequestSecurityManager.class, RequestSecurityManagerImpl.class);
        binder.bind(InternalRequestGlobals.class, InternalRequestGlobalsImpl.class);
        binder.bind(EndOfRequestEventHub.class);
        binder.bind(ComponentModelSource.class);
        binder.bind(JavaScriptStackPathConstructor.class);
        binder.bind(AjaxFormUpdateController.class);
        binder.bind(ResourceDigestManager.class, ResourceDigestManagerImpl.class);  // Remove in Tapestry 5.5
        binder.bind(RequestPageCache.class, RequestPageCacheImpl.class);
        binder.bind(ComponentInstantiatorSource.class);
        binder.bind(InternalComponentInvalidationEventHub.class);
        binder.bind(PageSource.class, PageSourceImpl.class);
        binder.bind(PageLoader.class, PageLoaderImpl.class).preventReloading();
        binder.bind(UnknownActivationContextHandler.class, UnknownActivationContextHandlerImpl.class);
        binder.bind(ReloadHelper.class, ReloadHelperImpl.class);
        binder.bind(FormControlNameManager.class, FormControlNameManagerImpl.class);

    }

    public static CookieSource buildCookieSource(final RequestGlobals requestGlobals)
    {
        return new CookieSource()
        {

            public Cookie[] getCookies()
            {
                return requestGlobals.getHTTPServletRequest().getCookies();
            }
        };
    }

    public static CookieSink buildCookieSink(final RequestGlobals requestGlobals)
    {
        return new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                requestGlobals.getHTTPServletResponse().addCookie(cookie);
            }
        };
    }

    /**
     * Contributes:
     * <dl>
     * <dt>LinkDecoration (instance of {@link LinkDecorationListener})</dt>
     * <dd>Triggers events for notifications about links</dd>
     * </dl>
     *
     * @since 5.2.0
     */
    public static void contributeLinkSource(OrderedConfiguration<LinkCreationListener2> configuration)
    {
        configuration.addInstance("LinkDecoration", LinkDecorationListener.class);
    }

    /**
     * Contributes packages identified by {@link ComponentClassResolver#getControlledPackageMapping()}.
     *
     * @since 5.3
     */
    @Contribute(ComponentInstantiatorSource.class)
    public static void configureControlledPackagesFromComponentClassResolver(
            MappedConfiguration<String, ControlledPackageType> configuration, ComponentClassResolver resolver)
    {
        for (Map.Entry<String, ControlledPackageType> entry : resolver.getControlledPackageMapping().entrySet())
        {
            configuration.add(entry.getKey(), entry.getValue());
        }
    }
}
