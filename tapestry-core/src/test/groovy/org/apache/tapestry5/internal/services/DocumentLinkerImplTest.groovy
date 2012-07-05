package org.apache.tapestry5.internal.services;


import org.apache.tapestry5.dom.Document
import org.apache.tapestry5.dom.Element
import org.apache.tapestry5.dom.XMLMarkupModel
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.services.javascript.InitializationPriority
import org.apache.tapestry5.services.javascript.ModuleManager
import org.apache.tapestry5.services.javascript.StylesheetLink
import org.apache.tapestry5.services.javascript.StylesheetOptions
import org.easymock.EasyMock
import org.easymock.IAnswer
import org.testng.annotations.Test

class DocumentLinkerImplTest extends InternalBaseTestCase {

    def check(Document document, String expectedContent) throws Exception {

        // TestNG's assertEquals() is actually more useful here than Groovy's assert. Normally,
        // it's the other way around.

        assertEquals document.toString(), expectedContent.trim()
    }

    @Test
    void exception_if_missing_html_root_element_and_javascript() {
        Document document = new Document()

        document.newRootElement("not-html").text("not an HTML document")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        // Only checked if there's something to link.

        linker.addScriptLink("foo.js")
        linker.addScript(InitializationPriority.NORMAL, "doSomething();")

        try {
            linker.updateDocument(document)
            unreachable()
        } catch (RuntimeException ex) {

            assert ex.message ==
                "The root element of the rendered document was <not-html>, not <html>. A root element of <html> is needed when linking JavaScript and stylesheet resources."
        }
    }

    @Test
    void logged_error_if_missing_html_element_and_css() {
        Document document = new Document()

        document.newRootElement("not-html").text("not an HTML document")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        // Only checked if there's something to link.

        linker.addStylesheetLink(new StylesheetLink("style.css"))

        replay()

        linker.updateDocument(document)

        // Check that document is unchanged.

        check document, "<not-html>not an HTML document</not-html>"

        verify()
    }

    @Test
    void missing_root_element_is_a_noop() {
        Document document = new Document()

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        linker.addScriptLink("foo.js")
        linker.addScript(InitializationPriority.NORMAL, "doSomething();")

        // No root element is not an error, even though there's work to do.
        // The failure to render is reported elsewhere.
        linker.updateDocument(document)
    }

    @Test
    void add_script_links() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addScriptLink("foo.js")
        linker.addScriptLink("bar/baz.js")
        linker.addScript(InitializationPriority.NORMAL, "pageInitialization();")

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><body><p>Ready to be updated with scripts.</p><!--MODULE-MANAGER-INITIALIZATION--><script src="foo.js" type="text/javascript"/><script src="bar/baz.js" type="text/javascript"/><script type="text/javascript">Tapestry.onDOMLoaded(function() {
pageInitialization();
});
</script></body></html>'''

        verify()
    }

    /**
     * TAP5-446
     */
    @Test
    void include_generator_meta() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be marked with generator meta.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, false, "1.2.3", true)

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><meta content="Apache Tapestry Framework (version 1.2.3)" name="generator"/></head><body><p>Ready to be marked with generator meta.</p></body></html>
'''
    }

    /**
     * TAP5-584
     */
    @Test
    void omit_generator_meta_on_no_html_root() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("no_html").text("Generator meta only added if root is html tag.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, false, "1.2.3", true)

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<no_html>Generator meta only added if root is html tag.</no_html>
'''
    }


    @Test
    void add_style_links() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with styles.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("foo.css"))
        linker.addStylesheetLink(new StylesheetLink("bar/baz.css", new StylesheetOptions("print")))

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><link type="text/css" rel="stylesheet" href="foo.css"/><link media="print" type="text/css" rel="stylesheet" href="bar/baz.css"/></head><body><p>Ready to be updated with styles.</p></body></html>
'''
    }

    @Test
    void existing_head_used_if_present() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("head").comment(" existing head ").container.element("body").text(
            "body content")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("foo.css"))

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><!-- existing head --><link type="text/css" rel="stylesheet" href="foo.css"/></head><body>body content</body></html>
'''
    }

    @Test
    void add_script() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addScript(InitializationPriority.IMMEDIATE, "doSomething();")
        linker.addScript(InitializationPriority.IMMEDIATE, "doSomethingElse();")

        linker.updateDocument(document)

        check document, '''
