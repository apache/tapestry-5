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
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.services.AssetFactory;
import org.testng.annotations.Test;

public class PageRenderSupportImplTest extends InternalBaseTestCase
{
    private static final String ASSET_URL = "/assets/foo/bar.pdf";

    @Test
    public void add_script_link_by_asset()
    {
        DocumentScriptBuilder builder = newDocumentScriptBuilder();
        Asset asset = newAsset();

        train_toClientURL(asset, ASSET_URL);
        builder.addScriptLink(ASSET_URL);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, null, null);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script()
    {
        DocumentScriptBuilder builder = newDocumentScriptBuilder();

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

        DocumentScriptBuilder builder = newDocumentScriptBuilder();
        Asset asset = newAsset();
        Resource root = newResource();
        Resource file = newResource();
        SymbolSource source = newSymbolSource();
        AssetFactory factory = newAssetFactory();

        train_expandSymbols(source, path, expanded);

        train_getRootResource(factory, root);
        train_forFile(root, expanded, file);

        train_createAsset(factory, file, asset);

        train_toClientURL(asset, ASSET_URL);
        builder.addScriptLink(ASSET_URL);

        replay();

        PageRenderSupport support = new PageRenderSupportImpl(builder, source, factory);

        support.addClasspathScriptLink(path);

        verify();
    }
}
