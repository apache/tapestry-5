package org.apache.tapestry5.internal.services;


import org.apache.tapestry5.Asset
import org.apache.tapestry5.FieldFocusPriority
import org.apache.tapestry5.RenderSupport
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.ioc.services.SymbolSource
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.services.AssetSource
import org.apache.tapestry5.services.javascript.JavaScriptSupport
import org.apache.tapestry5.services.javascript.StylesheetLink
import org.apache.tapestry5.services.javascript.StylesheetOptions
import org.testng.annotations.Test

class RenderSupportImplTest extends InternalBaseTestCase {

    def ASSET_URL = "/assets/foo/bar.pdf"

    @Test
    void add_script_link_by_asset() {
        JavaScriptSupport js = mockJavaScriptSupport()
        Asset asset = mockAsset()

        expect(js.importJavaScriptLibrary(asset)).andReturn(js)

        replay()

        RenderSupport support = new RenderSupportImpl(null, null, js)

        support.addScriptLink(asset)

        verify()
    }

    @Test
    void add_script_link_by_url() {
        JavaScriptSupport jss = mockJavaScriptSupport()

        RenderSupport support = new RenderSupportImpl(null, null, jss)

        expect(jss.importJavaScriptLibrary(ASSET_URL)).andReturn(jss)

        replay()

        support.addScriptLink(ASSET_URL)

        verify()
    }

    @Test
    void add_script() {
        JavaScriptSupport js = mockJavaScriptSupport()

        js.addScript("doSomething();")

        replay()

        RenderSupport support = new RenderSupportImpl(null, null, js)

        support.addScript("doSomething();")

        verify()
    }

    @Test
    void add_classpath_script_link() {
        String path = '${root}/foo/bar.pdf'
        String expanded = "org/apache/tapestry5/foo/bar.pdf";

        Asset asset = mockAsset()
        SymbolSource source = mockSymbolSource()
        AssetSource assetSource = mockAssetSource()
        JavaScriptSupport js = mockJavaScriptSupport()

        train_expandSymbols(source, path, expanded)

        train_getAsset(assetSource, null, expanded, null, asset)

        expect(js.importJavaScriptLibrary(asset)).andReturn(js)

        replay()

        RenderSupport support = new RenderSupportImpl(source, assetSource, js)

        support.addClasspathScriptLink(path)

        verify()
    }

    @Test
    void add_stylesheet_link_by_asset() {
        String media = "print";
        JavaScriptSupport javascriptSupport = mockJavaScriptSupport()
        Asset asset = mockAsset("foo.css")

        expect(javascriptSupport.importStylesheet(new StylesheetLink("foo.css", new StylesheetOptions(media)))).andReturn(javascriptSupport)

        replay()

        RenderSupport support = new RenderSupportImpl(null, null, javascriptSupport)

        support.addStylesheetLink(asset, media)

        verify()
    }

    @Test
    void add_stylesheet_link_by_url() {
        String media = "print";
        JavaScriptSupport javascriptSupport = mockJavaScriptSupport()

        expect(javascriptSupport.importStylesheet(new StylesheetLink(ASSET_URL, new StylesheetOptions(media)))).andReturn(javascriptSupport)

        replay()

        RenderSupport support = new RenderSupportImpl(null, null, javascriptSupport)

        support.addStylesheetLink(ASSET_URL, media)

        verify()
    }

    @Test
    void add_multiple_string_init_parameters() {
        JavaScriptSupport js = mockJavaScriptSupport()

        JSONArray array = new JSONArray("fred", "barney")

        js.addInitializerCall("foo", array)

        replay()

        RenderSupportImpl support = new RenderSupportImpl(null, null, js)

        support.addInit("foo", "fred", "barney")

        verify()
    }

    @Test
    void addInit_passes_through_to_JavaScriptSupport() {
        JSONObject parameter = new JSONObject("clientid", "fred")

        JavaScriptSupport js = mockJavaScriptSupport()

        js.addInitializerCall("setup", parameter)

        replay()

        RenderSupportImpl support = new RenderSupportImpl(null, null, js)

        support.addInit("setup", parameter)

        verify()
    }

    @Test
    void autofocus_pass_thru_to_javascriptsupport() {
        JavaScriptSupport js = mockJavaScriptSupport()

        expect(js.autofocus(FieldFocusPriority.OVERRIDE, "fred")).andReturn(js)

        replay()

        RenderSupportImpl support = new RenderSupportImpl(null, null, js)

        support.autofocus(FieldFocusPriority.OVERRIDE, "fred")

        verify()
    }
}