<html><body><p>Ready to be updated with scripts.</p><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">doSomething();
doSomethingElse();
</script></body></html>
'''

        verify()
    }

    /**
     * Perhaps the linker should create the &lt;body&gt; element in this case? In the meantime,
     */
    @Test
    void no_body_element() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("notbody").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addScriptLink("foo.js")

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><notbody><p>Ready to be updated with scripts.</p></notbody><body><!--MODULE-MANAGER-INITIALIZATION--><script src="foo.js" type="text/javascript"/></body></html>
'''

        verify()
    }

    @Test
    void script_written_raw() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addScript(InitializationPriority.IMMEDIATE, "for (var i = 0; i < 5; i++)  { doIt(i); }")

        linker.updateDocument(document)

        check document, '''
<html><body><p>Ready to be updated with scripts.</p><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">for (var i = 0; i < 5; i++)  { doIt(i); }
</script></body></html>
'''

        verify()
    }

    @Test
    void non_asset_script_link_disables_aggregation() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addScriptLink("/context/foo.js")

        linker.updateDocument(document)

        assert document.toString().contains('''<script src="/context/foo.js" type="text/javascript">''')

        verify()
    }


    @Test
    void immediate_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.IMMEDIATE, "immediate/module", "myfunc", new JSONArray("[{ 'fred' : 'barney' }]"))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/><script></script></head><body><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([["immediate/module:myfunc",{"fred":"barney"}]]);
});
</script></body></html>
'''

        verify()
    }

    @Test
    void pretty_print_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", false)

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, null)
        linker.addInitialization(InitializationPriority.NORMAL, "my/other/module", "normal", new JSONArray(111, 222))
        linker.addInitialization(InitializationPriority.LATE, "my/other/module", "late", new JSONArray(333, 444))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/></head><body><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">Tapestry.onDOMLoaded(function() {
require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([[
  "my/module"
],
  [
  "my/other/module:normal",
  111,
  222
]]);
});
require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([[
  "my/other/module:late",
  333,
  444
]]);
});
});
</script></body></html>
'''
    }

    @Test
    void other_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, new JSONArray("['barney']"))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/><script></script></head><body><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">Tapestry.onDOMLoaded(function() {
require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([["my/module","barney"]]);
});
});
</script></body></html>
'''
        verify()
    }

    @Test
    void ie_conditional_stylesheet() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("everybody.css"))
        linker.addStylesheetLink(new StylesheetLink("just_ie.css", new StylesheetOptions().withCondition("IE")))

        linker.updateDocument(document)

        check document, '''
<html><head><link type="text/css" rel="stylesheet" href="everybody.css"/>
<!--[if IE]>
<link type="text/css" rel="stylesheet" href="just_ie.css"/>
<![endif]-->
</head></html>
'''
    }

    @Test
    void stylesheet_insertion_point() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("whatever.css"))
        linker.addStylesheetLink(new StylesheetLink("insertion-point.css", new StylesheetOptions().asAjaxInsertionPoint()))

        linker.updateDocument(document)

        check document, '''
<html><head><link type="text/css" rel="stylesheet" href="whatever.css"/><link type="text/css" rel="stylesheet t-ajax-insertion-point" href="insertion-point.css"/></head></html>
'''
    }

    @Test
    void module_based_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, null)
        linker.addInitialization(InitializationPriority.NORMAL, "my/other/module", "normal", new JSONArray(111, 222))
        linker.addInitialization(InitializationPriority.LATE, "my/other/module", "late", new JSONArray(333, 444))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/></head><body><!--MODULE-MANAGER-INITIALIZATION--><script type="text/javascript">Tapestry.onDOMLoaded(function() {
require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([["my/module"],
  ["my/other/module:normal",111,222]]);
});
require(["core/pageinit"], function (pageinit) {
  pageinit.initialize([["my/other/module:late",333,444]]);
});
});
</script></body></html>
'''

        verify()
    }

    private ModuleManager mockModuleManager() {

        ModuleManager mock = newMock(ModuleManager);

        expect(mock.writeInitialization(EasyMock.isA(Element))).andAnswer({
            def body = EasyMock.currentArguments[0]

            body.comment("MODULE-MANAGER-INITIALIZATION")
        } as IAnswer).once()

        return mock;
    }
}
