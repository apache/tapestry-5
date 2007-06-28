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

import java.util.List;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

public class DocumentScriptBuilderImpl implements DocumentScriptBuilder
{
    private final List<String> _scripts = newList();

    private final StringBuilder _scriptBlock = new StringBuilder();

    public void addScriptLink(String scriptURL)
    {
        if (_scripts.contains(scriptURL))
            return;

        _scripts.add(scriptURL);
    }

    public void addScript(String script)
    {
        if (InternalUtils.isBlank(script))
            return;

        _scriptBlock.append(script);
        _scriptBlock.append("\n");
    }

    public void updateDocument(Document document)
    {
        Element body = document.find("html/body");

        if (body == null)
            return;

        for (int i = 0; i < _scripts.size(); i++)
        {
            String scriptURL = _scripts.get(i);

            body.elementAt(i, "script", "src", scriptURL, "type", "text/javascript");
        }

        if (_scriptBlock.length() > 0)
        {
            Element e = body.element("script", "type", "text/javascript", "language", "javascript");
            e.raw("\n<!--\n");

            // This assumes that Prototype is available.
            
            e.text("Event.observe(window, \"load\", function() {\n");
            
            e.text(_scriptBlock.toString());

            e.text("});\n");
            
            e.raw("// -->\n");
        }

    }

}
