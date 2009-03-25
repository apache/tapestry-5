// Copyright 2007, 2009 The Apache Software Foundation
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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Tests JSONObject, particularily in terms of parsing and writing JSON streams.
 */
public class JSONObjectTest extends Assert
{
    @Test
    public void copy_from_object_constructor()
    {
        JSONObject master = new JSONObject().put("fred", "flintstone").put("barney", "rubble");

        JSONObject emptyCopy = new JSONObject(master);

        assertTrue(emptyCopy.keys().isEmpty(), "No properties should have been copied.");

        JSONObject limitedCopy = new JSONObject(master, "fred");

        assertEquals(limitedCopy.keys().size(), 1);
        assertEquals(limitedCopy.get("fred").toString(), "flintstone");

        JSONObject fullCopy = new JSONObject(master, "fred", "barney");

        assertEquals(fullCopy.toString(), "{\"fred\":\"flintstone\",\"barney\":\"rubble\"}");

        JSONObject limitedCopy2 = new JSONObject(master, "fred", "wilma");
        assertEquals(limitedCopy2.keys().size(), 1);
    }

    @Test
    public void array_from_string()
    {
        JSONArray array = new JSONArray("[ 'foo', 'bar', \"baz\" ]");

        assertEquals(array.length(), 3);
        assertEquals(array.getString(0), "foo");
        assertEquals(array.getString(1), "bar");
        assertEquals(array.getString(2), "baz");
    }

    private void unreachable()
    {
        throw new AssertionError("This code should not be reachable.");
    }

    @Test
    public void get_not_found()
    {
        JSONObject master = new JSONObject().put("fred", "flintstone");

        assertEquals(master.get("fred"), "flintstone");

        try
        {
            master.get("barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"barney\"] not found.");
        }
    }

    @Test(dataProvider = "boolean_inputs")
    public void get_boolean(Object value, boolean expected)
    {
        JSONObject object = new JSONObject().put("mykey", value);

        assertEquals(object.getBoolean("mykey"), expected);
    }

    @DataProvider
    public Object[][] boolean_inputs()
    {
        return new Object[][] { { "true", true }, { "TRUE", true }, { "false", false }, { "FALSE", false },
                { Boolean.TRUE, true },
                { Boolean.FALSE, false } };
    }

