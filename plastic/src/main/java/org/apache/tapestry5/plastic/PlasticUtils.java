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

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.internal.plastic.PrimitiveType;

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
    
    private static final MethodDescription PROPERTY_VALUE_PROVIDER_METHOD_DESCRIPTION;
    
    static
    {
        try {
            PROPERTY_VALUE_PROVIDER_METHOD_DESCRIPTION = new MethodDescription(PropertyValueProvider.class.getMethod("__propertyValueProvider__get", String.class));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }


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

    /**
     * If the given class is an inner class, returns the enclosing class.
     * Otherwise, returns the class name unchanged.
     */
    public static String getEnclosingClassName(String className)
    {
        int index = className.indexOf('$');
        return index <= 0 ? className : className.substring(0, index);
    }

    /**
     * Utility method for creating {@linkplain FieldInfo} instances.
     * @param field a {@linkplain PlasticField}.
     * @return a corresponding {@linkplain FieldInfo}.
     * @since 5.8.4
     */
    public static FieldInfo toFieldInfo(PlasticField field)
    {
        return new FieldInfo(field.getName(), field.getTypeName());
    }
    
    /**
     * Transforms this {@linkplain PlasticClass} so it implements
     * {@linkplain FieldValueProvider} for the given set of field names.
     * Notice attempts to read a superclass' private field will result in 
     * an {@linkplain IllegalAccessError}.
     * 
     * @param plasticClass a {@linkplain PlasticClass} instance.
     * @param fieldNames a {@linkplain Set} of {@linkplain String}s containing the field names.
     * @since 5.8.4
     */
    public static void implementFieldValueProvider(PlasticClass plasticClass, Set<FieldInfo> fields)
    {
        
        final Set<PlasticMethod> methods = plasticClass.introduceInterface(FieldValueProvider.class);
        
        if (!methods.isEmpty())
        {
            final PlasticMethod method = methods.iterator().next();
            
            method.changeImplementation((builder) -> {
                
                for (FieldInfo field : fields) 
                {
                    builder.loadArgument(0);
                    builder.loadConstant(field.name);
                    builder.invokeVirtual(String.class.getName(), "boolean", "equals", Object.class.getName());
                    builder.when(Condition.NON_ZERO, ifBuilder -> {
                        ifBuilder.loadThis();
                        ifBuilder.getField(plasticClass.getClassName(), field.name, field.type);
                        ifBuilder.boxPrimitive(field.type);
                        ifBuilder.returnResult();
                    });
                }
                
                builder.throwException(RuntimeException.class, "Field not found or not supported");
                
            });
            
        }
        
    }
    
    /**
     * Transforms this {@linkplain PlasticClass} so it implements
     * {@linkplain PropertyValueProvider} for the given set of field names.
     * The implementation will use the fields' corresponding getters instead
     * of direct fields access.
     * 
     * @param plasticClass a {@linkplain PlasticClass} instance.
     * @param fieldNames a {@linkplain Set} of {@linkplain String}s containing the filed (i.e. property) names.
     * @since 5.8.4
     */
    public static void implementPropertyValueProvider(PlasticClass plasticClass, Set<FieldInfo> fields)
    {
        
        final Set<PlasticMethod> methods = plasticClass.introduceInterface(PropertyValueProvider.class);
        
        final InstructionBuilderCallback callback = (builder) -> {
            
            for (FieldInfo field : fields) 
            {
                builder.loadArgument(0);
                builder.loadConstant(field.name);
                builder.invokeVirtual(String.class.getName(), "boolean", "equals", Object.class.getName());
                builder.when(Condition.NON_ZERO, ifBuilder -> 
                {
                    final String prefix = field.type.equals("boolean") ? "is" : "get";
                    final String methodName = prefix + PlasticInternalUtils.capitalize(field.name);
                    
                    ifBuilder.loadThis();
                    builder.invokeVirtual(
                            plasticClass.getClassName(), 
                            field.type, 
                            methodName);
                    ifBuilder.boxPrimitive(field.type);
                    ifBuilder.returnResult();
                });
                
            }
            
            builder.loadThis();
            builder.instanceOf(PropertyValueProvider.class);
            
            builder.when(Condition.NON_ZERO, ifBuilder -> {
                builder.loadThis();
                builder.loadArgument(0);
                ifBuilder.invokeSpecial(
                        plasticClass.getSuperClassName(), 
                        PROPERTY_VALUE_PROVIDER_METHOD_DESCRIPTION);
                ifBuilder.returnResult();
            });
            
            // Field/property not found, so let's try the superclass in case
            // it also implement
            
            builder.throwException(RuntimeException.class, "Property not found or not supported");
            
        };
        
        final PlasticMethod method;
        
        // Superclass has already defined this method, so we need to override it so
        // it can also find the subclasses' declared fields/properties.
        if (methods.isEmpty())
        {
            method = plasticClass.introduceMethod(PROPERTY_VALUE_PROVIDER_METHOD_DESCRIPTION , callback);
        }
        else
        {
            method = methods.iterator().next();
        }
        
        method.changeImplementation(callback);
        
    }

    /**
     * Class used to represent a field name and its type for 
     * {@linkplain PlasticUtils#implementFieldValueProvider(PlasticClass, Set)}.
     * It shouldn't be used directly. Use {@linkplain PlasticUtils#toFieldInfo(PlasticField)}
     * instead.
     * @see PlasticUtils#implementFieldValueProvider(PlasticClass, Set)
     * @since 5.8.4
     */
    public static class FieldInfo {
        final private String name;
        final private String type;
        public FieldInfo(String name, String type) 
        {
            super();
            this.name = name;
            this.type = type;
        }
        @Override
        public int hashCode() 
        {
            return Objects.hash(name);
        }
        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) 
            {
                return true;
            }
            if (!(obj instanceof FieldInfo)) 
            {
                return false;
            }
            FieldInfo other = (FieldInfo) obj;
            return Objects.equals(name, other.name);
        }
        @Override
        public String toString() 
        {
            return "FieldInfo [name=" + name + ", type=" + type + "]";
        }
        
    }
    
}
