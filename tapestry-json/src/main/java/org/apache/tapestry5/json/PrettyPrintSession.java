// Copyright 2010, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.json;

import java.io.PrintWriter;

/**
 * Used to pretty-print JSON content, with a customizable indentation.
 *
 * @since 5.2.0
 */
class PrettyPrintSession implements JSONPrintSession
{
    private final JSONPrintSink out;

    private final String indentString;

    private int indentLevel;

    enum Position
    {
        MARGIN, INDENTED, CONTENT
    };

    private Position position = Position.MARGIN;

    /** Defaults the indentation to be two spaces per indentation level. */
    PrettyPrintSession(StringBuilder out)
    {
        this(new StringBuilderPrintSink(out), "  ");
    }

    /** Defaults the indentation to be two spaces per indentation level. */
    public PrettyPrintSession(PrintWriter writer)
    {
        this(new WriterPrintSink(writer), "  ");
    }

    /**
     * @param writer
     *            to which content is printed
     * @param indentString
     *            string used for indentation (written N times, once per current indent level)
     */
    public PrettyPrintSession(PrintWriter writer, String indentString)
    {
        this(new WriterPrintSink(writer), indentString);
    }

    private PrettyPrintSession(JSONPrintSink out, String indentString)
    {
        this.out = out;
        this.indentString = indentString;
    }

    @Override
    public JSONPrintSession indent()
    {
        indentLevel++;

        return this;
    }

    @Override
    public JSONPrintSession newline()
    {
        if (position != Position.MARGIN)
        {
            out.append('\n');
            position = Position.MARGIN;
        }

        return this;
    }

    @Override
    public JSONPrintSession outdent()
    {
        indentLevel--;

        return this;
    }

    private void addIndentation()
    {
        if (position == Position.MARGIN)
        {
            for (int i = 0; i < indentLevel; i++)
                out.append(indentString);

            position = Position.INDENTED;
        }
    }

    private void addSep()
    {
        if (position == Position.CONTENT)
        {
            out.append(' ');
        }
    }

    private void prepareToPrint()
    {
        addIndentation();

        addSep();
    }

    @Override
    public JSONPrintSession print(String value)
    {
        prepareToPrint();

        out.append(value);

        position = Position.CONTENT;

        return this;
    }

    @Override
    public JSONPrintSession printQuoted(String value)
    {
        prepareToPrint();

        out.append('"');
        JSONObject.escapeInto(out, value);
        out.append('"');

        position = Position.CONTENT;

        return this;
    }

    @Override
    public JSONPrintSession printSymbol(char symbol)
    {
        addIndentation();

        if (symbol != ',')
            addSep();

        out.append(symbol);

        return this;
    }

}
