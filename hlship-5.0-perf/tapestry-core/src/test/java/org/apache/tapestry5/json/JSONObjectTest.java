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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

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

    @DataProvider(name = "boolean_inputs")
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

    @Test(dataProvider = "double_inputs")
    public void double_to_string(double input, String expected)
    {
        String actual = JSONObject.doubleToString(input);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "double_inputs")
    public Object[][] double_inputs()
    {
        return new Object[][] { { 3d, "3" }, { -22.5d, "-22.5" }, { 0d, "0" }, { Double.NEGATIVE_INFINITY, "null" },
                { Double.POSITIVE_INFINITY, "null" }, { Double.NaN, "null" }, };
    }

    @Test(dataProvider = "get_double_inputs")
    public void get_double(Object value, double expected)
    {
        JSONObject object = new JSONObject();

        object.put("key", value);

        assertEquals(object.getDouble("key"), expected);
    }

    @DataProvider(name = "get_double_inputs")
    public Object[][] getDoubleInputs()
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

    @DataProvider(name = "get_int_inputs")
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
}
