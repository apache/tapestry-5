// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavascriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;

public class RenderSupportImpl implements RenderSupport
{
    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private FieldFocusPriority focusPriority;

    private String focusFieldId;

    private final JavascriptSupport javascriptSupport;

    /**
     * @param symbolSource
     *            Used to expand symbols (in {@linkplain #addClasspathScriptLink(String...)}
     * @param assetSource
     *            Used to convert classpath scripts to {@link org.apache.tapestry5.Asset}s
     * @param javascriptSupport
     *            Used to add JavaScript libraries and blocks of initialization JavaScript to the rendered page
     * @param ClientInfrastructure
     *            Identifies which JavaScript libraries and stylesheets are needed in a full page render
     */
    public RenderSupportImpl(SymbolSource symbolSource, AssetSource assetSource, JavascriptSupport javascriptSupport)
    {
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;
        this.javascriptSupport = javascriptSupport;
    }

    public String allocateClientId(String id)
    {
        return javascriptSupport.allocateClientId(id);
    }

    public String allocateClientId(ComponentResources resources)
    {
        return javascriptSupport.allocateClientId(resources);
    }

    public void addScriptLink(Asset... scriptAssets)
    {
        for (Asset asset : scriptAssets)
        {
            assert asset != null;

            javascriptSupport.importJavascriptLibrary(asset);
        }
    }

    public void addScriptLink(String... scriptURLs)
    {
        for (String url : scriptURLs)
            javascriptSupport.importJavascriptLibrary(url);
    }

    public void addClasspathScriptLink(String... classpaths)
    {
        for (String path : classpaths)
            addScriptLinkFromClasspath(path);
    }

    private void addScriptLinkFromClasspath(String path)
    {
        String expanded = symbolSource.expandSymbols(path);

        Asset asset = assetSource.getAsset(null, expanded, null);

        addScriptLink(asset);
    }

    public void addScript(String script)
    {
        javascriptSupport.addScript(script);
    }

    public void addScript(String format, Object... arguments)
    {
        javascriptSupport.addScript(format, arguments);
    }

    public void addInit(String functionName, JSONArray parameterList)
    {
        addInitFunctionInvocation(functionName, parameterList);
    }

    public void addInit(String functionName, JSONObject parameter)
    {
        javascriptSupport.addInitializerCall(functionName, parameter);
    }

    public void addInit(String functionName, String... parameters)
    {
        JSONArray array = new JSONArray();

        for (String parameter : parameters)
        {
            array.put(parameter);
        }

        addInit(functionName, array);
    }

    public void autofocus(FieldFocusPriority priority, String fieldId)
    {
        assert priority != null;
        assert InternalUtils.isNonBlank(fieldId);
        if (focusFieldId == null || priority.compareTo(focusPriority) > 0)
        {
            this.focusPriority = priority;
            focusFieldId = fieldId;
        }
    }

    /**
     * For the few existing places that use the old variations of addInit(), passing a list of
     * strings or a JSONArray, the end result is a bit inefficient. We end up generating lots
     * of calls to Tapestry.init, with no attempt to aggregate them. Most of the time, the init
     * occurs with a JSONObject (the "spec") and is handled by JavascriptSupport.
     */
    private void addInitFunctionInvocation(String functionName, Object parameters)
    {
        assert InternalUtils.isNonBlank(functionName);
        assert parameters != null;

        JSONArray list = new JSONArray().put(parameters);
        JSONObject wrapper = new JSONObject().put(functionName, list);

        addScript("Tapestry.init(%s);", wrapper);
    }

    /**
     * Commit any outstanding changes.
     */
    public void commit()
    {
        if (focusFieldId != null)
            javascriptSupport.addInitializerCall("activate", focusFieldId);
    }

    public void addStylesheetLink(Asset stylesheet, String media)
    {
        javascriptSupport.importStylesheet(new StylesheetLink(stylesheet, new StylesheetOptions(media)));
    }

    public void addStylesheetLink(String stylesheetURL, String media)
    {
        javascriptSupport.importStylesheet(new StylesheetLink(stylesheetURL, new StylesheetOptions(media)));
    }
}
