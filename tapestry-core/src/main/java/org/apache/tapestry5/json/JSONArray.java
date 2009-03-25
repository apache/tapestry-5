// Copyright 2007, 2008 The Apache Software Foundation
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

import java.util.List;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a string wrapped in square brackets with
 * commas separating the values. The internal form is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by index, and <code>put</code> methods for adding or replacing values. The values can be any of
 * these types: <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL object</code>.
 * <p/>
 * The constructor can convert a JSON text into a Java object. The <code>toString</code> method converts to JSON text.
 * <p/>
 * A <code>get</code> method returns a value if one can be found, and throws an exception if one cannot be found. An
 * <code>opt</code> method returns a default value instead of throwing an exception, and so is useful for obtaining
 * optional values.
 * <p/>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object which you can cast or query for type.
 * There are also typed <code>get</code> and <code>opt</code> methods that do type checking and type coersion for you.
 * <p/>
 * The texts produced by the <code>toString</code> methods strictly conform to JSON syntax rules. The constructors are
 * more forgiving in the texts they will accept: <ul> <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear
 * just before the closing bracket.</li> <li>The <code>null</code> value will be inserted when there is
 * <code>,</code>&nbsp;<small>(comma)</small> elision.</li> <li>Strings may be quoted with
 * <code>'</code>&nbsp;<small>(single quote)</small>.</li> <li>Strings do not need to be quoted at all if they do not
 * begin with a quote or single quote, and if they do not contain leading or trailing spaces, and if they do not contain
 * any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and if they are not
 * the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li> <li>Values can be separated by
 * <code>;</code> <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li> <li>Numbers may
 * have the <code>0-</code> <small>(octal)</small> or <code>0x-</code> <small>(hex)</small> prefix.</li> <li>Comments
 * written in the slashshlash, slashstar, and hash conventions will be ignored.</li> </ul>
 *
 * @author JSON.org
 * @version 2
 */
public final class JSONArray
{

    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private final List<Object> list = CollectionFactory.newList();

    /**
     * Construct an empty JSONArray.
     */
    public JSONArray()
    {
    }

    public JSONArray(String text)
    {
        JSONTokener tokener = new JSONTokener(text);

        parse(tokener);
    }

