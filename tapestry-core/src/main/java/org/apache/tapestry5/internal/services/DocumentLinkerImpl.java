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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.ModuleManager;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.List;
import java.util.Map;

public class DocumentLinkerImpl implements DocumentLinker
{
    private final List<String> scripts = CollectionFactory.newList();

    private final Map<InitializationPriority, StringBuilder> priorityToScript = CollectionFactory.newMap();

    private final Map<InitializationPriority, List<JSONArray>> priorityToModuleInit = CollectionFactory.newMap();

    private final List<StylesheetLink> includedStylesheets = CollectionFactory.newList();

    private final ModuleManager moduleManager;

    private final boolean compactJSON;

    private final boolean omitGeneratorMetaTag;

    private final String tapestryBanner;

    private boolean hasDynamicScript;

    private final Asset requireJS;

    /**
     * @param moduleManager
     *         used to identify the root folder for dynamically loaded modules
     * @param requireJS
     *         asset for the library
     * @param omitGeneratorMetaTag
     *         via symbol configuration
     * @param tapestryVersion
     *         version of Tapestry framework (for meta tag)
     * @param compactJSON
     */
    public DocumentLinkerImpl(ModuleManager moduleManager, Asset requireJS, boolean omitGeneratorMetaTag, String tapestryVersion, boolean compactJSON)
    {
        this.moduleManager = moduleManager;
        this.requireJS = requireJS;
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

    @Override
    public void addInitialization(InitializationPriority priority, String moduleName, String functionName, JSONArray arguments)
    {
        JSONArray init = new JSONArray();

        String name = functionName == null ? moduleName : moduleName + ":" + functionName;

        init.put(name);

        if (arguments != null)
        {
            for (Object o : arguments)
            {
                init.put(o);
            }
        }

        InternalUtils.addToMapList(priorityToModuleInit, priority, init);

        hasDynamicScript = true;
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
            return;

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
        if (scripts.isEmpty() && !hasDynamicScript)
        {
            return;
        }

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        String rootElementName = root.getName();

        if (!rootElementName.equals("html"))
            throw new RuntimeException(String.format("The root element of the rendered document was <%s>, not <html>. A root element of <html> is needed when linking JavaScript and stylesheet resources.", rootElementName));

        Element head = findOrCreateElement(root, "head", true);

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
            container = atTop ? root.elementAt(0, childElement) : root.element(childElement);

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
        // TODO: Do we need to include type="text/javascript"?

        body.element("script", "src", requireJS.toClientURL());

        moduleManager.writeConfiguration(body.element("script", "type", "text/javascript"));

        // Next, include all stacks and individual JavaScript files *after* RequireJS.

        for (String script : scripts)
        {
            body.element("script", "type", "text/javascript", "src", script);
        }

        if (priorityToScript.isEmpty() && priorityToModuleInit.isEmpty())
        {
            return;
        }

        StringBuilder block = new StringBuilder();

        boolean wrapped = false;

        for (InitializationPriority p : InitializationPriority.values())
        {
            if (p != InitializationPriority.IMMEDIATE && !wrapped
                    && (priorityToScript.containsKey(p) ||
                    priorityToModuleInit.containsKey(p)))
            {

                block.append("Tapestry.onDOMLoaded(function() {\n");

                wrapped = true;
            }

            addModuleInits(block, priorityToModuleInit.get(p));

            addDirectScriptInitialization(block, priorityToScript.get(p));
        }

        if (wrapped)
        {
            block.append("});\n");
        }

        body.element("script", "type", "text/javascript").raw(block.toString());
    }

    private void addModuleInits(StringBuilder block, List<JSONArray> moduleInits)
    {
        if (moduleInits == null)
        {
            return;
        }

        block.append("require([\"core/pageinit\"], function (pageinit) {\n");
        block.append("  pageinit([");

        String sep = "";

        for (JSONArray init : moduleInits)
        {
            block.append(sep);
            block.append(init.toString(compactJSON));
            sep = ",\n  ";
        }

        block.append("]);\n});\n");
    }

    private void addDirectScriptInitialization(StringBuilder block, StringBuilder content)
    {
        if (content == null)
            return;

        block.append(content);
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
