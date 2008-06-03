// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class GenericUtilsTest extends Assert
{
    protected Method find(Class clazz, String name)
    {
        for (Method m : clazz.getMethods())
        {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }

        throw new IllegalArgumentException(
                String.format("Could not locate a public method named '%s' in %s.", name, clazz));

    }

    @Test
    public void generic_return_type_of_non_generic_type()
    {
        Method m = find(NonGenericBean.class, "getvalue");

        assertSame(GenericsUtils.extractGenericReturnType(NonGenericBean.class, m), String.class);
    }

    @Test
    public void generic_return_type_of_parameterized_bean()
    {
        Method m = find(StringBean.class, "getvalue");

        assertSame(GenericsUtils.extractGenericReturnType(StringBean.class, m), String.class);
    }

    @Test
    public void generic_bean_with_multiple_parameters()
    {
        Method getKey = find(StringLongPair.class, "getkey");
        Method getValue = find(StringLongPair.class, "getvalue");

        assertSame(GenericsUtils.extractGenericReturnType(StringLongPair.class, getKey), String.class);
        assertSame(GenericsUtils.extractGenericReturnType(StringLongPair.class, getValue), Long.class);
    }
}
