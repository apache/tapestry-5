// Copyright 2010 The Apache Software Foundation
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
 * Encapsulates a {@link PrintWriter} and the rules for indentation and spacing.
 * 
 * @since 5.2.0
 */
interface JSONPrintSession
{
    /**
     * Prints a value as is; the value is assumed to be a string representation of a number of boolean
     * and not require quotes. A space may be inserted before the value.
     * 
     * @param value
     *            unquoted value to print
     * @return the session (for fluent method invocations)
     */
    JSONPrintSession print(String value);

    /**
     * Prints a value enclosed in double quotes. Any internal quotes are escaped.
     * A space may be inserted before the value.
     * 
     * @param value
     *            the string to be printed enclosed in quotes
     * @return the session (for fluent method invocations)
     */
    JSONPrintSession printQuoted(String value);

    /**
     * Begins a new line and the current indentation level.
     * 
     * @return the session (for fluent method invocations)
     */
    JSONPrintSession newline();

    /**
     * Prints a symbol (i.e., ':', '{', '}', '[', ']', or ','). A space may
     * be inserted before the symbol.
     */

    JSONPrintSession printSymbol(char symbol);

    /**
     * Increments the indentation level.
     * 
     * @return new session reflecting the indentation
     */
    JSONPrintSession indent();

    /**
     * Decrements the indentation level.
     * 
     * @return new session reflecting the indentation
     */
    JSONPrintSession outdent();
}
