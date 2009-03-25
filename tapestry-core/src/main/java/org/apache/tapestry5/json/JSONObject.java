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

package org.apache.tapestry5.json;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external form is a string wrapped in curly braces
 * with colons between the names and values, and commas between the values and names. The internal form is an object
 * having <code>get</code> and <code>opt</code> methods for accessing the values by name, and <code>put</code> methods
 * for adding or replacing values by name. The values can be any of these types: <code>Boolean</code>,
 * {@link org.apache.tapestry5.json.JSONArray}, {@link org.apache.tapestry5.json.JSONLiteral},
 *  <code>JSONObject</code>, <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL</code> object. A JSONObject constructor can be used to convert an external form JSON text into
 * an internal form whose values can be retrieved with the <code>get</code> and <code>opt</code> methods, or to convert
 * values into a JSON text using the <code>put</code> and <code>toString</code> methods. A <code>get</code> method
 * returns a value if one can be found, and throws an exception if one cannot be found. An <code>opt</code> method
 * returns a default value instead of throwing an exception, and so is useful for obtaining optional values.
 * <p/>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object, which you can cast or query for type.
 * There are also typed <code>get</code> and <code>opt</code> methods that do type checking and type coersion for you.
 * <p/>
 * The <code>put</code> methods adds values to an object. For example,
 * <p/>
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 * <p/>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p/>
 * The texts produced by the <code>toString</code> methods strictly conform to the JSON syntax rules. The constructors
 * are more forgiving in the texts they will accept: <ul> <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may
 * appear just before the closing brace.</li> <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li> <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote,
 * and if they do not contain leading or trailing spaces, and if they do not contain any of these characters: <code>{ }
 * [ ] / \ : , = ; #</code> and if they do not look like numbers and if they are not the reserved words
 * <code>true</code>, <code>false</code>, or <code>null</code>.</li> <li>Keys can be followed by <code>=</code> or
 * <code>=></code> as well as by <code>:</code>.</li> <li>Values can be followed by <code>;</code>
 * <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li> <li>Numbers may have the
 * <code>0-</code> <small>(octal)</small> or <code>0x-</code> <small>(hex)</small> prefix.</li> <li>Comments written in
 * the slashshlash, slashstar, and hash conventions will be ignored.</li> </ul> <hr/>
 * <p/>
 * This class, and the other related classes, have been heavily modified from the original source, to fit Tapestry
 * standards and to make use of JDK 1.5 features such as generics. Further, since the interest of Tapestry is primarily
 * constructing JSON (and not parsing it), many of the non-essential methods have been removed (since the original code
 * came with no tests).
 * <p/>
 * Finally, support for the {@link org.apache.tapestry5.json.JSONLiteral} type has been added, which allow the exact
 * output to be controlled; useful when a JSONObject is being used as a configuration object, and must contain values
 * that are not simple data, such as an inline function.
 *
 * @author JSON.org
 * @version 2
 */
@SuppressWarnings({ "CloneDoesntCallSuperClone" })
public final class JSONObject
{

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null, whilst Java's null is equivalent to the
     * value that JavaScript calls undefined.
     */
    private static final class Null
    {
        /**
         * A Null object is equal to the null value and to itself.
         *
         * @param object An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object or null.
         */
        @Override
        public boolean equals(Object object)
        {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         *
         * @return The string "null".
         */
        @Override
        public String toString()
        {
            return "null";
        }
    }

    /**
     * The map where the JSONObject's properties are kept.
     */
    private final Map<String, Object> properties = CollectionFactory.newMap();

