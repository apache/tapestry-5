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

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.ajax.JavascriptSupportImpl;
import org.apache.tapestry5.internal.services.javascript.JavascriptStackPathConstructor;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavascriptStack;
import org.apache.tapestry5.services.javascript.JavascriptStackSource;
import org.apache.tapestry5.services.javascript.JavascriptSupport;
import org.testng.annotations.Test;

public class JavascriptSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void allocate_id_from_resources()
    {
        ComponentResources resources = mockComponentResources();

        train_getId(resources, "tracy");

        replay();

        JavascriptSupport jss = new JavascriptSupportImpl(null, null, null);

        assertEquals(jss.allocateClientId(resources), "tracy");
        assertEquals(jss.allocateClientId(resources), "tracy_0");
        assertEquals(jss.allocateClientId(resources), "tracy_1");

        verify();
    }

    @Test
    public void commit_with_no_javascript()
    {
        JavascriptSupportImpl jss = new JavascriptSupportImpl(null, null, null);

        jss.commit();
    }

    @Test
    public void no_stack_or_dom_loading_callback_in_partial_mode()
    {
        DocumentLinker linker = mockDocumentLinker();

        linker.addScript("doSomething();\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, null, null, new IdAllocator(), true);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void adding_script_will_add_stack()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("stackInit();\nTapestry.onDOMLoaded(function() {\ndoSomething();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    private void trainForEmptyCoreStack(DocumentLinker linker, JavascriptStackSource stackSource,
            JavascriptStackPathConstructor pathConstructor)
    {
        JavascriptStack stack = mockJavascriptStack();

        List<String> libraryPaths = Collections.emptyList();
        List<Asset> stylesheets = Collections.emptyList();

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack);
        expect(pathConstructor.constructPathsForJavascriptStack(InternalConstants.CORE_STACK_NAME)).andReturn(
                libraryPaths);
        expect(stack.getStylesheets()).andReturn(stylesheets);

        expect(stack.getInitialization()).andReturn(null);
    }

    private void trainForCoreStack(DocumentLinker linker, JavascriptStackSource stackSource,
            JavascriptStackPathConstructor pathConstructor)
    {
        JavascriptStack stack = mockJavascriptStack();

        Asset stylesheet = mockAsset("style.css");

        expect(stackSource.getStack(InternalConstants.CORE_STACK_NAME)).andReturn(stack);
        expect(pathConstructor.constructPathsForJavascriptStack(InternalConstants.CORE_STACK_NAME)).andReturn(
                CollectionFactory.newList("stack1.js", "stack2.js"));
        expect(stack.getStylesheets()).andReturn(CollectionFactory.newList(stylesheet));

        expect(stack.getInitialization()).andReturn("stackInit();");

        linker.addScriptLink("stack1.js");
        linker.addScriptLink("stack2.js");
        linker.addStylesheetLink("style.css", null);
    }

    private JavascriptStack mockJavascriptStack()
    {
        return newMock(JavascriptStack.class);
    }

    private JavascriptStackPathConstructor mockJavascriptStackPathConstructor()
    {
        return newMock(JavascriptStackPathConstructor.class);
    }

    protected JavascriptStackSource mockJavascriptStackSource()
    {
        return newMock(JavascriptStackSource.class);
    }

    @Test
    public void only_immediate_script_added()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("doSomething();\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript(InitializationPriority.IMMEDIATE, "doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void script_within_priority_accumulates()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker
                .addScript("immediate1();\nimmediate2();\nTapestry.onDOMLoaded(function() {\nnormal1();\nnormal2();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript(InitializationPriority.IMMEDIATE, "immediate1();");
        jss.addScript("normal1();");
        jss.addScript(InitializationPriority.IMMEDIATE, "immediate2();");
        jss.addScript("normal2();");

        jss.commit();

        verify();
    }

    @Test
    public void priority_order()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("stackInit();\nTapestry.onDOMLoaded(function() {\nearly();\nnormal();\nlate();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addScript(InitializationPriority.EARLY, "early();");
        jss.addScript(InitializationPriority.NORMAL, "normal();");
        jss.addScript(InitializationPriority.LATE, "late();");

        jss.commit();

        verify();
    }

    @Test
    public void import_library()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        Asset library = mockAsset("mylib.js");

        linker.addScriptLink("mylib.js");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.importJavascriptLibrary(library);

        jss.commit();

        verify();
    }

    @Test
    public void init_once()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONObject spec = new JSONObject("clientId", "chuck");

        linker.addScript("Tapestry.init({\"setup\":[{\"clientId\":\"chuck\"}]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec);

        jss.commit();

        verify();
    }

    @Test
    public void init_with_string()
    {

        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("Tapestry.init({\"setup\":[\"chuck\"]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "chuck");

        jss.commit();

        verify();
    }

    @Test
    public void init_with_string_multiple()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("Tapestry.init({\"setup\":[\"chuck\",\"pat\"]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "chuck");
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", "pat");

        jss.commit();

        verify();
    }

    @Test
    public void default_for_init_string_is_normal_priority()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("Tapestry.onDOMLoaded(function() {\nTapestry.init({\"setup\":[\"chuck\"]});\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall("setup", "chuck");

        jss.commit();

        verify();
    }

    @Test
    public void init_multiple()
    {
        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        JSONObject spec1 = new JSONObject("clientId", "chuck");
        JSONObject spec2 = new JSONObject("clientId", "tony");

        linker.addScript("Tapestry.init({\"setup\":[{\"clientId\":\"chuck\"},{\"clientId\":\"tony\"}]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec1);
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec2);

        jss.commit();

        verify();
    }

    @Test
    public void init_default_is_normal()
    {

        DocumentLinker linker = mockDocumentLinker();
        JavascriptStackSource stackSource = mockJavascriptStackSource();
        JavascriptStackPathConstructor pathConstructor = mockJavascriptStackPathConstructor();
        trainForEmptyCoreStack(linker, stackSource, pathConstructor);

        linker.addScript("Tapestry.onDOMLoaded(function() {\nTapestry.init({\"early\":[{\"id\":\"foo\"}]});\n"
                + "Tapestry.init({\"normal\":[{\"id\":\"bar\"}]});\n"
                + "Tapestry.init({\"late\":[{\"id\":\"baz\"}]});\n" + "});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, stackSource, pathConstructor);

        jss.addInitializerCall(InitializationPriority.EARLY, "early", new JSONObject("id", "foo"));
        jss.addInitializerCall("normal", new JSONObject("id", "bar"));
        jss.addInitializerCall(InitializationPriority.LATE, "late", new JSONObject("id", "baz"));

        jss.commit();

        verify();
    }

    private void train_for_stack(ClientInfrastructure infra, DocumentLinker linker)
    {
        Asset asset1 = mockAsset("script1.js");
        Asset asset2 = mockAsset("script2.js");

        List<Asset> assets = CollectionFactory.newList(asset1, asset2);

        expect(infra.getJavascriptStack()).andReturn(assets);

        linker.addScriptLink("script1.js");
        linker.addScriptLink("script2.js");
    }

}
