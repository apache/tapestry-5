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

package org.apache.tapestry.dom;

import java.io.PrintWriter;

/**
 * A node that stores parsed character content (CDATA).  For XML documents (as per {@link MarkupModel#isXML()},
 * this will be writtens as a CDATA section.
 * For non-XML documents, the content is filtered as it is written out.
 */
public class CData extends Node
{
    private final String _content;
    private final Document _document;

    public CData(Node container, Document document, String content)
    {
        super(container);

        _document = document;
        _content = content;
    }


    public void toMarkup(PrintWriter writer)
    {
        MarkupModel model = _document.getMarkupModel();

        if (model.isXML())
        {
            writer.print("<![CDATA[");
            writer.print(_content);
            writer.print("]]>");
            return;
        }

        // CDATA not supported, so write it normally, with entities escaped.  Create a working
        // buffer that's plenty big even if a lot of characters need escaping.

        StringBuilder builder = new StringBuilder(2 * _content.length());

        model.encode(_content, builder);

        writer.print(builder.toString());
    }
}