    /**
     * It is sometimes more convenient and less ambiguous to have a <code>NULL</code> object than to use Java's
     * <code>null</code> value. <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * Construct an empty JSONObject.
     */
    public JSONObject()
    {
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject. An array of strings is used to identify the keys that
     * should be copied. Missing keys are ignored.
     *
     * @param source        A JSONObject.
     * @param propertyNames The strings to copy.
     * @throws RuntimeException If a value is a non-finite number.
     */
    public JSONObject(JSONObject source, String... propertyNames)
    {
        for (String name : propertyNames)
        {
            Object value = source.opt(name);

            if (value != null) put(name, value);
        }
    }

    /**
     * Construct a JSONObject from a JSONTokener.
     *
     * @param x A JSONTokener object containing the source string. @ If there is a syntax error in the source string.
     */
    JSONObject(JSONTokener x)
    {
        String key;

        if (x.nextClean() != '{')
        {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }

        while (true)
        {
            char c = x.nextClean();
            switch (c)
            {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = x.nextClean();
            if (c == '=')
            {
                if (x.next() != '>')
                {
                    x.back();
                }
            }
            else if (c != ':')
            {
                throw x.syntaxError("Expected a ':' after a key");
            }
            put(key, x.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch (x.nextClean())
            {
                case ';':
                case ',':
                    if (x.nextClean() == '}')
                    {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a JSONObject from a string. This is the most commonly used JSONObject constructor.
     *
     * @param string A string beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with
     *               <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws RuntimeException If there is a syntax error in the source string.
     */
    public JSONObject(String string)
    {
        this(new JSONTokener(string));
    }

    /**
     * Accumulate values under a key. It is similar to the put method except that if there is already an object stored
     * under the key then a JSONArray is stored under the key to hold all of the accumulated values. If there is already
     * a JSONArray, then the new value is appended to it. In contrast, the put method replaces the previous value.
     *
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws {@link RuntimeException} If the value is an invalid number or if the key is null.
     */
    public JSONObject accumulate(String key, Object value)
    {
        testValidity(value);

        Object existing = opt(key);

        if (existing == null)
        {
            // Note that the original implementation of this method contradicited the method
            // documentation.
            put(key, value);
            return this;
        }

        if (existing instanceof JSONArray)
        {
            ((JSONArray) existing).put(value);
            return this;
        }

        // Replace the existing value, of any type, with an array that includes both the
        // existing and the new value.

        put(key, new JSONArray().put(existing).put(value));

        return this;
    }

    /**
     * Append values to the array under a key. If the key does not exist in the JSONObject, then the key is put in the
     * JSONObject with its value being a JSONArray containing the value parameter. If the key was already associated
     * with a JSONArray, then the value parameter is appended to it.
     *
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this. @ If the key is null or if the current value associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value)
    {
        testValidity(value);
        Object o = opt(key);
        if (o == null)
        {
            put(key, new JSONArray().put(value));
        }
        else if (o instanceof JSONArray)
        {
            put(key, ((JSONArray) o).put(value));
        }
        else
        {
            throw new RuntimeException("JSONObject[" + quote(key) + "] is not a JSONArray.");
        }

        return this;
    }

    /**
     * Produce a string from a double. The string "null" will be returned if the number is not finite.
     *
     * @param d A double.
     * @return A String.
     */
    static String doubleToString(double d)
    {
        if (Double.isInfinite(d) || Double.isNaN(d))
        {
            return "null";
        }

        // Shave off trailing zeros and decimal point, if possible.

        String s = Double.toString(d);
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
        {
            while (s.endsWith("0"))
            {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith("."))
            {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key A key string.
     * @return The object associated with the key. @ if the key is not found.
     * @see #opt(String)
     */
    public Object get(String key)
    {
        Object o = opt(key);
        if (o == null)
        {
            throw new RuntimeException("JSONObject[" + quote(key) + "] not found.");
        }

        return o;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key A key string.
     * @return The truth.
     * @throws RuntimeException if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key)
    {
        Object o = get(key);

        if (o instanceof Boolean) return o.equals(Boolean.TRUE);

        if (o instanceof String)
        {
            String value = (String) o;

            if (value.equalsIgnoreCase("true")) return true;

            if (value.equalsIgnoreCase("false")) return false;
        }

        throw new RuntimeException("JSONObject[" + quote(key) + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key A key string.
     * @return The numeric value. @ if the key is not found or if the value is not a Number object and cannot be
     *         converted to a number.
     */
    public double getDouble(String key)
    {
        Object value = get(key);

        try
        {
            if (value instanceof Number) return ((Number) value).doubleValue();

            // This is a bit sloppy for the case where value is not a string.

            return Double.valueOf((String) value);
        }
        catch (Exception e)
        {
            throw new RuntimeException("JSONObject[" + quote(key) + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with a key. If the number value is too large for an int, it will be clipped.
     *
     * @param key A key string.
     * @return The integer value. @ if the key is not found or if the value cannot be converted to an integer.
     */
    public int getInt(String key)
    {
        Object value = get(key);

        if (value instanceof Number) return ((Number) value).intValue();

        // Very inefficient way to do this!
        return (int) getDouble(key);
    }

    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     * @throws RuntimeException if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key)
    {
        Object o = get(key);
        if (o instanceof JSONArray)
        {
            return (JSONArray) o;
        }

        throw new RuntimeException("JSONObject[" + quote(key) + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     * @throws RuntimeException if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key)
    {
        Object o = get(key);
        if (o instanceof JSONObject)
        {
            return (JSONObject) o;
        }

        throw new RuntimeException("JSONObject[" + quote(key) + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with a key. If the number value is too long for a long, it will be clipped.
     *
     * @param key A key string.
     * @return The long value. @ if the key is not found or if the value cannot be converted to a long.
     */
    public long getLong(String key)
    {
        Object o = get(key);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(key);
    }

    /**
     * Get the string associated with a key.
     *
     * @param key A key string.
     * @return A string which is the value.
     * @throws RuntimeException if the key is not found.
     */
    public String getString(String key)
    {
        return get(key).toString();
    }

    /**
     * Determine if the JSONObject contains a specific key.
     *
     * @param key A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(String key)
    {
        return properties.containsKey(key);
    }

    /**
     * Determine if the value associated with the key is null or if there is no value.
     *
     * @param key A key string.
     * @return true if there is no value associated with the key or if the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key)
    {
        return JSONObject.NULL.equals(opt(key));
    }

    /**
     * Get an enumeration of the keys of the JSONObject. Caution: the set should not be modified.
     *
     * @return An iterator of the keys.
     */
    public Set<String> keys()
    {
        return properties.keySet();
    }

    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length()
    {
        return properties.size();
    }

    /**
     * Produce a JSONArray containing the names of the elements of this JSONObject.
     *
     * @return A JSONArray containing the key strings, or null if the JSONObject is empty.
     */
    public JSONArray names()
    {
        JSONArray ja = new JSONArray();

        for (String key : keys())
        {
            ja.put(key);
        }

        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     *
     * @param n A Number
     * @return A String. @ If n is a non-finite number.
     */
    static String numberToString(Number n)
    {
        assert n != null;

        testValidity(n);

        // Shave off trailing zeros and decimal point, if possible.

        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
        {
            while (s.endsWith("0"))
            {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith("."))
            {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get an optional value associated with a key.
     *
     * @param key A key string.
     * @return An object which is the value, or null if there is no value.
     * @see #get(String)
     */
    public Object opt(String key)
    {
        return properties.get(key);
    }

    /**
     * Put a key/value pair in the JSONObject. If the value is null, then the key will be removed from the JSONObject if
     * it is present.
     *
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer,
     *              JSONArray, JSONObject, JSONLiteral, Long, String, or the JSONObject.NULL object.
     * @return this.
     * @throws RuntimeException If the value is non-finite number or if the key is null.
     */
    public JSONObject put(String key, Object value)
    {
        assert key != null;

        if (value != null)
        {
            testValidity(value);
            properties.put(key, value);
        }
        else
        {
            remove(key);
        }

        return this;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the right places. A backslash will be inserted
     * within </, allowing JSON text to be delivered in HTML. In JSON text, a string cannot contain a control character
     * or an unescaped quote or backslash.
     *
     * @param string A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string)
    {
        if (string == null || string.length() == 0)
        {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuilder buffer = new StringBuilder(len + 4);
        String t;

        buffer.append('"');
        for (i = 0; i < len; i += 1)
        {
            b = c;
            c = string.charAt(i);
            switch (c)
            {
                case '\\':
                case '"':
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                case '/':
                    if (b == '<')
                    {
                        buffer.append('\\');
                    }
                    buffer.append(c);
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100'))
                    {
                        t = "000" + Integer.toHexString(c);
                        buffer.append("\\u").append(t.substring(t.length() - 4));
                    }
                    else
                    {
                        buffer.append(c);
                    }
            }
        }
        buffer.append('"');
        return buffer.toString();
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key The name to be removed.
     * @return The value that was associated with the name, or null if there was no value.
     */
    public Object remove(String key)
    {
        return properties.remove(key);
    }

    private static final Class[] ALLOWED = new Class[] { String.class, Boolean.class, Number.class, JSONObject.class,
            JSONArray.class, JSONString.class, JSONLiteral.class, Null.class };

    /**
     * Throw an exception if the object is an NaN or infinite number, or not a type which may be stored.
     *
     * @param value The object to test. @ If o is a non-finite number.
     */
    @SuppressWarnings("unchecked")
    static void testValidity(Object value)
    {
        if (value == null) return;

        boolean found = false;
        Class actual = value.getClass();

        for (Class allowed : ALLOWED)
        {
            if (allowed.isAssignableFrom(actual))
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            List<String> typeNames = CollectionFactory.newList();

            for (Class c : ALLOWED)
            {
                String name = c.getName();

                if (name.startsWith("java.lang."))
                    name = name.substring(10);

                typeNames.add(name);
            }

            String message = String.format(
                    "JSONObject properties may be one of %s. Type %s is not allowed.",
                    InternalUtils.joinSorted(typeNames),
                    actual.getName());

            throw new RuntimeException(message);
        }

        if (value instanceof Double)
        {
            Double asDouble = (Double) value;

            if (asDouble.isInfinite() || asDouble.isNaN())
            {
                throw new RuntimeException("JSON does not allow non-finite numbers.");
            }

            return;
        }

        if (value instanceof Float)
        {
            Float asFloat = (Float) value;

            if (asFloat.isInfinite() || asFloat.isNaN())
            {
                throw new RuntimeException("JSON does not allow non-finite numbers.");
            }

        }

    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace is added. If this would not result in a
     * syntactically correct JSON text, then null will be returned instead.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable representation of the object, beginning with
     *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     */
    @Override
    public String toString()
    {
        boolean comma = false;

        StringBuilder buffer = new StringBuilder("{");

        for (String key : keys())
        {
            if (comma) buffer.append(',');

            buffer.append(quote(key));
            buffer.append(':');
            buffer.append(valueToString(properties.get(key)));

            comma = true;
        }

        buffer.append('}');

        return buffer.toString();
    }

    /**
     * Make a JSON text of an Object value. If the object has an value.toJSONString() method, then that method will be
     * used to produce the JSON text. The method is required to produce a strictly conforming text. If the object does
     * not contain a toJSONString method (which is the most common case), then a text will be produced by the rules.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable representation of the object, beginning with
     *         <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>. @ If the value is or contains an invalid number.
     */
    static String valueToString(Object value)
    {
        if (value == null || value.equals(null))
        {
            return "null";
        }

        if (value instanceof JSONString)
        {
            try
            {
                String json = ((JSONString) value).toJSONString();

                return quote(json);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        if (value instanceof Number)
        {
            return numberToString((Number) value);
        }

        if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray || value instanceof JSONLiteral)
        {
            return value.toString();
        }

        return quote(value.toString());
    }

    /**
     * Returns true if the other object is a JSONObject and its set of properties matches this object's properties.
     * <p/>
     * '
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (!(obj instanceof JSONObject)) return false;

        JSONObject other = (JSONObject) obj;

        return properties.equals(other.properties);
    }
}
