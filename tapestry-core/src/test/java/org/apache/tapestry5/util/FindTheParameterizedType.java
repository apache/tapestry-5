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

package org.apache.tapestry5.util;

import org.apache.tapestry5.ioc.internal.util.Orderer;

import static java.lang.System.out;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class FindTheParameterizedType
{

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        Method m = FindTheParameterizedType.class.getMethod("method", Map.class, List.class);

        out.println(m.toString());
        out.println(m.toGenericString());

        Type[] types = m.getGenericParameterTypes();
        ParameterizedType pt = (ParameterizedType) types[0];

        Type keyType = pt.getActualTypeArguments()[0];
        Type valueType = pt.getActualTypeArguments()[1];

        out.printf("   key type: %s\n", rawType(keyType));
        out.printf(" value type: %s\n", rawType(valueType));

        Type listType = types[1];

        out.printf("  list type: %s\n", rawType(listType));
    }

    private static Class rawType(Type type)
    {
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;

            return rawType(pt.getRawType());
        }

        if (type instanceof Class)
        {
            return (Class) type;
        }

        throw new IllegalArgumentException();
    }

    public void method(Map<String, Orderer<Runnable>> configuration, List list)
    {

    }

}
