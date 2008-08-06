// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;
import org.testng.annotations.Test;

public class RenderSupportImplTest extends InternalBaseTestCase
{
    private static final String CORE_ASSET_PATH_UNEXPANDED = "${core}";

    private static final String CORE_ASSET_PATH = "/org/apache/tapestry5/core/core.png";

    private static final String CORE_ASSET_URL = "/assets/core/core.png";

    private static final String ASSET_URL = "/assets/foo/bar.pdf";

    @Test
    public void add_script_link_by_asset()
    {
        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void core_assets_added()
    {
        getMocksControl().checkOrder(true);

        Asset coreAsset = mockAsset();
        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();
        AssetSource assetSource = mockAssetSource();
        SymbolSource symbolSource = mockSymbolSource();

        train_expandSymbols(symbolSource, CORE_ASSET_PATH_UNEXPANDED, CORE_ASSET_PATH);
        train_getAsset(assetSource, null, CORE_ASSET_PATH, null, coreAsset);

        train_toClientURL(coreAsset, CORE_ASSET_URL);
        linker.addScriptLink(CORE_ASSET_URL);

        train_toClientURL(asset, ASSET_URL);
        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, symbolSource, assetSource,
                                                      CORE_ASSET_PATH_UNEXPANDED);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("Tapestry.Foo(\"bar\");");

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null);

        support.addScript("Tapestry.Foo(\"%s\");", "bar");

        verify();
    }

    // TAPESTRY-2483

    @Test
    public void add_script_no_formatting()
    {
        DocumentLinker linker = mockDocumentLinker();

        String script = "foo('%');";

        linker.addScript(script);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null);

        support.addScript(script);

        verify();
    }

    @Test
    public void add_classpath_script_link()
    {
        String path = "${root}/foo/bar.pdf";
        String expanded = "org/apache/tapestry5/foo/bar.pdf";

        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();
        SymbolSource source = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();

        train_expandSymbols(source, path, expanded);

        train_getAsset(assetSource, null, expanded, null, asset);

        train_toClientURL(asset, ASSET_URL);
        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, source, assetSource);

        support.addClasspathScriptLink(path);

        verify();
    }

    @Test
    public void add_stylesheet_link()
    {
        String media = "print";
        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        linker.addStylesheetLink(ASSET_URL, media);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null);

        support.addStylesheetLink(asset, media);

        verify();
    }

    @Test
    public void add_init_with_single_string_parameter()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("Tapestry.init({\"foo\":[\"fred\",\"barney\"]});");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null);

        support.addInit("foo", "fred");
        support.addInit("foo", "barney");

        support.commit();

        verify();
    }

    @Test
    public void add_multiple_string_init_parameters()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("Tapestry.init({\"foo\":[[\"fred\",\"barney\"]]});");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null);

        support.addInit("foo", "fred", "barney");

        support.commit();

        verify();
    }
}
