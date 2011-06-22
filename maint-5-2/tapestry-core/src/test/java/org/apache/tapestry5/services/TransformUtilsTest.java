// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import static org.apache.tapestry5.services.TransformUtils.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TransformUtilsTest extends Assert
{
    @Test
    public void wrapper_type_by_name()
    {
        assertEquals(getWrapperTypeName("char"), "java.lang.Character");
        assertEquals(getWrapperTypeName("java.util.Map"), "java.util.Map");
    }

    @Test
    public void wrapper_type_by_class()
    {
        assertEquals(getWrapperType(char.class), Character.class);
        assertEquals(getWrapperType(Map.class), Map.class);
    }

    @Test
    public void default_value()
    {
        assertEquals(getDefaultValue("long"), "0L");
        assertEquals(getDefaultValue("java.util.Map"), "null");
    }

    @Test
    public void is_primitive()
    {
        assertTrue(isPrimitive("int"));
        assertFalse(isPrimitive("java.lang.Integer"));
    }

    @Test
    public void unwrapper_method_name()
    {
        assertEquals(getUnwrapperMethodName("boolean"), "booleanValue");
        assertEquals(getUnwrapperMethodName("int"), "intValue");
        assertNull(getUnwrapperMethodName("java.lang.Integer"));
    }
}
