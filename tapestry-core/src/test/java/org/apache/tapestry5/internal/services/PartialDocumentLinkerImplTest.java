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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PartialDocumentLinkerImplTest extends Assert
{
    @Test
    public void script_link_uniqueness()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar.js");
        linker.addScriptLink("foo.js");

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        assertEquals(reply.toString(), "{\"scripts\":[\"foo.js\",\"bar.js\"]}");
    }

    @Test
    public void stylesheet_link_uniqueness()
    {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

        linker.addStylesheetLink("foo.css", null);
        linker.addStylesheetLink("bar.css", "print");
        linker.addStylesheetLink("bar.css", "screen");
        linker.addStylesheetLink("foo.css", null);

        JSONObject reply = new JSONObject();

        linker.commit(reply);

        // JDK version affect this (order of attributes), so it's the hard way.

        JSONObject expected = new JSONObject().put("stylesheets", new JSONArray(new JSONObject()
                .put("href", "foo.css"), new JSONObject().put("media", "print").put("href",
                "bar.css")));

        assertEquals(reply, expected);
    }
}
