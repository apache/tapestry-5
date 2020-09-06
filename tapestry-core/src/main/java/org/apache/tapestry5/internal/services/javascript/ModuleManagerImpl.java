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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptModuleConfiguration;
import org.apache.tapestry5.services.javascript.ModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleManagerImpl implements ModuleManager
{

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final Messages globalMessages;

    private final boolean compactJSON;

    private final Map<String, Resource> shimModuleNameToResource = CollectionFactory.newMap();

    private final Resource classpathRoot;

    private final Set<String> extensions;

    // Note: ConcurrentHashMap does not support null as a value, alas. We use classpathRoot as a null.
    private final Map<String, Resource> cache = CollectionFactory.newConcurrentMap();

    private final JSONObject baseConfig;

    private final String basePath, compressedBasePath;

    public ModuleManagerImpl(ResponseCompressionAnalyzer compressionAnalyzer,
                             AssetSource assetSource,
                             Map<String, JavaScriptModuleConfiguration> configuration,
                             Messages globalMessages,
                             StreamableResourceSource streamableResourceSource,
                             @Symbol(SymbolConstants.COMPACT_JSON)
                             boolean compactJSON,
                             @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                             boolean productionMode,
                             @Symbol(SymbolConstants.MODULE_PATH_PREFIX)
                             String modulePathPrefix,
                             PathConstructor pathConstructor)
    {
        this.compressionAnalyzer = compressionAnalyzer;
        this.globalMessages = globalMessages;
        this.compactJSON = compactJSON;

        basePath = pathConstructor.constructClientPath(modulePathPrefix);
        compressedBasePath = pathConstructor.constructClientPath(modulePathPrefix + ".gz");

        classpathRoot = assetSource.resourceForPath("");
        extensions = CollectionFactory.newSet("js");

        extensions.addAll(streamableResourceSource.fileExtensionsForContentType(InternalConstants.JAVASCRIPT_CONTENT_TYPE));

        baseConfig = buildBaseConfig(configuration, !productionMode);
    }

    private String buildRequireJSConfig(List<ModuleConfigurationCallback> callbacks)
    {
        // This is the part that can vary from one request to another, based on the capabilities of the client.
        JSONObject config = baseConfig.copy().put("baseUrl", getBaseURL());

        // TAP5-2196: allow changes to the configuration in a per-request basis.
        for (ModuleConfigurationCallback callback : callbacks)
        {
            config = callback.configure(config);
            assert config != null;
        }

        // This part gets written out before any libraries are loaded (including RequireJS).
        return String.format("var require = %s;\n", config.toString(compactJSON));
    }

    private JSONObject buildBaseConfig(Map<String, JavaScriptModuleConfiguration> configuration, boolean devMode)
    {
        JSONObject config = new JSONObject();

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
        return config;
    }

    private String getBaseURL()
    {
        return compressionAnalyzer.isGZipSupported() ? compressedBasePath : basePath;
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

    public void writeConfiguration(Element body,
                                   List<ModuleConfigurationCallback> callbacks)
    {
        Element element = body.element("script", "type", "text/javascript");

        // Build it each time because we don't know if the client supports GZip or not, and
        // (in development mode) URLs for some referenced assets could change (due to URLs
        // containing a checksum on the resource content).
        element.raw(buildRequireJSConfig(callbacks));
    }

    public void writeInitialization(Element body, List<String> libraryURLs, List<?> inits)
    {

        Element element = body.element("script", "type", "text/javascript");

        element.raw(globalMessages.format("private-core-page-initialization-template",
                convert(libraryURLs),
                convert(inits)));
    }

    private String convert(List<?> input)
    {
        return new JSONArray().putAll(input).toString(compactJSON);
    }

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
