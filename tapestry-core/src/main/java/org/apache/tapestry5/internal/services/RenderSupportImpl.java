// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClientInfrastructure;

public class RenderSupportImpl implements RenderSupport
{
    private final IdAllocator idAllocator;

    private final DocumentLinker linker;

    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private final ClientInfrastructure clientInfrastructure;

    private boolean stackAssetsAdded;

    private final JSONObject init = new JSONObject();

    private FieldFocusPriority focusPriority;

    private String focusFieldId;

    /**
     * @param linker          Used to assemble JavaScript includes and snippets
     * @param symbolSource    Used to example symbols (in {@linkplain #addClasspathScriptLink(String...) in classpath
     *                        scripts)
     * @param assetSource     Used to convert classpath scripts to {@link org.apache.tapestry5.Asset}s
     * @param coreScripts     core scripts (evaluated as classpaths scripts) that are added to any page that includes a
     *                        script link or script block
     * @param javascriptStack
     */
    public RenderSupportImpl(DocumentLinker linker, SymbolSource symbolSource,
                             AssetSource assetSource, ClientInfrastructure clientInfrastructure)
    {
        this(linker, symbolSource, assetSource, new IdAllocator(), clientInfrastructure);
    }

    /**
     * @param linker          Used to assemble JavaScript includes and snippets
     * @param symbolSource    Used to example symbols (in {@linkplain #addClasspathScriptLink(String...) in classpath
     *                        scripts)
     * @param assetSource     Used to convert classpath scripts to {@link org.apache.tapestry5.Asset}s
     * @param idAllocator     Used to allocate unique client ids during the render
     * @param coreScripts     core scripts (evaluated as classpaths scripts) that are added to any page that includes a
     *                        script link or script block
     * @param javascriptStack
     */

    public RenderSupportImpl(DocumentLinker linker, SymbolSource symbolSource,
                             AssetSource assetSource, IdAllocator idAllocator,
                             ClientInfrastructure clientInfrastructure)

    {
        this.linker = linker;
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;
        this.idAllocator = idAllocator;
        this.clientInfrastructure = clientInfrastructure;
    }

    public String allocateClientId(String id)
    {
        return idAllocator.allocateId(id);
    }

    public String allocateClientId(ComponentResources resources)
    {
        return allocateClientId(resources.getId());
    }

    public void addScriptLink(Asset... scriptAssets)
    {
        addStack();

        for (Asset asset : scriptAssets)
        {
            Defense.notNull(asset, "scriptAsset");

            linker.addScriptLink(asset.toClientURL());
        }
    }

    public void addScriptLink(String... scriptURLs)
    {
        addStack();

        for (String url : scriptURLs)
        {
            linker.addScriptLink(url);
        }
    }

    public void addClasspathScriptLink(String... classpaths)
    {
        addStack();

        for (String path : classpaths)
            addScriptLinkFromClasspath(path);
    }

    private void addScriptLinkFromClasspath(String path)
    {
        String expanded = symbolSource.expandSymbols(path);

        Asset asset = assetSource.getAsset(null, expanded, null);

        linker.addScriptLink(asset.toClientURL());
    }

    public void addScript(String script)
    {
        Defense.notBlank(script, "script");

        addStack();

        linker.addScript(script);
    }

    public void addScript(String format, Object... arguments)
    {
        Defense.notNull(format, "format");

        addScript(String.format(format, arguments));
    }

    public void addInit(String functionName, JSONArray parameterList)
    {
        addInitFunctionInvocation(functionName, parameterList);
    }

    public void addInit(String functionName, JSONObject parameter)
    {
        addInitFunctionInvocation(functionName, parameter);
    }

    public void addInit(String functionName, String... parameters)
    {
        if (parameters.length == 1)
        {
            addInitFunctionInvocation(functionName, parameters[0]);
            return;
        }

        JSONArray array = new JSONArray();

        for (String parameter : parameters)
        {
            array.put(parameter);
        }

        addInitFunctionInvocation(functionName, array);
    }

    public void autofocus(FieldFocusPriority priority, String fieldId)
    {
        Defense.notNull(priority, "priority");
        Defense.notBlank(fieldId, "fieldId");

        if (focusFieldId == null || priority.compareTo(focusPriority) > 0)
        {
            this.focusPriority = priority;
            focusFieldId = fieldId;
        }
    }

    private void addInitFunctionInvocation(String functionName, Object parameters)
    {
        Defense.notBlank(functionName, "functionName");
        Defense.notNull(parameters, "parameters");

        JSONArray invocations = init.has(functionName) ? init.getJSONArray(functionName) : null;

        if (invocations == null)
        {
            invocations = new JSONArray();
            init.put(functionName, invocations);
        }

        invocations.put(parameters);
    }

    /**
     * Commit any outstanding changes.
     */
    public void commit()
    {
        if (focusFieldId != null)
        {
            addScript("$('%s').activate();", focusFieldId);
        }

        if (init.length() > 0)
        {
            addScript("Tapestry.init(%s);", init);
        }
    }

    public void addStylesheetLink(Asset stylesheet, String media)
    {
        Defense.notNull(stylesheet, "stylesheet");

        linker.addStylesheetLink(stylesheet.toClientURL(), media);
    }


    public void addStylesheetLink(String stylesheetURL, String media)
    {
        linker.addStylesheetLink(stylesheetURL, media);
    }

    private void addStack()
    {
        if (!stackAssetsAdded)
        {
            for (Asset script : clientInfrastructure.getJavascriptStack())
            {
                linker.addScriptLink(script.toClientURL());
            }

            stackAssetsAdded = true;
        }
    }
}
