// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

import java.io.PrintWriter;
import java.util.Map;

/**
 * A node that stores parsed character content (CDATA).  For XML documents (as per {@link MarkupModel#isXML()}, this
 * will be writtens as a CDATA section. For non-XML documents, the content is filtered as it is written out.
 */
public class CData extends Node
{
    private final String content;

    public CData(Element container, String content)
    {
        super(container);

        this.content = content;
    }

    @Override
    void toMarkup(Document document, PrintWriter writer, Map<String, String> namespaceURIToPrefix)
    {
        MarkupModel model = document.getMarkupModel();

        if (model.isXML())
        {
            writer.print("<![CDATA[");
            writer.print(content);
            writer.print("]]>");
            return;
        }

        // CDATA not supported, so write it normally, with entities escaped.

        writer.print(model.encode(content));
    }
}
