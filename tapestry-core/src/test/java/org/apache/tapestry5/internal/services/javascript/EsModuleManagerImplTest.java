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

package org.apache.tapestry5.internal.services.javascript;

import static org.testng.Assert.assertEquals;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.testng.annotations.Test;

public class EsModuleManagerImplTest 
{
    private static final String STRING = "asdfasdfasdfadsf";

    private static final JSONLiteral JSON_LITERAL = new JSONLiteral("literally");
    private static final JSONArray JSON_ARRAY = new JSONArray("1", "true");
    private static JSONObject JSON_OBJECT = new JSONObject("something", "else", "array",
            JSON_ARRAY, "literal", JSON_LITERAL);
    private static Number NUMBER = Math.PI * Math.E;

    @Test
    public void test_null_arguments()
    {
        assertEquals(convert(JSONObject.NULL, true), "null");
        assertEquals(convert(JSONObject.NULL, false), "null");
    }
    
    @Test
    public void test_empty_arguments()
    {
        assertEquals(convert(new JSONArray(), true), "");
        assertEquals(convert(new JSONArray(), false), "");
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void test_one_argument()
    {
        assertEquals(convert(new JSONArray(JSONObject.NULL), false), "null");
        
        assertEquals(convert(new JSONArray().put(STRING), false), quote(STRING));
        
        assertEquals(convert(new JSONArray(NUMBER), false), NUMBER.toString());
        assertEquals(convert(new JSONArray(Boolean.TRUE), false), Boolean.TRUE.toString());
        assertEquals(convert(new JSONArray(Boolean.FALSE), false), Boolean.FALSE.toString());

        assertEquals(convert(new JSONArray(JSON_LITERAL), false), quote(JSON_LITERAL.toString()));

        assertEquals(convert(new JSONArray(JSON_ARRAY), false), JSON_ARRAY.toString(false));
        assertEquals(convert(new JSONArray(JSON_ARRAY), true), JSON_ARRAY.toString(true));

        assertEquals(convert(new JSONArray(JSON_OBJECT), false), JSON_OBJECT.toString(false));
        assertEquals(convert(new JSONArray(JSON_OBJECT), true), JSON_OBJECT.toString(true));
        
    }
    
    @Test
    public void test_multiple_arguments()
    {
        JSONArray arguments = new JSONArray(JSONObject.NULL, STRING, JSON_LITERAL, JSON_ARRAY, JSON_OBJECT);
        final String format = "null, '%s', '%s', %s, %s";
        
        assertEquals(convert(arguments, false), 
                String.format(format, STRING, JSON_LITERAL, 
                        JSON_ARRAY.toString(false), JSON_OBJECT.toString(false)));
        
        assertEquals(convert(arguments, true), 
                String.format(format, STRING, JSON_LITERAL, 
                        JSON_ARRAY.toString(true), JSON_OBJECT.toString(true)));

    }
    
    private String quote(String string)
    {
        return "'" + string + "'";
    }
    
    private String convert(Object object, boolean compactJSON) 
    {
        return convert(new JSONArray(object), compactJSON);
    }

    private String convert(JSONArray array, boolean compactJSON) 
    {
        return EsModuleManagerImpl.convertToJsFunctionParameters(array, compactJSON);
    }
    
}
