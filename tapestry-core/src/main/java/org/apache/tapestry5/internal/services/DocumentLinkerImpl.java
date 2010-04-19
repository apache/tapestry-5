// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Func;
import org.apache.tapestry5.ioc.internal.util.Operation;

public class DocumentLinkerImpl implements DocumentLinker
{
    private final List<String> scripts = CollectionFactory.newList();

    private final StringBuilder scriptBlock = new StringBuilder();

    private final List<IncludedStylesheet> includedStylesheets = CollectionFactory.newList();

    private final boolean omitGeneratorMetaTag;

    private final String tapestryBanner;

    /**
     * @param omitGeneratorMetaTag
     *            via symbol configuration
     * @param tapestryVersion
     *            version of Tapestry framework (for meta tag)
     */
    public DocumentLinkerImpl(boolean omitGeneratorMetaTag, String tapestryVersion)
    {
        this.omitGeneratorMetaTag = omitGeneratorMetaTag;

        tapestryBanner = String.format("Apache Tapestry Framework (version %s)", tapestryVersion);
    }

    public void addStylesheetLink(String styleURL, String media)
    {
        includedStylesheets.add(new IncludedStylesheet(styleURL, media));
    }

    public void addScriptLink(String scriptURL)
    {
        scripts.add(scriptURL);
    }

    public void addScript(String script)
    {
        scriptBlock.append(script);
    }

    /**
     * Updates the supplied Document, possibly adding &lt;head&gt; or &lt;body&gt; elements.
     * 
     * @param document
     *            to be updated
     */
    public void updateDocument(Document document)
    {
        Element root = document.getRootElement();

        // If the document failed to render at all, that's a different problem and is reported elsewhere.

        if (root == null)
            return;

            addStylesheetsToHead(root, includedStylesheets);

        // only add the generator meta only to html documents

        boolean isHtmlRoot = root.getName().equals("html");

        if (!omitGeneratorMetaTag && isHtmlRoot)
        {
            Element head = findOrCreateElement(root, "head", true);
            head.element("meta", "name", "generator", "content", tapestryBanner);
        }

        addScriptElements(root);
    }

    private void addScriptElements(Element root)
    {
        if (scripts.isEmpty() && scriptBlock.length() == 0)
            return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        if (!rootElementName.equals("html"))
            throw new RuntimeException(ServicesMessages.documentMissingHTMLRoot(rootElementName));

        Element container = findOrCreateElement(root, "head", true);

        // TAPESTRY-2364

        addScriptLinksForIncludedScripts(container, scripts);

        addDynamicScriptBlock(findOrCreateElement(root, "body", false));
    }

    private Element findOrCreateElement(Element root, String childElement, boolean atTop)
    {
        Element container = root.find(childElement);

        // Create the element is it is missing.

        if (container == null)
            container = atTop ? root.elementAt(0, childElement) : root.element(childElement);

        return container;
    }

    /**
     * Adds the dynamic script block, which is, ultimately, a call to the client-side Tapestry.onDOMLoaded() function.
     * 
     * @param body
     *            element to add the dynamic scripting to
     */
    protected void addDynamicScriptBlock(Element body)
    {
        if (scriptBlock.length() == 0)
            return;

        Element e = body.element("script", "type", "text/javascript");

        e.raw(scriptBlock.toString());
    }

    /**
     * Adds a script link for each included script to the bottom of the container (the &lt;head&gt;).
     * 
     * @param container
     *            element to add the script links to
     * @param scripts
     *            scripts to add
     */
    protected void addScriptLinksForIncludedScripts(Element container, List<String> scripts)
    {
        Element existing = findExistingElement(container, "script");

        final Element scriptContainer = container.element("script-container");

        Func.each(scripts, new Operation<String>()
        {
            public void op(String scriptURL)
            {
                scriptContainer.element("script", "type", "text/javascript", "src", scriptURL);
            }
        });

        if (existing != null)
            scriptContainer.moveBefore(existing);

        scriptContainer.pop();
    }

    /**
     * Locates the head element under the root ("html") element, creating it if necessary, and adds the stylesheets to
     * it.
     * 
     * @param root
     *            element of document
     * @param stylesheets
     *            to add to the document
     */
    protected void addStylesheetsToHead(Element root, List<IncludedStylesheet> stylesheets)
    {
        int count = stylesheets.size();

        if (count == 0)
            return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        // Not an html document, don't add anything.
        if (!rootElementName.equals("html"))
            return;

        Element head = findOrCreateElement(root, "head", true);

        Element existing = findExistingElement(head, "link");

        // Create a temporary container element.
        Element container = head.element("stylesheet-link-container");

        for (int i = 0; i < count; i++)
            stylesheets.get(i).add(container);

        if (existing != null)
            container.moveBefore(existing);

        container.pop();
    }

    Element findExistingElement(Element container, String elementName)
    {
        for (Node n : container.getChildren())
        {
            if (n instanceof Element)
            {
                Element e = (Element) n;

                if (e.getName().equalsIgnoreCase(elementName))
                    return e;
            }
        }

        return null;
    }
}
