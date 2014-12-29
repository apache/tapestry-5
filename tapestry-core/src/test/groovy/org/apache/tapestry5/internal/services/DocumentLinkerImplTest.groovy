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

import static org.easymock.EasyMock.eq
import static org.easymock.EasyMock.isA

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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        // Only checked if there's something to link.

        linker.addLibrary("foo.js")
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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        linker.addLibrary("foo.js")
        linker.addScript(InitializationPriority.NORMAL, "doSomething();")

        // No root element is not an error, even though there's work to do.
        // The failure to render is reported elsewhere.
        linker.updateDocument(document)
    }

    @Test
    void add_script_links() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        def manager = mockModuleManager(["core.js", "foo.js", "bar/baz.js"], [new JSONArray("t5/core/pageinit:evalJavaScript", "pageINIT();")])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, false, "1.2.3")

        replay()

        linker.addLibrary("core.js")
        linker.addLibrary("foo.js")
        linker.addLibrary("bar/baz.js")
        linker.addScript(InitializationPriority.NORMAL, "pageINIT();")

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><body data-page-initialized="false"><p>Ready to be updated with scripts.</p><!--MM-CONFIG--><!--MM-INIT--></body></html>
'''

        verify()
    }

    /**
     * TAP5-446
     */
    @Test
    void include_generator_meta() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be marked with generator meta.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, false, false, "1.2.3")

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><meta content="Apache Tapestry Framework (version 1.2.3)" name="generator"/></head><body data-page-initialized="true"><p>Ready to be marked with generator meta.</p></body></html>
'''
    }

    /**
     * TAP5-584
     */
    @Test
    void omit_generator_meta_on_no_html_root() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("no_html").text("Generator meta only added if root is html tag.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, false, false, "1.2.3")

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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        linker.addStylesheetLink(new StylesheetLink("foo.css"))
        linker.addStylesheetLink(new StylesheetLink("bar/baz.css", new StylesheetOptions("print")))

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><link type="text/css" rel="stylesheet" href="foo.css"/><link media="print" type="text/css" rel="stylesheet" href="bar/baz.css"/></head><body data-page-initialized="true"><p>Ready to be updated with styles.</p></body></html>
'''
    }

    @Test
    void existing_head_used_if_present() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("head").comment(" existing head ").container.element("body").text(
            "body content")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        linker.addStylesheetLink(new StylesheetLink("foo.css"))

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><head><!-- existing head --><link type="text/css" rel="stylesheet" href="foo.css"/></head><body data-page-initialized="true">body content</body></html>
'''
    }

    @Test
    void add_script() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        def manager = mockModuleManager([], [new JSONArray("t5/core/pageinit:evalJavaScript", "doSomething();")])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, true, "1.2.3")

        replay()

        linker.addScript(InitializationPriority.IMMEDIATE, "doSomething();")

        linker.updateDocument(document)

        check document, '''
<html><body data-page-initialized="false"><script type="text/javascript">document.write("<div class=\\"pageloading-mask\\"><div></div></div>");</script><p>Ready to be updated with scripts.</p><!--MM-CONFIG--><!--MM-INIT--></body></html>
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

        def manager = mockModuleManager(["foo.js"], [])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, false, "1.2.3")

        replay()

        linker.addLibrary("foo.js")

        linker.updateDocument(document)

        check document, '''
<?xml version="1.0"?>
<html><notbody><p>Ready to be updated with scripts.</p></notbody><body data-page-initialized="false"><!--MM-CONFIG--><!--MM-INIT--></body></html>
'''

        verify()
    }

    @Test
    void immediate_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        def manager = mockModuleManager([], [new JSONArray("['immediate/module:myfunc', {'fred':'barney'}]")])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, false, "1.2.3")

        replay()

        linker.addInitialization(InitializationPriority.IMMEDIATE, "immediate/module", "myfunc", new JSONArray("[{ 'fred' : 'barney' }]"))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/><script></script></head><body data-page-initialized="false"><!--MM-CONFIG--><!--MM-INIT--></body></html>
'''

        verify()
    }


    @Test
    void ie_conditional_stylesheet() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        linker.addStylesheetLink(new StylesheetLink("everybody.css"))
        linker.addStylesheetLink(new StylesheetLink("just_ie.css", new StylesheetOptions().withCondition("IE")))

        linker.updateDocument(document)

        check document, '''
<html><head><link type="text/css" rel="stylesheet" href="everybody.css"/>
<!--[if IE]>
<link type="text/css" rel="stylesheet" href="just_ie.css"/>
<![endif]-->
</head><body data-page-initialized="true"></body></html>
'''
    }

    @Test
    void stylesheet_insertion_point() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, true, false, "1.2.3")

        linker.addStylesheetLink(new StylesheetLink("whatever.css"))
        linker.addStylesheetLink(new StylesheetLink("insertion-point.css", new StylesheetOptions().asAjaxInsertionPoint()))

        linker.updateDocument(document)

        check document, '''
<html><head><link type="text/css" rel="stylesheet" href="whatever.css"/><link type="text/css" rel="stylesheet ajax-insertion-point" href="insertion-point.css"/></head><body data-page-initialized="true"></body></html>
'''
    }

    @Test
    void module_based_INIT() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")

        def manager = mockModuleManager([], ["my/module",
            new JSONArray("my/other/module:normal", 111, 222),
            new JSONArray("my/other/module:late", 333, 444)])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, false, "1.2.3")

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, null)
        linker.addInitialization(InitializationPriority.NORMAL, "my/other/module", "normal", new JSONArray(111, 222))
        linker.addInitialization(InitializationPriority.LATE, "my/other/module", "late", new JSONArray(333, 444))

        linker.updateDocument(document)

        check document, '''
<html><head><meta/></head><body data-page-initialized="false"><!--MM-CONFIG--><!--MM-INIT--></body></html>
'''

        verify()
    }

    @Test
    void module_INIT_with_no_parameters_coalesce() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")

        def manager = mockModuleManager([], ["my/module",
            new JSONArray("my/other/module:normal", 111, 222)])

        DocumentLinkerImpl linker = new DocumentLinkerImpl(manager, true, false, "1.2.3")

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, null)
        linker.addInitialization(InitializationPriority.NORMAL, "my/other/module", "normal", new JSONArray(111, 222))
        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, new JSONArray());

        linker.updateDocument(document)

        check document, '''
<html><head><meta/></head><body data-page-initialized="false"><!--MM-CONFIG--><!--MM-INIT--></body></html>
'''

        verify()
    }

    private ModuleManager mockModuleManager(def libraryURLs, def inits) {

        ModuleManager mock = newMock(ModuleManager);

        expect(mock.writeConfiguration(isA(Element),
            eq([]))).andAnswer({
            def body = EasyMock.currentArguments[0]

            body.comment("MM-CONFIG")
        } as IAnswer).once()

        expect(mock.writeInitialization(isA(Element),
            eq(libraryURLs),
            eq(inits))).andAnswer({
            def body = EasyMock.currentArguments[0];

            body.comment("MM-INIT");
        } as IAnswer).once()


        return mock;
    }
}
