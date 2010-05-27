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
import java.util.Map;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.StylesheetLink;

public class DocumentLinkerImpl implements DocumentLinker
{
    private final List<String> scripts = CollectionFactory.newList();

    private final Map<InitializationPriority, StringBuilder> priorityToScript = CollectionFactory.newMap();

    private final Map<InitializationPriority, JSONObject> priorityToInit = CollectionFactory.newMap();

    private final List<StylesheetLink> includedStylesheets = CollectionFactory.newList();

    private final boolean compactJSON;

    private final boolean omitGeneratorMetaTag;

    private final String tapestryBanner;

    private boolean hasDynamicScript;

    /**
     * @param omitGeneratorMetaTag
     *            via symbol configuration
     * @param tapestryVersion
     *            version of Tapestry framework (for meta tag)
     * @param compactJSON
     *            should JSON content be compact or pretty printed?
     */
    public DocumentLinkerImpl(boolean omitGeneratorMetaTag, String tapestryVersion, boolean compactJSON)
    {
        this.omitGeneratorMetaTag = omitGeneratorMetaTag;

        tapestryBanner = String.format("Apache Tapestry Framework (version %s)", tapestryVersion);

        this.compactJSON = compactJSON;
    }

    public void addStylesheetLink(StylesheetLink sheet)
    {
        includedStylesheets.add(sheet);
    }

    public void addScriptLink(String scriptURL)
    {
        scripts.add(scriptURL);
    }

    public void addScript(InitializationPriority priority, String script)
    {

        StringBuilder builder = priorityToScript.get(priority);

        if (builder == null)
        {
            builder = new StringBuilder();
            priorityToScript.put(priority, builder);
        }

        builder.append(script);

        builder.append("\n");

        hasDynamicScript = true;
    }

    public void setInitialization(InitializationPriority priority, JSONObject initialization)
    {
        priorityToInit.put(priority, initialization);

        hasDynamicScript = true;
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
        if (scripts.isEmpty() && !hasDynamicScript)
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

        if (hasDynamicScript)
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
        StringBuilder block = new StringBuilder();

        boolean wrapped = false;

        for (InitializationPriority p : InitializationPriority.values())
        {
            if (p != InitializationPriority.IMMEDIATE && !wrapped
                    && (priorityToScript.containsKey(p) || priorityToInit.containsKey(p)))
            {

                block.append("Tapestry.onDOMLoaded(function() {\n");

                wrapped = true;
            }

            add(block, p);
        }

        if (wrapped)
            block.append("});\n");

        Element e = body.element("script", "type", "text/javascript");

        e.raw(block.toString());

    }

    private void add(StringBuilder block, InitializationPriority priority)
    {
        add(block, priorityToScript.get(priority));
        add(block, priorityToInit.get(priority));
    }

    private void add(StringBuilder block, JSONObject init)
    {
        if (init == null)
            return;

        block.append("Tapestry.init(");
        block.append(init.toString(compactJSON));
        block.append(");\n");
    }

    private void add(StringBuilder block, StringBuilder content)
    {
        if (content == null)
            return;

        block.append(content);
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

        Worker<String> addScript = new Worker<String>()
        {
            public void work(String scriptURL)
            {
                scriptContainer.element("script", "type", "text/javascript", "src", scriptURL);
            }
        };

        F.flow(scripts).each(addScript);

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
    protected void addStylesheetsToHead(Element root, List<StylesheetLink> stylesheets)
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
