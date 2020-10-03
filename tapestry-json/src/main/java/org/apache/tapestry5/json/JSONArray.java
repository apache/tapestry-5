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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.json.exceptions.JSONArrayIndexOutOfBoundsException;
import org.apache.tapestry5.json.exceptions.JSONSyntaxException;
import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException;
import org.apache.tapestry5.json.exceptions.JSONValueNotFoundException;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/**
 * A dense indexed sequence of values. Values may be any mix of
 * {@link JSONObject JSONObjects}, other {@link JSONArray JSONArrays}, Strings,
 * Booleans, Integers, Longs, Doubles, {@code null} or {@link JSONObject#NULL}.
 * Values may not be {@link Double#isNaN() NaNs}, {@link Double#isInfinite()
 * infinities}, or of any type not listed here.
 *
 * {@code JSONArray} has the same type coercion behavior and
 * optional/mandatory accessors as {@link JSONObject}. See that class'
 * documentation for details.
 *
 * <strong>Warning:</strong> this class represents null in two incompatible
 * ways: the standard Java {@code null} reference, and the sentinel value {@link
 * JSONObject#NULL}. In particular, {@code get} fails if the requested index
 * holds the null reference, but succeeds if it holds {@code JSONObject.NULL}.
 *
 * Instances of this class are not thread safe.
 */
public final class JSONArray extends JSONCollection implements Collection<Object> {
    
    private static final long serialVersionUID = 1L;

    private final List<Object> values;

    /**
     * Creates a {@code JSONArray} with no values.
     */
    public JSONArray() {
        values = new ArrayList<Object>();
    }

