// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.services.MethodSignature;

import java.util.Formatter;

/**
 * Utility class for assembling the <em>body</em> used with Javassist when defining a method or constructor. Basically,
 * assists with formatting and with indentation. This makes the code that assembles a method body much simpler ... and
 * it makes the result neater, which will be easier to debug (debugging dynamically generated code is hard enough that
 * it should be easy to read the input code before worrying about why it doesn't compile or execute properly).
 * <p/>
 * This class is not threadsafe.
 * <p/>
 * Most of the methods return the BodyBuilder, to form a fluent interface.
 */
public final class BodyBuilder
{
    /**
     * Feels right for the size of a typical body.
     */
    private static final int DEFAULT_LENGTH = 200;

    private static final String INDENT = "  ";

    private final StringBuilder buffer = new StringBuilder(DEFAULT_LENGTH);

    private final Formatter formatter = new Formatter(buffer);

    // Per level of nesting depth (two spaces).

    private int nestingDepth = 0;

    private boolean atNewLine = true;

    /**
     * Clears the builder, returning it to its initial, empty state.
     */
    public BodyBuilder clear()
    {
        nestingDepth = 0;
        atNewLine = true;
        buffer.setLength(0);

        return this;
    }

    /**
     * Adds text to the current line, without ending the line.
     *
     * @param format string format, as per {@link java.util.Formatter}
     * @param args   arguments referenced by format specifiers
     */
    public BodyBuilder add(String format, Object... args)
    {
        add(format, args, false);

        return this;
    }

    /**
     * Adds text to the current line and ends the line.
     *
     * @param format string format, as per {@link java.util.Formatter}
     * @param args   arguments referenced by format specifiers
     */
    public BodyBuilder addln(String format, Object... args)
    {
        add(format, args, true);

        return this;
    }

    private BodyBuilder add(String format, Object[] args, boolean newLine)
    {
        indent();

        // Format output, send to buffer

        formatter.format(format, args);

        if (newLine) newline();

        return this;
    }

    private void newline()
    {
        buffer.append("\n");
        atNewLine = true;
    }

    /**
     * Begins a new block. Emits a "{", properly indented, on a new line.
     */
    public BodyBuilder begin()
    {
        if (!atNewLine) newline();

        indent();
        buffer.append("{");
        newline();

        nestingDepth++;

        return this;
    }

    /**
     * Ends the current block. Emits a "}", propertly indented, on a new line.
     */
    public BodyBuilder end()
    {
        if (!atNewLine) newline();

        // TODO: Could check here if nesting depth goes below zero.

        nestingDepth--;

        indent();
        buffer.append("}");

        newline();

        return this;
    }

    private void indent()
    {
        if (atNewLine)
        {
            for (int i = 0; i < nestingDepth; i++)
                buffer.append(INDENT);

            atNewLine = false;
        }
    }

    /**
     * Returns the current contents of the buffer. This value is often passed to methods such as {@link
     * org.apache.tapestry5.ioc.services.ClassFab#addConstructor(Class[], Class[], String)} or {@link
     * org.apache.tapestry5.ioc.services.ClassFab#addMethod(int, MethodSignature, String)}.
     * <p/>
     * A BodyBuilder can be used again after invoking toString(), typically by invoking {@link #clear()}.
     */
    @Override
    public String toString()
    {
        return buffer.toString();
    }
}
