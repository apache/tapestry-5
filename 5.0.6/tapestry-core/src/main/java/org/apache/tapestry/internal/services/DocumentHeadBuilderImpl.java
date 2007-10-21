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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.List;
import java.util.Set;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

public class DocumentHeadBuilderImpl implements DocumentHeadBuilder
{
    private final List<String> _scripts = newList();

    private final StringBuilder _scriptBlock = new StringBuilder();

    private final Set<String> _stylesheets = newSet();

    private final List<IncludedStylesheet> _includedStylesheets = newList();

    private class IncludedStylesheet
    {
        private final String _url;

        private final String _media;

        IncludedStylesheet(String url, String media)
        {
            _url = url;
            _media = media;
        }

        void add(Element head, int index)
        {
            head.elementAt(index, "link",

            "href", _url,

            "rel", "stylesheet",

            "type", "text/css",

            "media", _media);
        }
    }

    public void addStylesheetLink(String styleURL, String media)
    {
        if (_stylesheets.contains(styleURL)) return;

        _includedStylesheets.add(new IncludedStylesheet(styleURL, media));

        _stylesheets.add(styleURL);
    }

    public void addScriptLink(String scriptURL)
    {
        if (_scripts.contains(scriptURL)) return;

        _scripts.add(scriptURL);
    }

    public void addScript(String script)
    {
        if (InternalUtils.isBlank(script)) return;

        _scriptBlock.append(script);
        _scriptBlock.append("\n");
    }

    public void updateDocument(Document document)
    {
        Element root = document.getRootElement();

        // This can happen due to a catastrophic rendering error, such as a missing page template.
        if (root == null) return;

        // This only applies when the document is an HTML document. This may need to change in the
        // future, perhaps configurable, to allow for html and xhtml and perhaps others. Does SVG
        // use stylesheets?

        if (!root.getName().equals("html")) return;

        int stylesheets = _includedStylesheets.size();

        if (stylesheets > 0)
        {
            Element head = root.find("head");

            if (head == null) head = root.elementAt(0, "head");

            for (int i = 0; i < stylesheets; i++)
                _includedStylesheets.get(i).add(head, i);
        }

        Element body = root.find("body");

        if (body == null) return;

        for (int i = 0; i < _scripts.size(); i++)
        {
            String scriptURL = _scripts.get(i);

            body.elementAt(i, "script", "src", scriptURL, "type", "text/javascript");
        }

        if (_scriptBlock.length() > 0)
        {
            Element e = body.element("script", "type", "text/javascript");
            e.raw("\n<!--\n");

            // This assumes that Prototype is available.

            e.text("Event.observe(window, \"load\", function() {\n");

            e.text(_scriptBlock.toString());

            e.text("});\n");

            e.raw("// -->\n");
        }

    }

}
