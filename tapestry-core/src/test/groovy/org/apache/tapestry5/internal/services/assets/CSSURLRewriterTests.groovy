package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.Asset
import org.apache.tapestry5.commons.Resource
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.services.AssetSource
import org.testng.annotations.Test


class CSSURLRewriterTests extends InternalBaseTestCase {

    @Test
    void no_urls_in_content_returns_null() {
        def input = '''
body {
  color: red;
}
'''

        def rewriter = new CSSURLRewriter(null, null, null, null, true)

        assertNull rewriter.replaceURLs(input, null)
    }

    @Test
    void simple_replacement() {
        def input = '''
body {
  background: white url("images/back.png") attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/back.png", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/back.png"

        replay()


        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/ctx/images/back.png") attach-x;
}
'''

        verify()

    }

    // See TAP5-2106
    @Test
    void query_parameters_in_relative_url_are_maintained() {
        def input = '''
body {
  background: white url("images/back.png?v=1.0.0") attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/back.png", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/back.png"

        replay()

        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/ctx/images/back.png?v=1.0.0") attach-x;
}
'''


    }

    @Test
    void unquoted_urls_are_matched() {
        def input = '''
body {
  background: white url(images/back.png) attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/back.png", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/back.png"

        replay()


        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/ctx/images/back.png") attach-x;
}
'''

        verify()

    }

    @Test
    void asset_urls_are_expanded() {
        def input = '''
body {
  background: white url("asset:context:images/back.png") attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "context:images/back.png", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/back.png"

        replay()


        def rewriter = new CSSURLRewriter(null, null, assetSource, null, false)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/ctx/images/back.png") attach-x;
}
'''

        verify()

    }

    @Test
    void absolute_urls_not_replaced() {
        def input = '''
body {
  background: white url("/images/back.png") attach-x;
}
'''

        def rewriter = new CSSURLRewriter(null, null, null, null, true)

        assertNull rewriter.replaceURLs(input, null)
    }

    @Test
    void complete_urls_are_not_replaced() {
        def input = '''
body {
  background: white url("data:image/png;base64,CODE64A") attach-x;
}
'''

        def rewriter = new CSSURLRewriter(null, null, null, null, true)

        assertNull rewriter.replaceURLs(input, null)
    }

    @Test
    void multiple_urls_per_line() {

        def input = '''
body {
  src: url('font/fontawesome-webfont.eot?#iefix&v=3.1.0') format('embedded-opentype'), url('font/fontawesome-webfont.woff?v=3.1.0') format('woff'), url('font/fontawesome-webfont.ttf?v=3.1.0') format('truetype'), url('font/fontawesome-webfont.svg#fontawesomeregular?v=3.1.0') format('svg');
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource

        ["fontawesome-webfont.eot", "fontawesome-webfont.woff", "fontawesome-webfont.ttf", "fontawesome-webfont.svg"].each { name ->

            def asset = newMock Asset

            expect(
                assetSource.getAsset(resource, "font/$name", null)
            ).andReturn asset

            expect(asset.toClientURL()).andReturn "/ctx/font/$name".toString()
        }

        replay()


        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  src: url("/ctx/font/fontawesome-webfont.eot?#iefix&v=3.1.0") format('embedded-opentype'), url("/ctx/font/fontawesome-webfont.woff?v=3.1.0") format('woff'), url("/ctx/font/fontawesome-webfont.ttf?v=3.1.0") format('truetype'), url("/ctx/font/fontawesome-webfont.svg#fontawesomeregular?v=3.1.0") format('svg');
}
'''
        verify()
    }


    @Test
    void vml_urls_are_not_replaced() {
        def input = '''
span {
  behavior: url(#default#VML);
}
'''

        def rewriter = new CSSURLRewriter(null, null, null, null, true)

        assertNull rewriter.replaceURLs(input, null)
    }

    @Test
    void absolute_urls_passed_through_unchanged() {

        def input = '''
body {
  background: white url("/images/back.png") attach-x;
}

div.busy {
  background-image: url( "images/ajax.gif" );
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/ajax.gif", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/ajax.gif"

        replay()

        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/images/back.png") attach-x;
}

div.busy {
  background-image: url("/ctx/images/ajax.gif");
}
'''

    }


    @Test
    void query_parameters_in_absolute_url_are_maintained() {

        def input = '''
@import url(https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic);

div.busy {
  background-image: url( "images/ajax.gif" );
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/ajax.gif", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/ajax.gif"

        replay()

        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
@import url("https://fonts.googleapis.com/css?family=Lato:400,700,400italic,700italic");

div.busy {
  background-image: url("/ctx/images/ajax.gif");
}
'''


    }

    // See TAP5-2187    
    @Test
    void dont_fail_when_rewritten_url_is_not_found() {
        def input = '''
body {
  background: white url("images/back.png") attach-x;
}
h1 {
  background: white url("images/i_dont_exist.png") attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource
        def asset = newMock Asset

        expect(
            assetSource.getAsset(resource, "images/back.png", null)
        ).andReturn asset

        expect(asset.toClientURL()).andReturn "/ctx/images/back.png"
        expect(resource.toURL()).andReturn new java.net.URL("file:/home/you/layout.css")
        
        expect(
            assetSource.getAsset(resource, "images/i_dont_exist.png", null)
        ).andReturn null

        replay()


        def rewriter = new CSSURLRewriter(null, null, assetSource, null, false)

        def output = rewriter.replaceURLs input, resource

        assertEquals output, '''
body {
  background: white url("/ctx/images/back.png") attach-x;
}
h1 {
  background: white url("images/i_dont_exist.png") attach-x;
}
'''
        verify()

    }
    
    // See TAP5-2187
    @Test(expectedExceptions = RuntimeException.class)
    void strict_css_url_rewriting() {
        def input = '''
h1 {
  background: white url("images/i_dont_exist.png") attach-x;
}
'''

        def assetSource = newMock AssetSource
        def resource = newMock Resource

        expect(
            assetSource.getAsset(resource, "images/i_dont_exist.png", null)
        ).andReturn null
        expect(resource.toURL()).andReturn new java.net.URL("file:/home/you/layout.css")
    
        replay()

        def rewriter = new CSSURLRewriter(null, null, assetSource, null, true) 
        
        // should throw an exception here
        rewriter.replaceURLs input, resource

    }

}
