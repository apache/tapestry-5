// Copyright 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.ModuleManager;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.List;

public class DocumentLinkerImpl implements DocumentLinker
{
    private final List<String> libraryURLs = CollectionFactory.newList();

    private final ModuleInitsManager initsManager = new ModuleInitsManager();

    private final List<StylesheetLink> includedStylesheets = CollectionFactory.newList();

    private final ModuleManager moduleManager;

    private final boolean omitGeneratorMetaTag;

    private final String tapestryBanner;

    // Initially false; set to true when a scriptURL or any kind of initialization is added.
    private boolean hasScriptsOrInitializations;

    /**
     * @param moduleManager
     *         used to identify the root folder for dynamically loaded modules
     * @param omitGeneratorMetaTag
     *         via symbol configuration
     * @param tapestryVersion
     *         version of Tapestry framework (for meta tag)
     */
    public DocumentLinkerImpl(ModuleManager moduleManager, boolean omitGeneratorMetaTag, String tapestryVersion)
    {
        this.moduleManager = moduleManager;
        this.omitGeneratorMetaTag = omitGeneratorMetaTag;

        tapestryBanner = String.format("Apache Tapestry Framework (version %s)", tapestryVersion);
    }

    public void addStylesheetLink(StylesheetLink sheet)
    {
        includedStylesheets.add(sheet);
    }


    public void addLibrary(String libraryURL)
    {
        libraryURLs.add(libraryURL);

        hasScriptsOrInitializations = true;
    }

    public void addScript(InitializationPriority priority, String script)
    {
        addInitialization(priority, "core/pageinit", "evalJavaScript", new JSONArray().put(script));
    }

    public void addInitialization(InitializationPriority priority, String moduleName, String functionName, JSONArray arguments)
    {
        initsManager.addInitialization(priority, moduleName, functionName, arguments);

        hasScriptsOrInitializations = true;
    }

    /**
     * Updates the supplied Document, possibly adding &lt;head&gt; or &lt;body&gt; elements.
     *
     * @param document
     *         to be updated
     */
    public void updateDocument(Document document)
    {
        Element root = document.getRootElement();

        // If the document failed to render at all, that's a different problem and is reported elsewhere.

        if (root == null)
        {
            return;
        }

        addStylesheetsToHead(root, includedStylesheets);

        // only add the generator meta only to html documents

        boolean isHtmlRoot = root.getName().equals("html");

        if (!omitGeneratorMetaTag && isHtmlRoot)
        {
            Element head = findOrCreateElement(root, "head", true);

            Element existingMeta = head.find("meta");

            addElementBefore(head, existingMeta, "meta", "name", "generator", "content", tapestryBanner);
        }

        addScriptElements(root);
    }

    private static Element addElementBefore(Element container, Element insertionPoint, String name, String... namesAndValues)
    {
        if (insertionPoint == null)
        {
            return container.element(name, namesAndValues);
        }

        return insertionPoint.elementBefore(name, namesAndValues);
    }


    private void addScriptElements(Element root)
    {
        if (!hasScriptsOrInitializations)
        {
            return;
        }

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        if (!rootElementName.equals("html"))
            throw new RuntimeException(String.format("The root element of the rendered document was <%s>, not <html>. A root element of <html> is needed when linking JavaScript and stylesheet resources.", rootElementName));

        // TAPESTRY-2364

        addScriptsToEndOfBody(findOrCreateElement(root, "body", false));
    }

    /**
     * Finds an element by name, or creates it. Returns the element (if found), or creates a new element
     * with the given name, and returns it. The new element will be positioned at the top or bottom of the root element.
     *
     * @param root
     *         element to search
     * @param childElement
     *         element name of child
     * @param atTop
     *         if not found, create new element at top of root, or at bottom
     * @return the located element, or null
     */
    private Element findOrCreateElement(Element root, String childElement, boolean atTop)
    {
        Element container = root.find(childElement);

        // Create the element is it is missing.

        if (container == null)
        {
            container = atTop ? root.elementAt(0, childElement) : root.element(childElement);
        }

        return container;
    }


    /**
     * Adds {@code <script>} elements for the RequireJS library, then any statically includes JavaScript libraries
     * (including JavaScript stack virtual assets), then the initialization script block.
     *
     * @param body
     *         element to add the dynamic scripting to
     */
    protected void addScriptsToEndOfBody(Element body)
    {
        // In prior releases of Tapestry, we've vacillated about where the <script> tags go
        // (in <head> or at bottom of <body>). Switching to a module approach gives us a new chance to fix this.
        // Eventually, (nearly) everything will be loaded as modules.

        moduleManager.writeInitialization(body, libraryURLs,
                initsManager.forPriority(InitializationPriority.IMMEDIATE),
                initsManager.forPriority(InitializationPriority.EARLY, InitializationPriority.NORMAL, InitializationPriority.LATE));
    }

    private static Element createTemporaryContainer(Element headElement, String existingElementName, String newElementName)
    {
        Element existingScript = headElement.find(existingElementName);

        // Create temporary container for the new <script> elements

        return addElementBefore(headElement, existingScript, newElementName);
    }

    /**
     * Locates the head element under the root ("html") element, creating it if necessary, and adds the stylesheets to
     * it.
     *
     * @param root
     *         element of document
     * @param stylesheets
     *         to add to the document
     */
    protected void addStylesheetsToHead(Element root, List<StylesheetLink> stylesheets)
    {
        int count = stylesheets.size();

        if (count == 0)
        {
            return;
        }

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        // Not an html document, don't add anything.
        if (!rootElementName.equals("html"))
        {
            return;
        }

        Element head = findOrCreateElement(root, "head", true);

        // Create a temporary container element.
        Element container = createTemporaryContainer(head, "style", "stylesheet-container");

        for (int i = 0; i < count; i++)
        {
            stylesheets.get(i).add(container);
        }

        container.pop();
    }
}
