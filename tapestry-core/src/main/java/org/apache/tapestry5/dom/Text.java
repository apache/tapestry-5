// Copyright 2006, 2008, 2009, 2026 The Apache Software Foundation
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
 * A type of node that contains text.
 */
public final class Text extends Node
{
    private final StringBuilder builder;

    Text(Element container, String text)
    {
        super(container);

        builder = new StringBuilder(text.length());

        write(text);
    }

    boolean isEmpty()
    {
        return builder.length() == 0 || builder.toString().trim().length() == 0;
    }

    /**
     * Writes additional text into the node, appending it to any existing text.
     */
    public void write(String text)
    {
        builder.append(text);
    }

    public void writef(String format, Object... args)
    {
        write(String.format(format, args));
    }

    @Override
    void toMarkup(Document document, PrintWriter writer, Map<String, String> namespaceURIToPrefix)
    {
        String encoded = document.getMarkupModel().encode(builder.toString());

        writer.print(encoded);
    }

    /**
     * Returns a deep copy of this text node, detached from any parent.
     *
     * @since 5.10
     */
    @Override
    public Text deepClone()
    {
        return new Text(null, builder.toString());
    }
}
