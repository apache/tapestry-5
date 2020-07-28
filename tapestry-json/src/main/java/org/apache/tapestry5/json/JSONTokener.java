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

// Note: this class was written without inspecting the non-free org.json sourcecode.

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

import org.apache.tapestry5.json.exceptions.JSONSyntaxException;

/**
 * Parses a JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>)
 * encoded string into the corresponding object. Most clients of
 * this class will use only need the {@link #JSONTokener(String) constructor}
 * and {@link #nextValue} method. Example usage: <pre>
 * String json = "{"
 *         + "  \"query\": \"Pizza\", "
 *         + "  \"locations\": [ 94043, 90210 ] "
 *         + "}";
 *
 * JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
 * String query = object.getString("query");
 * JSONArray locations = object.getJSONArray("locations");</pre>
 *
 * <p>For best interoperability and performance use JSON that complies with
 * RFC 4627. For legacy reasons
 * this parser is lenient, so a successful parse does not indicate that the
 * input string was valid JSON. All of the following syntax errors will be
 * ignored:
 * <ul>
 * <li>End of line comments starting with {@code //} or {@code #} and ending
 * with a newline character.
 * <li>C-style comments starting with {@code /*} and ending with
 * {@code *}{@code /}. Such comments may not be nested.
 * <li>Strings that are unquoted or {@code 'single quoted'}.
 * <li>Hexadecimal integers prefixed with {@code 0x} or {@code 0X}.
 * <li>Octal integers prefixed with {@code 0}.
 * <li>Array elements separated by {@code ;}.
 * <li>Unnecessary array separators. These are interpreted as if null was the
 * omitted value.
 * <li>Key-value pairs separated by {@code =} or {@code =>}.
 * <li>Key-value pairs separated by {@code ;}.
 * </ul>
 *
 * <p>Each tokener may be used to parse a single JSON string. Instances of this
 * class are not thread safe. Although this class is nonfinal, it was not
 * designed for inheritance and should not be subclassed. In particular,
 * self-use by overrideable methods is not specified. See <i>Effective Java</i>
 * Item 17, "Design and Document or inheritance or else prohibit it" for further
 * information.
 */
class JSONTokener {

    /**
     * The input JSON.
     */
    private final String in;

    /**
     * The index of the next character to be returned by {@link #next}. When
     * the input is exhausted, this equals the input's length.
     */
    private int pos;

    /**
     * @param in JSON encoded string. Null is not permitted and will yield a
     *           tokener that throws {@code NullPointerExceptions} when methods are
     *           called.
     */
    JSONTokener(String in) {
        // consume an optional byte order mark (BOM) if it exists
        if (in != null && in.startsWith("\ufeff")) {
            in = in.substring(1);
        }
        this.in = in;
    }

    JSONTokener(Reader input) throws IOException {
        StringBuilder s = new StringBuilder();
        char[] readBuf = new char[102400];
        int n = input.read(readBuf);
        while (n >= 0) {
            s.append(readBuf, 0, n);
            n = input.read(readBuf);
        }
        in = s.toString();
        pos = 0;
    }

    /**
     * Returns the next value from the input.
     *
     * @return a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     * Integer, Long, Double or {@link JSONObject#NULL}.
     * @throws JSONSyntaxException if the input is malformed.
     */
     Object nextValue(Class<?> desiredType) {
        int c = nextCleanInternal();
        if (JSONObject.class.equals(desiredType) && c != '{'){
            throw syntaxError(MessageFormat.format("A JSONObject text must start with '''{''' (actual: ''{0}'')", Character.toString((char)c)));
        }
        if (JSONArray.class.equals(desiredType) && c != '['){
          throw syntaxError(MessageFormat.format("A JSONArray text must start with ''['' (actual: ''{0}'')", Character.toString((char)c)));
        }
        switch (c) {
            case -1:
                throw syntaxError("End of input");

            case '{':
                return readObject();

            case '[':
                return readArray();

            case '\'':
            case '"':
                return nextString((char) c);

            default:
                pos--;
                return readLiteral();
        }
    }

