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
import org.junit.jupiter.api.Test;

// [Thiago] This is only here because I couldn't get Groovy tests to run on Eclipse
// (and I admit to not liking Groovy anyway . . .).
public class PlasticUtilsTest 
{
    
    public static void main(String[] args) throws ClassNotFoundException {
        final PlasticUtilsTest plasticUtilsTest = new PlasticUtilsTest();
        plasticUtilsTest.implement_field_value_pProvider();
        plasticUtilsTest.implement_property_value_provider();
    }
    
    @Test
    public void implement_field_value_pProvider() throws ClassNotFoundException
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
        
    }
    
}
