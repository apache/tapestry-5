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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;

import java.util.Map;

/**
 * Support code for generating code (used when transforming component classes).
 */
public final class TransformUtils
{
    private static final Map<String, PrimitiveTypeInfo> nameToInfo = newMap();

    private static final Map<Class, PrimitiveTypeInfo> classToInfo = newMap();

    static class PrimitiveTypeInfo
    {
        private final Class wrapperType;

        private final String unwrapperMethodName;

        private final String defaultValue;

        public PrimitiveTypeInfo(Class wrapperType, String unwrapperMethodName, String defaultValue)
        {
            this.wrapperType = wrapperType;
            this.unwrapperMethodName = unwrapperMethodName;
            this.defaultValue = defaultValue;
        }
    }

    static
    {
        add(boolean.class, Boolean.class, "booleanValue", "false");
        add(byte.class, Byte.class, "byteValue", "0");
        add(char.class, Character.class, "charValue", "0");
        add(short.class, Short.class, "shortValue", "0");
        add(int.class, Integer.class, "intValue", "0");
        add(long.class, Long.class, "longValue", "0L");
        add(float.class, Float.class, "floatValue", "0.0f");
        add(double.class, Double.class, "doubleValue", "0.0d");
    }

    private TransformUtils()
    {
    }

    private static void add(Class primitiveType, Class wrapperType, String unwrapperMethodName, String defaultValue)
    {
        PrimitiveTypeInfo info = new PrimitiveTypeInfo(wrapperType, unwrapperMethodName, defaultValue);

        classToInfo.put(primitiveType, info);
        nameToInfo.put(primitiveType.getName(), info);
    }

    /**
     * Returns true if the specified type is a primitive type.
     */
    public static boolean isPrimitive(String type)
    {
        return nameToInfo.containsKey(type);
    }

    /**
     * Returns the name of wrapper type for a given input type. For primitive types, returns the wrapper type. For other
     * types, returns the input type name.
     *
     * @param type primitive type name, or fully qualified class name
     */
    public static String getWrapperTypeName(String type)
    {
        PrimitiveTypeInfo info = nameToInfo.get(type);

        return info == null ? type : info.wrapperType.getName();
    }

    /**
     * For primitive types, returns the method on the <em>wrapper type</em> that converts back to the primitive.
     *
     * @param type the primitive type
     * @return the method of the corresponding wrapper type, or null if type is not a primitive type
     */
    public static String getUnwrapperMethodName(String type)
    {
        PrimitiveTypeInfo info = nameToInfo.get(type);

        return info == null ? null : info.unwrapperMethodName;
    }

    /**
     * Returns the wrapper type for a given input type. For primitive types, returns the wrapper type. For other types,
     * returns the type itself.
     *
     * @param type primitive or object type
     */
    public static Class getWrapperType(Class type)
    {
        PrimitiveTypeInfo info = classToInfo.get(type);

        return info == null ? type : info.wrapperType;
    }

    /**
     * Returns the default value for a type. This is the string "null" for most types, or a literal value for primtive
     * types.
     */
    public static String getDefaultValue(String type)
    {
        PrimitiveTypeInfo info = nameToInfo.get(type);

        return info == null ? "null" : info.defaultValue;
    }
}
