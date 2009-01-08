// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.ObjectCreator;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;

import static java.lang.String.format;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handy method useful when creating new classes using {@link org.apache.tapestry5.ioc.services.ClassFab}.
 */
public final class ClassFabUtils
{
    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private static String nextUID()
    {
        return Long.toHexString(UID_GENERATOR.getAndIncrement());
    }

    /**
     * Generates a unique class name, which will be in the default package.
     */
    public static synchronized String generateClassName(String baseName)
    {
        return "$" + baseName + "_" + nextUID();
    }

    /**
     * Returns a class name derived from the provided interfaceClass. The package part of the interface name is stripped
     * out, and the result passed to {@link #generateClassName(String)}.
     */
    public static String generateClassName(Class interfaceClass)
    {
        return generateClassName(interfaceClass.getSimpleName());
    }

    /**
     * Javassist needs the class name to be as it appears in source code, even for arrays. Invoking getName() on a Class
     * instance representing an array returns the internal format (i.e, "[...;" or something). This returns it as it
     * would appear in Java code.
     */
    public static String toJavaClassName(Class inputClass)
    {
        if (inputClass.isArray()) return toJavaClassName(inputClass.getComponentType()) + "[]";

        return inputClass.getName();
    }

    /**
     * Returns true if the method is the standard toString() method. Very few interfaces will ever include this method
     * as part of the interface, but we have to be sure.
     */
    public static boolean isToString(Method method)
    {
        if (!method.getName().equals("toString")) return false;

        if (method.getParameterTypes().length > 0) return false;

        return method.getReturnType().equals(String.class);
    }

    public static Class getPrimitiveType(String primitiveTypeName)
    {
        return PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(primitiveTypeName).primitiveType;
    }

    private static class PrimitiveInfo
    {
        private final Class primitiveType;

        private final String typeCode;

        private final Class wrapperType;

        private final String unwrapMethod;

        public PrimitiveInfo(Class primitiveType, String typeCode, Class wrapperType, String unwrapMethod)
        {
            this.primitiveType = primitiveType;
            this.typeCode = typeCode;
            this.wrapperType = wrapperType;
            this.unwrapMethod = unwrapMethod;
        }
    }

    private static final Map<String, PrimitiveInfo> PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO = newMap();
    private static final Map<Class, PrimitiveInfo> WRAPPER_TYPE_TO_PRIMITIVE_INFO = newMap();

    static
    {
        add(boolean.class, "Z", Boolean.class, "booleanValue");
        add(short.class, "S", Short.class, "shortValue");
        add(int.class, "I", Integer.class, "intValue");
        add(long.class, "J", Long.class, "longValue");
        add(float.class, "F", Float.class, "floatValue");
        add(double.class, "D", Double.class, "doubleValue");
        add(char.class, "C", Character.class, "charValue");
        add(byte.class, "B", Byte.class, "byteValue");
    }

    private static void add(Class primitiveType, String typeCode, Class wrapperType, String unwrapMethod)
    {
        PrimitiveInfo info = new PrimitiveInfo(primitiveType, typeCode, wrapperType, unwrapMethod);

        PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.put(primitiveType.getName(), info);

        WRAPPER_TYPE_TO_PRIMITIVE_INFO.put(wrapperType, info);
    }

    /**
     * Translates types from standard Java format to Java VM format. For example, java.util.Locale remains
     * java.util.Locale, but int[][] is translated to [[I and java.lang.Object[] to [Ljava.lang.Object;
     */
    public static String toJVMBinaryName(String type)
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

        PrimitiveInfo pi = PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(type);

        if (pi != null)
        {
            buffer.append(pi.typeCode);
        }
        else
        {
            buffer.append("L");
            buffer.append(type);
            buffer.append(";");
        }

        return buffer.toString();
    }

    /**
     * Given a wrapper type, determines the corresponding primitive type.
     */
    public static Class getPrimitiveType(Class wrapperType)
    {
        return WRAPPER_TYPE_TO_PRIMITIVE_INFO.get(wrapperType).primitiveType;
    }

    /**
     * Returns the wrapper type for an input type; for most types, this is the type.  For primitive types, it is the
     * corresponding wrapper type.
     *
     * @param type type to check
     * @return type or corresponding wrapper type
     */
    public static Class getWrapperType(Class type)
    {
        PrimitiveInfo info = PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(type.getName());

        return info == null ? type : info.wrapperType;
    }

    /**
     * Takes a reference and casts it to the desired type.  If the desired type is a primitive type, then the reference
     * is cast to the correct wrapper type and a call to the correct unwrapper method is added. The end result is code
     * that can be assigned to a field or parameter of the desired type (even if desired type is a primitive).
     *
     * @param reference   to be cast
     * @param desiredType desired object or primitive type
     * @return Javassist code to peform the cast
     */
    public static String castReference(String reference, String desiredType)
    {
        if (isPrimitiveType(desiredType))
        {
            PrimitiveInfo info = PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(desiredType);

            return String.format("((%s)%s).%s()",
                                 info.wrapperType.getName(), reference,
                                 info.unwrapMethod);
        }

        return String.format("(%s)%s", desiredType, reference);
    }

    /**
     * Given a primitive type, finds the unwrap method of the corresponding wrapper type.
     *
     * @param primitiveType
     * @return method name
     */
    public static String getUnwrapMethodName(Class primitiveType)
    {
        return PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(primitiveType.getName()).unwrapMethod;
    }


    /**
     * Given a type name, determines if that is the name of a primitive type.
     */
    public static boolean isPrimitiveType(String typeName)
    {
        return PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.containsKey(typeName);
    }

    /**
     * Converts a Class to a JVM type code (the way class information is expressed in a class file).
     */
    public static String getTypeCode(Class type)
    {
        if (type.equals(void.class)) return "V";

        if (type.isPrimitive()) return PRIMITIVE_TYPE_NAME_TO_PRIMITIVE_INFO.get(type.getName()).typeCode;

        if (type.isArray()) return "[" + getTypeCode(type.getComponentType());

        return "L" + type.getName().replace('.', '/') + ";";
    }

    /**
     * Creates a proxy for a given service interface around an {@link org.apache.tapestry5.ioc.ObjectCreator} that can
     * provide (on demand) an object (implementing the service interface) to delegate to. The ObjectCreator will be
     * invoked on every method invocation (if it is caching, that should be internal to its implementation).
     *
     * @param <T>
     * @param classFab         used to create the new class
     * @param serviceInterface the interface the proxy will implement
     * @param creator          the createor which will provide an instance of the interface
     * @param description      description to be returned from the proxy's toString() method
     * @return the instantiated proxy object
     */
    public static <T> T createObjectCreatorProxy(ClassFab classFab, Class<T> serviceInterface, ObjectCreator creator,
                                                 String description)
    {
        classFab.addField("_creator", Modifier.PRIVATE | Modifier.FINAL, ObjectCreator.class);

        classFab.addConstructor(new Class[] {ObjectCreator.class}, null, "_creator = $1;");

        String body = format("return (%s) _creator.createObject();", serviceInterface.getName());

        MethodSignature sig = new MethodSignature(serviceInterface, "_delegate", null, null);

        classFab.addMethod(Modifier.PRIVATE, sig, body);

        classFab.proxyMethodsToDelegate(serviceInterface, "_delegate()", description);
        Class proxyClass = classFab.createClass();

        try
        {
            Object proxy = proxyClass.getConstructors()[0].newInstance(creator);

            return serviceInterface.cast(proxy);
        }
        catch (Exception ex)
        {
            // This should never happen, so we won't go to a lot of trouble
            // reporting it.
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
