// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Asset;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.services.AssetSource;
import org.testng.annotations.Test;

public class PageRenderSupportImplTest extends InternalBaseTestCase
{
    private static final String CORE_ASSET_PATH_UNEXPANDED = "${core}";

    private static final String CORE_ASSET_PATH = "/org/apache/tapestry/core/core.png";

    private static final String CORE_ASSET_URL = "/assets/core/core.png";

    private static final String ASSET_URL = "/assets/foo/bar.pdf";

    @Test
    public void add_script_link_by_asset()
    {
        DocumentHeadBuilder builder = mockDocumentScriptBuilder();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        builder.addScriptLink(ASSET_URL);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, null, null);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void core_assets_added()
    {
        getMocksControl().checkOrder(true);

        Asset coreAsset = mockAsset();
        DocumentHeadBuilder builder = mockDocumentScriptBuilder();
        Asset asset = mockAsset();
        AssetSource assetSource = mockAssetSource();
        SymbolSource symbolSource = mockSymbolSource();

        train_expandSymbols(symbolSource, CORE_ASSET_PATH_UNEXPANDED, CORE_ASSET_PATH);
        train_findAsset(assetSource, null, CORE_ASSET_PATH, null, coreAsset);

        train_toClientURL(coreAsset, CORE_ASSET_URL);
        builder.addScriptLink(CORE_ASSET_URL);

        train_toClientURL(asset, ASSET_URL);
        builder.addScriptLink(ASSET_URL);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, symbolSource, assetSource,
                                                              CORE_ASSET_PATH_UNEXPANDED);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script()
    {
        DocumentHeadBuilder builder = mockDocumentScriptBuilder();

        builder.addScript("Tapestry.Foo(\"bar\");");

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, null, null);

        support.addScript("Tapestry.Foo(\"%s\");", "bar");

        verify();
    }

    @Test
    public void add_classpath_script_link()
    {
        String path = "${root}/foo/bar.pdf";
        String expanded = "org/apache/tapestry/foo/bar.pdf";

        DocumentHeadBuilder builder = mockDocumentScriptBuilder();
        Asset asset = mockAsset();
        SymbolSource source = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();

        train_expandSymbols(source, path, expanded);

        train_findAsset(assetSource, null, expanded, null, asset);

        train_toClientURL(asset, ASSET_URL);
        builder.addScriptLink(ASSET_URL);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, source, assetSource);

        support.addClasspathScriptLink(path);

        verify();
    }

    @Test
    public void add_stylesheet_link()
    {
        String media = "print";
        DocumentHeadBuilder builder = mockDocumentScriptBuilder();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        builder.addStylesheetLink(ASSET_URL, media);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, null, null);

        support.addStylesheetLink(asset, media);

        verify();
    }
}