    private int nextCleanInternal() {
        while (pos < in.length()) {
            int c = in.charAt(pos++);
            switch (c) {
                case '\t':
                case ' ':
                case '\n':
                case '\r':
                    continue;

                case '/':
                    if (pos == in.length()) {
                        return c;
                    }

                    char peek = in.charAt(pos);
                    switch (peek) {
                        case '*':
                            // skip a /* c-style comment */
                            pos++;
                            int commentEnd = in.indexOf("*/", pos);
                            if (commentEnd == -1) {
                                pos = in.length();
                                throw syntaxError("Unclosed comment");
                            }
                            pos = commentEnd + 2;
                            continue;

                        case '/':
                            // skip a // end-of-line comment
                            pos++;
                            skipToEndOfLine();
                            continue;

                        default:
                            return c;
                    }

                case '#':
                    /*
                     * Skip a # hash end-of-line comment. The JSON RFC doesn't
                     * specify this behavior, but it's required to parse
                     * existing documents. See http://b/2571423.
                     */
                    skipToEndOfLine();
                    continue;

                default:
                    return c;
            }
        }

        return -1;
    }

    /**
     * Advances the position until after the next newline character. If the line
     * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
     * caller.
     */
    private void skipToEndOfLine() {
        for (; pos < in.length(); pos++) {
            char c = in.charAt(pos);
            if (c == '\r' || c == '\n') {
                pos++;
                break;
            }
        }
    }

