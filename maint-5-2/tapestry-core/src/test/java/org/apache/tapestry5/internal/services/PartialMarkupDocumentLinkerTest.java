// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PartialMarkupDocumentLinkerTest extends Assert
{
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void script()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addScript(InitializationPriority.NORMAL, "foo();");

        throw new IllegalStateException("Unreachable code.");
    }

    @Test
    public void script_link()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar.js");

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        assertEquals(reply.toCompactString(), "{\"scripts\":[\"foo.js\",\"bar.js\"]}");

    }

    @Test
    public void stylesheet_link()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addStylesheetLink(new StylesheetLink("foo.css", new StylesheetOptions("print")));
        linker.addStylesheetLink(new StylesheetLink("bar.css"));

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        JSONObject expected = new JSONObject(
                "{\"stylesheets\":[{\"href\":\"foo.css\",\"media\":\"print\"},{\"href\":\"bar.css\"}]}");

        assertEquals(reply, expected);
    }

    @Test
    public void set_initialization()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        JSONObject spec1 = new JSONObject("order", "immediate");
        JSONObject spec2 = new JSONObject("order", "normal");

        JSONObject reply = new JSONObject();

        linker.setInitialization(InitializationPriority.NORMAL, spec2);
        linker.setInitialization(InitializationPriority.IMMEDIATE, spec1);

        linker.commit(reply);

        JSONObject expected = new JSONObject().put("inits", new JSONArray(spec1, spec2));

        assertEquals(reply, expected);
    }
}
