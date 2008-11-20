// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class DocumentLinkerImplTest extends InternalBaseTestCase
{

    private void check(Document document, String file) throws Exception
    {
        assertEquals(document.toString(), readFile(file));
    }

    @Test
    public void exception_if_missing_html_root_element_and_javascript()
    {
        Document document = new Document();

        document.newRootElement("not-html").text("not an HTML document");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        // Only checked if there's something to link.

        linker.addScript("foo.js");
        linker.addScript("doSomething();");

        try
        {
            linker.updateDocument(document);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "The root element of the rendered document was <not-html>, not <html>. A root element of <html> is needed when linking JavaScript and stylesheet resources.");
        }
    }

    @Test
    public void logged_error_if_missing_html_element_and_css()
    {
        Document document = new Document();

        document.newRootElement("not-html").text("not an HTML document");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        // Only checked if there's something to link.

        linker.addStylesheetLink("style.css", null);

        replay();

        linker.updateDocument(document);

        // Check that document is unchanged.

        assertEquals(document.toString(), "<not-html>not an HTML document</not-html>");

        verify();
    }

    @Test
    public void missing_root_element_is_a_noop()
    {
        Document document = new Document();

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addScript("foo.js");
        linker.addScript("doSomething();");

        // No root element is not an error, even though there's work to do.
        // The failure to render is reported elsewhere.
        linker.updateDocument(document);
    }

    @Test
    public void add_script_links() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar/baz.js");
        linker.addScript("pageInitialization();");

        linker.updateDocument(document);

        check(document, "add_script_links.txt");
    }

    @Test
    public void empty_document_with_scripts_at_top() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, true);

        linker.addStylesheetLink("style.css", "print");
        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar/baz.js");
        linker.addScript("pageInitialization();");

        linker.updateDocument(document);

        check(document, "empty_document_with_scripts_at_top.txt");
    }

    @Test
    public void empty_document_with_scripts_at_bottom() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addStylesheetLink("style.css", "print");
        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar/baz.js");
        linker.addScript("pageInitialization();");

        linker.updateDocument(document);

        check(document, "empty_document_with_scripts_at_bottom.txt");
    }

    @Test
    public void add_script_links_at_top() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts at top.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, true);

        linker.addScriptLink("foo.js");
        linker.addScriptLink("bar/baz.js");
        linker.addScript("pageInitialization();");

        linker.updateDocument(document);

        check(document, "add_script_links_at_top.txt");
    }

    @Test
    public void add_style_links() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with styles.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addStylesheetLink("foo.css", null);
        linker.addStylesheetLink("bar/baz.css", "print");

        linker.updateDocument(document);

        check(document, "add_style_links.txt");
    }

    @Test
    public void duplicate_scripts_ignored_first_media_wins() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with styles.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addStylesheetLink("foo.css", null);
        linker.addStylesheetLink("bar/baz.css", "print");
        linker.addStylesheetLink("foo.css", "implant");
        linker.addStylesheetLink("bar/baz.css", null);
        linker.addStylesheetLink("bar/baz.css", "duplicate");

        linker.updateDocument(document);

        check(document, "duplicate_scripts_ignored_first_media_wins.txt");
    }

    @Test
    public void existing_head_used_if_present() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("head").comment("existing head").getParent()
                .element("body").text("body content");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addStylesheetLink("foo.css", null);

        linker.updateDocument(document);

        check(document, "existing_head_used_if_present.txt");
    }

    @Test
    public void duplicate_script_links_ignored() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        for (int i = 0; i < 3; i++)
        {
            linker.addScriptLink("foo.js");
            linker.addScriptLink("bar/baz.js");
            linker.addScriptLink("biff.js");
        }

        linker.updateDocument(document);

        check(document, "duplicate_script_links_ignored.txt");
    }

    @Test
    public void add_script() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addScript("doSomething();");
        linker.addScript("doSomethingElse();");

        linker.updateDocument(document);

        assertEquals(document.toString(), readFile("add_script.txt").trim());
    }

    @Test
    public void add_script_in_development_mode() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(false, false);

        linker.addScriptLink("foo.js");

        linker.updateDocument(document);

        assertEquals(document.toString(), readFile("add_script_in_development_mode.txt").trim());
    }

    /**
     * Perhaps the linker should create the &lt;body&gt; element in this case? In the meantime,
     */
    @Test
    public void no_body_element() throws Exception
    {
        Document document = new Document(new XMLMarkupModel());

        document.newRootElement("html").element("notbody").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addScriptLink("foo.js");

        linker.updateDocument(document);

        check(document, "no_body_element.txt");
    }

    @Test
    public void script_written_raw() throws Exception
    {
        Document document = new Document();

        document.newRootElement("html").element("body").element("p").text("Ready to be updated with scripts.");

        DocumentLinkerImpl linker = new DocumentLinkerImpl(true, false);

        linker.addScript("for (var i = 0; i < 5; i++)  { doIt(i); }");

        linker.updateDocument(document);

        assertEquals(document.toString(), readFile("script_written_raw.txt").trim());
    }
}
