// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.*;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaScriptSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void allocate_id_from_resources()
    {
        ComponentResources resources = mockComponentResources();

        train_getId(resources, "tracy");

        replay();

        JavaScriptSupport jss = new JavaScriptSupportImpl(null, null, null);

        assertEquals(jss.allocateClientId(resources), "tracy");
        assertEquals(jss.allocateClientId(resources), "tracy_0");
        assertEquals(jss.allocateClientId(resources), "tracy_1");

        verify();
    }

    @Test
    public void commit_with_no_javascript()
    {
        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(null, null, null);

        jss.commit();
    }

    @Test
    public void partial_mode_add_script()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.setInitialization(InitializationPriority.NORMAL, new JSONObject(
                "{ 'evalScript' : [ 'doSomething();' ] }"));

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, null, null, new IdAllocator(), true);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void adding_script_will_add_stack()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForCoreStack(linker, stackSource, pathConstructor);

        linker.addScript(InitializationPriority.IMMEDIATE, "stackInit();");
        linker.addScript(InitializationPriority.NORMAL, "doSomething();");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    private void trainForEmptyCoreStack(DocumentLinker linker, JavaScriptStackSource stackSource,
                                        JavaScriptStackPathConstructor pathConstructor)
    {
        JavaScriptStack stack = mockJavaScriptStack();

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack);
        expect(pathConstructor.constructPathsForJavaScriptStack(InternalConstants.CORE_STACK_NAME)).andReturn(
                Collections.<String>emptyList());
        expect(stack.getStylesheets()).andReturn(Collections.<StylesheetLink>emptyList());

        expect(stack.getInitialization()).andReturn(null);

        expect(stack.getStacks()).andReturn(Collections.<String>emptyList());
    }

    private void trainForCoreStack(DocumentLinker linker, JavaScriptStackSource stackSource,
                                   JavaScriptStackPathConstructor pathConstructor)
    {
        JavaScriptStack stack = mockJavaScriptStack();

        StylesheetLink stylesheetLink = new StylesheetLink("style.css");
        List<String> stackList = CollectionFactory.newList("stack1.js", "stack2.js");

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack);
        expect(pathConstructor.constructPathsForJavaScriptStack(InternalConstants.CORE_STACK_NAME)).andReturn(
                stackList);
        List<StylesheetLink> stylesheetLst = CollectionFactory.newList(stylesheetLink);
        expect(stack.getStylesheets()).andReturn(stylesheetLst);

        expect(stack.getInitialization()).andReturn("stackInit();");

        expect(stack.getStacks()).andReturn(Collections.<String>emptyList());

        linker.addScriptLink("stack1.js");
        linker.addScriptLink("stack2.js");
        linker.addStylesheetLink(stylesheetLink);
    }

    protected final JavaScriptStack mockJavaScriptStack()
    {
        return newMock(JavaScriptStack.class);
    }

    protected final JavaScriptStackPathConstructor mockJavaScriptStackPathConstructor()
    {
        return newMock(JavaScriptStackPathConstructor.class);
    }

    protected final JavaScriptStackSource mockJavaScriptStackSource()
    {
        return newMock(JavaScriptStackSource.class);
    }

    @Test
    public void add_script_passes_thru_to_document_linker()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript(InitializationPriority.IMMEDIATE, "doSomething();");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript(InitializationPriority.IMMEDIATE, "doSomething();");

        verify();
    }

    @Test
    public void import_library()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        trainForNoStackNames(stackSource);

        Asset library = mockAsset("mylib.js");

        linker.addScriptLink("mylib.js");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importJavaScriptLibrary(library);

        jss.commit();

        verify();
    }

    @Test
    public void import_library_from_stack_imports_the_stack()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        Asset library1 = mockAsset("mylib1.js");
        Asset library2 = mockAsset("mylib2.js");

        JavaScriptStack mystack = mockJavaScriptStack();

        expect(stackSource.getStackNames()).andReturn(Arrays.asList("mystack"));
        expect(stackSource.getStack("mystack")).andReturn(mystack).atLeastOnce();

        expect(mystack.getStacks()).andReturn(Collections.<String>emptyList());
        expect(mystack.getJavaScriptLibraries()).andReturn(Arrays.asList(library1, library2));

        expect(pathConstructor.constructPathsForJavaScriptStack("mystack")).andReturn(
                Arrays.asList("stacks/mystack.js"));
        expect(mystack.getStylesheets()).andReturn(Collections.<StylesheetLink>emptyList());

        expect(mystack.getInitialization()).andReturn(null);

        linker.addScriptLink("stacks/mystack.js");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importJavaScriptLibrary(library1);

        jss.commit();

        verify();
    }

    private void trainForNoStackNames(JavaScriptStackSource stackSource)
    {
        // This is slightly odd, as it would normally return "core" at a minimum, but we test for that separately.

        expect(stackSource.getStackNames()).andReturn(Collections.<String>emptyList());
    }

    @Test
    public void import_stack()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();

        trainForCoreStack(linker, stackSource, pathConstructor);

        JavaScriptStack stack = mockJavaScriptStack();

        StylesheetLink stylesheetLink = new StylesheetLink("stack.css");
        List<String> stackLst = CollectionFactory.newList("stack.js");

        expect(stackSource.getStack("custom")).andReturn(stack);
        expect(pathConstructor.constructPathsForJavaScriptStack("custom")).andReturn(
                stackLst);
        List<StylesheetLink> stylesheetLst = CollectionFactory.newList(stylesheetLink);
        expect(stack.getStylesheets()).andReturn(stylesheetLst);

        expect(stack.getInitialization()).andReturn("customInit();");

        List<String> stacks = Collections.emptyList();
        expect(stack.getStacks()).andReturn(stacks);

        linker.addScriptLink("stack.js");
        linker.addStylesheetLink(stylesheetLink);

        linker.addScript(InitializationPriority.IMMEDIATE, "stackInit();");
        linker.addScript(InitializationPriority.IMMEDIATE, "customInit();");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importStack("custom");

        // Duplicate calls are ignored.
        jss.importStack("Custom");

        jss.commit();

        verify();
    }

    @Test
    public void import_stack_with_dependencies()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();

        trainForCoreStack(linker, stackSource, pathConstructor);

        JavaScriptStack child = mockJavaScriptStack();
        JavaScriptStack parent = mockJavaScriptStack();

        StylesheetLink parentStylesheetLink = new StylesheetLink("parent.css");

        StylesheetLink childStylesheetLink = new StylesheetLink("child.css");

        expect(stackSource.getStack("child")).andReturn(child);

        expect(child.getStacks()).andReturn(Arrays.asList("parent"));

        expect(stackSource.getStack("parent")).andReturn(parent);

        expect(pathConstructor.constructPathsForJavaScriptStack("parent")).andReturn(Arrays.asList("parent.js"));
        expect(parent.getStylesheets()).andReturn(Arrays.asList(parentStylesheetLink));

        expect(parent.getInitialization()).andReturn("parentInit();");

        expect(pathConstructor.constructPathsForJavaScriptStack("child")).andReturn(Arrays.asList("child.js"));
        expect(child.getStylesheets()).andReturn(Arrays.asList(childStylesheetLink));

        expect(child.getInitialization()).andReturn("childInit();");

        expect(parent.getStacks()).andReturn(Collections.<String>emptyList());

        linker.addScriptLink("parent.js");
        linker.addScriptLink("child.js");

        linker.addStylesheetLink(parentStylesheetLink);
        linker.addStylesheetLink(childStylesheetLink);

        linker.addScript(InitializationPriority.IMMEDIATE, "stackInit();");
        linker.addScript(InitializationPriority.IMMEDIATE, "parentInit();");
        linker.addScript(InitializationPriority.IMMEDIATE, "childInit();");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importStack("child");

        jss.commit();

        verify();
    }

    @Test
    public void duplicate_imported_libraries_are_filtered()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        trainForNoStackNames(stackSource);

        Asset library1 = mockAsset("mylib1.js");
        Asset library2 = mockAsset("mylib2.js");

        linker.addScriptLink("mylib1.js");
        linker.addScriptLink("mylib2.js");

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importJavaScriptLibrary(library1);
        jss.importJavaScriptLibrary(library2);
        jss.importJavaScriptLibrary(library1);

        jss.commit();

        verify();
    }

    @Test
    public void initialize_calls_are_aggregated_within_priority()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONObject spec1 = new JSONObject("clientId", "chuck");
        JSONObject spec2 = new JSONObject("clientId", "fred");

        JSONObject aggregated = new JSONObject().put("setup", new JSONArray(spec1, spec2));

        linker.setInitialization(InitializationPriority.IMMEDIATE, aggregated);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec1);
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec2);

        jss.commit();

        verify();
    }

    @Test
    public void init_with_string()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONObject aggregated = new JSONObject().put("setup", new JSONArray("chuck", "charley"));

        linker.setInitialization(InitializationPriority.IMMEDIATE, aggregated);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "chuck");
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "charley");

        jss.commit();

        verify();
    }

    @Test
    public void init_with_array()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONArray chuck = new JSONArray("chuck", "yeager");
        JSONArray buzz = new JSONArray("buzz", "aldrin");

        JSONObject aggregated = new JSONObject().put("setup", new JSONArray(chuck, buzz));

        linker.setInitialization(InitializationPriority.IMMEDIATE, aggregated);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", chuck);
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", buzz);

        jss.commit();

        verify();
    }

    @Test
    public void default_for_init_string_is_normal_priority()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONObject aggregated = new JSONObject().put("setup", new JSONArray().put("chuck"));

        linker.setInitialization(InitializationPriority.NORMAL, aggregated);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall("setup", "chuck");

        jss.commit();

        verify();
    }

    @Test
    public void default_for_init_array_is_normal_priority()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavaScriptStackSource stackSource = mockJavaScriptStackSource();
        JavaScriptStackPathConstructor pathConstructor = mockJavaScriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONArray chuck = new JSONArray("chuck", "yeager");

        JSONObject aggregated = new JSONObject().put("setup", new JSONArray(chuck));

        linker.setInitialization(InitializationPriority.NORMAL, aggregated);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall("setup", chuck);

        jss.commit();

        verify();
    }

    @Test
    public void import_stylesheet_as_asset()
    {
        DocumentLinker linker = mockDocumentLinker();
        Asset stylesheet = mockAsset("style.css");

        StylesheetLink link = new StylesheetLink("style.css");
        linker.addStylesheetLink(link);

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, null, null);

        jss.importStylesheet(stylesheet);

        jss.commit();

        verify();
    }

    @Test
    public void duplicate_stylesheet_ignored_first_media_wins()
    {
        DocumentLinker linker = mockDocumentLinker();
        StylesheetOptions options = new StylesheetOptions("print");

        linker.addStylesheetLink(new StylesheetLink("style.css", options));

        replay();

        JavaScriptSupportImpl jss = new JavaScriptSupportImpl(linker, null, null);

        jss.importStylesheet(new StylesheetLink("style.css", options));
        jss.importStylesheet(new StylesheetLink("style.css", new StylesheetOptions("hologram")));

        jss.commit();

        verify();
    }
}