    /**
     * Returns the string up to but not including {@code quote}, unescaping any
     * character escape sequences encountered along the way. The opening quote
     * should have already been read. This consumes the closing quote, but does
     * not include it in the returned string.
     *
     * @param quote either ' or ".
     * @return The unescaped string.
     * @throws RuntimeException if the string isn't terminated by a closing quote correctly.
     */
    private String nextString(char quote) {
        /*
         * For strings that are free of escape sequences, we can just extract
         * the result as a substring of the input. But if we encounter an escape
         * sequence, we need to use a StringBuilder to compose the result.
         */
        StringBuilder builder = null;

        /* the index of the first character not yet appended to the builder. */
        int start = pos;

        while (pos < in.length()) {
            int c = in.charAt(pos++);
            if (c == quote) {
                if (builder == null) {
                    // a new string avoids leaking memory
                    //noinspection RedundantStringConstructorCall
                    return new String(in.substring(start, pos - 1));
                } else {
                    builder.append(in, start, pos - 1);
                    return builder.toString();
                }
            }

            if (c == '\\') {
                if (pos == in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(in, start, pos - 1);
                builder.append(readEscapeCharacter());
                start = pos;
            }
        }

        throw syntaxError("Unterminated string");
    }

    /**
     * Unescapes the character identified by the character or characters that
     * immediately follow a backslash. The backslash '\' should have already
     * been read. This supports both unicode escapes "u000A" and two-character
     * escapes "\n".
     */
    private char readEscapeCharacter() {
        char escaped = in.charAt(pos++);
        switch (escaped) {
            case 'u': {
                if (pos + 4 > in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                String hex = in.substring(pos, pos + 4);
                try {
                    return (char) Integer.parseInt(hex, 16);
                } catch (NumberFormatException nfe) {
                    throw syntaxError("Invalid escape sequence: " + hex);
                }
                finally {
                    pos += 4;
                }
            }
            case 'x': {
              if (pos + 2 > in.length()) {
                  throw syntaxError("Unterminated escape sequence");
              }
              String hex = in.substring(pos, pos + 2);
              try {
                  return (char) Integer.parseInt(hex, 16);
              } catch (NumberFormatException nfe) {
                  throw syntaxError("Invalid escape sequence: " + hex);
              }
              finally {
                  pos += 2;
              }

            }
            case 't':
                return '\t';

            case 'b':
                return '\b';

            case 'n':
                return '\n';

            case 'r':
                return '\r';

            case 'f':
                return '\f';

            case '\'':
            case '"':
            case '\\':
            default:
                return escaped;
        }
    }

    /**
     * Reads a null, boolean, numeric or unquoted string literal value. Numeric
     * values will be returned as an Integer, Long, or Double, in that order of
     * preference.
     */
    private Object readLiteral() {
        String literal = nextToInternal("{}[]/\\:,=;# \t\f");

        if (literal.length() == 0) {
            throw syntaxError("Missing value");
        } else if ("null".equalsIgnoreCase(literal)) {
            return JSONObject.NULL;
        } else if ("true".equalsIgnoreCase(literal)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(literal)) {
            return Boolean.FALSE;
        }

        /* try to parse as an integral type... */
        if (literal.indexOf('.') == -1) {
            int base = 10;
            String number = literal;
            if (number.startsWith("0x") || number.startsWith("0X")) {
                number = number.substring(2);
                base = 16;
            } else if (number.startsWith("0") && number.length() > 1) {
                number = number.substring(1);
                base = 8;
            }
            try {
                long longValue = Long.parseLong(number, base);
                if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            } catch (NumberFormatException e) {
                /*
                 * This only happens for integral numbers greater than
                 * Long.MAX_VALUE, numbers in exponential form (5e-10) and
                 * unquoted strings. Fall through to try floating point.
                 */
            }
        }

        /* ...next try to parse as a floating point... */
        try {
            return Double.valueOf(literal);
        } catch (NumberFormatException ignored) {
        }

        /* ... finally give up. We have an unquoted string */
        //noinspection RedundantStringConstructorCall
        return new String(literal); // a new string avoids leaking memory
    }

    /**
     * Returns the string up to but not including any of the given characters or
     * a newline character. This does not consume the excluded character.
     */
    private String nextToInternal(String excluded) {
        int start = pos;
        for (; pos < in.length(); pos++) {
            char c = in.charAt(pos);
            if (c == '\r' || c == '\n' || excluded.indexOf(c) != -1) {
                return in.substring(start, pos);
            }
        }
        return in.substring(start);
    }

    /**
     * Reads a sequence of key/value pairs and the trailing closing brace '}' of
     * an object. The opening brace '{' should have already been read.
     */
    private JSONObject readObject() {
        JSONObject result = new JSONObject();

        /* Peek to see if this is the empty object. */
        int first = nextCleanInternal();
        if (first == '}') {
            return result;
        } else if (first != -1) {
            pos--;
        }

        while (true) {
            Object name = null;
            try {
                name = nextValue(null);
            } catch (RuntimeException e){
                if (e.getMessage().equals("End of input" + this)){
                    // hack to maintain compatibility with earlier releases of tapestry-json
                    throw syntaxError("A JSONObject text must end with '}'");
                }
                throw e;
            }
            if (!(name instanceof String)) {
                if (name == null) {
                    throw syntaxError("Names cannot be null");
                } else {
                    throw syntaxError("Names must be strings, but " + name
                            + " is of type " + name.getClass().getName());
                }
            }

            /*
             * Expect the name/value separator to be either a colon ':', an
             * equals sign '=', or an arrow "=>". The last two are bogus but we
             * include them because that's what the original implementation did.
             */
            int separator = nextCleanInternal();
            if (separator != ':' && separator != '=') {
                throw syntaxError("Expected a ':' after a key");
            }
            if (pos < in.length() && in.charAt(pos) == '>') {
                pos++;
            }

            result.put((String) name, nextValue(null));

            switch (nextCleanInternal()) {
                case '}':
                    return result;
                case ';':
                case ',':
                    continue;
                default:
                    throw syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Reads a sequence of values and the trailing closing brace ']' of an
     * array. The opening brace '[' should have already been read. Note that
     * "[]" yields an empty array, but "[,]" returns a two-element array
     * equivalent to "[null,null]".
     */
    private JSONArray readArray() {
        JSONArray result = new JSONArray();

        /* to cover input that ends with ",]". */
        boolean hasTrailingSeparator = false;

        while (true) {
            switch (nextCleanInternal()) {
                case -1:
                    throw syntaxError("Expected a ',' or ']'");
                case ']':
                    if (hasTrailingSeparator) {
                        //result.put(null);
                    }
                    return result;
                case ',':
                case ';':
                    /* A separator without a value first means "null". */
                    result.put(JSONObject.NULL);
                    hasTrailingSeparator = true;
                    continue;
                default:
                    pos--;
            }

            result.put(nextValue(null));

            switch (nextCleanInternal()) {
                case ']':
                    return result;
                case ',':
                case ';':
                    hasTrailingSeparator = true;
                    continue;
                default:
                    throw syntaxError("Expected a ',' or ']'");
            }
        }
    }

    /**
     * Returns an exception containing the given message plus the current
     * position and the entire input string.
     *
     * @param message The message we want to include.
     * @return An exception that we can throw.
     */
    private JSONSyntaxException syntaxError(String message) {
        return new JSONSyntaxException(this.pos, message + this);
    }

    /**
     * Returns the current position and the entire input string.
     */
    @Override
    public String toString() {
        // consistent with the original implementation
        return " at character " + pos + " of " + in;
    }

}