    public JSONArray(Object... values)
    {
        for (Object value : values) put(value);
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param tokenizer A JSONTokener
     * @throws RuntimeException If there is a syntax error.
     */
    JSONArray(JSONTokener tokenizer)
    {
        assert tokenizer != null;

        parse(tokenizer);
    }

    private void parse(JSONTokener tokenizer)
    {
        if (tokenizer.nextClean() != '[')
        {
            throw tokenizer
                    .syntaxError("A JSONArray text must start with '['");
        }

        if (tokenizer.nextClean() == ']')
        {
            return;
        }

        tokenizer.back();

        while (true)
        {
            if (tokenizer.nextClean() == ',')
            {
                tokenizer.back();
                list.add(JSONObject.NULL);
            }
            else
            {
                tokenizer.back();
                list.add(tokenizer.nextValue());
            }

            switch (tokenizer.nextClean())
            {
                case ';':
                case ',':
                    if (tokenizer.nextClean() == ']')
                    {
                        return;
                    }
                    tokenizer.back();
                    break;

                case ']':
                    return;

                default:
                    throw tokenizer.syntaxError("Expected a ',' or ']'");
            }
        }
    }

    /**
     * Get the object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws RuntimeException If there is no value for the index.
     */
    public Object get(int index)
    {
        return list.get(index);
    }

    /**
     * Get the boolean value associated with an index. The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     * @throws RuntimeException If there is no value for the index or if the value is not convertable to boolean.
     */
    public boolean getBoolean(int index)
    {
        Object value = get(index);

        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }

        if (value instanceof String)
        {
            String asString = (String) value;

            if (asString.equalsIgnoreCase("false")) return false;

            if (asString.equalsIgnoreCase("true")) return true;
        }

        throw new RuntimeException("JSONArray[" + index + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws IllegalArgumentException If the key is not found or if the value cannot be converted to a number.
     */
    public double getDouble(int index)
    {
        Object value = get(index);

        try
        {
            if (value instanceof Number) return ((Number) value).doubleValue();

            return Double.valueOf((String) value);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("JSONArray[" + index + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws IllegalArgumentException If the key is not found or if the value cannot be converted to a number. if the
     *                                  value cannot be converted to a number.
     */
    public int getInt(int index)
    {
        Object o = get(index);
        return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(index);
    }

    /**
     * Get the JSONArray associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONArray value.
     * @throws RuntimeException If there is no value for the index. or if the value is not a JSONArray
     */
    public JSONArray getJSONArray(int index)
    {
        Object o = get(index);
        if (o instanceof JSONArray)
        {
            return (JSONArray) o;
        }

        throw new RuntimeException("JSONArray[" + index + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject associated with an index.
     *
     * @param index subscript
     * @return A JSONObject value.
     * @throws RuntimeException If there is no value for the index or if the value is not a JSONObject
     */
    public JSONObject getJSONObject(int index)
    {
        Object o = get(index);
        if (o instanceof JSONObject)
        {
            return (JSONObject) o;
        }

        throw new RuntimeException("JSONArray[" + index + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws IllegalArgumentException If the key is not found or if the value cannot be converted to a number.
     */
    public long getLong(int index)
    {
        Object o = get(index);
        return o instanceof Number ? ((Number) o).longValue() : (long) getDouble(index);
    }

    /**
     * Get the string associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A string value.
     * @throws RuntimeException If there is no value for the index.
     */
    public String getString(int index)
    {
        return get(index).toString();
    }

    /**
     * Determine if the value is null.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index)
    {
        return get(index) == JSONObject.NULL;
    }

    /**
     * Make a string from the contents of this JSONArray. The <code>separator</code> string is inserted between each
     * element. Warning: This method assumes that the data structure is acyclical.
     *
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws RuntimeException If the array contains an invalid number.
     */
    public String join(String separator)
    {
        int len = length();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < len; i += 1)
        {
            if (i > 0) buffer.append(separator);

            buffer.append(JSONObject.valueToString(list.get(i)));
        }

        return buffer.toString();
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length()
    {
        return list.size();
    }

    /**
     * Append an object value. This increases the array's length by one.
     *
     * @param value An object value. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, JSONLiteral,
     *              Long, or String, or the JSONObject.NULL singleton.
     * @return
     */
    public JSONArray put(Object value)
    {
        assert value != null;

        JSONObject.testValidity(value);

        list.add(value);

        return this;
    }

    /**
     * Put or replace an object value in the JSONArray. If the index is greater than the length of the JSONArray, then
     * null elements will be added as necessary to pad it out.
     *
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a Boolean, Double, Integer, JSONArray,
     *              JSONObject, JSONString, Long, or String, or the JSONObject.NULL singeton.
     * @return
     * @throws RuntimeException If the index is negative or if the the value is an invalid number.
     */
    public JSONArray put(int index, Object value)
    {
        assert value != null;

        if (index < 0)
        {
            throw new RuntimeException("JSONArray[" + index + "] not found.");
        }

        JSONObject.testValidity(value);

        if (index < length())
        {
            list.set(index, value);
        }
        else
        {
            while (index != length()) list.add(JSONObject.NULL);

            list.add(value);
        }

        return this;
    }

    /**
     * Make a JSON text of this JSONArray. For compactness, no unnecessary whitespace is added. If it is not possible to
     * produce a syntactically correct JSON text then null will be returned instead. This could occur if the array
     * contains an invalid number.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable representation of the array.
     */
    @Override
    public String toString()
    {
        try
        {
            return '[' + join(",") + ']';
        }
        catch (Exception e)
        {
            return null;
        }
    }

    Object[] toArray()
    {
        return list.toArray();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (!(obj instanceof JSONArray)) return false;

        JSONArray other = (JSONArray) obj;

        return list.equals(other.list);
    }
}
