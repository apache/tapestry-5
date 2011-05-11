// Copyright 2007, 2008, 2009, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;
import org.testng.annotations.Test;

public class RenderSupportImplTest extends InternalBaseTestCase
{
    private static final String ASSET_URL = "/assets/foo/bar.pdf";

    @Test
    public void add_script_link_by_asset()
    {
        JavaScriptSupport js = mockJavaScriptSupport();
        Asset asset = mockAsset();

        js.importJavaScriptLibrary(asset);

        replay();

        RenderSupport support = new RenderSupportImpl(null, null, js);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script_link_by_url()
    {
        JavaScriptSupport jss = mockJavaScriptSupport();

        RenderSupport support = new RenderSupportImpl(null, null, jss);

        jss.importJavaScriptLibrary(ASSET_URL);

        replay();

        support.addScriptLink(ASSET_URL);

        verify();
    }

    @Test
    public void add_script()
    {
        JavaScriptSupport js = mockJavaScriptSupport();

        js.addScript("doSomething();");

        replay();

        RenderSupport support = new RenderSupportImpl(null, null, js);

        support.addScript("doSomething();");

        verify();
    }

    @Test
    public void add_classpath_script_link()
    {
        String path = "${root}/foo/bar.pdf";
        String expanded = "org/apache/tapestry5/foo/bar.pdf";

        Asset asset = mockAsset();
        SymbolSource source = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        JavaScriptSupport js = mockJavaScriptSupport();

        train_expandSymbols(source, path, expanded);

        train_getAsset(assetSource, null, expanded, null, asset);

        js.importJavaScriptLibrary(asset);

        replay();

        RenderSupport support = new RenderSupportImpl(source, assetSource, js);

        support.addClasspathScriptLink(path);

        verify();
    }

    @Test
    public void add_stylesheet_link_by_asset()
    {
        String media = "print";
        JavaScriptSupport javascriptSupport = mockJavaScriptSupport();
        Asset asset = mockAsset("foo.css");

        javascriptSupport.importStylesheet(new StylesheetLink("foo.css", new StylesheetOptions(media)));

        replay();

        RenderSupport support = new RenderSupportImpl(null, null, javascriptSupport);

        support.addStylesheetLink(asset, media);

        verify();
    }

    @Test
    public void add_stylesheet_link_by_url()
    {
        String media = "print";
        JavaScriptSupport javascriptSupport = mockJavaScriptSupport();

        javascriptSupport.importStylesheet(new StylesheetLink(ASSET_URL, new StylesheetOptions(media)));

        replay();

        RenderSupport support = new RenderSupportImpl(null, null, javascriptSupport);

        support.addStylesheetLink(ASSET_URL, media);

        verify();
    }

    @Test
    public void add_multiple_string_init_parameters()
    {
        JavaScriptSupport js = mockJavaScriptSupport();

        JSONArray array = new JSONArray("fred", "barney");

        js.addInitializerCall("foo", array);

        replay();

        RenderSupportImpl support = new RenderSupportImpl(null, null, js);

        support.addInit("foo", "fred", "barney");

        verify();
    }

    @Test
    public void addInit_passes_through_to_JavaScriptSupport()
    {
        JSONObject parameter = new JSONObject("clientid", "fred");

        JavaScriptSupport js = mockJavaScriptSupport();

        js.addInitializerCall("setup", parameter);

        replay();

        RenderSupportImpl support = new RenderSupportImpl(null, null, js);

        support.addInit("setup", parameter);

        verify();
    }

    @Test
    public void autofocus_pass_thru_to_javascriptsupport()
    {
        JavaScriptSupport js = mockJavaScriptSupport();

        js.autofocus(FieldFocusPriority.OVERRIDE, "fred");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(null, null, js);

        support.autofocus(FieldFocusPriority.OVERRIDE, "fred");

        verify();
    }
}
