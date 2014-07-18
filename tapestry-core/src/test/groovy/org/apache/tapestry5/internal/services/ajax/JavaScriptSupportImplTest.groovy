package org.apache.tapestry5.internal.services.ajax

import org.apache.tapestry5.Asset
import org.apache.tapestry5.BooleanHook
import org.apache.tapestry5.ComponentResources
import org.apache.tapestry5.internal.InternalConstants
import org.apache.tapestry5.internal.services.DocumentLinker
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.ioc.util.IdAllocator
import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.services.javascript.*
import org.testng.annotations.Test

class JavaScriptSupportImplTest extends InternalBaseTestCase {

    class StaticHook implements BooleanHook {

        final boolean value

        StaticHook(value) { this.value = value }

        @Override
        boolean checkHook() { value }
    }

    def falseHook = new StaticHook(false)

    @Test
    void allocate_id_from_resources() {
        ComponentResources resources = mockComponentResources()

        expect(resources.id).andReturn("tracy").atLeastOnce()

        replay()

        JavaScriptSupport jss = new JavaScriptSupportImpl(null, null, null, null)

        assertEquals(jss.allocateClientId(resources), "tracy")
        assertEquals(jss.allocateClientId(resources), "tracy_0")
        assertEquals(jss.allocateClientId(resources), "tracy_1")

        verify()
    }

    @Test
    void commit_with_no_javascript() {
        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(null, null, null, null)

        jss.commit()
    }

    @Test
    void partial_mode_add_script() {
        DocumentLinker linker = mockDocumentLinker()
        def stackSource = mockJavaScriptStackSource()

        train_for_just_core_stack stackSource

        linker.addInitialization(InitializationPriority.NORMAL, "t5/core/pageinit", "evalJavaScript",
            new JSONArray().put("doSomething();"))

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, null, new IdAllocator(), true, null)

        jss.addScript("doSomething();")

        jss.commit()

