// Copyright 2025 The Apache Software Foundation
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException;
import org.apache.tapestry5.json.exceptions.JSONSyntaxException;
import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException;
import org.apache.tapestry5.json.exceptions.JSONValueNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JSONObjectTest {

    @Test
    void copyAnExistingJSONObject() {

        JSONObject master = new JSONObject("fred", "flintstone", "barney", "rubble");

        JSONObject emptyCopy = new JSONObject(master);

        assertTrue(emptyCopy.keys().isEmpty());

        JSONObject limitedCopy = new JSONObject(master, "fred");

        assertEquals(1, limitedCopy.keys().size());
        assertEquals("flintstone", limitedCopy.get("fred"));

        JSONObject fullCopy = new JSONObject(master, "fred", "barney");

        assertEquals("{\"fred\":\"flintstone\",\"barney\":\"rubble\"}", fullCopy.toCompactString());
    }

    @Test
    void copyAllPropertiesOfJSONObject() {

        JSONObject master = new JSONObject("fred", "flintstone", "barney", "rubble");

        JSONObject fullCopy = master.copy();

        assertEquals(master, fullCopy);

        master.put("wilma", "flintstone");

        assertNotEquals(master, fullCopy);
    }

    @Test
    void unknownKeysWhenCopyingAJSONObjectAreIgnored() {

        JSONObject master = new JSONObject("fred", "flintstone", "barney", "rubble");

        JSONObject copy = new JSONObject(master, "fred", "wilma");

        assertEquals(1, copy.keys().size());
        assertEquals("flintstone", copy.get("fred"));
    }

    @Test
    void getWithUnknownKeyIsAnException() {

        JSONObject master = new JSONObject("fred", "flintstone");

        JSONValueNotFoundException e = assertThrows(JSONValueNotFoundException.class, () -> {
            master.get("barney");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"barney\"] is not found."));
        assertTrue(e.getMessage().contains("Required: ANY"));
    }

    @Test
    void getOrDefaultReturnsDefaultValueIfNotFound() {

        JSONObject master = new JSONObject("fred", "flintstone");

        Object result = master.getOrDefault("barney", "gumble");

        assertEquals("gumble", result);
    }

    @Test
    void getOrDefaultReturnsValueIfFound() {

        JSONObject master = new JSONObject("fred", "flintstone");

        Object result = master.getOrDefault("fred", "other");

        assertEquals("flintstone", result);
    }

    // Data provider for ensureThatStoredValueIsExpected
    static Stream<Arguments> storedValueProvider() {
        return Stream.of(
                Arguments.of("true", true, "Boolean"),
                Arguments.of("TRUE", true, "Boolean"),
                Arguments.of("false", false, "Boolean"),
                Arguments.of("FALSE", false, "Boolean"),
                Arguments.of(Boolean.TRUE, true, "Boolean"),
                Arguments.of(Boolean.FALSE, false, "Boolean"),
                Arguments.of(3.5d, 3.5d, "Double"),
                Arguments.of(1_000L, 1_000d, "Double"),
                Arguments.of("-101.7", -101.7d, "Double"),
                Arguments.of("3", 3, "Int"),
                Arguments.of(97L, 97, "Int"),
                Arguments.of("-8.76", -8, "Int"),
                Arguments.of(92d, 92L, "Long"),
                Arguments.of("-200", -200L, "Long"),
                Arguments.of(123, "123", "String")
        );
    }

    @ParameterizedTest(name = "ensure that stored value {0} ({argumentsWithNames}) is {2} {1}")
    @MethodSource("storedValueProvider")
    void ensureThatStoredValueIsExpected(Object value, Object expected, String expectedType) {

        JSONObject object = new JSONObject().put("key", value);
        Function<String, Object> fn;
        switch (expectedType) {
            case "Boolean":
                fn = object::getBoolean;
                break;
            case "Double":
                fn = object::getDouble;
                break;
            case "Int":
                fn = object::getInt;
                break;
            case "Long":
                fn = object::getLong;
                break;
            case "String":
                fn = object::getString;
                break;
            default:
                throw new IllegalArgumentException("Unknown expected type: " + expectedType);
        }

        Object result = fn.apply("key");

        assertEquals(expected, result);
    }


    static Stream<Arguments> doubleToStringProvider() {
        return Stream.of(
                Arguments.of(3d, "3"),
                Arguments.of(-22.5d, "-22.5"),
                Arguments.of(0d, "0"),
                Arguments.of(Double.NaN, "null"),
                Arguments.of(Double.NEGATIVE_INFINITY, "null"),
                Arguments.of(Double.POSITIVE_INFINITY, "null")
        );
    }

    @ParameterizedTest(name = "conversion of double {0} to string should be ''{1}''")
    @MethodSource("doubleToStringProvider")
    void doubleToStringConversion(double input, String expected) {

        assertEquals(expected, JSONObject.doubleToString(input));
    }

    @Test
    void exceptionWhenGetBooleanOnAValueThatIsNotABoolean() {

        JSONObject object = new JSONObject().put("key", 37);

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            object.getBoolean("key");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"key\"] is not a BOOLEAN."));
    }

    @Test
    void accumulateSimpleValues() {

        JSONObject object = new JSONObject();
        String key = "acckey";

        object.accumulate(key, "alpha");

        assertEquals("{\"acckey\":\"alpha\"}", object.toCompactString());

        object.accumulate(key, "beta");
        object.accumulate(key, "gamma");

        assertEquals("{\"acckey\":[\"alpha\",\"beta\",\"gamma\"]}", object.toCompactString());
        assertEquals(3, object.getJSONArray(key).size());
    }

    @Test
    void accumulateWhenTheStoredValueIsAlreadyAnArrayJustAppendsTheValue() {

        JSONObject object = new JSONObject();

        object.accumulate("key", new JSONArray("fred", "barney"));

        assertEquals("{\"key\":[\"fred\",\"barney\"]}", object.toCompactString());

        object.accumulate("key", "wilma");

        assertEquals("{\"key\":[\"fred\",\"barney\",\"wilma\"]}", object.toCompactString());
    }

    @Test
    void canCreateAJSONObjectFromAJSONString() {

        JSONObject object = new JSONObject("{ fred: \"flintstone\", caveman: true, friends: [\"barney\"] }");

        assertEquals("flintstone", object.get("fred"));
        assertTrue(object.getBoolean("caveman"));

        JSONArray friends = object.getJSONArray("friends");

        assertEquals(1, friends.size());
        assertEquals("barney", friends.get(0));
    }

    @Test
    void appendToAnUnknownKeyCreatesAJSONArrayForTheValue() {

        JSONObject object = new JSONObject();

        object.append("friends", "barney");

        assertEquals("{\"friends\":[\"barney\"]}", object.toCompactString());
    }

    @Test
    void appendToAnExistingKeyContainingAJSONArrayAppendsToTheArray() {

        JSONObject object = new JSONObject("{friends: [\"barney\"] }");

        object.append("friends", "zaphod");

        assertEquals("{\"friends\":[\"barney\",\"zaphod\"]}", object.toCompactString());
    }

    @Test
    void appendingToAKeyWhoseValueIsNotAArrayIsAnException() {

        JSONObject object = new JSONObject("{friends: 0 }");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            object.append("friends", "zaphod");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"friends\"] is not a ARRAY."));
    }

    @Test
    void getDoubleWithANonNumericValueIsAnException() {

        JSONObject object = new JSONObject("{notdouble: true}");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            object.getDouble("notdouble");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"notdouble\"] is not a NUMBER."));
    }

    @Test
    void getDoubleWithAStringThatCanNotBeParsedAsANumberIsAnException() {

        JSONObject object = new JSONObject("{notdouble: \"this is a fact\"}");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            object.getDouble("notdouble");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"notdouble\"] is not a NUMBER."));
    }

    @Test
    void hasWillIdentifyWhichKeysHaveValuesAndWhichDoNot() {

        JSONObject object = new JSONObject("{fred: 'flintstone'}");

        assertTrue(object.has("fred"));
        assertFalse(object.has("barney"));
    }

    @Test
    void getJSONArrayForAValueThatIsNotAArrayIsAnException() {

        JSONObject object = new JSONObject("{notarray: 22.7}");

        JSONTypeMismatchException e = assertThrows(JSONTypeMismatchException.class, () -> {
            object.getJSONArray("notarray");
        });

        assertTrue(e.getMessage().startsWith("JSONObject[\"notarray\"] is not a ARRAY."));
    }

    @Test
    void lengthOfAJSONObjectIsTheNumberOfKeys() {

        JSONObject object = new JSONObject();

        assertEquals(0, object.size());

        object.put("fred", "flintstone");

        assertEquals(1, object.size());

        object.accumulate("fred", "murray");

        assertEquals(1, object.size());

        object.put("barney", "rubble");

        assertEquals(2, object.size());
    }

    @Test
    void namesReturnsTheKeysAsAJSONArray() {

        JSONObject object = new JSONObject("{fred: 'flintstone', barney: 'rubble' }");

        JSONArray names = object.names();

        assertNotNull(names);
        assertTrue(names instanceof JSONArray);
        assertEquals(2, names.size());

        List<Object> nameList = new ArrayList<>(names.toList());
        Collections.sort(nameList, (o1, o2) -> ((String) o1).compareTo((String) o2));

        assertEquals(Arrays.asList("barney", "fred"), nameList);
    }

    @Test
    void namesWithNoPropertiesReturnsNull() {

        JSONObject object = new JSONObject();

        assertNull(object.names());
    }

    @Test
    void theNullLiteralIsTheNULLObject() {

        JSONObject object = new JSONObject("{nullkey: null}");

        assertSame(JSONObject.NULL, object.get("nullkey"));
        assertTrue(object.isNull("nullkey"));
    }

    @Test
    void theNULLObjectIsOutputAsNullLiteral() {

        JSONObject object = new JSONObject().put("nullkey", JSONObject.NULL);

        assertTrue(object.isNull("nullkey"));
        assertEquals("{\"nullkey\":null}", object.toCompactString());
    }

    @Test
    void theNULLObjectMatchesJavasNull() {

        assertTrue(JSONObject.NULL.equals(null));
    }

    static Stream<Arguments> invalidInputProvider() {
        return Stream.of(
            Arguments.of("{  ", "A JSONObject text must end with '}' at character 3", "unmatched open brace"),
            Arguments.of("fred", "A JSONObject text must start with '{' (actual: 'f') at character 1 of", "missing open brace"),
            Arguments.of("{ \"akey\" }", "Expected a ':' after a key at character 10 of", "missing value after key"),
            Arguments.of("{ \"fred\" : 1 \"barney\" }", "Expected a ',' or '}' at character 14 of", "missing property separator"),
            Arguments.of("{ \"list\" : [1, 2", "Expected a ',' or ']' at character 16 of", "missing seperator or closing bracket"),
            Arguments.of("/* unclosed", "Unclosed comment at character 11 of", "unclosed C-style comment"),
            Arguments.of("{ \"fred \\n}", "Unterminated string at character 11 of", "unterminated string at line end"),
            Arguments.of("{ fred: ,}", "Missing value at character 8 of ", "missing value after key")
        );
    }

    @ParameterizedTest(name = "invalid input ({2}) throws exception")
    @MethodSource("invalidInputProvider")
    void invalidInputThrowsException(String input, String expected, String desc) {
        // When/Then
        JSONSyntaxException e = assertThrows(JSONSyntaxException.class, () -> {
            new JSONObject(input);
        });
        
        assertTrue(e.getMessage().trim().startsWith(expected), 
            "Expected message to start with: " + expected + ", but was: " + e.getMessage().trim());
    }


    @Test
    void canUseColonOrArrowAsKeySeperatorAndSemicolonAsPropertySeparator() {
        JSONObject object = new JSONObject("{ \"fred\" = 1; \"barney\" => 2}");

        assertEquals(1, object.getInt("fred"));
        assertEquals(2, object.getInt("barney"));
    }

    static Stream<Arguments> numberToStringConversionProvider() {
        return Stream.of(
                Arguments.of(new BigDecimal("100.0000000"), "100")
        );
    }

    @ParameterizedTest(name = "conversion of {0} ({argumentsWithNames}) to string is {1}")
    @MethodSource("numberToStringConversionProvider")
    void numberToStringConversion(Number value, String expected) {

        assertEquals(expected, JSONObject.numberToString(value));
    }

   static Stream<Arguments> quoteProvider() {
        return Stream.of(
            Arguments.of(null, "\"\"", "null is empty string"),
            Arguments.of("", "\"\"", "empty string is empty string"),
            Arguments.of("\"/\b\t\n\f\r\u2001/a</", "\"\\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/\"", "special characters")
        );
    }

    @ParameterizedTest(name = "quote(): {2}")
    @MethodSource("quoteProvider")
    void quoteTest(String value, String expected, String desc) {

        assertEquals(expected, JSONObject.quote(value));
    }

    static Stream<Arguments> nonFiniteNumbersProvider() {
        return Stream.of(
                Arguments.of(Double.NaN),
                Arguments.of(Double.NEGATIVE_INFINITY),
                Arguments.of(Double.POSITIVE_INFINITY),
                Arguments.of(Float.NaN),
                Arguments.of(Float.NEGATIVE_INFINITY),
                Arguments.of(Float.POSITIVE_INFINITY)
        );
    }

    @ParameterizedTest
    @MethodSource("nonFiniteNumbersProvider")
    void nonFiniteNumbersAreNotAllowed(Number value) {

        JSONObject object = new JSONObject();

        RuntimeException e = assertThrows(IllegalArgumentException.class, () -> {
            object.put("nonfinite", value);
        });

        assertEquals("JSON does not allow non-finite numbers.", e.getMessage());
    }

    @Test
    void parseAnEmptyObjectIsEmpty() {
        JSONObject object = new JSONObject("{}");
        assertEquals(0, object.size());
    }

    @Test
    void putAJavaNullIsARemove() {

        JSONObject object = new JSONObject("{fred:'flintstone', barney:'rubble'}");

        object.put("fred", null);

        assertEquals("{\"barney\":\"rubble\"}", object.toCompactString());
    }

    static Stream<Arguments> validTypesForPutProvider() {
        return Stream.of(
                Arguments.of((Object) null),
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
    @MethodSource("validTypesForPutProvider")
    void onlySpecificObjectTypesMayBeAddedNoException(Object value) {

        JSONObject object = new JSONObject("{}");

        assertDoesNotThrow(() -> object.put("key", value));
    }


    static Stream<Arguments> invalidTypesForPutProvider() {
        return Stream.of(
                Arguments.of(new java.util.Date()),
                Arguments.of((Object) new Object[] {}),
                Arguments.of(Collections.emptyMap())
        );
    }
    @ParameterizedTest
    @MethodSource("invalidTypesForPutProvider")
    void onlySpecificObjectTypesMayBeAddedException(Object value) {

        JSONObject object = new JSONObject("{}");

        assertThrows(JSONInvalidTypeException.class, () -> object.put("key", value));
    }

    @Test
    void JSONStringCanOutputAnythingItWants() {
        JSONString string = () -> "*VALUE*";
        JSONObject object = new JSONObject();

        object.put("key", string);

        assertEquals("{\"key\":*VALUE*}", object.toCompactString());
    }

    @Test
    void equalsImplementation() {
        String jsonStr = "{ \"key\" : 99 }";
        JSONObject obj1 = new JSONObject(jsonStr);
        JSONObject obj2 = new JSONObject(jsonStr);

        assertEquals(obj1, obj2);
        assertEquals(obj2, obj1);
        assertNotEquals(null, obj1);
        assertNotEquals(jsonStr, obj1);

        obj2.put("screw", "the pooch");

        assertNotEquals(obj1, obj2);
    }

    @Test
    void hashCodeImplementation() {

        String jsonStr = "{ \"key\" : 99 }";
        JSONObject obj1 = new JSONObject(jsonStr);
        JSONObject obj2 = new JSONObject(jsonStr);

        assertEquals(obj1.hashCode(), obj2.hashCode());

        obj2.put("screw", "the pooch");

        assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void escapedCharactersInTheJSONAreParsedCorrectly() {

        JSONObject object = new JSONObject("{ \"key\" : \"\\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/\\x20\" }");

        assertEquals("\"/\b\t\n\f\r\u2001/a</ ", object.get("key"));
    }

    @Test
    void parseANestedObject() {

        JSONObject object = new JSONObject("{ key: { name: \"inner\" }}");

        assertEquals("inner", object.getJSONObject("key").getString("name"));
    }

    @Test
    void parseBooleanLiterals() {

        JSONObject object = new JSONObject("{ t: true, f: false }");

        assertTrue(object.getBoolean("t"));
        assertFalse(object.getBoolean("f"));
    }

    @Test
    void parseNumericForms() {

        JSONObject object = new JSONObject("{ hex: 0x50, oct: 030, posInt: +50, negInt: -50, long: 4294968530, float: -32.7 }");

        assertEquals(80, object.getInt("hex"));
        assertEquals(24, object.getInt("oct"));
        assertEquals(50, object.getInt("posInt"));
        assertEquals(-50, object.getInt("negInt"));
        assertEquals(4294968530L, object.getLong("long"));
        assertEquals(-32.7d, object.getDouble("float"));
    }

    @Test
    void NULLToStringIsNullKeyword() {

        assertEquals("null", JSONObject.NULL.toString());
    }

    @Test
    void JSONLiteralCanOutputAnyContent() {

        String text = "function(x) { $('bar').show(); }";
        JSONLiteral literal = new JSONLiteral(text);
        JSONObject object = new JSONObject().put("callback", literal);

        assertEquals("{\"callback\":" + text + "}", object.toCompactString());
        assertEquals(text, literal.toString());
    }

    @Test
    void prettyPrintTest() {

        JSONObject object = new JSONObject("fred", "flintstone", "barney", "rubble");
        String expected = "{\n  \"fred\" : \"flintstone\",\n  \"barney\" : \"rubble\"\n}";

        assertEquals(expected.trim(), object.toString().trim());
    }

    @Test
    void cStyleCommentsAreIgnored() {

        JSONObject object = new JSONObject("/* C comment */ { \"foo\" : \"bar\" /* *ignored* */ }");

        assertEquals("bar", object.get("foo"));
    }

    @Test
    void doubleSlashCommentsExtendToEndOfLine() {

        JSONObject object = new JSONObject("{ foo: // This is a double-slash comment\n\"bar\" }");

        assertEquals("bar", object.get("foo"));
    }

    @Test
    void hashCommentsExtendToEndOfLine() {

        JSONObject object = new JSONObject("{ foo: # This is a hash comment\n\"bar\" }");

        assertEquals("bar", object.get("foo"));
    }

    @Test
    void prettyPrintAnEmptyJSONObject() {

        JSONObject object = new JSONObject();

        assertEquals("{}", object.toString());
    }

    @Test
    void complexPrettyPrint() {
        JSONObject object = new JSONObject(
                "{ outer: { inner: \"here\" },\n" +
                "  number: 22.7,\n" +
                "  array: [1, 2, 3],\n" +
                "  boolean: true,\n" +
                "  otherwise: 'a quoted string'\n" +
                "}"
        );
        String expected = "{\n" +
                          "  \"outer\" : {\n" +
                          "    \"inner\" : \"here\"\n" +
                          "  },\n" +
                          "  \"number\" : 22.7,\n" +
                          "  \"array\" : [\n" +
                          "    1,\n" +
                          "    2,\n" +
                          "    3\n" +
                          "  ],\n" +
                          "  \"boolean\" : true,\n" +
                          "  \"otherwise\" : \"a quoted string\"\n" +
                          "}";
        assertEquals(expected.trim(), object.toString().trim());
    }


    @Test
    void toCompactStringTest() {

        JSONObject object = new JSONObject("kermit", "frog");

        assertEquals("{\"kermit\":\"frog\"}", object.toString(true));
        assertEquals("{\n  \"kermit\" : \"frog\"\n}", object.toString(false).replace("\r\n", "\n"));
    }

    private static String withPrintWriter(Consumer<PrintWriter> closure) {

        CharArrayWriter caw = new CharArrayWriter();
        try (PrintWriter pw = new PrintWriter(caw)) {
            closure.accept(pw);
        }

        return caw.toString();
    }

    private static String print(JSONObject object, boolean compact) {
        return withPrintWriter(pw -> object.print(pw, compact));
    }

    @Test
    void printToPrintWriterTest() {

        JSONObject object = new JSONObject("kermit", "frog");

        assertEquals("{\"kermit\":\"frog\"}", print(object, true));
        assertEquals("{\n  \"kermit\" : \"frog\"\n}", print(object, false).replace("\r\n", "\n"));
    }


    @Test
    void prettyPrintToPrintWriter() {

        JSONObject object = new JSONObject("kermit", "frog");

        String json = withPrintWriter(pw -> object.prettyPrint(pw));
        assertEquals("{\n  \"kermit\" : \"frog\"\n}", json.replace("\r\n", "\n"));
    }

    @Test
    void getStringAtIndex() {

        JSONArray array = new JSONArray("one", 2, false);

        assertEquals("one", array.getString(0));
        assertEquals("2", array.getString(1));
        assertEquals("false", array.getString(2));
    }

    @Test
    void toMapIsNotModifiable() {

        JSONObject object = new JSONObject();
        Map<String, Object> map = object.toMap();

        assertThrows(UnsupportedOperationException.class, map::clear);
    }

    @Test
    void canAccessContentsOfObjectAsAMap() {

        JSONObject object = new JSONObject("foo", "bar")
                .put("null", JSONObject.NULL)
                .put("number", 6);
        Map<String, Object> map = object.toMap();

        assertEquals("bar", map.get("foo"));
        assertEquals(6, map.get("number"));
        assertSame(JSONObject.NULL, map.get("null"));
    }

    @Test
    void theMapReturnedByToMapIsLive() {

        JSONObject object = new JSONObject("foo", "bar");
        Map<String, Object> map = object.toMap();

        object.put("foo", null);

        assertEquals(0, object.size());
        assertTrue(map.isEmpty());
    }

    @Test
    void canAddNewPropertiesViaPutAll() {

        JSONObject object = new JSONObject("fred", "flintstone");
        Map<String, Object> newProps = new HashMap<>();
        newProps.put("barney", "rubble");
        newProps.put("wilma", "flintstone");

        object.putAll(newProps);

        assertTrue(object.keySet().containsAll(Arrays.asList("fred", "barney", "wilma")));
        assertEquals(
            Stream.of("flintstone", "rubble", "flintstone").sorted().collect(Collectors.toList()),
            object.values().stream().sorted().collect(Collectors.toList())
        );
    }

    @Test
    void inWhereTheValueIsNotJSONObjectIsAnException() {

        JSONObject object = new JSONObject("time", "to rock");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            object.in("time");
        });

        assertEquals("JSONObject[\"time\"] is not a JSONObject.", e.getMessage());
    }

    @Test
    void inCreatesAJSONObjectIfNecessary() {

        JSONObject object = new JSONObject();

        object.in("nested").put("created", true);

        assertEquals("{\"nested\":{\"created\":true}}", object.toCompactString());
    }

    @Test
    void inReturnsTheJSONObject() {

        JSONObject object = new JSONObject("{nested: {}}");

        object.in("nested").put("time", "to rock");

        assertEquals("{\"nested\":{\"time\":\"to rock\"}}", object.toCompactString());
    }

    @Test
    void constructorSupportsNonStringKeysAndValues() {

        JSONObject object = new JSONObject(true, "truthy", false, false);

        assertTrue(object.containsKey("true")); // Keys are stringified
        assertEquals("truthy", object.getString("true"));
        assertTrue(object.containsKey("false"));
        assertEquals(false, object.get("false"));
    }


    private static Object copyViaSerialization(Object source) throws IOException, ClassNotFoundException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }

    @Test
    void serializeAndDeSerialize() throws IOException, ClassNotFoundException {

        JSONObject source = new JSONObject("string", "a string", "null", JSONObject.NULL);

        JSONObject copy = (JSONObject) copyViaSerialization(source);

        assertEquals(source, copy);
    }

    // TAP5-2759
    @Test
    void getLongOrDefaultCorrectTypeForDefaultValue() {
        JSONObject object = new JSONObject();
        long defaultValue = 2_147_483_648L; // This is Integer.MAX_VALUE + 1, so it's a long
        long value = object.getLongOrDefault("key", defaultValue);

        assertEquals(defaultValue, value);
    }
}