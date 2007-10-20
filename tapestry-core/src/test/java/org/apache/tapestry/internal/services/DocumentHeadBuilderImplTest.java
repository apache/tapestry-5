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

public class DocumentHeadBuilderImplTest extends InternalBaseTestCase
{
    private void check(Document document, String file) throws Exception
    {
        assertEquals(document.toString(), readFile(file, true));
    }

    @Test
    public void do_nothing_if_no_body()
    {
        Document document = new Document();

        document.newRootElement("not-html").text("not an HTML document");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

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

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addScriptLink("foo.js");
        builder.addScriptLink("bar/baz.js");

        builder.updateDocument(document);

        check(document, "add_script_links.txt");
    }

    @Test
    public void add_style_links() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with styles.");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addStylesheetLink("foo.css", null);
        builder.addStylesheetLink("bar/baz.css", "print");

        builder.updateDocument(document);

        check(document, "add_style_links.txt");
    }

    @Test
    public void duplicate_scripts_ignored_first_media_wins() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with styles.");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addStylesheetLink("foo.css", null);
        builder.addStylesheetLink("bar/baz.css", "print");
        builder.addStylesheetLink("foo.css", "implant");
        builder.addStylesheetLink("bar/baz.css", null);
        builder.addStylesheetLink("bar/baz.css", "duplicate");

        builder.updateDocument(document);

        check(document, "duplicate_scripts_ignored_first_media_wins.txt");
    }

    @Test
    public void existing_head_used_if_present() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("head").comment("existing head").getParent()
                .element("body").text("body content");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addStylesheetLink("foo.css", null);

        builder.updateDocument(document);

        check(document, "existing_head_used_if_present.txt");
    }

    @Test
    public void duplicate_script_links_ignored() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with scripts.");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        for (int i = 0; i < 3; i++)
        {
            builder.addScriptLink("foo.js");
            builder.addScriptLink("bar/baz.js");
            builder.addScriptLink("biff.js");
        }

        builder.updateDocument(document);

        check(document, "duplicate_script_links_ignored.txt");
    }

    @Test
    public void add_script() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text(
                "Ready to be updated with scripts.");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addScript("doSomething();");
        builder.addScript("doSomethingElse();");

        builder.updateDocument(document);

      assertEquals(document.toString(), readFile("add_script.txt", false).trim());
    }

    /**
     * Perhaps the builder should create the &lt;body&gt; element in this case? In the meantime,
     */
    @Test
    public void no_body_element() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("notbody").element("p").text(
                "Ready to be updated with scripts.");

        DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

        builder.addScriptLink("foo.js");

        builder.updateDocument(document);

        check(document, "no_body_element.txt");
    }
}
