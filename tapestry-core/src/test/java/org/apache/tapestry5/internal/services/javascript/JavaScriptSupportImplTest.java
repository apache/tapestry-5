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

package org.apache.tapestry5.internal.services.javascript;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavascriptSupport;
import org.testng.annotations.Test;

public class JavaScriptSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void allocate_id_from_resources()
    {
        ComponentResources resources = mockComponentResources();

        train_getId(resources, "tracy");

        replay();

        JavascriptSupport jss = new JavascriptSupportImpl(null, null);

        assertEquals(jss.allocateClientId(resources), "tracy");
        assertEquals(jss.allocateClientId(resources), "tracy_0");
        assertEquals(jss.allocateClientId(resources), "tracy_1");

        verify();
    }

    @Test
    public void commit_with_no_javascript()
    {
        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.commit();

        verify();
    }

    @Test
    public void no_stack_or_dom_loading_callback_in_partial_mode()
    {
        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        linker.addScript("doSomething();\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra, new IdAllocator(), true);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void adding_script_will_add_stack()
    {
        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("Tapestry.onDOMLoaded(function() {\ndoSomething();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.addScript("doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void only_immediate_script_added()
    {
        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("doSomething();\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.addScript(InitializationPriority.IMMEDIATE, "doSomething();");

        jss.commit();

        verify();
    }

    @Test
    public void script_within_priority_accumulates()
    {
        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker
                .addScript("immediate1();\nimmediate2();\nTapestry.onDOMLoaded(function() {\nnormal1();\nnormal2();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

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
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("Tapestry.onDOMLoaded(function() {\nearly();\nnormal();\nlate();\n});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

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
        ClientInfrastructure infra = mockClientInfrastucture();
        Asset library = mockAsset("mylib.js");

        train_for_stack(infra, linker);

        linker.addScriptLink("mylib.js");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.importJavascriptLibrary(library);

        jss.commit();

        verify();
    }

    @Test
    public void init_once()
    {
        JSONObject spec = new JSONObject("clientId", "chuck");

        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("Tapestry.init({\"setup\":[{\"clientId\":\"chuck\"}]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec);

        jss.commit();

        verify();
    }

    @Test
    public void init_multiple()
    {
        JSONObject spec1 = new JSONObject("clientId", "chuck");
        JSONObject spec2 = new JSONObject("clientId", "tony");

        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("Tapestry.init({\"setup\":[{\"clientId\":\"chuck\"},{\"clientId\":\"tony\"}]});\n");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec1);
        jss.addInitializerCall(InitializationPriority.IMMEDIATE, "setup", spec2);

        jss.commit();

        verify();
    }

    @Test
    public void init_default_is_normal()
    {

        DocumentLinker linker = mockDocumentLinker();
        ClientInfrastructure infra = mockClientInfrastucture();

        train_for_stack(infra, linker);

        linker.addScript("Tapestry.onDOMLoaded(function() {\nTapestry.init({\"early\":[{\"id\":\"foo\"}]});\n"
                + "Tapestry.init({\"normal\":[{\"id\":\"bar\"}]});\n"
                + "Tapestry.init({\"late\":[{\"id\":\"baz\"}]});\n" + "});");

        replay();

        JavascriptSupportImpl jss = new JavascriptSupportImpl(linker, infra);

        jss.addInitializerCall(InitializationPriority.EARLY, "early", new JSONObject("id", "foo"));
        jss.addInitializerCall("normal", new JSONObject("id", "bar"));
        jss.addInitializerCall(InitializationPriority.LATE, "late", new JSONObject("id", "baz"));

        jss.commit();

        verify();
    }

    private Asset mockAsset(String clientURL)
    {
        Asset asset = mockAsset();

        train_toClientURL(asset, clientURL);

        return asset;
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
