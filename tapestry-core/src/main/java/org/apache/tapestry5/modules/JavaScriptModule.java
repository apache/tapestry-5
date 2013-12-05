// Copyright 2012, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.ajax.JavaScriptSupportImpl;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.services.javascript.*;
import org.apache.tapestry5.internal.util.MessageCatalogResource;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.services.javascript.*;
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

    // These are automatically bundles with the core JavaScript stack; some applications may want to add a few
    // additional ones, such as t5/core/zone.
    private static final String[] bundledModules = new String[]{
            "alert", "ajax", "bootstrap", "console", "dom", "events", "exception-frame", "fields", "forms",
            "pageinit", "messages", "utils", "validation"
    };

    /**
     * The core JavaScriptStack has a number of entries:
     * <dl>
     * <dt>requirejs</dt> <dd>The RequireJS AMD JavaScript library</dd>
     * <dt>scriptaculous.js, effects.js</dt> <dd>Optional JavaScript libraries in compatibility mode (see {@link Trait#SCRIPTACULOUS})</dd>
     * <dt>t53-compatibility.js</dt> <dd>Optional JavaScript library (see {@link Trait#INITIALIZERS})</dd>
     * <dt>underscore-library, underscore-module</dt>
     * <dt>The Underscore JavaScript library, and the shim that allows underscore to be injected</dt>
     * <dt>t5/core/init</dt> <dd>Optional module related to t53-compatibility.js</dd>
     * <dt>jquery-library</dt> <dd>The jQuery library</dd>
     * <dt>jquery-noconflict</dt> <dd>Switches jQuery to no-conflict mode (only present when the infrastructure is "prototype").</dd>
     * <dt>jquery</dt> <dd>A module shim that allows jQuery to be injected (and also switches jQuery to no-conflict mode)</dd>
     * <dt>bootstrap.css, tapestry.css, exception-frame.css, tapestry-console.css, tree.css</dt>
     * <dd>CSS files</dd>
     * <dt>t5/core/[...]</dt>
     * <dd>Additional JavaScript modules</dd>
     * <dt>jquery</dt>
     * <dd>Added if the infrastructure provider is "jquery".</dd>
     * </dl>
     * <p/>
     * User modules may replace or extend this list.
     */
    @Contribute(JavaScriptStack.class)
    @Core
    public static void setupCoreJavaScriptStack(OrderedConfiguration<StackExtension> configuration,
                                                Compatibility compatibility,
                                                @Symbol(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER)
                                                String provider)
    {

        final String ROOT = "${tapestry.asset.root}";

        configuration.add("requirejs", StackExtension.library(ROOT + "/require-2.1.9.js"));
        configuration.add("underscore-library", StackExtension.library(ROOT + "/underscore-1.5.2.js"));

        if (provider.equals("prototype"))
        {
            final String SCRIPTY = "${tapestry.scriptaculous}";

            add(configuration, StackExtensionType.LIBRARY, SCRIPTY + "/prototype.js");

            if (compatibility.enabled(Trait.SCRIPTACULOUS))
            {
                add(configuration, StackExtensionType.LIBRARY,
                        SCRIPTY + "/scriptaculous.js",
                        SCRIPTY + "/effects.js");
            }
        }

        if (compatibility.enabled(Trait.INITIALIZERS))
        {
            add(configuration, StackExtensionType.LIBRARY, ROOT + "/t53-compatibility.js");
            configuration.add("t5/core/init", new StackExtension(StackExtensionType.MODULE, "t5/core/init"));
        }

        configuration.add("jquery-library", StackExtension.library(ROOT + "/jquery-1.9.1.js"));

        if (provider.equals("prototype"))
        {
            configuration.add("jquery-noconflict", StackExtension.library(ROOT + "/jquery-noconflict.js"));
        }

        add(configuration, StackExtensionType.MODULE, "jquery");

        add(configuration, StackExtensionType.STYLESHEET,
                "${" + SymbolConstants.BOOTSTRAP_ROOT + "}/css/bootstrap.css",

                ROOT + "/tapestry.css",

                ROOT + "/exception-frame.css",

                ROOT + "/tapestry-console.css",

                ROOT + "/tree.css");

        for (String name : bundledModules)
        {
            String full = "t5/core/" + name;
            configuration.add(full, new StackExtension(StackExtensionType.MODULE, full));
        }

        configuration.add("underscore-module", StackExtension.module("underscore"));
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

    @Contribute(Dispatcher.class)
    @Primary
    public static void setupModuleDispatchers(OrderedConfiguration<Dispatcher> configuration,
                                              ModuleManager moduleManager,
                                              OperationTracker tracker,
                                              ResourceStreamer resourceStreamer,
                                              PathConstructor pathConstructor,
                                              @Symbol(SymbolConstants.MODULE_PATH_PREFIX)
                                              String modulePathPrefix)
    {
        configuration.add("Modules",
                new ModuleDispatcher(moduleManager, resourceStreamer, tracker, pathConstructor, modulePathPrefix, false),
                "after:Asset", "before:ComponentEvent");

        configuration.add("ComnpressedModules",
                new ModuleDispatcher(moduleManager, resourceStreamer, tracker, pathConstructor, modulePathPrefix, true),
                "after:Modules", "before:ComponentEvent");
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
                String uid = Long.toHexString(System.nanoTime());

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


    @Contribute(ModuleManager.class)
    public static void setupBaseModules(MappedConfiguration<String, Object> configuration,
                                        @Path("${tapestry.asset.root}/underscore-shim.js")
                                        Resource underscoreShim,

                                        @Path("${tapestry.asset.root}/jquery-shim.js")
                                        Resource jqueryShim,

                                        @Path("${tapestry.asset.root}/typeahead-0.9.3.js")
                                        Resource typeahead,

                                        @Path("${tapestry.asset.root}/moment-2.4.0.js")
                                        Resource moment,

                                        @Path("${" + SymbolConstants.BOOTSTRAP_ROOT + "}/js/transition.js")
                                        Resource transition)
    {
        // The underscore shim module allows Underscore to be injected
        configuration.add("underscore", new JavaScriptModuleConfiguration(underscoreShim));
        configuration.add("jquery", new JavaScriptModuleConfiguration(jqueryShim));

        configuration.add("bootstrap/transition", new JavaScriptModuleConfiguration(transition).dependsOn("jquery"));

        for (String name : new String[]{"affix", "alert", "button", "carousel", "collapse", "dropdown", "modal",
                "scrollspy", "tab", "tooltip"})
        {
            Resource lib = transition.forFile(name + ".js");

            configuration.add("bootstrap/" + name, new JavaScriptModuleConfiguration(lib).dependsOn("bootstrap/transition"));
        }

        Resource popover = transition.forFile("popover.js");

        configuration.add("bootstrap/popover", new JavaScriptModuleConfiguration(popover).dependsOn("bootstrap/tooltip"));

        configuration.add("t5/core/typeahead", new JavaScriptModuleConfiguration(typeahead).dependsOn("jquery"));

        configuration.add("moment", new JavaScriptModuleConfiguration(moment));

    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void setupFactoryDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "prototype");
        configuration.add(SymbolConstants.MODULE_PATH_PREFIX, "modules");
    }

    @Contribute(ModuleManager.class)
    public static void setupFoundationFramework(MappedConfiguration<String, Object> configuration,
                                                @Symbol(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER)
                                                String provider,
                                                @Path("classpath:org/apache/tapestry5/t5-core-dom-prototype.js")
                                                Resource domPrototype,
                                                @Path("classpath:org/apache/tapestry5/t5-core-dom-jquery.js")
                                                Resource domJQuery)
    {
        if (provider.equals("prototype"))
        {
            configuration.add("t5/core/dom", new JavaScriptModuleConfiguration(domPrototype));
        }

        if (provider.equals("jquery"))
        {
            configuration.add("t5/core/dom", new JavaScriptModuleConfiguration(domJQuery));
        }

        // If someone wants to support a different infrastructure, they should set the provider symbol to some other value
        // and contribute their own version of the t5/core/dom module.
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

            configuration.add("t5/core/messages/" + locale.toString(), new JavaScriptModuleConfiguration(resource));
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
