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
package org.apache.tapestry5.integration.app1.pages.rest;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.integration.app1.data.rest.entities.Point;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

/**
 * REST endpoint class just to test the parameter and return type descriptions.
 */
@RestInfo(produces = "application/json")
public class RestTypeDescriptionsDemo {

    private static final String TEXT_PLAIN = "text/plain";

    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(consumes = "application/json", produces = "application/json")
    Object point(@StaticActivationContextValue("point") String ignored, Point p1, @RequestBody Point p2) {
        return null;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(returnType = JSONArray.class, consumes = "application/json", produces = "application/json")    
    Object jsonArray(
            @StaticActivationContextValue("jsonArray") String ignored, 
            JSONArray jsonArray1,
            @RequestBody(allowEmpty = false) JSONArray jsonArray2) {
        return null;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(returnType = JSONObject.class, consumes = "application/json", produces = "application/json")    
    Object jsonObject(@StaticActivationContextValue("jsonObject") String ignored, JSONObject jsonObject, @RequestBody JSONObject jsonObject2) {
        return null;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = boolean.class)
    Object booleanMethod(@StaticActivationContextValue("boolean") String ignored, boolean b1, Boolean b2, @RequestBody boolean b3) {
        return true;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, consumes = TEXT_PLAIN)
    float floatMethod(@StaticActivationContextValue("float") String ignored, float b1, Float b2, @RequestBody float b3) {
        return 0.1f;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, consumes = TEXT_PLAIN)
    double doubleMethod(@StaticActivationContextValue("double") String ignored, double b1, Double b2, @RequestBody double b3) {
        return 0.2;
    }

    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = byte.class, consumes = TEXT_PLAIN)
    byte byteMethod(@StaticActivationContextValue("byte") String ignored, byte b1, Byte b2, @RequestBody byte b3) {
        return 0;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = short.class, consumes = TEXT_PLAIN)
    short shortMethod(@StaticActivationContextValue("short") String ignored, short b1, short b2, @RequestBody short b3) {
        return 0;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = int.class, consumes = TEXT_PLAIN)
    int intMethod(@StaticActivationContextValue("int") String ignored, int b1, Integer b2, @RequestBody int b3) {
        return 0;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = long.class, consumes = TEXT_PLAIN)
    long longMethod(@StaticActivationContextValue("long") String ignored, long b1, long b2, @RequestBody long b3) {
        return 0;
    }

    @OnEvent(EventConstants.HTTP_GET)
    @RestInfo(produces = TEXT_PLAIN, returnType = String.class, consumes = TEXT_PLAIN)
    String string(@StaticActivationContextValue("string") String ignored, String b1, @RequestBody String b2) {
        return "Ok";
    }

}
