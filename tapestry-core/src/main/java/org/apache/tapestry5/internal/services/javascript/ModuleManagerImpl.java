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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptModuleConfiguration;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleManagerImpl implements ModuleManager
{
    private final String requireConfig;

    private final Asset requireJS;

    private final Messages globalMessages;

    private final boolean compactJSON;

    private final Map<String, Resource> shimModuleNameToResource = CollectionFactory.newMap();

    private final Resource classpathRoot;

    private final Set<String> extensions;

    // Note: ConcurrentHashMap does not support null as a value, alas. We use classpathRoot as a null.
    private final Map<String, Resource> cache = CollectionFactory.newConcurrentMap();

    public ModuleManagerImpl(AssetPathConstructor constructor, final ComponentClassResolver resolver, AssetSource assetSource,
                             @Path("${" + SymbolConstants.REQUIRE_JS + "}")
                             Asset requireJS,
                             Map<String, JavaScriptModuleConfiguration> configuration,
                             Messages globalMessages,
                             StreamableResourceSource streamableResourceSource,
                             @Symbol(SymbolConstants.COMPACT_JSON)
                             boolean compactJSON,
                             @Symbol(SymbolConstants.PRODUCTION_MODE)
                             boolean productionMode)
    {
        this.requireJS = requireJS;
        this.globalMessages = globalMessages;
        this.compactJSON = compactJSON;

        this.requireConfig = buildRequireJSConfig(constructor.constructAssetPath("module-root", ""), configuration, !productionMode);

        classpathRoot = assetSource.resourceForPath("");

        extensions = CollectionFactory.newSet("js");

        extensions.addAll(streamableResourceSource.fileExtensionsForContentType("text/javascript"));
    }

    private String buildRequireJSConfig(String baseURL, Map<String, JavaScriptModuleConfiguration> configuration, boolean devMode)
    {
        JSONObject config = new JSONObject("baseUrl", baseURL);

        // In DevMode, wait up to five minutes for a script, as the developer may be using the debugger.
        if (devMode)
        {
            config.put("waitSeconds", 300);
        }

        for (String name : configuration.keySet())
        {
            JavaScriptModuleConfiguration module = configuration.get(name);

            shimModuleNameToResource.put(name, module.resource);

            // Some modules (particularly overrides) just need an alternate location for their content
            // on the server.
            if (module.getNeedsConfiguration())
            {
                // Others are libraries being shimmed as AMD modules, and need some configuration
                // to ensure that everything hooks up properly on the client.
                addModuleToConfig(config, name, module);
            }
        }

        return String.format("requirejs.config(%s);\n", config.toString(compactJSON));
    }

    private void addModuleToConfig(JSONObject config, String name, JavaScriptModuleConfiguration module)
    {
        JSONObject shimConfig = config.in("shim");

        boolean nestDependencies = false;

        String exports = module.getExports();

        if (exports != null)
        {
            shimConfig.in(name).put("exports", exports);
            nestDependencies = true;
        }

        String initExpression = module.getInitExpression();

        if (initExpression != null)
        {
            String function = String.format("function() { return %s; }", initExpression);
            shimConfig.in(name).put("init", new JSONLiteral(function));
            nestDependencies = true;
        }

        List<String> dependencies = module.getDependencies();

        if (dependencies != null)
        {
            JSONObject container = nestDependencies ? shimConfig.in(name) : shimConfig;
            String key = nestDependencies ? "deps" : name;

            for (String dep : dependencies)
            {
                container.append(key, dep);
            }
        }
    }

    @PostInjection
    public void setupInvalidation(ResourceChangeTracker tracker)
    {
        tracker.clearOnInvalidation(cache);
    }

    public void writeInitialization(Element body, List<String> libraryURLs, List<?> inits)
    {
        body.element("script", "src", requireJS.toClientURL());

        Element element = body.element("script", "type", "text/javascript");

        element.raw(requireConfig);

        StringBuilder content = new StringBuilder(1000);

        content.append(globalMessages.format("core-page-initialization-template",
                convert(libraryURLs),
                convert(inits)));

        element.raw(content.toString());
    }

    private String convert(List<?> input)
    {
        return new JSONArray().putAll(input).toString(compactJSON);
    }

    @Override
    public Resource findResourceForModule(String moduleName)
    {
        Resource resource = cache.get(moduleName);

        if (resource == null)
        {
            resource = resolveModuleNameToResource(moduleName);
            cache.put(moduleName, resource);
        }

        // We're treating classpathRoot as a placeholder for null.

        return resource == classpathRoot ? null : resource;
    }

    private Resource resolveModuleNameToResource(String moduleName)
    {
        Resource resource = shimModuleNameToResource.get(moduleName);

        if (resource != null)
        {
            return resource;
        }


        // Tack on a fake extension; otherwise modules whose name includes a '.' get mangled
        // by Resource.withExtension().
        String baseName = String.format("/META-INF/modules/%s.EXT", moduleName);

        Resource baseResource = classpathRoot.forFile(baseName);

        for (String extension : extensions)
        {
            resource = baseResource.withExtension(extension);

            if (resource.exists())
            {
                return resource;
            }
        }

        // Return placeholder for null:
        return classpathRoot;
    }
}