        verify()
    }

    protected final JavaScriptStack mockJavaScriptStack() {
        return newMock(JavaScriptStack.class)
    }

    protected final JavaScriptStackPathConstructor mockJavaScriptStackPathConstructor() {
        return newMock(JavaScriptStackPathConstructor.class)
    }

    protected final JavaScriptStackSource mockJavaScriptStackSource() {
        return newMock(JavaScriptStackSource.class)
    }

    protected final train_for_empty_core_stack(stackSource, pathConstructor) {
        JavaScriptStack stack = mockJavaScriptStack()

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack)

        expect(stack.initialization).andReturn(null)

        expect(stack.stacks).andReturn([])
        expect(stack.stylesheets).andReturn([])

        expect(pathConstructor.constructPathsForJavaScriptStack(InternalConstants.CORE_STACK_NAME)).andReturn([])
    }

    @Test
    void add_script_passes_thru_to_document_linker() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_empty_core_stack stackSource, pathConstructor

        linker.addScript(InitializationPriority.IMMEDIATE, "doSomething();")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, falseHook)

        jss.addScript(InitializationPriority.IMMEDIATE, "doSomething();")

        verify()
    }

    @Test
    void import_library() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        trainForNoStackNames(stackSource)

        Asset library = mockAsset("mylib.js")

        linker.addLibrary("mylib.js")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.importJavaScriptLibrary(library)

        jss.commit()

        verify()
    }

    @Test
    void import_library_from_stack_imports_the_stack() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        Asset library1 = mockAsset("mylib1.js")
        Asset library2 = mockAsset("mylib2.js")

        JavaScriptStack mystack = mockJavaScriptStack()

        expect(stackSource.stackNames).andReturn(["mystack"])
        expect(stackSource.getStack("mystack")).andReturn(mystack).atLeastOnce()

        expect(mystack.stacks).andReturn([])
        expect(mystack.javaScriptLibraries).andReturn([library1, library2])

        expect(pathConstructor.constructPathsForJavaScriptStack("mystack")).andReturn(["stacks/mystack.js"])

        expect(mystack.stylesheets).andReturn([])

        expect(mystack.initialization).andReturn null

        linker.addLibrary("stacks/mystack.js")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.importJavaScriptLibrary(library1)

        jss.commit()

        verify()
    }

    @Test
    void core_stack_stylesheets_may_be_suppressed() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        JavaScriptStack stack = mockJavaScriptStack()

        def cssLink = new StylesheetLink("foo.css", null)

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack)

        expect(stack.stacks).andReturn([])

        // NO class to getStylesheets, because its the core stack and the hook is true.

        expect(pathConstructor.constructPathsForJavaScriptStack(InternalConstants.CORE_STACK_NAME)).andReturn([])

        expect(stack.initialization).andReturn null

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, false, new StaticHook(true))

        jss.importStack(InternalConstants.CORE_STACK_NAME)

        jss.commit()

        verify()

    }

    @Test
    void requireing_a_module_may_import_a_stack() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        JavaScriptStack mystack = mockJavaScriptStack()

        expect(stackSource.stackNames).andReturn(["mystack"])
        expect(stackSource.getStack("mystack")).andReturn(mystack).atLeastOnce()

        expect(mystack.modules).andReturn(["foo/bar"])

        expect(mystack.stacks).andReturn([])

        expect(pathConstructor.constructPathsForJavaScriptStack("mystack")).andReturn(["stacks/mystack.js"])

        expect(mystack.stylesheets).andReturn([])

        expect(mystack.initialization).andReturn null

        linker.addLibrary("stacks/mystack.js")

        linker.addInitialization(InitializationPriority.NORMAL, "foo/bar", null, null)

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.require("foo/bar")

        jss.commit()

        verify()
    }


    private void trainForNoStackNames(JavaScriptStackSource stackSource) {
        // This is slightly odd, as it would normally return "core" at a minimum, but we test for that separately.

        expect(stackSource.getStackNames()).andReturn([])
    }

    @Test
    void import_stack() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_empty_core_stack stackSource, pathConstructor

        JavaScriptStack stack = mockJavaScriptStack()

        StylesheetLink stylesheetLink = new StylesheetLink("stack.css")

        expect(stackSource.getStack("custom")).andReturn(stack)
        expect(pathConstructor.constructPathsForJavaScriptStack("custom")).andReturn(["stack.js"])
        expect(stack.stylesheets).andReturn([stylesheetLink])
        expect(stack.initialization).andReturn "customInit();"

        expect(stack.stacks).andReturn([])

        linker.addLibrary("stack.js")
        linker.addStylesheetLink(stylesheetLink)

        linker.addScript(InitializationPriority.IMMEDIATE, "customInit();")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, falseHook)

        jss.importStack("custom")

        // Duplicate calls are ignored.
        jss.importStack("Custom")

        jss.commit()

        verify()
    }

    @Test
    void import_stack_with_dependencies() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_empty_core_stack stackSource, pathConstructor

        JavaScriptStack child = mockJavaScriptStack()
        JavaScriptStack parent = mockJavaScriptStack()

        StylesheetLink parentStylesheetLink = new StylesheetLink("parent.css")

        StylesheetLink childStylesheetLink = new StylesheetLink("child.css")

        expect(stackSource.getStack("child")).andReturn(child)

        expect(child.stacks).andReturn(["parent"])

        expect(stackSource.getStack("parent")).andReturn(parent)

        expect(pathConstructor.constructPathsForJavaScriptStack("parent")).andReturn(["parent.js"])
        expect(parent.stylesheets).andReturn([parentStylesheetLink])

        expect(parent.initialization).andReturn("parentInit();")

        expect(pathConstructor.constructPathsForJavaScriptStack("child")).andReturn(["child.js"])
        expect(child.stylesheets).andReturn([childStylesheetLink])

        expect(child.getInitialization()).andReturn("childInit();")

        expect(parent.getStacks()).andReturn([])

        linker.addLibrary("parent.js")
        linker.addLibrary("child.js")

        linker.addStylesheetLink(parentStylesheetLink)
        linker.addStylesheetLink(childStylesheetLink)

        linker.addScript(InitializationPriority.IMMEDIATE, "parentInit();")
        linker.addScript(InitializationPriority.IMMEDIATE, "childInit();")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, falseHook)

        jss.importStack("child")

        jss.commit()

        verify()
    }

    @Test
    void duplicate_imported_libraries_are_filtered() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        trainForNoStackNames(stackSource)

        Asset library1 = mockAsset("mylib1.js")
        Asset library2 = mockAsset("mylib2.js")

        linker.addLibrary("mylib1.js")
        linker.addLibrary("mylib2.js")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.importJavaScriptLibrary(library1)
        jss.importJavaScriptLibrary(library2)
        jss.importJavaScriptLibrary(library1)

        jss.commit()

        verify()
    }

    @Test
    void init_with_string() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_just_core_stack stackSource

        train_init(linker, InitializationPriority.IMMEDIATE, "setup", "chuck")
        train_init(linker, InitializationPriority.IMMEDIATE, "setup", "charley")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "chuck")
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "charley")

        jss.commit()

        verify()
    }

    @Test
    void init_with_array() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        JSONArray chuck = new JSONArray("chuck", "yeager")
        JSONArray buzz = new JSONArray("buzz", "aldrin")

        train_for_just_core_stack stackSource

        train_init(linker, InitializationPriority.IMMEDIATE, "setup", "chuck", "yeager")
        train_init(linker, InitializationPriority.IMMEDIATE, "setup", "buzz", "aldrin")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", chuck)
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", buzz)

        jss.commit()

        verify()
    }

    def train_init(DocumentLinker linker, InitializationPriority priority, String initName, Object... arguments) {
        JSONArray initArgs = new JSONArray().put(initName)

        arguments.each { initArgs.put it }

        linker.addInitialization(priority, "t5/core/init", null, initArgs)
    }

    @Test
    void default_for_init_string_is_normal_priority() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_just_core_stack stackSource

        train_init(linker, InitializationPriority.NORMAL, "setup", "chuck")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, null)

        jss.addInitializerCall("setup", "chuck")

        jss.commit()

        verify()
    }

    def train_for_just_core_stack(stackSource) {
        def coreStack = mockJavaScriptStack()

        expect(stackSource.stackNames).andReturn(["core"])

        expect(stackSource.getStack("core")).andReturn(coreStack)

        expect(coreStack.modules).andReturn([])
    }


    @Test
    void default_for_init_array_is_normal_priority() {
        DocumentLinker linker = mockDocumentLinker()
        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        JSONArray chuck = new JSONArray("chuck", "yeager")

        train_for_just_core_stack stackSource

        train_init(linker, InitializationPriority.NORMAL, "setup", "chuck", "yeager")

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, null, true, falseHook)

        jss.addInitializerCall("setup", chuck)

        jss.commit()

        verify()
    }

    @Test
    void import_stylesheet_as_asset() {
        DocumentLinker linker = mockDocumentLinker()
        Asset stylesheet = mockAsset("style.css")

        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_empty_core_stack stackSource, pathConstructor

        StylesheetLink link = new StylesheetLink("style.css")
        linker.addStylesheetLink(link)

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, falseHook)

        jss.importStylesheet(stylesheet)

        jss.commit()

        verify()
    }

    @Test
    void duplicate_stylesheet_ignored_first_media_wins() {
        DocumentLinker linker = mockDocumentLinker()
        StylesheetOptions options = new StylesheetOptions("print")

        JavaScriptStackSource stackSource = mockJavaScriptStackSource()
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor()

        train_for_empty_core_stack stackSource, pathConstructor

        linker.addStylesheetLink(new StylesheetLink("style.css", options))

        replay()

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor, falseHook)

        jss.importStylesheet(new StylesheetLink("style.css", options))
        jss.importStylesheet(new StylesheetLink("style.css", new StylesheetOptions("hologram")))

        jss.commit()

        verify()
    }
}
