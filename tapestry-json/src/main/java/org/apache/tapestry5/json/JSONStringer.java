/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tapestry5.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/**
 * Implements {@link JSONObject#toString} and {@link JSONArray#toString}. Most
 * application developers should use those methods directly and disregard this
 * API. For example:<pre>
 * JSONObject object = ...
 * String json = object.toString();</pre>
 *
 * <p>Stringers only encode well-formed JSON strings. In particular:
 * <ul>
 * <li>The stringer must have exactly one top-level array or object.
 * <li>Lexical scopes must be balanced: every call to {@link #array} must
 * have a matching call to {@link #endArray} and every call to {@link
 * #object} must have a matching call to {@link #endObject}.
 * <li>Arrays may not contain keys (property names).
 * <li>Objects must alternate keys (property names) and values.
 * <li>Values are inserted with either literal {@link #value(Object) value}
 * calls, or by nesting arrays or objects.
 * </ul>
 * Calls that would result in a malformed JSON string will fail with a
 * {@link RuntimeException}.
 *
 * <p>This class provides no facility for pretty-printing (ie. indenting)
 * output. To encode indented output, use {@link JSONObject#toString(int)} or
 * {@link JSONArray#toString(int)}.
 *
 * <p>Some implementations of the API support at most 20 levels of nesting.
 * Attempts to create more than 20 levels of nesting may fail with a {@link
 * RuntimeException}.
 *
 * <p>Each stringer may be used to encode a single top level value. Instances of
 * this class are not thread safe. Although this class is nonfinal, it was not
 * designed for inheritance and should not be subclassed. In particular,
 * self-use by overrideable methods is not specified. See <i>Effective Java</i>
 * Item 17, "Design and Document or inheritance or else prohibit it" for further
 * information.
 */
class JSONStringer {

    /**
     * The output data, containing at most one top-level array or object.
     */
    final StringBuilder out = new StringBuilder();

    /**
     * Lexical scoping elements within this stringer, necessary to insert the
     * appropriate separator characters (ie. commas and colons) and to detect
     * nesting errors.
     */
    enum Scope {

        /**
         * An array with no elements requires no separators or newlines before
         * it is closed.
         */
        EMPTY_ARRAY,

        /**
         * A array with at least one value requires a comma and newline before
         * the next element.
         */
        NONEMPTY_ARRAY,

        /**
         * An object with no keys or values requires no separators or newlines
         * before it is closed.
         */
        EMPTY_OBJECT,

        /**
         * An object whose most recent element is a key. The next element must
         * be a value.
         */
        DANGLING_KEY,

        /**
         * An object with at least one name/value pair requires a comma and
         * newline before the next element.
         */
        NONEMPTY_OBJECT,

        /**
         * A special bracketless array needed by JSONStringer.join() and
         * JSONObject.quote() only. Not used for JSON encoding.
         */
        NULL,
    }

    /**
     * Unlike the original implementation, this stack isn't limited to 20
     * levels of nesting.
     */
    private final List<Scope> stack = new ArrayList<Scope>();

    /**
     * A string containing a full set of spaces for a single level of
     * indentation, or null for no pretty printing.
     */
    private final String indent;

    JSONStringer() {
        indent = null;
    }

    JSONStringer(int indentSpaces) {
        char[] indentChars = new char[indentSpaces];
        Arrays.fill(indentChars, ' ');
        indent = new String(indentChars);
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    JSONStringer open(Scope empty, String openBracket){
        if (stack.isEmpty() && out.length() > 0) {
            throw new RuntimeException("Nesting problem: multiple top-level roots");
        }
        beforeValue();
        stack.add(empty);
        out.append(openBracket);
        return this;
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    JSONStringer close(Scope empty, Scope nonempty, String closeBracket){
        Scope context = peek();
        if (context != nonempty && context != empty) {
            throw new RuntimeException("Nesting problem");
        }

        stack.remove(stack.size() - 1);
        if (context == nonempty) {
            newline();
        }
        out.append(closeBracket);
        return this;
    }

    /**
     * Returns the value on the top of the stack.
     */
    private Scope peek(){
        if (stack.isEmpty()) {
            throw new RuntimeException("Nesting problem");
        }
        return stack.get(stack.size() - 1);
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private void replaceTop(Scope topOfStack) {
        stack.set(stack.size() - 1, topOfStack);
    }


    void string(String value) {
        out.append("\"");
        char currentChar = 0;

        for (int i = 0, length = value.length(); i < length; i++) {
            char previousChar = currentChar;
            currentChar = value.charAt(i);

            /*
             * From RFC 4627, "All Unicode characters may be placed within the
             * quotation marks except for the characters that must be escaped:
             * quotation mark, reverse solidus, and the control characters
             * (U+0000 through U+001F)."
             */
            switch (currentChar) {
                case '"':
                case '\\':
                    out.append('\\').append(currentChar);
                    break;

                case '/':
                    // it makes life easier for HTML embedding of javascript if we escape </ sequences
                    if (previousChar == '<') {
                        out.append('\\');
                    }
                    out.append(currentChar);
                    break;

                case '\t':
                    out.append("\\t");
                    break;

                case '\b':
                    out.append("\\b");
                    break;

                case '\n':
                    out.append("\\n");
                    break;

                case '\r':
                    out.append("\\r");
                    break;

                case '\f':
                    out.append("\\f");
                    break;

                default:
                    if (currentChar <= 0x1F || (currentChar >= 0x0080 && currentChar < 0x00a0) || (currentChar >= 0x2000 && currentChar < 0x2100)) {
                        out.append(String.format("\\u%04x", (int) currentChar));
                    } else {
                        out.append(currentChar);
                    }
                    break;
            }

        }
        out.append("\"");
    }

    private void newline() {
        if (indent == null) {
            return;
        }

        out.append("\n");
        for (int i = 0; i < stack.size(); i++) {
            out.append(indent);
        }
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     */
    private void beforeValue(){
        if (stack.isEmpty()) {
            return;
        }

        Scope context = peek();
        if (context == Scope.EMPTY_ARRAY) { // first in array
            replaceTop(Scope.NONEMPTY_ARRAY);
            newline();
        } else if (context == Scope.NONEMPTY_ARRAY) { // another in array
            out.append(',');
            newline();
        } else if (context == Scope.DANGLING_KEY) { // value for key
            out.append(indent == null ? ":" : ": ");
            replaceTop(Scope.NONEMPTY_OBJECT);
        } else if (context != Scope.NULL) {
            throw new RuntimeException("Nesting problem");
        }
    }

    /**
     * Returns the encoded JSON string.
     *
     * <p>If invoked with unterminated arrays or unclosed objects, this method's
     * return value is undefined.
     *
     * <p><strong>Warning:</strong> although it contradicts the general contract
     * of {@link Object#toString}, this method returns null if the stringer
     * contains no data.
     */
    @Override
    public String toString() {
        return out.length() == 0 ? null : out.toString();
    }
}
