// Copyright 2010-2023, 2025 The Apache Software Foundation
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.tapestry5.json.exceptions.JSONArrayIndexOutOfBoundsException;
import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException;
import org.apache.tapestry5.json.exceptions.JSONSyntaxException;
import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JSONArrayTest {

    @Test
    void canCreateAJSONArrayByParsingAJSONString() {
        String jsonString = "[ 'foo', 'bar', \"baz\" ]";

        JSONArray array = new JSONArray(jsonString);

        assertEquals(3, array.size());
        assertEquals("foo", array.get(0));
        assertEquals("bar", array.get(1));
        assertEquals("baz", array.get(2));
    }

    @Test
    void createAJSONArrayFromVarags() {
        JSONArray array = new JSONArray("fred", "barney", "wilma");

        assertEquals(3, array.size());
        assertEquals("[\"fred\",\"barney\",\"wilma\"]", array.toCompactString());
    }

    @Test
    void arrayMustStartWithABracket() {
        JSONSyntaxException e = assertThrows(JSONSyntaxException.class, () -> {
            new JSONArray("1, 2, 3]");
        });

        assertEquals("A JSONArray text must start with '[' (actual: '1') at character 1 of 1, 2, 3]", e.getMessage());
    }

    @Test
    void parseAnEmptyArray() {
        JSONArray array = new JSONArray("[]");
        assertTrue(array.isEmpty());
    }

    @Test
    void isEmptyIsFalseIfArrayIsNonEmpty() {
        JSONArray array = new JSONArray("[1]");
        assertFalse(array.isEmpty());
    }

    @Test
    void isEmptyEqualsZeroLengthEqualsZeroSize() {
        JSONArray array = new JSONArray("[]");

        assertTrue(array.isEmpty());
        assertEquals(0, array.length());
        assertEquals(0, array.size());
    }

    @Test
    void anEmptyElementInTheParseIsANull() {
        JSONArray array = new JSONArray("[1,,3]");

        assertEquals(3, array.size());
        assertEquals(1, array.getInt(0));
        assertTrue(array.isNull(1));
        assertEquals(3, array.getInt(2));
    }

    @Test
    void aCommaAtTheEndOfTheListIsIgnored() {
        JSONArray array = new JSONArray("[1,2,]");

        assertEquals(2, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
    }

    @Test
    void getBooleanWhenNotABooleanAtIndexIsAnException() {
        JSONArray array = new JSONArray("[123]");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            array.getBoolean(0);
        });

        assertEquals("JSONArray[0] is not a BOOLEAN. Actual: java.lang.Integer", e.getMessage());
    }

    @Test
    void handlingOfBooleanValuesPassedIntoConstructor() {
        JSONArray array = new JSONArray(true, false, "True", "False");

        assertTrue(array.getBoolean(0));
        assertFalse(array.getBoolean(1));
        assertTrue(array.getBoolean(2));
        assertFalse(array.getBoolean(3));
    }

    @Test
    void getDoubleWhenNotADoubleAtIndexIsAnException() {
        JSONArray array = new JSONArray(true);

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            array.getDouble(0);
        });

        assertEquals("JSONArray[0] is not a NUMBER. Actual: java.lang.Boolean", e.getMessage());
    }

    @Test
    void getDoubleWorksWithNumbersAndParseableStrings() {
        JSONArray array = new JSONArray(400L, "95.5");

        assertEquals(400d, array.getDouble(0));
        assertEquals(95.5d, array.getDouble(1));
    }

    @Test
    void getLongWorksWithNumbersAndParseableStrings() {
        JSONArray array = new JSONArray(400L, "95.5");

        assertEquals(400L, array.getLong(0));
        assertEquals(95L, array.getLong(1));
    }

    @Test
    void getJSONArrayWhenNotAnArrayAtIndexIsException() {
        JSONArray array = new JSONArray("fred", "barney");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            array.getJSONArray(1);
        });

        assertEquals("JSONArray[1] is not a ARRAY. Actual: java.lang.String", e.getMessage());
    }

    @Test
    void getANestedArray() {
        JSONArray nested = new JSONArray();
        JSONArray outer = new JSONArray(nested);

        assertSame(nested, outer.getJSONArray(0));
    }

    @Test
    void getJSONObjectWhenNotAJSONObjectAtIndexIsAnException() {
        JSONArray array = new JSONArray("fred", "barney", "wilma");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            array.getJSONObject(1);
        });

        assertEquals("JSONArray[1] is not a OBJECT. Actual: java.lang.String", e.getMessage());
    }

    @Test
    void mayNotPutAtANegativeIndex() {
        JSONArray array = new JSONArray();

        JSONArrayIndexOutOfBoundsException e = assertThrows(JSONArrayIndexOutOfBoundsException.class, () -> {
            array.put(-2, "fred");
        });

        assertEquals(-2, e.getIndex());
    }

    @Test
    void putOverridesExistingValueInArray() {
        JSONArray array = new JSONArray("fred", "barney", "wilma");
        array.put(2, "betty");
        assertEquals("betty", array.get(2));
    }

    @Test
    void putWithIndexPastEndPadsArrayWithNulls() {
        JSONArray array = new JSONArray("fred", "barney", "wilma");
        array.put(4, "bambam");

        assertTrue(array.isNull(3));
        assertEquals("[\"fred\",\"barney\",\"wilma\",null,\"bambam\"]", array.toCompactString());
    }

    @Test
    void arrayEquality() {
        JSONArray array1 = new JSONArray(1, 2, 3);
        JSONArray array2 = new JSONArray(1, 2, 3);

        assertEquals(array1, array1);
        assertNotEquals(null, array1);
        assertNotEquals(this, array1);
        assertEquals(array1, array2);

        array2.put(9, "stuff");
        assertNotEquals(array1, array2);
    }

    @Test
    void arrayHashCode() {
        JSONArray array1 = new JSONArray(1, 2, 3);
        JSONArray array2 = new JSONArray(1, 2, 3);

        assertEquals(array1.hashCode(), array2.hashCode());

        array2.put(9, "stuff");
        assertNotEquals(array1.hashCode(), array2.hashCode());
    }

    @Test
    void prettyPrint() {
        JSONArray array = new JSONArray("fred", "barney");
        String expected = "[\n  \"fred\",\n  \"barney\"\n]";

        assertEquals(expected, array.toString());
    }

    @Test
    void JSONArrayIsIterable() {
        JSONArray array = new JSONArray(1, 2, false);
        Iterator<?> i = array.iterator();

        assertEquals(1, i.next());
        assertEquals(2, i.next());
        assertEquals(false, i.next());
        assertFalse(i.hasNext());
    }

    @Test
    void removeAnElementByIndex() {
        JSONArray array = new JSONArray("one", "two", "three");
        array.remove(1);

        assertEquals(2, array.size());
        assertEquals("[\"one\",\"three\"]", array.toCompactString());
    }

    @Test
    void removeAnElementByValue() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.remove("one");

        assertTrue(result);
        assertEquals(2, array.size());
        assertEquals("[\"two\",\"three\"]", array.toCompactString());
    }

    @Test
    void removeAnElementByValueNotFound() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.remove("four");

        assertFalse(result);
        assertEquals(3, array.size());
        assertEquals("[\"one\",\"two\",\"three\"]", array.toCompactString());
    }

    @Test
    void removeAllElementsByCollection() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.removeAll(Arrays.asList("one", "three"));

        assertTrue(result);
        assertEquals(1, array.size());
        assertEquals("[\"two\"]", array.toCompactString());
    }

    @Test
    void removeAllElementsByCollectionPartial() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.removeAll(Arrays.asList("one", "four"));

        assertTrue(result);
        assertEquals(2, array.size());
        assertEquals("[\"two\",\"three\"]", array.toCompactString());
    }

    @Test
    void removeAllElementsByCollectionNoneFound() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.removeAll(Arrays.asList("four", "five"));

        assertFalse(result);
        assertEquals(3, array.size());
        assertEquals("[\"one\",\"two\",\"three\"]", array.toCompactString());
    }

    @Test
    void retainAllElementsByCollectionAll() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.retainAll(Arrays.asList("one", "two", "three"));

        assertFalse(result);
        assertEquals(3, array.size());
        assertEquals("[\"one\",\"two\",\"three\"]", array.toCompactString());
    }

    @Test
    void retainAllElementsByCollectionPartial() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.retainAll(Arrays.asList("one", "three"));

        assertTrue(result);
        assertEquals(2, array.size());
        assertEquals("[\"one\",\"three\"]", array.toCompactString());
    }

    @Test
    void retainAllElementsByCollectionNone() {
        JSONArray array = new JSONArray("one", "two", "three");
        boolean result = array.retainAll(Arrays.asList("four", "five"));

        assertTrue(result);
        assertTrue(array.isEmpty());
        assertEquals("[]", array.toCompactString());
    }

    @Test
    void putAllAddsNewObjectsToExistingArray() {
        JSONArray array = new JSONArray(100, 200);
        array.putAll(Arrays.asList(300, 400, 500));

        assertEquals("[100,200,300,400,500]", array.toCompactString());
    }

    @Test
    void putAllWithNullDoesNotModifyTheArray() {
        JSONArray array = new JSONArray(100, 200);
        array.putAll(null);

        assertEquals("[100,200]", array.toCompactString());
    }

    @Test
    void addAllAddsNewObjectsToExistingArray() {
        JSONArray array = new JSONArray(100, 200);
        array.addAll(Arrays.asList(300, 400, 500));

        assertEquals("[100,200,300,400,500]", array.toCompactString());
    }

    @Test
    void addAllReturnsTrueIfChanged() {
        JSONArray array = new JSONArray(100, 200);
        boolean result = array.addAll(Arrays.asList(300, 400, 500));

        assertTrue(result);
    }

    @Test
    void addAllReturnsFalseIfNotChanged() {
        JSONArray array = new JSONArray(100, 200);
        boolean result = array.addAll((Collection<?>) null);

        assertFalse(result);
    }

    @Test
    void listReturnedByToListIsUnmodifiable() {
        JSONArray array = new JSONArray(100, 200);
        List<?> list = array.toList();

        assertThrows(UnsupportedOperationException.class, list::clear);
    }

    @Test
    void listFromToListIsLive() {
        JSONArray array = new JSONArray(100, 200);
        List<?> list = array.toList();

        assertEquals(Arrays.asList(100, 200), list);

        array.remove(0);

        assertEquals(Arrays.asList(200), list);
    }

    @Test
    void canInitializeJSONArrayFromAnIterable() {
        JSONArray list = JSONArray.from(Arrays.asList(1, 2, 3));

        assertEquals("[1,2,3]", list.toString(true));
    }

    @Test
    void putShouldThrowAnIllegalArgumentExceptionWhenReceivingNull() {
        JSONArray array = new JSONArray();

        assertThrows(IllegalArgumentException.class, () -> array.put(null));
    }

    @Test
    void newJSONArrayShouldThrowAnIllegalArgumentExceptionWhenReceivingNull() {
        assertThrows(IllegalArgumentException.class, () -> new JSONArray(1, null, 3));
    }

    static Stream<Object> validTypesProvider() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(3),
            Arguments.of(3.5),
            Arguments.of("*VALUE*"),
            Arguments.of(new JSONLiteral("*LITERAL*")),
            Arguments.of(new JSONObject()),
            Arguments.of(new JSONArray())
        );
    }

    @ParameterizedTest
    @MethodSource("validTypesProvider")
    void onlySpecificObjectTypesMayBeAddedNoException(Object value) {
        JSONArray array = new JSONArray();

        assertDoesNotThrow(() -> array.put(value));
    }

    static Stream<Arguments> invalidTypesProvider() {
        return Stream.of(
            Arguments.of(new java.util.Date()),
            Arguments.of((Object) new Object[] {}),
            Arguments.of(Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTypesProvider")
    void onlySpecificObjectTypesMayBeAddedException(Object value) {
        JSONArray array = new JSONArray();

        assertThrows(JSONInvalidTypeException.class, () -> array.put(value));
    }

    @Test
    void arrayIndexOutOfBoundsMustThrowInformativeException() {
        JSONArray array = new JSONArray(1, 2, 3);
        JSONArrayIndexOutOfBoundsException e = assertThrows(JSONArrayIndexOutOfBoundsException.class, () -> {
            array.get(array.size());
        });

        assertEquals(3, e.getIndex());
    }

    static Stream<Arguments> nonFiniteDoubleProvider() {
        return Stream.of(
            Arguments.of(Double.POSITIVE_INFINITY),
            Arguments.of(Double.NEGATIVE_INFINITY),
            Arguments.of(Double.NaN)
        );
    }

    @ParameterizedTest
    @MethodSource("nonFiniteDoubleProvider")
    void nonFiniteOrNaNDoubleNotAllowedInConstructor(Double value) {
        assertThrows(IllegalArgumentException.class, () -> new JSONArray(value));
    }
}