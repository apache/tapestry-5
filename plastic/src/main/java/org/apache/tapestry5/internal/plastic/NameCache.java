// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticUtils;

/**
 * A cache of translations from type names to internal names and descriptors, as well as a cache from MethodDescription
 * to method descriptor.
 */
@SuppressWarnings("rawtypes")
public class NameCache
{
    private final Cache<String, String> class2internal = new Cache<String, String>()
    {

        @Override
        protected String convert(String className)
        {
            return PlasticInternalUtils.toInternalName(className);
        }
    };

    private final Cache<Class, String> type2internal = new Cache<Class, String>()
    {
        @Override
        protected String convert(Class input)
        {
            return toInternalName(input.getName());
        }
    };

    private final Cache<MethodDescription, String> md2desc = new Cache<MethodDescription, String>()
    {
        @Override
        protected String convert(MethodDescription methodDescription)
        {
            return toMethodDescriptor(methodDescription.returnType, methodDescription.argumentTypes);
        }
    };

    private final Cache<String, String> type2desc = new Cache<String, String>()
    {
        @Override
        protected String convert(String typeName)
        {
            return PlasticInternalUtils.toDescriptor(typeName);
        }
    };

    private final Cache<Class, String> typeToTypeName = new Cache<Class, String>()
    {
        @Override
        protected String convert(Class type)
        {
            return PlasticUtils.toTypeName(type);
        }
    };

    public String toInternalName(String className)
    {
        return class2internal.get(className);
    }

    public String toInternalName(Class type)
    {
        return type2internal.get(type);
    }

    public String toDesc(MethodDescription md)
    {
        return md2desc.get(md);
    }

    public String toDesc(String typeName)
    {
        return type2desc.get(typeName);
    }

    public String toTypeName(Class type)
    {
        return typeToTypeName.get(type);
    }

    public String[] toTypeNames(Class... types)
    {
        String[] result = new String[types.length];

        for (int i = 0; i < result.length; i++)
            result[i] = toTypeName(types[i]);

        return result;
    }

    public String toMethodDescriptor(Class returnType, Class... argumentTypes)
    {
        return toMethodDescriptor(toTypeName(returnType), toTypeNames(argumentTypes));
    }

    public String toMethodDescriptor(String returnType, String... argumentTypes)
    {
        StringBuilder builder = new StringBuilder("(");

        for (String argumentType : argumentTypes)
        {
            builder.append(toDesc(argumentType));
        }

        builder.append(')').append(toDesc(returnType));

        return builder.toString();
    }
}
