package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Asset
import org.apache.tapestry5.dom.Document
import org.apache.tapestry5.dom.Element
import org.apache.tapestry5.dom.XMLMarkupModel
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.ioc.Resource
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.services.javascript.InitializationPriority
import org.apache.tapestry5.services.javascript.ModuleManager
import org.apache.tapestry5.services.javascript.StylesheetLink
import org.apache.tapestry5.services.javascript.StylesheetOptions
import org.testng.annotations.Test

class DocumentLinkerImplTest extends InternalBaseTestCase {

    def check(Document document, String file) throws Exception {

        def content = readFile(file)
        assert document.toString() == content
    }

    @Test
    void exception_if_missing_html_root_element_and_javascript() {
        Document document = new Document()

        document.newRootElement("not-html").text("not an HTML document")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

        // Only checked if there's something to link.

        linker.addStylesheetLink(new StylesheetLink("style.css"))

        replay()

        linker.updateDocument(document)

        // Check that document is unchanged.

        assert document.toString() == "<not-html>not an HTML document</not-html>"

        verify()
    }

    @Test
    void missing_root_element_is_a_noop() {
        Document document = new Document()

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

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

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addScriptLink("foo.js")
        linker.addScriptLink("bar/baz.js")
        linker.addScript(InitializationPriority.NORMAL, "pageInitialization();")

        linker.updateDocument(document)

        check(document, "add_script_links.txt")

        verify()
    }

    /**
     * TAP5-446
     */
    @Test
    void include_generator_meta() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be marked with generator meta.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, false, "1.2.3", true)

        linker.updateDocument(document)

        check(document, "include_generator_meta.txt")
    }

    /**
     * TAP5-584
     */
    @Test
    void omit_generator_meta_on_no_html_root() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("no_html").text("Generator meta only added if root is html tag.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, false, "1.2.3", true)

        linker.updateDocument(document)

        check(document, "omit_generator_meta_on_no_html_root.txt")
    }

    @Test
    void empty_document_with_scripts_at_top() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addStylesheetLink(new StylesheetLink("style.css", new StylesheetOptions("print")))
        linker.addScriptLink("foo.js")
        linker.addScriptLink("bar/baz.js")
        linker.addScript(InitializationPriority.IMMEDIATE, "pageInitialization();")

        linker.updateDocument(document)

        check(document, "empty_document_with_scripts_at_top.txt")

        verify()
    }

    @Test
    void add_script_links_at_top() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts at top.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addScriptLink("foo.js")
        linker.addScriptLink("bar/baz.js")
        linker.addScript(InitializationPriority.NORMAL, "pageInitialization();")

        linker.updateDocument(document)

        check(document, "add_script_links_at_top.txt")

        verify()
    }

    @Test
    void add_style_links() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with styles.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, mockRequireJS(), true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("foo.css"))
        linker.addStylesheetLink(new StylesheetLink("bar/baz.css", new StylesheetOptions("print")))

        linker.updateDocument(document)

        check(document, "add_style_links.txt")
    }

    @Test
    void existing_head_used_if_present() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("head").comment(" existing head ").container.element("body").text(
            "body content")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, mockRequireJS(), true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("foo.css"))

        linker.updateDocument(document)

        check(document, "existing_head_used_if_present.txt")
    }

    @Test
    void add_script() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addScript(InitializationPriority.IMMEDIATE, "doSomething();")
        linker.addScript(InitializationPriority.IMMEDIATE, "doSomethingElse();")

        linker.updateDocument(document)

        check(document, "add_script.txt")

        verify()
    }

    /**
     * Perhaps the linker should create the &lt;body&gt; element in this case? In the meantime,
     */
    @Test
    void no_body_element() throws Exception {
        Document document = new Document(new XMLMarkupModel())

        document.newRootElement("html").element("notbody").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, mockRequireJS(), true, "1.2.3", true)

        linker.addScriptLink("foo.js")

        linker.updateDocument(document)

        check(document, "no_body_element.txt")
    }

    @Test
    void script_written_raw() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addScript(InitializationPriority.IMMEDIATE, "for (var i = 0; i < 5; i++)  { doIt(i); }")

        linker.updateDocument(document)

        check(document, "script_written_raw.txt")

        verify()
    }

    @Test
    void non_asset_script_link_disables_aggregation() throws Exception {
        Document document = new Document()

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

        linker.addScriptLink("/context/foo.js")

        linker.updateDocument(document)

        Element script = document.getRootElement().find("head/script")

        String assetURL = script.getAttribute("src")

        assertEquals(assetURL, "/context/foo.js")
    }

    @Test
    void added_scripts_go_before_existing_script() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

        linker.addScriptLink("/foo.js")

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("added_scripts_go_before_existing_script.txt"))
    }

    @Test
    void immediate_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.IMMEDIATE, "immediate/module", "myfunc", new JSONArray("[{ 'fred' : 'barney' }]"))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("immediate_initialization.txt"))

        verify()
    }

    @Test
    void pretty_print_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", false)

        replay()

        linker.setInitialization(InitializationPriority.IMMEDIATE, new JSONObject().put("fred", new JSONArray("barney",
            "wilma", "betty")))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("pretty_print_initialization.txt"))

        verify()
    }

    @Test
    void other_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")
        head.element("script")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, new JSONArray("['barney']"))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("other_initialization.txt"))

        verify()
    }

    @Test
    void ie_conditional_stylesheet() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, null, true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("everybody.css"))
        linker.addStylesheetLink(new StylesheetLink("just_ie.css", new StylesheetOptions().withCondition("IE")))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("ie_conditional_stylesheet.txt"))
    }

    @Test
    void stylesheet_insertion_point() throws Exception {
        Document document = new Document()

        document.newRootElement("html")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(null, mockRequireJS(), true, "1.2.3", true)

        linker.addStylesheetLink(new StylesheetLink("whatever.css"))
        linker.addStylesheetLink(new StylesheetLink("insertion-point.css", new StylesheetOptions().asAjaxInsertionPoint()))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("stylesheet_insertion_point.txt"))
    }

    @Test
    void module_based_initialization() throws Exception {
        Document document = new Document()

        Element head = document.newRootElement("html").element("head")

        head.element("meta")

        DocumentLinkerImpl linker = new DocumentLinkerImpl(mockModuleManager(), mockRequireJS(), true, "1.2.3", true)

        replay()

        linker.addInitialization(InitializationPriority.NORMAL, "my/module", null, null)
        linker.addInitialization(InitializationPriority.NORMAL, "my/other/module", "normal", new JSONArray(111, 222))
        linker.addInitialization(InitializationPriority.LATE, "my/other/module", "late", new JSONArray(333, 444))

        linker.updateDocument(document)

        assertEquals(document.toString(), readFile("module_based_initialization.txt"))

    }

    private ModuleManager mockModuleManager() {
        return new ModuleManager() {

            @Override
            void writeConfiguration(Element scriptElement) {
                scriptElement.raw("require.config();\n")
            }

            @Override
            public Resource findResourceForModule(String moduleName) {
                return null;
            }
        };
    }

    private Asset mockRequireJS() {
        Asset mock = newMock(Asset.class)

        expect(mock.toClientURL()).andReturn("/js/require.js").once()

        return mock;
    }
}
