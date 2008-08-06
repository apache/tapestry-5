// Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PartialMarkupDocumentLinkerTest extends Assert
{
    @Test
    public void script()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addScript("foo();");
        linker.addScript("bar();");

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        assertEquals(reply.get("script"), "foo();\nbar();\n");
    }

    @Test
    public void script_link()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar.js");

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        assertEquals(reply.toString(), "{\"scripts\":[\"foo.js\",\"bar.js\"]}");

    }

    @Test
    public void stylesheet_link()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addStylesheetLink("foo.css", "print");
        linker.addStylesheetLink("bar.css", null);

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        assertEquals(reply.toString(),
                     "{\"stylesheets\":[{\"href\":\"foo.css\",\"media\":\"print\"},{\"href\":\"bar.css\"}]}");

    }
}
