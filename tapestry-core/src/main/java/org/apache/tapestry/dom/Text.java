// Copyright 2006 The Apache Software Foundation
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
 * A type of node that contains text.
 */
public final class Text extends Node
{
    private final StringBuilder _buffer;

    private final Document _document;

    Text(Node container, Document document, String text)
    {
        super(container);

        _document = document;

        _buffer = new StringBuilder();

        write(text);
    }

    /**
     * Writes additional text into the node, appending it to any existing text.
     */
    public void write(String text)
    {
        _document.getMarkupModel().encode(text, _buffer);
    }

    public void writef(String format, Object... args)
    {
        write(String.format(format, args));
    }

    @Override
    public void toMarkup(PrintWriter writer)
    {
        writer.print(_buffer.toString());
    }

}
