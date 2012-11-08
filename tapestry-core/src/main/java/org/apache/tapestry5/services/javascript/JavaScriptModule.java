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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.ajax.JavaScriptSupportImpl;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.services.javascript.*;
import org.apache.tapestry5.internal.util.MessageCatalogResource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

import java.util.Locale;

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
        binder.bind(JavaScriptStack.class, ExtensibleJavaScriptStack.class).withMarker(Core.class).withId("CoreJavaScriptStack");
    }

    /**
     * Contributes the "core" {@link JavaScriptStack}s
     *
     * @since 5.2.0
     */
    @Contribute(JavaScriptStackSource.class)
    public static void provideBuiltinJavaScriptStacks(MappedConfiguration<String, JavaScriptStack> configuration, @Core JavaScriptStack coreStack)
    {
        configuration.add(InternalConstants.CORE_STACK_NAME, coreStack);
    }

    @Contribute(JavaScriptStack.class)
    @Core
    public static void setupCoreJavaScriptStack(OrderedConfiguration<StackExtension> configuration)
    {
        final String ROOT = "${tapestry.asset.root}";

        add(configuration, StackExtensionType.LIBRARY, "${tapestry.scriptaculous}/scriptaculous.js",
                "${tapestry.scriptaculous}/effects.js",
                ROOT + "/t53-compatibility.js"
        );

        add(configuration, StackExtensionType.STYLESHEET,
                "${tapestry.bootstrap-root}/css/bootstrap.css",

                ROOT + "/exception-frame.css",

                ROOT + "/tapestry-console.css",

                ROOT + "/tree.css");
    }

    private static void add(OrderedConfiguration<StackExtension> configuration, StackExtensionType type, String... paths)
    {
        for (String path : paths)
        {
            int slashx = path.lastIndexOf('/');
            String id = path.substring(slashx + 1);

            configuration.add(id, new StackExtension(type, path));
        }
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
     * </dl>
     */
    @Contribute(MarkupRenderer.class)
    public void exposeJavaScriptSupportForFullPageRenders(OrderedConfiguration<MarkupRendererFilter> configuration,
                                                          final JavaScriptStackSource javascriptStackSource,
                                                          final JavaScriptStackPathConstructor javascriptStackPathConstructor)
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

        configuration.add("JavaScriptSupport", javaScriptSupport, "after:DocumentLinker");
    }

    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a
     * partial Ajax response.
     * <dl>
     * <dt>JavaScriptSupport
     * <dd>Provides {@link JavaScriptSupport}</dd>
     * </dl>
     */
    @Contribute(PartialMarkupRenderer.class)
    public void exposeJavaScriptSupportForPartialPageRender(OrderedConfiguration<PartialMarkupRendererFilter> configuration,
                                                            final JavaScriptStackSource javascriptStackSource,

                                                            final JavaScriptStackPathConstructor javascriptStackPathConstructor)
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

        configuration.add("JavaScriptSupport", javascriptSupport, "after:DocumentLinker");
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
                                            @Inject @Path("${tapestry.asset.root}/underscore_1_4_2.js")
                                            Resource underscore,

                                            @Inject @Path("${tapestry.scriptaculous}/prototype.js")
                                            Resource prototype,

                                            @Inject @Path("${tapestry.asset.root}/jquery-1.8.2.js")
                                            Resource jQuery,

                                            @Inject @Path("${" + SymbolConstants.BOOTSTRAP_ROOT + "}/js/bootstrap.js")
                                            Resource bootstrap)
    {
        configuration.add("_", new ShimModule(underscore).exports("_"));
        configuration.add("$", new ShimModule(jQuery).initializeWith("jQuery.noConflict()"));
        configuration.add("prototype", new ShimModule(prototype));
        configuration.add("bootstrap", new ShimModule(bootstrap).dependsOn("$"));
    }

    @Contribute(ModuleManager.class)
    public static void setupApplicationCatalogModules(MappedConfiguration<String, Object> configuration,
                                                      LocalizationSetter localizationSetter,
                                                      ComponentMessagesSource messagesSource,
                                                      ResourceChangeTracker resourceChangeTracker,
                                                      @Symbol(SymbolConstants.COMPACT_JSON) boolean compactJSON)
    {
        for (Locale locale : localizationSetter.getSupportedLocales())
        {
            MessageCatalogResource resource = new MessageCatalogResource(locale, messagesSource, resourceChangeTracker, compactJSON);

            configuration.add("core/messages/" + locale.toString(), new ShimModule(resource));
        }
    }

    /**
     * Contributes 'ConfigureHTMLElement', which writes the attributes into the HTML tag to describe locale, etc.
     */
    @Contribute(MarkupRenderer.class)
    public static void renderLocaleAttributeIntoPages(OrderedConfiguration<MarkupRendererFilter> configuration)
    {
        configuration.addInstance("ConfigureHTMLElement", ConfigureHTMLElementFilter.class);
    }

}
