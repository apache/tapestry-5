// Copyright 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.testng.annotations.Test;

import java.util.Arrays;

public class RenderSupportImplTest extends InternalBaseTestCase
{
    private static final String ASSET_URL = "/assets/foo/bar.pdf";

    private static final EmptyClientInfrastructure EMPTY_CLIENT_INFRASTRUCTURE = new EmptyClientInfrastructure();

    @Test
    public void add_script_link_by_asset()
    {
        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script_link_by_url()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addScriptLink(ASSET_URL);

        verify();
    }

    @Test
    public void core_assets_added()
    {
        getMocksControl().checkOrder(true);

        String coreURL1 = "/foo/core1.js";
        String coreURL2 = "/foo/core2.js";

        Asset asset = mockAsset();

        DocumentLinker linker = mockDocumentLinker();

        Asset coreAsset1 = mockAsset();
        Asset coreAsset2 = mockAsset();

        AssetSource assetSource = mockAssetSource();
        SymbolSource symbolSource = mockSymbolSource();

        ClientInfrastructure infrastructure = mockJavascriptStack(coreAsset1, coreAsset2);

        train_toClientURL(coreAsset1, coreURL1);
        linker.addScriptLink(coreURL1);

        train_toClientURL(coreAsset2, coreURL2);
        linker.addScriptLink(coreURL2);

        train_toClientURL(asset, ASSET_URL);
        linker.addScriptLink(ASSET_URL);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, symbolSource, assetSource, infrastructure);

        support.addScriptLink(asset);

        verify();
    }

    @Test
    public void add_script()
    {
        String coreScript = "corescript.js";

        DocumentLinker linker = mockDocumentLinker();
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        Asset coreAsset = mockAsset(coreScript);
        ClientInfrastructure infrastructure = mockJavascriptStack(coreAsset);

        linker.addScriptLink(coreScript);
        linker.addScript("Tapestry.Foo(\"bar\");");

        replay();

        RenderSupport support = new RenderSupportImpl(linker, symbolSource, assetSource, infrastructure);

        support.addScript("Tapestry.Foo(\"%s\");", "bar");

        verify();
    }

    // TAPESTRY-2483

    @Test
    public void add_script_no_formatting()
    {
        String coreScript = "corescript.js";
        DocumentLinker linker = mockDocumentLinker();
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        Asset coreAsset = mockAsset(coreScript);

        ClientInfrastructure infrastructure = mockJavascriptStack(coreAsset);

        linker.addScriptLink(coreScript);

        String script = "foo('%');";

        linker.addScript(script);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, symbolSource, assetSource, infrastructure);

        support.addScript(script);

        verify();
    }

    protected final ClientInfrastructure mockJavascriptStack(Asset... asset)
    {
        ClientInfrastructure infrastructure = newMock(ClientInfrastructure.class);

        expect(infrastructure.getJavascriptStack()).andReturn(Arrays.asList(asset)).atLeastOnce();

        return infrastructure;
    }

    protected final Asset mockAsset(String assetURL)
    {
        Asset asset = mockAsset();

        train_toClientURL(asset, assetURL);

        return asset;
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

        RenderSupport support = new RenderSupportImpl(linker, source, assetSource, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addClasspathScriptLink(path);

        verify();
    }

    @Test
    public void add_stylesheet_link_by_asset()
    {
        String media = "print";
        DocumentLinker linker = mockDocumentLinker();
        Asset asset = mockAsset();

        train_toClientURL(asset, ASSET_URL);
        linker.addStylesheetLink(ASSET_URL, media);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addStylesheetLink(asset, media);

        verify();
    }

    @Test
    public void add_stylesheet_link_by_url()
    {
        String media = "print";
        DocumentLinker linker = mockDocumentLinker();

        linker.addStylesheetLink(ASSET_URL, media);

        replay();

        RenderSupport support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addStylesheetLink(ASSET_URL, media);

        verify();
    }

    @Test
    public void add_init_with_single_string_parameter()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("Tapestry.init({\"foo\":[\"fred\",\"barney\"]});");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

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

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.addInit("foo", "fred", "barney");

        support.commit();

        verify();
    }

    @Test
    public void field_focus()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("$('foo').activate();");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.autofocus(FieldFocusPriority.OPTIONAL, "foo");

        support.commit();

        verify();
    }

    @Test
    public void first_focus_field_at_priority_wins()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("$('foo').activate();");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.autofocus(FieldFocusPriority.OPTIONAL, "foo");
        support.autofocus(FieldFocusPriority.OPTIONAL, "bar");

        support.commit();

        verify();
    }

    @Test
    public void higher_priority_wins_focus()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("$('bar').activate();");

        replay();

        RenderSupportImpl support = new RenderSupportImpl(linker, null, null, EMPTY_CLIENT_INFRASTRUCTURE);

        support.autofocus(FieldFocusPriority.OPTIONAL, "foo");
        support.autofocus(FieldFocusPriority.REQUIRED, "bar");

        support.commit();

        verify();
    }
}
