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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.ajax.EsModuleInitializationImpl;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ClasspathMatcher;
import org.apache.tapestry5.ioc.services.ClasspathScanner;
import org.apache.tapestry5.json.JSONCollection;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.EsModuleInitialization;
import org.apache.tapestry5.services.javascript.EsModuleManager;
import org.apache.tapestry5.services.javascript.ImportPlacement;

public class EsModuleManagerImpl implements EsModuleManager
{

    private static final String GENERIC_IMPORTED_VARIABLE = "m";

    /**
     * Name of the JSON object property containing the imports in an import map.
     */
    public static final String IMPORTS_ATTRIBUTE = EsModuleConfigurationCallback.IMPORTS_ATTRIBUTE;

    private static final String CLASSPATH_ROOT = "META-INF/assets/es-modules/";

    private final boolean compactJSON;

    private final boolean productionMode;

    private final Set<String> extensions;
    
    private final AssetSource assetSource;

    // Note: ConcurrentHashMap does not support null as a value, alas. We use classpathRoot as a null.
    private final Map<String, String> cache = CollectionFactory.newConcurrentMap();
    
    private final ClasspathScanner classpathScanner;
    
    private JSONObject importMap;
    
    private final ResourceChangeTracker resourceChangeTracker;
    
    private final List<EsModuleConfigurationCallback> globalCallbacks;

    public EsModuleManagerImpl(
                             List<EsModuleConfigurationCallback> globalCallbacks,
                             AssetSource assetSource,
                             StreamableResourceSource streamableResourceSource,
                             @Symbol(SymbolConstants.COMPACT_JSON)
                             boolean compactJSON,
                             @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                             boolean productionMode,
                             ClasspathScanner classpathScanner,
                             ResourceChangeTracker resourceChangeTracker)
    {
        this.compactJSON = compactJSON;
        this.assetSource = assetSource;
        this.classpathScanner = classpathScanner;
        this.globalCallbacks = globalCallbacks;
        this.productionMode = productionMode;
        this.resourceChangeTracker = resourceChangeTracker;
        importMap = new JSONObject();

        extensions = CollectionFactory.newSet("js");
        extensions.addAll(streamableResourceSource.fileExtensionsForContentType(InternalConstants.JAVASCRIPT_CONTENT_TYPE));
        
        createImportMap();

    }
    
    private void createImportMap()
    {
        
        JSONObject importMap = new JSONObject();
        JSONObject imports = importMap.in(IMPORTS_ATTRIBUTE);
        
        resourceChangeTracker.addInvalidationCallback(this::createImportMap);
        cache.clear();

        loadBaseModuleList(imports);
        
        for (String name : cache.keySet())
        {
            imports.put(name, cache.get(name));
        }
        
        this.importMap = executeCallbacks(importMap, globalCallbacks);
        
        for (String id : imports.keySet()) 
        {
            cache.put(id, imports.getString(id));
        }
            
    }

    private void loadBaseModuleList(JSONObject imports) 
    {
        ClasspathMatcher matcher = (packagePath, fileName) -> 
            extensions.stream().anyMatch(e -> fileName.endsWith(e));
        try 
        {
            final Set<String> scan = classpathScanner.scan(CLASSPATH_ROOT, matcher);
            for (String file : scan) 
            {
                String id = file.replace(CLASSPATH_ROOT, "");
                id = id.substring(0, id.lastIndexOf('.'));
                            
                final Asset asset = assetSource.getClasspathAsset(file);
                resourceChangeTracker.trackResource(asset.getResource());
                imports.put(id, asset.toClientURL());
            }
        } catch (IOException e) 
        {
            throw new RuntimeException(e);
        }
    }

    @PostInjection
    public void setupInvalidation(ResourceChangeTracker tracker)
    {
        
    }

    @Override
    public void writeImportMap(Element head, List<EsModuleConfigurationCallback> moduleConfigurationCallbacks) {
        
        // Cloning the original import map JSON object
        final JSONObject imports = ((JSONObject) importMap.get(EsModuleConfigurationCallback.IMPORTS_ATTRIBUTE))
                .copy();
        JSONObject newImportMap = new JSONObject(
                EsModuleConfigurationCallback.IMPORTS_ATTRIBUTE, imports);
        
        newImportMap = executeCallbacks(newImportMap, moduleConfigurationCallbacks);
        
        head.element("script")
                .attribute("type", "importmap")
                .text(newImportMap.toString(compactJSON));
    }
        
