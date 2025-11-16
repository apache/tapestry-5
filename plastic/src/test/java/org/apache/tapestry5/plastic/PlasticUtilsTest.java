// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.plastic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tapestry5.plastic.test.PlasticUtilsTestObject;
import org.apache.tapestry5.plastic.test.PlasticUtilsTestObjectSuperclass;
import org.apache.tapestry5.plastic.test_.Enumeration;
import org.junit.jupiter.api.Test;

// [Thiago] This is only here because I couldn't get Groovy tests to run on Eclipse
// (and I admit to not liking Groovy anyway . . .).
public class PlasticUtilsTest 
{
    
    public static void main(String[] args) throws Exception {
//        ASMifier.main(new String[] {PlasticUtilsTestObject.class.getName(), "-nodebug"});
        final PlasticUtilsTest plasticUtilsTest = new PlasticUtilsTest();
        plasticUtilsTest.implement_field_value_provider();
        plasticUtilsTest.implement_property_value_provider();
    }
    
    @Test
    public void implement_field_value_provider() throws ClassNotFoundException
    {
        
        Set<String> packages = new HashSet<>();
        packages.add(PlasticUtilsTestObject.class.getPackage().getName());

        PlasticManager plasticManager = PlasticManager.withContextClassLoader()
                .packages(packages).create();
        
        Set<PlasticUtils.FieldInfo> fieldInfos = new HashSet<PlasticUtils.FieldInfo>();

        PlasticClassTransformation<Object> transformation2 = plasticManager.getPlasticClass(PlasticUtilsTestObjectSuperclass.class.getName());
        PlasticClass pc2 = transformation2.getPlasticClass();
        fieldInfos.clear();
        fieldInfos.add(new PlasticUtils.FieldInfo("superString", "java.lang.String"));
        PlasticUtils.implementFieldValueProvider(pc2, fieldInfos);

        PlasticClassTransformation<Object> transformation = plasticManager.getPlasticClass(PlasticUtilsTestObject.class.getName());
        PlasticClass pc = transformation.getPlasticClass();
        for (PlasticField field : pc.getAllFields()) {
            fieldInfos.add(PlasticUtils.toFieldInfo(field));
        }
        PlasticUtils.implementFieldValueProvider(pc, fieldInfos);

        
        Object object = transformation.createInstantiator().newInstance();
        
        Class<?> original = PlasticUtilsTestObject.class;
        Class<?> transformed = object.getClass();
        
        assertNotEquals(original, transformed);
        
        assertEquals(PlasticUtilsTestObject.STRING, FieldValueProvider.get(object, "string"));
        assertEquals(PlasticUtilsTestObject.OTHER_STRING, FieldValueProvider.get(object, "otherString"));
        assertEquals(null, FieldValueProvider.get(object, "nullString"));
        assertEquals(PlasticUtilsTestObject.ENUMERATION.toString(), FieldValueProvider.get(object, "enumeration").toString());
        assertTrue(Arrays.equals(PlasticUtilsTestObject.INT_ARRAY, (int[]) FieldValueProvider.get(object, "intArray")));
        assertEquals(PlasticUtilsTestObject.TRUE_OF_FALSE, (Boolean) FieldValueProvider.get(object, "trueOrFalse"));
//        assertEquals(PlasticUtilsTestObjectSuperclass.SUPER, FieldValueProvider.get(object, "superString"));
    }
    
    @Test
    public void implement_property_value_provider() throws ClassNotFoundException
    {
        
        Set<String> packages = new HashSet<>();
        packages.add(PlasticUtilsTestObject.class.getPackage().getName());
        PlasticManager plasticManager = PlasticManager.withContextClassLoader()
                .packages(packages).create();
        final PlasticClassTransformation<Object> transformation = plasticManager.getPlasticClass(PlasticUtilsTestObject.class.getName());
        PlasticClass pc = transformation.getPlasticClass();
        Set<PlasticUtils.FieldInfo> fieldInfos = new HashSet<PlasticUtils.FieldInfo>();
        for (PlasticField field : pc.getAllFields()) {
            fieldInfos.add(PlasticUtils.toFieldInfo(field));
        }
        fieldInfos.add(new PlasticUtils.FieldInfo("superString", "java.lang.String"));
        PlasticUtils.implementPropertyValueProvider(pc, fieldInfos);
        Object object = transformation.createInstantiator().newInstance();
        
        Class<?> original = PlasticUtilsTestObject.class;
        Class<?> transformed = object.getClass();
        
        assertNotEquals(original, transformed);
        
        assertEquals(PlasticUtilsTestObject.STRING, PropertyValueProvider.get(object, "string"));
        assertEquals(PlasticUtilsTestObject.OTHER_STRING, PropertyValueProvider.get(object, "otherString"));
        assertEquals(null, PropertyValueProvider.get(object, "nullString"));
        assertEquals(PlasticUtilsTestObject.ENUMERATION.toString(), PropertyValueProvider.get(object, "enumeration").toString());
        assertTrue(Arrays.equals(PlasticUtilsTestObject.INT_ARRAY, (int[]) PropertyValueProvider.get(object, "intArray")));
        assertEquals(PlasticUtilsTestObject.TRUE_OF_FALSE, (Boolean) PropertyValueProvider.get(object, "trueOrFalse"));
        assertEquals(PlasticUtilsTestObjectSuperclass.SUPER, PropertyValueProvider.get(object, "superString"));
        
        final String newStringValue = "something else";
        final String newOtherStringValue = "what?";
        final String newNullStringValue = "not null anymore";
        final Enumeration newEnumerationValue = Enumeration.TRUE;
        final int[] newIntArrayValue = new int[] { 3, 1, 4 };
        final boolean newTrueOfFalseValue = !PlasticUtilsTestObject.TRUE_OF_FALSE;
        final String newSuperStringValue = "Batman";
        
        PropertyValueProvider.set(object, "string", newStringValue);
        PropertyValueProvider.set(object, "otherString", newOtherStringValue);
        PropertyValueProvider.set(object, "nullString", newNullStringValue);
        PropertyValueProvider.set(object, "enumeration", newEnumerationValue);
        PropertyValueProvider.set(object, "intArray", newIntArrayValue);
        PropertyValueProvider.set(object, "trueOrFalse", newTrueOfFalseValue);
        PropertyValueProvider.set(object, "superString", newSuperStringValue);
        
        assertEquals(newStringValue, PropertyValueProvider.get(object, "string"));
        assertEquals(newOtherStringValue, PropertyValueProvider.get(object, "otherString"));
        assertEquals(newNullStringValue, PropertyValueProvider.get(object, "nullString"));
        assertEquals(newEnumerationValue.toString(), PropertyValueProvider.get(object, "enumeration").toString());
        assertTrue(Arrays.equals(newIntArrayValue, (int[]) PropertyValueProvider.get(object, "intArray")));
        assertEquals(newTrueOfFalseValue, (Boolean) PropertyValueProvider.get(object, "trueOrFalse"));
        assertEquals(newSuperStringValue, PropertyValueProvider.get(object, "superString"));
        
    }
    
}
