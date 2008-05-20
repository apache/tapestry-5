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

    public DocumentLinkerImpl(boolean productionMode)
    {
        developmentMode = !productionMode;
    }

    private class IncludedStylesheet
    {
        private final String url;

        private final String media;

        IncludedStylesheet(String url, String media)
        {
            this.url = url;
            this.media = media;
        }

        void add(Element head, int index)
        {
            head.elementAt(index, "link",

                           "href", url,

                           "rel", "stylesheet",

                           "type", "text/css",

                           "media", media);
        }
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

        // This can happen due to a catastrophic rendering error, such as a missing page template.
        if (root == null) return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        if (!root.getName().equals("html")) return;

        int stylesheets = includedStylesheets.size();

        if (stylesheets > 0)
        {
            Element head = root.find("head");

            if (head == null) head = root.elementAt(0, "head");

            for (int i = 0; i < stylesheets; i++)
                includedStylesheets.get(i).add(head, i);
        }

        Element body = root.find("body");

        if (body == null) return;

        // TAPESTRY-2364


        for (String scriptURL : scripts)
        {
            body.element("script", "src", scriptURL, "type", "text/javascript");
        }

        boolean blockNeeded = (developmentMode && !scripts.isEmpty()) || scriptBlock.length() > 0;

        if (blockNeeded)
        {
            Element e = body.element("script", "type", "text/javascript");
            e.raw("\n<!--\n");

            if (developmentMode)
                e.text("Tapestry.DEBUG_ENABLED = true;\n");

            e.text("Tapestry.onDOMLoaded(function() {\n");

            e.text(scriptBlock.toString());

            e.text("});\n");

            e.raw("// -->\n");
        }

    }

}
