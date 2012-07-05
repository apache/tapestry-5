// Copyright 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.RenderSupportImpl;
import org.apache.tapestry5.internal.services.ajax.JavaScriptSupportImpl;
import org.apache.tapestry5.internal.services.javascript.*;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

/**
 * Defines the services related to JavaScript.
 *
 * @since 5.4
 */
public class JavaScriptModule
{
    private final Environment environment;

    private final EnvironmentalShadowBuilder environmentalBuilder;

    public JavaScriptModule(Environment environment, EnvironmentalShadowBuilder environmentalBuilder)
    {
        this.environment = environment;
        this.environmentalBuilder = environmentalBuilder;
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(ModuleManager.class, ModuleManagerImpl.class);
        binder.bind(JavaScriptStackSource.class, JavaScriptStackSourceImpl.class);
    }

    /**
     * Contributes the "core" and "core-datefield" {@link JavaScriptStack}s
     *
     * @since 5.2.0
     */
    public static void contributeJavaScriptStackSource(MappedConfiguration<String, JavaScriptStack> configuration)
    {
        configuration.addInstance(InternalConstants.CORE_STACK_NAME, CoreJavaScriptStack.class);
        configuration.addInstance("core-datefield", DateFieldStack.class);
    }


    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.RenderSupport} inside this thread's
     * {@link org.apache.tapestry5.services.Environment}.
     */
    public RenderSupport buildRenderSupport()
    {
        return environmentalBuilder.build(RenderSupport.class);
    }

    /**
     * Builds a proxy to the current {@link JavaScriptSupport} inside this thread's {@link org.apache.tapestry5.services.Environment}.
     *
     * @since 5.2.0
     */
    public JavaScriptSupport buildJavaScriptSupport()
    {
        return environmentalBuilder.build(JavaScriptSupport.class);
    }

    /**
     * Adds page render filters, each of which provides an {@link org.apache.tapestry5.annotations.Environmental}
     * service. Filters
     * often provide {@link org.apache.tapestry5.annotations.Environmental} services needed by
     * components as they render.
     * <dl>
     * <dt>JavascriptSupport</dt>
     * <dd>Provides {@link JavaScriptSupport}</dd>
     * <dt>RenderSupport</dt>
     * <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * </dl>
     */
    @Contribute(MarkupRenderer.class)
    public void exposeJavaScriptSupportForFullPageRenders(OrderedConfiguration<MarkupRendererFilter> configuration,
                                                          final JavaScriptStackSource javascriptStackSource,
                                                          final JavaScriptStackPathConstructor javascriptStackPathConstructor,
                                                          final SymbolSource symbolSource,
                                                          final AssetSource assetSource)
    {

        MarkupRendererFilter javaScriptSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                JavaScriptSupportImpl support = new JavaScriptSupportImpl(linker, javascriptStackSource,
                        javascriptStackPathConstructor);

                environment.push(JavaScriptSupport.class, support);

                renderer.renderMarkup(writer);

                environment.pop(JavaScriptSupport.class);

                support.commit();
            }
        };

        MarkupRendererFilter renderSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                RenderSupportImpl support = new RenderSupportImpl(symbolSource, assetSource, javascriptSupport);

                environment.push(RenderSupport.class, support);

                renderer.renderMarkup(writer);

                environment.pop(RenderSupport.class);
            }
        };

        configuration.add("JavaScriptSupport", javaScriptSupport, "after:DocumentLinker");

        configuration.add("RenderSupport", renderSupport);
    }

    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a
     * partial Ajax response.
     * <dl>
     * <dt>JavaScriptSupport
     * <dd>Provides {@link JavaScriptSupport}</dd>
     * <dt>PageRenderSupport</dt>
     * <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * </dl>
     */
    @Contribute(PartialMarkupRenderer.class)
    public void exposeJavaScriptSupportForPartialPageRender(OrderedConfiguration<PartialMarkupRendererFilter> configuration,
                                                            final JavaScriptStackSource javascriptStackSource,

                                                            final JavaScriptStackPathConstructor javascriptStackPathConstructor,

                                                            final SymbolSource symbolSource,

                                                            final AssetSource assetSource)
    {
        PartialMarkupRendererFilter javascriptSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                String uid = Long.toHexString(System.currentTimeMillis());

                String namespace = "_" + uid;

                IdAllocator idAllocator = new IdAllocator(namespace);

                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                JavaScriptSupportImpl support = new JavaScriptSupportImpl(linker, javascriptStackSource,
                        javascriptStackPathConstructor, idAllocator, true);

                environment.push(JavaScriptSupport.class, support);

                renderer.renderMarkup(writer, reply);

                environment.pop(JavaScriptSupport.class);

                support.commit();
            }
        };

        PartialMarkupRendererFilter renderSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                RenderSupportImpl support = new RenderSupportImpl(symbolSource, assetSource, javascriptSupport);

                environment.push(RenderSupport.class, support);

                renderer.renderMarkup(writer, reply);

                environment.pop(RenderSupport.class);
            }
        };


        configuration.add("JavaScriptSupport", javascriptSupport, "after:DocumentLinker");
        configuration.add("RenderSupport", renderSupport);
    }

    @Contribute(Dispatcher.class)
    @AssetRequestDispatcher
    public static void handleModuleAssetRequests(MappedConfiguration<String, AssetRequestHandler> configuration)
    {
        configuration.addInstance("module-root", ModuleAssetRequestHandler.class);
    }

    @Contribute(StreamableResourceSource.class)
    public static void setupJavaScriptWrapper(MappedConfiguration<String, ResourceTransformer> configuration)
    {
        configuration.addInstance("jsw", JavaScriptWrapperResourceTransformer.class);
    }

    @Contribute(ModuleManager.class)
    public static void setupBaseModuleShims(MappedConfiguration<String, Object> configuration,
                                            @Inject @Path("classpath:org/apache/tapestry5/underscore_1_3_3.js")
                                            Resource underscore)
    {
        configuration.add("_", new ShimModule(underscore, null, "_"));
    }

}
