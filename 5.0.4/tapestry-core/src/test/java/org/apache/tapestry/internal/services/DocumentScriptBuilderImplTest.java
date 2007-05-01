// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.dom.XMLMarkupModel;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class DocumentScriptBuilderImplTest extends InternalBaseTestCase
{
    @Test
    public void do_nothing_if_no_body()
    {
        Document document = new Document();

        document.newRootElement("not-html").text("not an HTML document");

        DocumentScriptBuilder builder = new DocumentScriptBuilderImpl();

        builder.addScript("foo.js");
        builder.addScript("doSomething();");

        builder.updateDocument(document);

        assertEquals(document.toString(), "<not-html>not an HTML document</not-html>");
    }

    @Test
    public void add_script_links() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with scripts.");

        DocumentScriptBuilder builder = new DocumentScriptBuilderImpl();

        builder.addScriptLink("foo.js");
        builder.addScriptLink("bar/baz.js");

        builder.updateDocument(document);

        assertEquals(document.toString(), readFile("add_script_links.txt", true));
    }

    @Test
    public void duplicate_script_links_ignored() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with scripts.");

        DocumentScriptBuilder builder = new DocumentScriptBuilderImpl();

        for (int i = 0; i < 3; i++)
        {
            builder.addScriptLink("foo.js");
            builder.addScriptLink("bar/baz.js");
            builder.addScriptLink("biff.js");
        }

        builder.updateDocument(document);

        assertEquals(document.toString(), readFile("duplicate_script_links_ignored.txt", true));
    }

    @Test
    public void add_script() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with scripts.");

        DocumentScriptBuilder builder = new DocumentScriptBuilderImpl();

        builder.addScript("doSomething();");
        builder.addScript("doSomethingElse();");

        builder.updateDocument(document);

        assertEquals(document.toString(), readFile("add_script.txt", false).trim());
    }
}
