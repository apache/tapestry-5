// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.util;

import org.apache.tapestry.ioc.services.MethodSignature;

import java.util.Formatter;

/**
 * Utility class for assembling the <em>body</em> used with Javassist when defining a method or
 * constructor. Basically, assists with formatting and with indentation. This makes the code that
 * assembles a method body much simpler ... and it makes the result neater, which will be easier to
 * debug (debugging dynamically generated code is hard enough that it should be easy to read the
 * input code before worrying about why it doesn't compile or execute properly).
 * <p/>
 * This class is not threadsafe.
 */
public final class BodyBuilder
{
    /**
     * Feels right for the size of a typical body.
     */
    private static final int DEFAULT_LENGTH = 200;

    private final StringBuilder _buffer = new StringBuilder(DEFAULT_LENGTH);

    private final Formatter _formatter = new Formatter(_buffer);

    // Per level of nesting depth (two spaces).

    private static final String INDENT = "  ";

    private int _nestingDepth = 0;

    private boolean _atNewLine = true;

    /**
     * Clears the builder, returning it to its initial, empty state.
     */
    public void clear()
    {
        _nestingDepth = 0;
        _atNewLine = true;
        _buffer.setLength(0);
    }

    /**
     * Adds text to the current line, without ending the line.
     *
     * @param a    string format, as per {@link java.util.Formatter}
     * @param args arguments referenced by format specifiers
     */
    public void add(String format, Object... args)
    {
        add(format, args, false);
    }

    /**
     * Adds text to the current line and ends the line.
     *
     * @param a    string format, as per {@link java.util.Formatter}
     * @param args arguments referenced by format specifiers
     */
    public void addln(String format, Object... args)
    {
        add(format, args, true);
    }

    private void add(String format, Object[] args, boolean newLine)
    {
        indent();

        // Format output, send to buffer

        _formatter.format(format, args);

        if (newLine) newline();
    }

    private void newline()
    {
        _buffer.append("\n");
        _atNewLine = true;
    }

    /**
     * Begins a new block. Emits a "{", properly indented, on a new line.
     */
    public void begin()
    {
        if (!_atNewLine) newline();

        indent();
        _buffer.append("{");
        newline();

        _nestingDepth++;
    }

    /**
     * Ends the current block. Emits a "}", propertly indented, on a new line.
     */
    public void end()
    {
        if (!_atNewLine) newline();

        // TODO: Could check here if nesting depth goes below zero.

        _nestingDepth--;

        indent();
        _buffer.append("}");

        newline();
    }

    private void indent()
    {
        if (_atNewLine)
        {
            for (int i = 0; i < _nestingDepth; i++)
                _buffer.append(INDENT);

            _atNewLine = false;
        }
    }

    /**
     * Returns the current contents of the buffer. This value is often passed to methods such as
     * {@link org.apache.tapestry.ioc.services.ClassFab#addConstructor(Class[], Class[], String)} or
     * {@link org.apache.tapestry.ioc.services.ClassFab#addMethod(int, MethodSignature, String)}.
     * <p/>
     * A BodyBuilder can be used again after invoking toString(), typically by invoking
     * {@link #clear()}.
     */
    @Override
    public String toString()
    {
        return _buffer.toString();
    }
}