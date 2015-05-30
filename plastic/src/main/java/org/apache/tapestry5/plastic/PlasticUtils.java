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

import org.apache.tapestry5.internal.plastic.PrimitiveType;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utilities for user code making use of Plastic.
 */
public class PlasticUtils
{
    /**
     * The {@code toString()} method inherited from Object.
     */
    public static final Method TO_STRING = getMethod(Object.class, "toString");

    /**
     * The MethodDescription version of {@code toString()}.
     */
    public static final MethodDescription TO_STRING_DESCRIPTION = new MethodDescription(TO_STRING);

    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.nanoTime());

    /**
     * Returns a string that can be used as part of a Java identifier and is unique
     * for this JVM. Currently returns a hexadecimal string and initialized by
     * System.nanoTime() (but both those details may change in the future).
     *
     * Note that the returned value may start with a numeric digit, so it should be used as a <em>suffix</em>, not
     * <em>prefix</em> of a Java identifier.
     * 
     * @return unique id that can be used as part of a Java identifier
     */
    public static String nextUID()
    {
        return Long.toHexString(PlasticUtils.UID_GENERATOR.getAndIncrement());
    }

    /**
     * Converts a type (including array and primitive types) to their type name (the way they are represented in Java
     * source files).
     */
    public static String toTypeName(Class type)
    {
        if (type.isArray())
            return toTypeName(type.getComponentType()) + "[]";

        return type.getName();
    }

    /** Converts a number of types (usually, arguments to a method or constructor) into their type names. */
    public static String[] toTypeNames(Class[] types)
    {
        String[] result = new String[types.length];

        for (int i = 0; i < result.length; i++)
            result[i] = toTypeName(types[i]);

        return result;
    }

    /**
     * Gets the wrapper type for a given type (if primitive)
     * 
     * @param type
     *            type to look up
     * @return the input type for non-primitive type, or corresponding wrapper type (Boolean.class for boolean.class,
     *         etc.)
     */
    public static Class toWrapperType(Class type)
    {
        assert type != null;

        return type.isPrimitive() ? PrimitiveType.getByPrimitiveType(type).wrapperType : type;
    }

    /**
     * Convenience for getting a method from a class.
     * 
     * @param declaringClass
     *            containing class
     * @param name
     *            name of method
     * @param parameterTypes
     *            types of parameters
     * @return the Method
     * @throws RuntimeException
     *             if any error (such as method not found)
     */
    @SuppressWarnings("unchecked")
    public static Method getMethod(Class declaringClass, String name, Class... parameterTypes)
    {
        try
        {
            return declaringClass.getMethod(name, parameterTypes);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Uses {@link #getMethod(Class, String, Class...)} and wraps the result as a {@link MethodDescription}.
     * 
     * @param declaringClass
     *            containing class
     * @param name
     *            name of method
     * @param parameterTypes
     *            types of parameters
     * @return description for method
     * @throws RuntimeException
     *             if any error (such as method not found)
     */
    public static MethodDescription getMethodDescription(Class declaringClass, String name, Class... parameterTypes)
    {
        return new MethodDescription(getMethod(declaringClass, name, parameterTypes));
    }

    /**
     * Determines if the provided type name is a primitive type.
     *
     * @param typeName Java type name, such as "boolean" or "java.lang.String"
     * @return true if primitive
     */
    public static boolean isPrimitive(String typeName)
    {
        return PrimitiveType.getByName(typeName) != null;
    }
}
