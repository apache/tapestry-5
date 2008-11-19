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
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.List;
import java.util.Set;

public class DocumentLinkerImpl implements DocumentLinker
{
    private final List<String> scripts = CollectionFactory.newList();

    private final StringBuilder scriptBlock = new StringBuilder();

    private final Set<String> stylesheets = CollectionFactory.newSet();

    private final List<IncludedStylesheet> includedStylesheets = CollectionFactory.newList();

    private final boolean developmentMode;

    private final boolean scriptsAtTop;

    public DocumentLinkerImpl(boolean productionMode, boolean scriptsAtTop)
    {
        developmentMode = !productionMode;
        this.scriptsAtTop = scriptsAtTop;
    }

    public void addStylesheetLink(String styleURL, String media)
    {
        if (stylesheets.contains(styleURL)) return;

        includedStylesheets.add(new IncludedStylesheet(styleURL, media));

        stylesheets.add(styleURL);
    }

    public void addScriptLink(String scriptURL)
    {
        if (scripts.contains(scriptURL)) return;

        scripts.add(scriptURL);
    }

    public void addScript(String script)
    {
        if (InternalUtils.isBlank(script)) return;

        scriptBlock.append(script);
        scriptBlock.append("\n");
    }

    /**
     * Updates the supplied Document, locating the html/body element and adding script links (to the top) and a script
     * block (to the end).
     *
     * @param document to be updated
     */
    public void updateDocument(Document document)
    {
        Element root = document.getRootElement();

        // If the document failed to render entirely, that's a different problem and is reported elsewhere.        
        if (root == null) return;


        if (!stylesheets.isEmpty())
            addStylesheetsToHead(root, includedStylesheets);

        addScriptElementsToBody(root);
    }

    private void addScriptElementsToBody(Element root)
    {
        if (scripts.isEmpty() && scriptBlock.length() == 0) return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        if (!rootElementName.equals("html"))
            throw new RuntimeException(ServicesMessages.documentMissingHTMLRoot(rootElementName));

        Element body = root.find("body");

        // Create the body element is it is somehow missing.

        if (body == null) body = root.element("body");

        // TAPESTRY-2364

        addScriptLinksForIncludedScripts(body, scripts);

        addDynamicScriptBlock(body);
    }

    /**
     * Adds the dynamic script block, which is, ultimately, a call to the client-side Tapestry.onDOMLoaded() function.
     *
     * @param body element to add the dynamic scripting to
     */
    protected void addDynamicScriptBlock(Element body)
    {
        boolean blockNeeded = (developmentMode && !scripts.isEmpty()) || scriptBlock.length() > 0;

        if (blockNeeded)
        {
            Element e = body.element("script", "type", "text/javascript");
            e.raw("\n<!--\n");

            if (developmentMode)
                e.raw("Tapestry.DEBUG_ENABLED = true;\n");

            e.raw("Tapestry.onDOMLoaded(function() {\n");

            e.raw(scriptBlock.toString());

            e.raw("});\n");

            e.raw("// -->\n");
        }
    }

    /**
     * Adds a script link for each included script.
     *
     * @param body    element to add the script links to
     * @param scripts scripts to add
     */
    protected void addScriptLinksForIncludedScripts(Element body, List<String> scripts)
    {
        int count = scripts.size();

        for (int i = 0; i < count; i++)
        {
            String scriptURL = scripts.get(i);

            Element element = scriptsAtTop
                              ? body.elementAt(i, "script")
                              : body.element("script");

            element.attributes("src", scriptURL, "type", "text/javascript");
        }
    }

    /**
     * Locates the head element under the root ("html") element, creating it if necessary, and adds the stylesheets to
     * it.
     *
     * @param root        element of document
     * @param stylesheets to add to the document
     */
    protected void addStylesheetsToHead(Element root, List<IncludedStylesheet> stylesheets)
    {
        int count = stylesheets.size();

        if (count == 0) return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        // Not an html document, don't add anything. 
        if (!rootElementName.equals("html")) return;

        Element head = root.find("head");

        if (head == null) head = root.elementAt(0, "head");

        for (int i = 0; i < count; i++)
            stylesheets.get(i).add(head, i);
    }
}
