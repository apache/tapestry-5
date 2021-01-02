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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException;
import org.apache.tapestry5.json.exceptions.JSONValueNotFoundException;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/**
 * A modifiable set of name/value mappings. Names are unique, non-null strings.
 * Values may be any mix of {@link JSONObject JSONObjects}, {@link JSONArray
 * JSONArrays}, Strings, Booleans, Integers, Longs, Doubles or {@link #NULL}.
 * Values may not be {@code null}, {@link Double#isNaN() NaNs}, {@link
 * Double#isInfinite() infinities}, or of any type not listed here.
 *
 * <p>This class can coerce values to another type when requested.
 * <ul>
 * <li>When the requested type is a boolean, strings will be coerced using a
 * case-insensitive comparison to "true" and "false".
 * <li>When the requested type is a double, other {@link Number} types will
 * be coerced using {@link Number#doubleValue() doubleValue}. Strings
 * that can be coerced using {@link Double#valueOf(String)} will be.
 * <li>When the requested type is an int, other {@link Number} types will
 * be coerced using {@link Number#intValue() intValue}. Strings
 * that can be coerced using {@link Double#valueOf(String)} will be,
 * and then cast to int.
 * <li><a href="#lossy">When the requested type is a long, other {@link Number} types will
 * be coerced using {@link Number#longValue() longValue}. Strings
 * that can be coerced using {@link Double#valueOf(String)} will be,
 * and then cast to long. This two-step conversion is lossy for very
 * large values. For example, the string "9223372036854775806" yields the
 * long 9223372036854775807.</a>
 * <li>When the requested type is a String, other non-null values will be
 * coerced using {@link String#valueOf(Object)}. Although null cannot be
 * coerced, the sentinel value {@link JSONObject#NULL} is coerced to the
 * string "null".
 * </ul>
 *
 * <p>This class can look up both mandatory and optional values:
 * <ul>
 * <li>Use <code>get<i>Type</i>()</code> to retrieve a mandatory value. This
 * fails with a {@code RuntimeException} if the requested name has no value
 * or if the value cannot be coerced to the requested type.
 * <li>Use <code>opt()</code> to retrieve an optional value.
 * </ul>
 *
 * <p><strong>Warning:</strong> this class represents null in two incompatible
 * ways: the standard Java {@code null} reference, and the sentinel value {@link
 * JSONObject#NULL}. In particular, calling {@code put(name, null)} removes the
 * named entry from the object but {@code put(name, JSONObject.NULL)} stores an
 * entry whose value is {@code JSONObject.NULL}.
 *
 * <p>Instances of this class are not thread safe.
 */
public final class JSONObject extends JSONCollection implements Map<String, Object> {

    private static final long serialVersionUID = 1L;

    private static final Double NEGATIVE_ZERO = -0d;

    /**
     * A sentinel value used to explicitly define a name with no value. Unlike
     * {@code null}, names with this value:
     * <ul>
     * <li>show up in the {@link #names} array
     * <li>show up in the {@link #keys} iterator
     * <li>return {@code true} for {@link #has(String)}
     * <li>do not throw on {@link #get}
     * <li>are included in the encoded JSON string.
     * </ul>
     *
     * <p>This value violates the general contract of {@link Object#equals} by
     * returning true when compared to {@code null}. Its {@link #toString}
     * method returns "null".
     */
    public static final Object NULL = new Serializable() {

      private static final long serialVersionUID = 1L;

        @Override
        public boolean equals(Object o) {
            return o == this || o == null; // API specifies this broken equals implementation
        }

        // at least make the broken equals(null) consistent with Objects.hashCode(null).
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "null";
        }