    @Test
    public void not_a_boolean_value()
    {
        JSONObject object = new JSONObject().put("somekey", 37);

        try
        {
            object.getBoolean("somekey");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"somekey\"] is not a Boolean.");
        }
    }

    @Test
    public void accumulate_simple_values()
    {
        JSONObject object = new JSONObject();

        String key = "key";

        object.accumulate(key, "alpha");
        object.accumulate(key, "beta");
        object.accumulate(key, "gamma");

        assertEquals(object.toString(), "{\"key\":[\"alpha\",\"beta\",\"gamma\"]}");

        JSONArray array = object.getJSONArray(key);

        assertEquals(array.length(), 3);
    }

    @Test
    public void accumulate_with_initial_array()
    {
        JSONArray array = new JSONArray();

        array.put("alpha");

        String key = "key";

        JSONObject object = new JSONObject();

        object.accumulate(key, array);
        object.accumulate(key, "beta");

        array.put("gamma");

        assertEquals(object.toString(), "{\"key\":[\"alpha\",\"beta\",\"gamma\"]}");
    }

    @Test
    public void object_from_string()
    {
        JSONObject object = new JSONObject("{ fred: \"flintstone\", caveman: true, friends: [\"barney\"] }");

        assertEquals(object.get("fred"), "flintstone");
        assertEquals(object.getBoolean("caveman"), true);

        JSONArray array = object.getJSONArray("friends");

        assertEquals(array.length(), 1);
        assertEquals(array.get(0), "barney");
    }

    @Test
    public void append()
    {
        JSONObject object = new JSONObject();
        String key = "fubar";

        object.append(key, "alpha");

        assertEquals(object.toString(), "{\"fubar\":[\"alpha\"]}");

        object.append(key, "beta");

        assertEquals(object.toString(), "{\"fubar\":[\"alpha\",\"beta\"]}");
    }

    @Test
    public void append_existng_key_not_an_array()
    {
        JSONObject object = new JSONObject();
        String key = "fubar";

        object.put(key, "existing");

        try
        {
            object.append(key, "additional");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"fubar\"] is not a JSONArray.");
        }
    }

    @Test(dataProvider = "double_to_string_data")
    public void double_to_string(double input, String expected)
    {
        String actual = JSONObject.doubleToString(input);

        assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] double_to_string_data()
    {
        return new Object[][] { { 3d, "3" }, { -22.5d, "-22.5" }, { 0d, "0" }, { Double.NEGATIVE_INFINITY, "null" },
                { Double.POSITIVE_INFINITY, "null" }, { Double.NaN, "null" }, };
    }

    @Test(dataProvider = "get_double_data")
    public void get_double(Object value, double expected)
    {
        JSONObject object = new JSONObject();

        object.put("key", value);

        assertEquals(object.getDouble("key"), expected);
    }

    @DataProvider
    public Object[][] get_double_data()
    {
        return new Object[][] { { new Double(3.5), 3.5d }, { new Long(1000), 1000d }, { "-101.7", -101.7d } };
    }

    @Test
    public void get_double_not_a_string()
    {
        JSONObject object = new JSONObject();

        object.put("notstring", Boolean.FALSE);

        try
        {
            object.getDouble("notstring");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"notstring\"] is not a number.");
        }
    }

    @Test
    public void get_double_string_invalid_format()
    {
        JSONObject object = new JSONObject();

        object.put("badstring", "google");

        try
        {
            object.getDouble("badstring");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"badstring\"] is not a number.");
        }
    }

    @Test(dataProvider = "get_int_inputs")
    public void get_int(Object value, int expected)
    {
        JSONObject object = new JSONObject();

        object.put("intkey", value);

        assertEquals(object.getInt("intkey"), expected);
    }

    @DataProvider
    public Object[][] get_int_inputs()
    {
        return new Object[][] { { "3", 3 }, { new Long(97), 97 }, { "-8.76", -8 } };
    }

    @Test
    public void has()
    {
        JSONObject object = new JSONObject();

        object.put("fred", "flintstone");

        assertTrue(object.has("fred"));
        assertFalse(object.has("barney"));
    }

    @Test
    public void get_json_array()
    {
        JSONArray array = new JSONArray();

        JSONObject object = new JSONObject();

        object.put("arraykey", array);
        object.put("boolkey", true);

        assertSame(object.getJSONArray("arraykey"), array);

        try
        {
            object.getJSONArray("boolkey");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"boolkey\"] is not a JSONArray.");
        }

    }

    @Test
    public void get_json_object()
    {
        JSONObject child = new JSONObject();
        JSONObject root = new JSONObject();

        root.put("child", child);
        root.put("boolkey", false);

        assertSame(root.getJSONObject("child"), child);

        try
        {
            root.getJSONObject("boolkey");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONObject[\"boolkey\"] is not a JSONObject.");
        }
    }

    @Test
    public void length()
    {
        JSONObject object = new JSONObject();

        assertEquals(object.length(), 0);

        object.put("key", "fred");

        assertEquals(object.length(), 1);

        object.accumulate("key", "barney");
        assertEquals(object.length(), 1);

        object.put("key2", "wilma");

        assertEquals(object.length(), 2);
    }

    @Test
    public void names_is_null_if_no_properties()
    {
        JSONObject object = new JSONObject();

        assertNull(object.names());
    }

    @Test
    public void names()
    {
        JSONObject object = new JSONObject();

        object.put("fred", "flintstone");
        object.put("barney", "rubble");

        JSONArray array = object.names();

        assertEquals(array.length(), 2);

        Object[] names = array.toArray();

        Arrays.sort(names);

        assertEquals(names, new String[] { "barney", "fred" });

    }

    @Test
    public void parse_null()
    {
        JSONObject object = new JSONObject("{ \"nullkey\": null }");

        assertSame(object.get("nullkey"), JSONObject.NULL);
    }

    @Test
    public void emit_null()
    {
        JSONObject object = new JSONObject();

        object.put("nullkey", JSONObject.NULL);

        assertEquals(object.toString(), "{\"nullkey\":null}");

        assertTrue(object.isNull("nullkey"));
    }

    @Test
    public void null_matches_java_null()
    {
        assertTrue(JSONObject.NULL.equals(null));
    }

    @DataProvider
    public final Object[][] bad_parse_data()
    {
        return new Object[][]
                {
                        { "{  ", "A JSONObject text must end with '}' at character 3 of {" },
                        { "fred", "A JSONObject text must begin with '{' at character 1 of fred" },
                        { "{ \"akey\" }", "Expected a ':' after a key at character 10 of { \"akey\" }" },
                        { "{ \"fred\" : 1 \"barney\" }",
                                "Expected a ',' or '}' at character 14 of { \"fred\" : 1 \"barney\" }" },
                        { "{ \"list\" : [1, 2", "Expected a ',' or ']' at character 16 of { \"list\" : [1, 2" }
                };

    }

    @Test(dataProvider = "bad_parse_data")
    public void jsonobject_parse_errors(String input, String expectedMessage)
    {
        try
        {
            new JSONObject(input);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage().trim(), expectedMessage);
        }
    }

    @Test
    public void alternate_key_value_seperators()
    {
        JSONObject object = new JSONObject("{ \"fred\" = 1; \"barney\" => 2 }");

        assertEquals(object.getInt("fred"), 1);
        assertEquals(object.getInt("barney"), 2);
    }

    @Test
    public void get_long_from_number()
    {
        JSONObject object = new JSONObject("{ \"key\": 92 }");

        assertEquals(object.getLong("key"), 92l);
    }

    @Test
    public void get_long_from_string()
    {
        JSONObject object = new JSONObject("{ \"key\": \"-200\" }");

        assertEquals(object.getLong("key"), -200l);
    }

    @Test
    public void get_string_from_number()
    {
        JSONObject object = new JSONObject("{ \"key\": 92 }");

        assertEquals(object.getString("key"), "92");
    }

    @Test
    public void number_to_string_conversion()
    {
        JSONObject object = new JSONObject();

        object.put("key", new BigDecimal("100.0000000"));

        assertEquals(object.toString(), "{\"key\":100}");
    }

    @Test
    public void parse_empty_object()
    {
        assertEquals(new JSONObject("{}").length(), 0);
    }

    @Test
    public void put_null_is_remove()
    {
        JSONObject object = new JSONObject();

        object.put("key", "value");

        object.put("key", null);

        assertEquals(object.toString(), "{}");

        assertTrue(object.keys().isEmpty());
    }

    @Test
    public void quote_null_is_empty_string()
    {
        assertEquals(JSONObject.quote(null), "\"\"");
    }

    @Test
    public void quote_empty_string_is_empty_string()
    {
        assertEquals(JSONObject.quote(""), "\"\"");
    }

    @Test
    public void character_escapes_in_quote()
    {
        assertEquals(JSONObject.quote("\"/\b\t\n\f\r\u2001/a</"), "\"\\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/\"");
    }

    @DataProvider
    public Object[][] non_finite_data()
    {
        return new Object[][]
                {
                        { Double.NaN },
                        { Double.NEGATIVE_INFINITY },
                        { Float.NEGATIVE_INFINITY },
                        { Float.NaN }
                };
    }

    @Test(dataProvider = "non_finite_data")
    public void non_finite_numbers_not_allowed(Number nonfinite)
    {
        JSONObject object = new JSONObject();

        try
        {
            object.put("nonfinite", nonfinite);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSON does not allow non-finite numbers.");
        }
    }

    @Test
    public void invalid_object_added()
    {
        try
        {
            JSONObject.testValidity(new HashMap());
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "JSONObject properties may be one of Boolean, Number, String, org.apache.tapestry5.json.JSONArray, org.apache.tapestry5.json.JSONLiteral, org.apache.tapestry5.json.JSONObject, org.apache.tapestry5.json.JSONObject$Null, org.apache.tapestry5.json.JSONString. Type java.util.HashMap is not allowed.");
        }
    }

    @Test
    public void output_json_string()
    {
        JSONString string = new JSONString()
        {
            public String toJSONString()
            {
                return "*VALUE*";
            }
        };

        JSONObject object = new JSONObject();

        object.put("key", string);

        assertEquals(object.toString(), "{\"key\":\"*VALUE*\"}");
    }

    @Test
    public void equals_implementation()
    {
        String text = "{ \"key\" : 99 }";

        JSONObject obj1 = new JSONObject(text);
        JSONObject obj2 = new JSONObject(text);

        assertTrue(obj1.equals(obj1));
        assertTrue(obj1.equals(obj2));
        assertFalse(obj1.equals(null));
        assertFalse(obj1.equals(text));

        obj2.put("anotherkey", "something");

        assertFalse(obj1.equals(obj2));
    }

    @Test
    public void parse_escaped()
    {
        JSONObject object = new JSONObject("{ \"key\" : \"\\\"/\\b\\t\\n\\f\\r\\u2001/a<\\/\\x20\" }");

        assertEquals(object.get("key"), "\"/\b\t\n\f\r\u2001/a</ ");
    }

    @Test
    public void parse_nested_object()
    {
        JSONObject object = new JSONObject("{ \"key\" : { \"name\" : \"inner\" }}");

        JSONObject inner = object.getJSONObject("key");
        assertEquals(inner.getString("name"), "inner");
    }

    @Test
    public void parse_true_and_false()
    {
        JSONObject object = new JSONObject("{ \"t\" : true, \"f\" : false }");

        assertEquals(object.getBoolean("t"), true);
        assertEquals(object.getBoolean("f"), false);
    }

    @Test
    public void parse_number_forms()
    {
        JSONObject object = new JSONObject("{ \"hex\" : 0x50, \"oct\" : 030, \"posInt\" : +50, " +
                " \"negInt\" : -50, \"long\" : 4294968530, \"float\": -32.7 }");

        assertEquals(object.getInt("hex"), 80);
        assertEquals(object.getInt("oct"), 24);
        assertEquals(object.getInt("posInt"), 50);
        assertEquals(object.getInt("negInt"), -50);
        assertEquals(object.getLong("long"), 4294968530l);
        assertEquals(object.getDouble("float"), -32.7d);
    }

    @Test
    public void json_array_from_values()
    {
        assertEquals(new JSONArray("fred", "barney", "wilma").toString(), "[\"fred\",\"barney\",\"wilma\"]");
    }

    @Test
    public void array_must_start_with_bracket()
    {
        try
        {
            new JSONArray("1, 2, 3]");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "A JSONArray text must start with '[' at character 1 of 1, 2, 3]");
        }
    }

    @Test
    public void parse_empty_array()
    {
        assertEquals(new JSONArray("[]").length(), 0);
    }

    @Test
    public void empty_element_is_null()
    {
        JSONArray array = new JSONArray("[1,,3]");

        assertEquals(array.getInt(0), 1);
        assertTrue(array.isNull(1));
        assertEquals(array.getInt(2), 3);
    }

    @Test
    public void comma_at_end_of_list_is_ignored()
    {
        JSONArray array = new JSONArray("[1,2,]");

        assertEquals(array.length(), 2);
        assertEquals(array.getInt(0), 1);
        assertEquals(array.getInt(1), 2);
    }

    @Test
    public void not_a_boolean_at_index()
    {
        JSONArray array = new JSONArray("[alpha]");

        try
        {
            array.getBoolean(0);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONArray[0] is not a Boolean.");
        }
    }

    @Test
    public void boolean_values()
    {
        JSONArray array = new JSONArray(true, false, "True", "False");

        assertTrue(array.getBoolean(0));
        assertTrue(array.getBoolean(2));

        assertFalse(array.getBoolean(1));
        assertFalse(array.getBoolean(3));
    }


    @Test
    public void not_a_double_at_index()
    {
        JSONArray array = new JSONArray(true);

        try
        {
            array.getDouble(0);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONArray[0] is not a number.");
        }
    }

    @Test
    public void get_double_from_array()
    {
        JSONArray array = new JSONArray(400l, "95.5");

        assertEquals(array.getDouble(0), 400.d);
        assertEquals(array.getDouble(1), 95.5d);
    }

    @Test
    public void get_long_from_array()
    {
        JSONArray array = new JSONArray(400l, "95.5");

        assertEquals(array.getLong(0), 400l);
        assertEquals(array.getLong(1), 95l);
    }

    @Test
    public void not_a_nested_array_at_index()
    {
        JSONArray array = new JSONArray("fred", "barney");

        try
        {
            array.getJSONArray(1);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONArray[1] is not a JSONArray.");
        }
    }

    @Test
    public void get_nested_array()
    {
        JSONArray nested = new JSONArray();
        JSONArray outer = new JSONArray(nested);

        assertSame(outer.getJSONArray(0), nested);
    }

    @Test
    public void not_a_json_object_at_index()
    {
        JSONArray array = new JSONArray("fred", "barney", "wilma");

        try
        {
            array.getJSONObject(1);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONArray[1] is not a JSONObject.");
        }
    }

    @Test
    public void get_json_object_at_index()
    {
        JSONObject inner = new JSONObject();

        JSONArray array = new JSONArray("fred", true, inner);

        assertSame(array.getJSONObject(2), inner);
    }

    @Test
    public void put_at_negative_index_is_invalid()
    {
        JSONArray array = new JSONArray();

        try
        {
            array.put(-1, "fred");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "JSONArray[-1] not found.");
        }
    }

    @Test
    public void put_overrides_existing_value_in_array()
    {
        JSONArray array = new JSONArray("fred", "barney", "wilma");

        array.put(2, "betty");

        assertEquals(array.getString(2), "betty");
    }

    @Test
    public void put_pads_short_array_with_nulls()
    {
        JSONArray array = new JSONArray("fred", "barney", "wilma");

        array.put(4, "bambam");

        assertTrue(array.isNull(3));
        assertEquals(array.getString(4), "bambam");
    }

    @Test
    public void array_equality()
    {
        JSONArray array1 = new JSONArray(1, 2, 3);
        JSONArray array2 = new JSONArray(1, 2, 3);

        assertTrue(array1.equals(array1));
        assertFalse(array1.equals(null));
        assertFalse(array1.equals(this));

        assertTrue(array1.equals(array2));

        array2.put(9, "stuff");

        assertFalse(array1.equals(array2));
    }

    @Test
    public void null_to_string()
    {
        assertEquals(JSONObject.NULL.toString(), "null");
    }

    @Test
    public void json_literal()
    {
        JSONObject obj = new JSONObject();

        obj.put("callback", new JSONLiteral("function(x) { $('bar').show(); }"));

        assertEquals(obj.toString(), "{\"callback\":function(x) { $('bar').show(); }}");
    }
}