    @Override
    public void writeImports(Element root, List<EsModuleInitialization> inits) 
    {
        Element script;
        Element body = null;
        Element head = null;
        ImportPlacement placement;
        EsModuleInitializationImpl init;
        String functionName;
        Object[] arguments;
        
        for (EsModuleInitialization i : inits) 
        {
            
            init = (EsModuleInitializationImpl) i;
            final String moduleId = init.getModuleId();
            // Making sure the user doesn't shoot heir own foot
            final String url = cache.get(moduleId);
            if (url == null)
            {
                throw new UnknownValueException("ES module not found: " + moduleId, 
                        new AvailableValues("String", cache));
            }
            
            placement = init.getPlacement();
            if (placement.equals(ImportPlacement.HEAD))
            {
                if (head == null) 
                {
                    head = root.find("head");
                }
                script = head.element("script");
            }
            else {
                if (body == null)
                {
                    body = root.find("body");
                }
                if (placement.equals(ImportPlacement.BODY_BOTTOM)) {
                    script = body.element("script");
                }
                else if (placement.equals(ImportPlacement.BODY_TOP))
                {
                    script = body.elementAt(0, "script");
                }
                else
                {
                    throw new IllegalArgumentException("Unknown import placement: " + placement);
                }
            }
            
            writeAttributes(script, init);
            script.attribute("src", url);
            
            functionName = init.getFunctionName();
            arguments = init.getArguments();
            
            if (!productionMode)
            {
                script.attribute("data-module-id", moduleId);
                final Element log = script.element("script", "type", "text/javascript");
                log.text(String.format("console.debug('Imported ES module %s');", moduleId));
                log.moveBefore(script);
            }
            
            // If we have not only the import, but also an automatic function call
            if (arguments != null || functionName != null)
            {
                final Element moduleFunctionCall = script.element("script");
                
                moduleFunctionCall.moveAfter(script);
                
                final String moduleFunctionCallFormat = 
                        "import %s from '%s';\n"
                        + "%s(%s);";
                
                final String importName = functionName != null ? functionName : GENERIC_IMPORTED_VARIABLE;
                final String importDeclaration = functionName != null ? 
                        "{ " + functionName + " }": 
                            GENERIC_IMPORTED_VARIABLE;
                
                moduleFunctionCall.text(String.format(moduleFunctionCallFormat, 
                        importDeclaration, moduleId, importName,
                        convertToJsFunctionParameters(arguments, compactJSON)));
                
                writeAttributes(moduleFunctionCall, init);
                
                // Avoiding duplicated ids
                final String id = moduleFunctionCall.getAttribute("id");
                if (id != null)
                {
                    moduleFunctionCall.forceAttributes("id", id + "-function-call");
                }
                
            }
            
        }
        
    }
    
    static String convertToJsFunctionParameters(Object[] arguments, boolean compactJSON)
    {
        String result;
        if (arguments == null || arguments.length == 0)
        {
            result = "";
        }
        else if (arguments.length == 1)
        {
            result = convertToJsFunctionParameter(arguments[0], compactJSON);
        }
        else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < arguments.length; i++) 
            {
                if (i > 0)
                {
                    builder.append(", ");
                }
                builder.append(convertToJsFunctionParameter(arguments[i], compactJSON));
            }
            result = builder.toString();
        }
        return result;
    }

    static String convertToJsFunctionParameter(Object object, boolean compactJSON) 
    {
        String result;
        
        if (object == null)
        {
            result = null;
        }
        else if (object instanceof String || object instanceof JSONLiteral)
        {
            result = "'" + object.toString() + "'";
        }
        else if (object instanceof Number || object instanceof Boolean)
        {
            result = object.toString();
        }
        else if (object instanceof JSONCollection)
        {
            result = ((JSONCollection) object).toString(compactJSON);
        }
        else
        {
            throw new IllegalArgumentException(String.format(
                    "Unsupported value: %s (type %s)", object.toString(), object.getClass().getName()));
        }
        
        return result;
    }

    private void writeAttributes(Element script, EsModuleInitializationImpl init) {
        final Map<String, String> attributes = init.getAttributes();
        for (String name : attributes.keySet())
        {
            script.attribute(name, attributes.get(name));
        }
        
        script.attribute("type", "module");
    }

    private JSONObject executeCallbacks(JSONObject importMap, List<EsModuleConfigurationCallback> callbacks) 
    {
        for (EsModuleConfigurationCallback callback : callbacks) 
        {
            callback.configure(importMap);
        }
        
        return importMap;
    }

}