        // Serialization magic: after de-serializing, it will be back to the singleton instance of NULL.
        private Object readResolve() throws ObjectStreamException
        {
            return NULL;
        }

    };

    private final LinkedHashMap<String, Object> nameValuePairs;

    /**
     * Creates a {@code JSONObject} with no name/value mappings.
     */
    public JSONObject() {
        nameValuePairs = new LinkedHashMap<String, Object>();
    }

    /**
     * Creates a new {@code JSONObject} with name/value mappings from the next
     * object in the tokener.
     *
     * @param readFrom a tokener whose nextValue() method will yield a
     *                 {@code JSONObject}.
     * @throws RuntimeException if the parse fails or doesn't yield a
     *                       {@code JSONObject}.
     */
    JSONObject(JSONTokener readFrom) {
        /*
         * Getting the parser to populate this could get tricky. Instead, just
         * parse to temporary JSONObject and then steal the data from that.
         */
        Object object = readFrom.nextValue(JSONObject.class);
        if (object instanceof JSONObject) {
            this.nameValuePairs = ((JSONObject) object).nameValuePairs;
        } else {
            throw JSONExceptionBuilder.tokenerTypeMismatch(object, JSONType.OBJECT);
        }
    }

    /**
     * Creates a new {@code JSONObject} with name/value mappings from the JSON
     * string.
     *
     * @param json a JSON-encoded string containing an object.
     * @throws RuntimeException if the parse fails or doesn't yield a {@code
     *                       JSONObject}.
     */
    public JSONObject(String json) {
        this(new JSONTokener(json));
    }

    /**
     * Creates a new {@code JSONObject} by copying mappings for the listed names
     * from the given object. Names that aren't present in {@code copyFrom} will
     * be skipped.
     *
     * @param copyFrom The source object.
     * @param names    The names of the fields to copy.
     * @throws RuntimeException On internal errors. Shouldn't happen.
     */
    public JSONObject(JSONObject copyFrom, String... names) {
        this();
        for (String name : names) {
            Object value = copyFrom.opt(name);
            if (value != null) {
                nameValuePairs.put(name, value);
            }
        }
    }


    /**
     * Returns a new JSONObject that is a shallow copy of this JSONObject.
     *
     * @since 5.4
     */
    public JSONObject copy()
    {
        JSONObject dupe = new JSONObject();
        dupe.nameValuePairs.putAll(nameValuePairs);

        return dupe;
    }

    /**
     * Constructs a new JSONObject using a series of String keys and object values.
     * Object values should be compatible with {@link #put(String, Object)}. Keys must be strings
     * (toString() will be invoked on each key).
     *
     * Prior to release 5.4, keysAndValues was type String...; changing it to Object... makes
     * it much easier to initialize a JSONObject in a single statement, which is more readable.
     *
     * @since 5.2.0
     */
    public JSONObject(Object... keysAndValues)
    {
        this();

        int i = 0;

        while (i < keysAndValues.length)
        {
            put(keysAndValues[i++].toString(), keysAndValues[i++]);
        }
    }

    /**
     * Returns the number of name/value mappings in this object.
     * 
     * @deprecated Use {@link #size()} instead.
     * @return the length of this.
     */
    @Deprecated
    public int length() {
        return nameValuePairs.size();
    }

    /**
     * Maps {@code name} to {@code value}, clobbering any existing name/value
     * mapping with the same name. If the value is {@code null}, any existing
     * mapping for {@code name} is removed.
     *
     * @param name  The name of the new value.
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, {@link #NULL}, or {@code null}. May not be
     *              {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
     *              infinities}.
     * @return this object.
     * @throws IllegalArgumentException if the value is an invalid double (infinite or NaN).
     */
    @Override
    public JSONObject put(String name, Object value) {
        if (value == null) {
            nameValuePairs.remove(name);
            return this;
        }
        JSON.testValidity(value);
        if (value instanceof Number) {
            // deviate from the original by checking all Numbers, not just floats & doubles
            JSON.checkDouble(((Number) value).doubleValue());
        }
        nameValuePairs.put(checkName(name), value);
        return this;
    }

    /**
     * Appends {@code value} to the array already mapped to {@code name}. If
     * this object has no mapping for {@code name}, this inserts a new mapping.
     * If the mapping exists but its value is not an array, the existing
     * and new values are inserted in order into a new array which is itself
     * mapped to {@code name}. In aggregate, this allows values to be added to a
     * mapping one at a time.
     *
     * Note that {@code append(String, Object)} provides better semantics.
     * In particular, the mapping for {@code name} will <b>always</b> be a
     * {@link JSONArray}. Using {@code accumulate} will result in either a
     * {@link JSONArray} or a mapping whose type is the type of {@code value}
     * depending on the number of calls to it.
     *
     * @param name  The name of the field to change.
     * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
     *              Integer, Long, Double, {@link #NULL} or null. May not be {@link
     *              Double#isNaN() NaNs} or {@link Double#isInfinite() infinities}.
     * @return this object after mutation.
     * @throws RuntimeException If the object being added is an invalid number.
     */
    // TODO: Change {@code append) to {@link #append} when append is
    // unhidden.
    public JSONObject accumulate(String name, Object value) {
        Object current = nameValuePairs.get(checkName(name));
        if (current == null) {
            return put(name, value);
        }

        if (current instanceof JSONArray) {
            JSONArray array = (JSONArray) current;
            array.checkedPut(value);
        } else {
            JSONArray array = new JSONArray();
            array.checkedPut(current);
            array.checkedPut(value);
            nameValuePairs.put(name, array);
        }
        return this;
    }

    /**
     * Appends values to the array mapped to {@code name}. A new {@link JSONArray}
     * mapping for {@code name} will be inserted if no mapping exists. If the existing
     * mapping for {@code name} is not a {@link JSONArray}, a {@link RuntimeException}
     * will be thrown.
     *
     * @param name  The name of the array to which the value should be appended.
     * @param value The value to append.
     * @return this object.
     * @throws JSONTypeMismatchException if {@code name} is {@code null} or if the mapping for
     *                       {@code name} is non-null and is not a {@link JSONArray}.
     */
    public JSONObject append(String name, Object value) {
        JSON.testValidity(value);
        Object current = nameValuePairs.get(checkName(name));

        final JSONArray array;
        if (current instanceof JSONArray) {
            array = (JSONArray) current;
        } else if (current == null) {
            JSONArray newArray = new JSONArray();
            nameValuePairs.put(name, newArray);
            array = newArray;
        } else {
            throw new JSONTypeMismatchException("JSONObject[\"" + name + "\"]", JSONType.ARRAY, current.getClass());
        }

        array.checkedPut(value);

        return this;
    }

    String checkName(String name) {
        if (name == null) {
            throw new RuntimeException("Names must be non-null");
        }
        return name;
    }

    /**
     * Returns true if this object has no mapping for {@code name} or if it has
     * a mapping whose value is {@link #NULL}.
     *
     * @param name The name of the value to check on.
     * @return true if the field doesn't exist or is null.
     */
    public boolean isNull(String name) {
        Object value = nameValuePairs.get(name);
        return value == null || value == NULL;
    }

    /**
     * Returns true if this object has a mapping for {@code name}. The mapping
     * may be {@link #NULL}.
     *
     * @deprecated use {@link #containsKey(Object)} instead
     * @param name
     *            The name of the value to check on.
     * @return true if this object has a field named {@code name}
     */
    @Deprecated
    public boolean has(String name) {
        return containsKey(name);
    }

    /**
     * Returns the value mapped by {@code name}, or null if no such mapping
     * exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     */
    public Object opt(Object name) {
        return nameValuePairs.get(name);
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a boolean or
     * can be coerced to a boolean, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a boolean.
     */
    public boolean getBoolean(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.BOOLEAN);
        }
        Boolean result = JSON.toBoolean(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.BOOLEAN);
        }
        return result;
    }

    /**
     * Returns the value to which the specified key is mapped and a boolean, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a boolean.
     * @since 5.7
     */
    public boolean getBooleanOrDefault(String name, boolean defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        Boolean result = JSON.toBoolean(object);
        if (result == null)
        {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.BOOLEAN);
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is a double or
     * can be coerced to a double, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a double.
     */
    public double getDouble(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.NUMBER);
        }
        Double result = JSON.toDouble(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists and is an int or
     * can be coerced to an int, or throws otherwise.
     *
     * @param name The name of the field we want.
     * @return The selected value if it exists.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to an int.
     */
    public int getInt(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.NUMBER);
        }
        Integer result = JSON.toInteger(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value to which the specified key is mapped and an int, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to an int.
     * @since 5.7
     */
    public int getIntOrDefault(String name, int defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        Integer result = JSON.toInteger(object);
        if (result == null)
        {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.NUMBER);
        }
        return result;
    }
    
    /**
     * Returns the value mapped by {@code name} if it exists and is a long or
     * can be coerced to a long, or throws otherwise.
     * Note that JSON represents numbers as doubles,
     *
     * so this is <a href="#lossy">lossy</a>; use strings to transfer numbers
     * via JSON without loss.
     *
     * @param name The name of the field that we want.
     * @return The value of the field.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a long.
     */
    public long getLong(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.NUMBER);
        }
        Long result = JSON.toLong(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value to which the specified key is mapped and a long, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a long.
     * @since 5.7
     */
    public long getLongOrDefault(String name, int defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        Long result = JSON.toLong(object);
        if (result == null)
        {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.NUMBER);
        }
        return result;
    }

    /**
     * Returns the value mapped by {@code name} if it exists, coercing it if
     * necessary, or throws if no such mapping exists.
     *
     * @param name The name of the field we want.
     * @return The value of the field.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                               to String
     */
    public String getString(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.STRING);
        }
        String result = JSON.toString(object);
        if (result == null) {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.STRING);
        }
        return result;
    }

    /**
     * Returns the value to which the specified key is mapped and a string, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a string.
     * @since 5.7
     */
    public String getStringOrDefault(String name, String defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        String result = JSON.toString(object);
        if (result == null)
        {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.STRING);
        }
        return result;
    }
    
    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONArray}, or throws otherwise.
     *
     * @param name The field we want to get.
     * @return The value of the field (if it is a JSONArray.
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping is not a {@code
     *                       JSONArray}.
     */
    public JSONArray getJSONArray(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.ARRAY);
        }
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.ARRAY);
        }
    }

    /**
     * Returns the value to which the specified key is mapped and a JSONArray, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this JSONObject contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a JSONArray.
     * @since 5.7
     */
    public JSONArray getJSONArrayOrDefault(String name, JSONArray defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.ARRAY);
        }
    }
    
    /**
     * Returns the value mapped by {@code name} if it exists and is a {@code
     * JSONObject}, or throws otherwise.
     *
     * @param name The name of the field that we want.
     * @return a specified field value (if it is a JSONObject)
     * @throws JSONValueNotFoundException if the mapping doesn't exist
     * @throws JSONTypeMismatchException if the mapping is not a {@code
     *                       JSONObject}.
     */
    public JSONObject getJSONObject(String name) {
        Object object = opt(name);
        if (object == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.OBJECT);
        }
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.OBJECT);
        }
    }

    
    /**
     * Returns the value to which the specified key is mapped and a JSONObject, or
     * {@code defaultValue} if this map contains no mapping for the key.
     *
     * @param name the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key
     * @throws JSONTypeMismatchException if the mapping cannot be coerced
     *                       to a JSONObject.
     * @since 5.7
     */
    public JSONObject getJSONObjectOrDefault(String name, JSONObject defaultValue)
    {
        Object object = opt(name);
        if (object == null)
        {
            return defaultValue;
        }
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        } else {
            throw JSONExceptionBuilder.typeMismatch(false, name, object, JSONType.OBJECT);
        }
    }
    /**
     * Returns the set of {@code String} names in this object. The returned set
     * is a view of the keys in this object. {@link Set#remove(Object)} will remove
     * the corresponding mapping from this object and set iterator behaviour
     * is undefined if this object is modified after it is returned.
     *
     * See {@link #keys()}.
     *
     * @return The names in this object.
     */
    public Set<String> keys() {
        return nameValuePairs.keySet();
    }

    /**
     * Returns an array containing the string names in this object. This method
     * returns null if this object contains no mappings.
     *
     * @return the names.
     */
    public JSONArray names() {
        return nameValuePairs.isEmpty()
                ? null
                : JSONArray.from(nameValuePairs.keySet());
    }

    /**
     * Encodes the number as a JSON string.
     *
     * @param number a finite value. May not be {@link Double#isNaN() NaNs} or
     *               {@link Double#isInfinite() infinities}.
     * @return The encoded number in string form.
     * @throws RuntimeException On internal errors. Shouldn't happen.
     */
    public static String numberToString(Number number) {
        if (number == null) {
            throw new RuntimeException("Number must be non-null");
        }

        double doubleValue = number.doubleValue();
        JSON.checkDouble(doubleValue);

        // the original returns "-0" instead of "-0.0" for negative zero
        if (number.equals(NEGATIVE_ZERO)) {
            return "-0";
        }

        long longValue = number.longValue();
        if (doubleValue == (double) longValue) {
            return Long.toString(longValue);
        }

        return number.toString();
    }

    static String doubleToString(double d)
    {
        if (Double.isInfinite(d) || Double.isNaN(d))
        {
            return "null";
        }

        return numberToString(d);
    }

    /**
     * Encodes {@code data} as a JSON string. This applies quotes and any
     * necessary character escaping.
     *
     * @param data the string to encode. Null will be interpreted as an empty
     *             string.
     * @return the quoted string.
     */
    public static String quote(String data) {
        if (data == null) {
            return "\"\"";
        }
        try {
            JSONStringer stringer = new JSONStringer();
            stringer.open(JSONStringer.Scope.NULL, "");
            stringer.string(data);
            stringer.close(JSONStringer.Scope.NULL, JSONStringer.Scope.NULL, "");
            return stringer.toString();
        } catch (RuntimeException e) {
            throw new AssertionError();
        }
    }



    /**
     * Prints the JSONObject using the session.
     *
     * @since 5.2.0
     */
    @Override
    void print(JSONPrintSession session)
    {
        session.printSymbol('{');

        session.indent();

        boolean comma = false;

        for (String key : keys())
        {
            if (comma)
                session.printSymbol(',');

            session.newline();

            session.printQuoted(key);

            session.printSymbol(':');

            printValue(session, nameValuePairs.get(key));

            comma = true;
        }

        session.outdent();

        if (comma)
            session.newline();

        session.printSymbol('}');
    }


    /**
     * Prints a value (a JSONArray or JSONObject, or a value stored in an array or object) using
     * the session.
     *
     * @since 5.2.0
     */
    static void printValue(JSONPrintSession session, Object value)
    {

        if (value == null || value == NULL)
        {
            session.print("null");
            return;
        }
        if (value instanceof JSONObject)
        {
            ((JSONObject) value).print(session);
            return;
        }

        if (value instanceof JSONArray)
        {
            ((JSONArray) value).print(session);
            return;
        }

        if (value instanceof JSONString)
        {
            String printValue = ((JSONString) value).toJSONString();

            session.print(printValue);

            return;
        }

        if (value instanceof Number)
        {
            String printValue = numberToString((Number) value);
            session.print(printValue);
            return;
        }

        if (value instanceof Boolean)
        {
            session.print(value.toString());

            return;
        }

        // Otherwise it really should just be a string. Nothing else can go in.
        session.printQuoted(value.toString());
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (!(obj instanceof JSONObject))
            return false;

        JSONObject other = (JSONObject) obj;

        return nameValuePairs.equals(other.nameValuePairs);
    }

    /**
     * Returns a Map of the keys and values of the JSONObject. The returned map is unmodifiable.
     * Note that changes to the JSONObject will be reflected in the map. In addition, null values in the JSONObject
     * are represented as {@link JSONObject#NULL} in the map.
     *
     * @return unmodifiable map of properties and values
     * @since 5.4
     */
    public Map<String, Object> toMap()
    {
        return Collections.unmodifiableMap(nameValuePairs);
    }

    /**
     * Navigates into a nested JSONObject, creating the JSONObject if necessary. They key must not exist,
     * or must be a JSONObject.
     *
     * @param key
     * @return the nested JSONObject
     * @throws IllegalStateException
     *         if the current value for the key is not null and not JSONObject
     */
    public JSONObject in(String key)
    {
        assert key != null;

        Object nested = nameValuePairs.get(key);

        if (nested != null && !(nested instanceof JSONObject))
        {
            throw new IllegalStateException(String.format("JSONObject[%s] is not a JSONObject.", quote(key)));
        }

        if (nested == null)
        {
            nested = new JSONObject();
            nameValuePairs.put(key, nested);
        }

        return (JSONObject) nested;
    }

    /**
     * Returns the number of key-value mappings in this JSONObject.
     * If it contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this JSONObject
     * @since 5.7
     */
    @Override
    public int size()
    {
        return nameValuePairs.size();
    }

    /**
     * Returns {@code true} if this JSONObject contains no key-value mappings.
     *
     * @return {@code true} if this JSONObject contains no key-value mappings
     * @since 5.7
     */
    @Override
    public boolean isEmpty()
    {
        return nameValuePairs.isEmpty();
    }

    /**
     * Returns {@code true} if this JSONObject contains a mapping for the specified
     * key.
     *
     * @param key
     *            key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified
     *         key
     * @since 5.7
     */
    @Override
    public boolean containsKey(Object key)
    {
        return nameValuePairs.containsKey(key);
    }

    /**
     * Returns {@code true} if this JSONObject maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this JSONObject maps one or more keys to the
     *         specified value
     * @since 5.7
     */
    @Override
    public boolean containsValue(Object value)
    {
        return nameValuePairs.containsValue(value);
    }

    /**
     * Returns the value mapped by {@code name}, or throws if no such mapping exists.
     *
     * @param name The name of the value to get.
     * @return The value.
     * @throws JSONValueNotFoundException if no such mapping exists.
     */ @Override
    public Object get(Object name)
    {
        Object result = nameValuePairs.get(name);
         if (result == null) {
            throw JSONExceptionBuilder.valueNotFound(false, name, JSONType.ANY);
         }
         return result;
    }

    /**
      * Returns the value to which the specified key is mapped, or
      * {@code defaultValue} if this JSONObject contains no mapping for the key.
      *
      * @param key the key whose associated value is to be returned
      * @param defaultValue the default mapping of the key
      * @return the value to which the specified key is mapped, or
      *         {@code defaultValue} if this JSONObject contains no mapping for the key
      * @since 5.7
      */
    @Override
    public Object getOrDefault(Object key, Object defaultValue)
    {
        Object value = opt(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Removes the named mapping if it exists; does nothing otherwise.
     *
     * @param name The name of the mapping to remove.
     * @return the value previously mapped by {@code name}, or null if there was
     *         no such mapping.
     */
    @Override
    public Object remove(Object name)
    {
        return nameValuePairs.remove(name);
    }

    /**
     * Invokes {@link #put(String, Object)} for each value from the map.
     *
     * @param newProperties
     *            to add to this JSONObject
     * @since 5.7
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> newProperties)
    {
        assert newProperties != null;

        for (Map.Entry<? extends String, ? extends Object> e : newProperties.entrySet())
        {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes all of the mappings from this JSONObject.
     * 
     * @since 5.7
     */
    @Override
    public void clear()
    {
        nameValuePairs.clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this JSONObject.
     * The set is backed by the JSONObject, so changes to the map are
     * reflected in the set, and vice-versa.
     *
     * @return a set view of the keys contained in this JSONObject
     * @since 5.7
     */
    @Override
    public Set<String> keySet()
    {
        return nameValuePairs.keySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this JSONObject.
     * The collection is backed by the JSONObject, so changes to the map are
     * reflected in the collection, and vice-versa.
     *
     * @return a collection view of the values contained in this JSONObject
     * @since 5.7
     */
    @Override
    public Collection<Object> values()
    {
        return nameValuePairs.values();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this JSONObject.
     * The set is backed by the JSONObject, so changes to the map are
     * reflected in the set, and vice-versa.
     *
     * @return a set view of the mappings contained in this JSONObject
     * @since 5.7
     */
    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return nameValuePairs.entrySet();
    }

    /**
     * If the specified key is not already associated with a value or is
     * associated with null, associates it with the given non-null value.
     *
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}.
     *
     * @param key
     *            key with which the resulting value is to be associated
     * @param value
     *            the non-null value to be merged with the existing value
     *            associated with the key or, if no existing value or a null value
     *            is associated with the key, to be associated with the key
     * @param remappingFunction
     *            the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no
     *         value is associated with the key
     * @throws NullPointerException
     *             if the specified key is null or the value or remappingFunction
     *             is null
     * @since 5.7
     */
    @Override
    public Object merge(String key, Object value,
            BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction)
    {
        // We need to override the merge method due to the default implementation using
        // #get(String) to check for the key, which will throw a {@code JSONValueNotFoundException}
        // if not found.

        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);

        Object oldValue = opt(key);
        Object newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);

        put(key, newValue);

        return newValue;
    }

}
