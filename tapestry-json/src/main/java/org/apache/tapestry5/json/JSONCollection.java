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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Base class for {@link JSONArray} and {@link JSONObject} that exists to organize the code
 * for printing such objects (either compact or pretty).
 * 
 * @since 5.2.0
 */
public abstract class JSONCollection implements Serializable
{
    /**
     * Converts this JSON collection into a parsable string representation.
     *
     * Warning: This method assumes that the data structure is acyclical.
     *
     * Starting in release 5.2, the result will be pretty printed for readability.
     * 
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with
     *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     */
    @Override
    public String toString()
    {
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(caw);

        JSONPrintSession session = new PrettyPrintSession(pw);

        print(session);

        pw.close();

        return caw.toString();
    }

    /**
     * Converts the JSONObject to a compact or pretty-print string representation
     * 
     * @param compact
     *            if true, return minimal format string.
     * @since 5.2.0
     */
    public String toString(boolean compact)
    {
        return compact ? toCompactString() : toString();
    }

    /**
     * Prints the JSONObject as a compact string (not extra punctuation). This is, essentially, what
     * Tapestry 5.1 did inside {@link #toString()}.
     */
    public String toCompactString()
    {
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(caw);

        print(pw);

        pw.close();

        return caw.toString();
    }

    /**
     * Prints the JSONObject to the write (compactly or not).
     * 
     * @param writer
     *            to write content to
     * @param compact
     *            if true, then write compactly, if false, write with pretty printing
     * @since 5.2.1
     */
    public void print(PrintWriter writer, boolean compact)
    {
        JSONPrintSession session = compact ? new CompactSession(writer) : new PrettyPrintSession(writer);

        print(session);
    }

    /**
     * Prints the JSONObject to the writer compactly (with no extra whitespace).
     */
    public void print(PrintWriter writer)
    {
        print(writer, true);
    }

    /**
     * Prints the JSONObject to the writer using indentation (two spaces per indentation level).
     */
    public void prettyPrint(PrintWriter writer)
    {
        print(writer, false);
    }

    /**
     * Print the collection in a parsable format using the session to (optionally) inject extra
     * whitespace (for "pretty printing").
     */
    abstract void print(JSONPrintSession session);
}
