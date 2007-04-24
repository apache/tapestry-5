// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Handy method useful when creating new classes using
 * {@link org.apache.tapestry.ioc.services.ClassFab}.
 */
public class ClassFabUtils
{
    private static long _uid = System.currentTimeMillis();

    private ClassFabUtils()
    {
    }

    /**
     * Generates a unique class name, which will be in the default package.
     */

    public static synchronized String generateClassName(String baseName)
    {
        return "$" + baseName + "_" + Long.toHexString(_uid++);
    }

    /**
     * Returns a class name derived from the provided interfaceClass. The package part of the
     * interface name is stripped out, and the result passed to {@link #generateClassName(String)}.
     */

    public static String generateClassName(Class interfaceClass)
    {
        String name = interfaceClass.getName();

        int dotx = name.lastIndexOf('.');

        return generateClassName(name.substring(dotx + 1));
    }

    /**
     * Javassist needs the class name to be as it appears in source code, even for arrays. Invoking
     * getName() on a Class instance representing an array returns the internal format (i.e, "[...;"
     * or something). This returns it as it would appear in Java code.
     */
    public static String getJavaClassName(Class inputClass)
    {
        if (inputClass.isArray()) return getJavaClassName(inputClass.getComponentType()) + "[]";

        return inputClass.getName();
    }

    /**
     * Returns true if the method is the standard toString() method. Very few interfaces will ever
     * include this method as part of the interface, but we have to be sure.
     */
    public static boolean isToString(Method method)
    {
        if (!method.getName().equals("toString")) return false;

        if (method.getParameterTypes().length > 0) return false;

        return method.getReturnType().equals(String.class);
    }

    private static class PrimitiveInfo
    {
        private final String _typeCode;

        private final Class _wrapperType;

        private final String _unwrapMethod;

        public PrimitiveInfo(String typeCode, Class wrapperType, String unwrapMethod)
        {
            _typeCode = typeCode;
            _wrapperType = wrapperType;
            _unwrapMethod = unwrapMethod;
        }

        public String getTypeCode()
        {
            return _typeCode;
        }

        public String getUnwrapMethod()
        {
            return _unwrapMethod;
        }

        public Class getWrapperType()
        {
            return _wrapperType;
        }
    }

    private static final Map<String, PrimitiveInfo> PRIMITIVE_INFO = newMap();

    static
    {
        add("boolean", "Z", Boolean.class, "booleanValue");
        add("short", "S", Short.class, "shortValue");
        add("int", "I", Integer.class, "intValue");
        add("long", "J", Long.class, "longValue");
        add("float", "F", Float.class, "floatValue");
        add("double", "D", Double.class, "doubleValue");
        add("char", "C", Character.class, "charValue");
        add("byte", "B", Byte.class, "byteValue");
    }

    private static void add(String primitiveType, String typeCode, Class wrapperType,
            String unwrapMethod)
    {
        PRIMITIVE_INFO.put(primitiveType, new PrimitiveInfo(typeCode, wrapperType, unwrapMethod));
    }

    /**
     * Translates types from standard Java format to Java VM format. For example, java.util.Locale
     * remains java.util.Locale, but int[][] is translated to [[I and java.lang.Object[] to
     * [Ljava.lang.Object;
     */
    public static String getJVMClassName(String type)
    {
        // if it is not an array, just return the type itself
        if (!type.endsWith("[]")) return type;

        // if it is an array, convert it to JavaVM-style format
        StringBuilder buffer = new StringBuilder();

        while (type.endsWith("[]"))
        {
            buffer.append("[");
            type = type.substring(0, type.length() - 2);
        }

        PrimitiveInfo pi = PRIMITIVE_INFO.get(type);

        if (pi != null)
            buffer.append(pi.getTypeCode());
        else
        {
            buffer.append("L");
            buffer.append(type);
            buffer.append(";");
        }

        return buffer.toString();
    }

    /**
     * Given one of the primitive types, returns the name of the method that will unwrap the wrapped
     * type to the primitive type.
     */
    public static String getUnwrapMethodName(String primitiveTypeName)
    {
        return PRIMITIVE_INFO.get(primitiveTypeName).getUnwrapMethod();
    }

    /** Given the name of a primitive type, returns the name of the corresponding wrapper class. */

    public static String getWrapperTypeName(String primitiveType)
    {
        return PRIMITIVE_INFO.get(primitiveType).getWrapperType().getName();
    }

    /**
     * Given some type (possibly a primitive) returns the corresponding primitive type. For
     * non-primitives, the provided type is returned.
     */
    public static Class getWrapperType(Class primitiveType)
    {
        if (primitiveType.isPrimitive())
            return PRIMITIVE_INFO.get(primitiveType.getName()).getWrapperType();

        return primitiveType; // Not a primitive!
    }
}