    /**
     * Creates a new {@code JSONArray} with values from the next array in the
     * tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     *                 {@code JSONArray}.
     * @throws JSONSyntaxException if the parse fails
     * @throws JSONTypeMismatchException if it doesn't yield a
     *                       {@code JSONArray}.
     */
    JSONArray(JSONTokener readFrom) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONArray and then steal the data from that.
         */
        Object object = readFrom.nextValue(JSONArray.class);
        if (object instanceof JSONArray) {
            values = ((JSONArray) object).values;
        } else {
            throw JSONExceptionBuilder.tokenerTypeMismatch(object, JSONType.ARRAY);
        }
    }

    /**
     * Creates a new {@code JSONArray} with values from the JSON string.
     *
     * @param json a JSON-encoded string containing an array.
     * @throws JSONSyntaxException if the parse fails
     * @throws JSONTypeMismatchException if it doesn't yield a
     *                       {@code JSONArray}.
     */
    public JSONArray(String json) {
        this(new JSONTokener(json));
    }

    /**
     * Creates a new {@code JSONArray} with values from the given primitive array.
     *
     * @param values The values to use.
     * @throws IllegalArgumentException if any of the values are non-finite double values (i.e. NaN or infinite)
     */
    public JSONArray(Object... values) {
        this();
        for (int i = 0; i < values.length; ++i) {
            checkedPut(values[i]);
        }
    }

    /**
     * Create a new array, and adds all values from the iterable to the array (using {@link #putAll(Iterable)}.
     *
     * This is implemented as a static method so as not to break the semantics of the existing {@link #JSONArray(Object...)} constructor.
     * Adding a constructor of type Iterable would change the meaning of <code>new JSONArray(new JSONArray())</code>.
     *
     * @param iterable
     *         collection ot value to include, or null
     * @since 5.4
     */
    public static JSONArray from(Iterable<?> iterable)
    {
        return new JSONArray().putAll(iterable);
    }

    /**
     * @return Returns the number of values in this array.
     * @deprecated Use {@link #size()} instead.
     */
    public int length() {
        return size();
    }

    /**
     * Returns the number of values in this array.
     * If this list contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of values in this array
     * @since 5.7
     */
    @Override
    public int size()
    {
        return values.size();
    }

    /**
     * Returns {@code true} if this array contains no values.
     *
     * @return {@code true} if this array contains no values
     * @since 5.7
     */
    @Override
    public boolean isEmpty()
    {
        return values.isEmpty();
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, or {@link JSONObject#NULL}}. May
     *              not be {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}. Unsupported values are not permitted and will cause the
     *              array to be in an inconsistent state.
     * @return this array.
     * @deprecated The use of {@link #add(Object)} is encouraged.
     */
    public JSONArray put(Object value) {
        add(value);
        return this;
    }

    /**
     * Appends {@code value} to the end of this array.
     *
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, or {@link JSONObject#NULL}}. May
     *              not be {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}. Unsupported values are not permitted and will cause the
     *              array to be in an inconsistent state.
     * @return {@code true} (as specified by {@link Collection#add})
     * @since 5.7
     */
    @Override
    public boolean add(Object value)
    {
        JSON.testValidity(value);
        return values.add(value);
    }

    /**
     * Same as {@link #put}, with added validity checks.
     *
     * @param value The value to append.
     */
    void checkedPut(Object value) {
        JSON.testValidity(value);
        if (value instanceof Number) {
            JSON.checkDouble(((Number) value).doubleValue());
        }

        put(value);
    }

    /**
     * Sets the value at {@code index} to {@code value}, null padding this array
     * to the required length if necessary. If a value already exists at {@code
     * index}, it will be replaced.
     *
     * @param index Where to put the value.
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, {@link JSONObject#NULL}, or {@code null}. May
     *              not be {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}.
     * @return this array.
     * @throws IllegalArgumentException If the value cannot be represented as a finite double value.
     * @throws ArrayIndexOutOfBoundsException if the index is lower than 0
     */
    public JSONArray put(int index, Object value) {
        if (index < 0)
        {
            throw new JSONArrayIndexOutOfBoundsException(index);
        }
        JSON.testValidity(value);
        if (value instanceof Number) {
            // deviate from the original by checking all Numbers, not just floats & doubles
            JSON.checkDouble(((Number) value).doubleValue());
        }
        while (values.size() <= index) {
            values.add(null);
        }
        values.set(index, value);
        return this;
    }

    /**
     * Returns true if this array has no value at {@code index}, or if its value
     * is the {@code null} reference or {@link JSONObject#NULL}.
     *
     * @param index Which value to check.
     * @return true if the value is null.
     */
    public boolean isNull(int index) {
        Object value = values.get(index);
        return value == null || value == JSONObject.NULL;
    }

    /**
     * Returns the value at {@code index}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws JSONValueNotFoundException if this array has no value at {@code index}, or if
     *                       that value is the {@code null} reference. This method returns
     *                       normally if the value is {@code JSONObject#NULL}.
     */
    public Object get(int index) {
        try {
            Object value = values.get(index);
            if (value == null) {
                throw JSONExceptionBuilder.valueNotFound(true, index, JSONType.ANY);
            }
            return value;
        }
        catch (IndexOutOfBoundsException e) {
            throw new JSONArrayIndexOutOfBoundsException(index);
        }
    }

    /**
     * Removes and returns the value at {@code index}, or null if the array has no value
     * at {@code index}.
     *
     * @param index Which value to remove.
     * @return The value previously at the specified location.
     */
    public Object remove(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.remove(index);
    }

    /**
     * Removes the first occurrence of the specified value from this JSONArray,
     * if it is present.
     *
     * @param value value to be removed from this JSONArray, if present
     * @return {@code true} if the element was removed
     * @since 5.7
     */
    @Override
    public boolean remove(Object value)
    {
        return values.remove(value);
    }

    /**
     * Removes from this JSONArray all of its values that are contained in the
     * specified collection.
     *
     * @param collection collection containing value to be removed from this JSONArray
     * @return {@code true} if this JSONArray changed as a result of the call
     * @throws NullPointerException if the specified collection is null.
     * @see Collection#contains(Object)
     * @since 5.7
     */
    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return values.removeAll(collection);
    }

    /**
     * Removes all of the values from this JSONArray.
     * 
     * @since 5.7
     */
    @Override
    public void clear()
    {
        values.clear();
    }

    /**
     * Retains only the values in this JSONArray that are contained in the
     * specified collection.
     *
     * @param collection collection containing elements to be retained in this list
     * @return {@code true} if this list changed as a result of the call
     * @since 5.7
     */
    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return values.retainAll(collection);
    }

    /**
     * Returns the value at {@code index} if it exists and is a boolean or can
     * be coerced to a boolean.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a boolean.
     */
    public boolean getBoolean(int index) {
        Object object = get(index);
        Boolean result = JSON.toBoolean(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.BOOLEAN);
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a double or can
     * be coerced to a double.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a double.
     */
    public double getDouble(int index) {
        Object object = get(index);
        Double result = JSON.toDouble(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is an int or
     * can be coerced to an int.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a int.
     */
    public int getInt(int index) {
        Object object = get(index);
        Integer result = JSON.toInteger(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a long or
     * can be coerced to a long.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value at {@code index} doesn't exist or
     *                       cannot be coerced to a long.
     */
    public long getLong(int index) {
        Object object = get(index);
        Long result = JSON.toLong(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists, coercing it if
     * necessary.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if no such value exists.
     */
    public String getString(int index) {
        Object object = get(index);
        String result = JSON.toString(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.STRING);
        }
        return result;
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONArray}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value doesn't exist or is not a {@code
     *                       JSONArray}.
     */
    public JSONArray getJSONArray(int index) {
        Object object = get(index);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.ARRAY);
        }
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code
     * JSONObject}.
     *
     * @param index Which value to get.
     * @return the value at the specified location.
     * @throws JSONTypeMismatchException if the value doesn't exist or is not a {@code
     *                       JSONObject}.
     */
    public JSONObject getJSONObject(int index) {
        Object object = get(index);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(true, index, object, JSONType.OBJECT);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JSONArray && ((JSONArray) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        // diverge from the original, which doesn't implement hashCode
        return values.hashCode();
    }

    void print(JSONPrintSession session)
    {
        session.printSymbol('[');

        session.indent();

        boolean comma = false;

        for (Object value : values)
        {
            if (comma)
                session.printSymbol(',');

            session.newline();

            JSONObject.printValue(session, value);

            comma = true;
        }

        session.outdent();

        if (comma)
            session.newline();

        session.printSymbol(']');
    }

    /**
     * Puts all objects from the collection into this JSONArray, using {@link #put(Object)}.
     *
     * @param collection
     *         List, array, JSONArray, or other iterable object, or null
     * @return this JSONArray
     * @since 5.4
     */
    public JSONArray putAll(Iterable<?> collection)
    {
        if (collection != null)
        {
            for (Object o : collection)
            {
                put(o);
            }
        }

        return this;
    }

    /**
     * Adds all objects from the collection into this JSONArray, using {@link #add(Object)}.
     *
     * @param collection Any collection, or null
     * @return boolean true, if JSONArray was changed.
     * @since 5.7
     */
    @Override
    public boolean addAll(Collection<? extends Object> collection)
    {
        if (collection == null)
        { 
            return false;
        }

        boolean changed = false;
        for (Object value : collection)
        {
            changed = add(value) || changed;
        }

        return changed;
    }

    /**
     * Returns an unmodifiable list of the contents of the array. This is a wrapper around the list's internal
     * storage and is live (changes to the JSONArray affect the returned List).
     *
     * @return unmodifiable list of array contents
     * @since 5.4
     */
    public List<Object> toList()
    {
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns an array containing all of the values in this JSONArray in proper
     * sequence.
     *
     * @return an array containing all of the values in this JSONArray in proper
     *         sequence
     * @since 5.7
     */
    @Override
    public Object[] toArray()
    {
        return values.toArray();
    }

    /**
     * Returns an array containing all of the values in this JSONArray in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.
     *
     * @param array
     *            the array into which the values of this JSONArray are to
     *            be stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose.
     * @return an array containing the values of this JSONArray
     * @throws ArrayStoreException
     *             if the runtime type of the specified array
     *             is not a supertype of the runtime type of every element in
     *             this list
     * @throws NullPointerException
     *             if the specified array is null
     * @since 5.7
     */
    @Override
    public <T> T[] toArray(T[] array)
    {
        return values.toArray(array);
    }

    /**
     * Returns an iterator over the values in this array in proper sequence.
     *
     * @return an iterator over the values in this array in proper sequence
     */
    @Override
    public Iterator<Object> iterator()
    {
        return values.iterator();
    }

    /**
     * Returns {@code true} if this JSONArray contains the specified value.
     *
     * @param value value whose presence in this JSONArray is to be tested
     * @return {@code true} if this JSONArray contains the specified
     *         value
     * @since 5.7
     */
    @Override
    public boolean contains(Object value)
    {
        return values.contains(value);
    }

    /**
     * Returns {@code true} if this JSONArray contains all of the values
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return {@code true} if this collection contains all of the elements
     *         in the specified collection
     * @throws NullPointerException
     *             if the specified collection is null.
     * @see #contains(Object)
     * @since 5.7
     */
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return values.containsAll(c);
    }
}
