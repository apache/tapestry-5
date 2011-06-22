// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import java.lang.reflect.*;
import java.util.LinkedList;

/**
 * Static methods related to the use of JDK 1.5 generics.
 */
@SuppressWarnings("unchecked")
public class GenericsUtils
{
    /**
     * Analyzes the method (often defined in a base class) in the context of a particular concrete implementation of the
     * class to establish the generic type of a property. This works when the property type is defined as a class
     * generic parameter.
     * 
     * @param containingClassType
     *            class containing the method, used to reason about generics
     * @param method
     *            method (possibly from a base class of type) to extract
     * @return the generic type if it may be determined, or the raw type (that is, with type erasure, most often
     *         Object)
     */
    public static Class extractGenericReturnType(Class containingClassType, Method method)
    {
        return extractActualTypeAsClass(containingClassType, method.getDeclaringClass(), method.getGenericReturnType(),
                method.getReturnType());

    }

    /**
     * Analyzes the field in the context of a particular concrete implementation of the class to establish
     * the generic type of a (public) field. This works when the field type is defined as a class
     * generic parameter.
     * 
     * @param containingClassType
     *            class containing the method, used to reason about generics
     * @param field
     *            public field to extract type from
     * @return the generic type if it may be determined, or the raw type (that is, with type erasure, most often
     * @since 5.2.0
     */
    public static Class extractGenericFieldType(Class containingClassType, Field field)
    {
        return extractActualTypeAsClass(containingClassType, field.getDeclaringClass(), field.getGenericType(),
                field.getType());
    }

    /**
     * @param owner
     *            - type that owns the field
     * @param field
     *            - field that is generic
     * @return Type
     */
    public static Type extractActualType(Type owner, Field field)
    {
        return extractActualType(owner, field.getDeclaringClass(), field.getGenericType(), field.getType());
    }

    /**
     * @param owner
     *            - type that owns the field
     * @param method
     *            - method with generic return type
     * @return Type
     */
    public static Type extractActualType(Type owner, Method method)
    {
        return extractActualType(owner, method.getDeclaringClass(), method.getGenericReturnType(),
                method.getReturnType());
    }

    /**
     * Extracts the Class used as a type argument when declaring a
     * 
     * @param containingType
     *            - the type which the method is being/will be called on
     * @param declaringClass
     *            - the class that the method is actually declared in (base class)
     * @param type
     *            - the generic type from the field/method being inspected
     * @param defaultType
     *            - the default type to return if no parameterized type can be found
     * @return a Class or ParameterizedType that the field/method can reliably be cast to.
     * @since 5.2.?
     */
    private static Type extractActualType(final Type containingType, final Class declaringClass, final Type type,
            final Class defaultType)
    {

        if (type instanceof ParameterizedType) { return type; }
        if (!(type instanceof TypeVariable))
            return defaultType;

        TypeVariable typeVariable = (TypeVariable) type;

        if (!declaringClass.isAssignableFrom(asClass(containingType))) { throw new RuntimeException(String.format(
                "%s must be a subclass of %s", declaringClass.getName(), asClass(containingType).getName())); }

        // First, check to see if we are operating on a parameterized type already.
        Type extractedType = type;
        if (containingType instanceof ParameterizedType)
        {
            final int i = getTypeVariableIndex(asClass(containingType), typeVariable);
            extractedType = ((ParameterizedType) containingType).getActualTypeArguments()[i];
            if (extractedType instanceof Class || extractedType instanceof ParameterizedType) { return extractedType; }
        }

        // Somewhere between declaringClass and containingClass are the parameter type arguments
        // We are going to drop down the containingClassType until we find the declaring class.
        // The class that extends declaringClass will define the ParameterizedType or a new TypeVariable

        final LinkedList<Type> classStack = new LinkedList<Type>();
        Type cur = containingType;
        while (cur != null && !asClass(cur).equals(declaringClass))
        {
            classStack.add(0, cur);
            cur = asClass(cur).getSuperclass();
        }

        int typeArgumentIndex = getTypeVariableIndex(declaringClass, (TypeVariable) extractedType);

        for (Type descendant : classStack)
        {
            final Class descendantClass = asClass(descendant);
            final ParameterizedType parameterizedType = (ParameterizedType) descendantClass.getGenericSuperclass();

            extractedType = parameterizedType.getActualTypeArguments()[typeArgumentIndex];

            if (extractedType instanceof Class || extractedType instanceof ParameterizedType) { return extractedType; }

            if (extractedType instanceof TypeVariable)
            {
                typeArgumentIndex = getTypeVariableIndex(descendantClass, (TypeVariable) extractedType);
            }
            else
            {
                // I don't know what else this could be?
                break;
            }
        }

        return defaultType;
    }

    /**
     * Convenience method to get actual type as raw class.
     * 
     * @param containingClassType
     * @param declaringClass
     * @param type
     * @param defaultType
     * @return
     * @see #extractActualType(Type, Class, Type, Class)
     */
    private static Class extractActualTypeAsClass(Class containingClassType, Class<?> declaringClass, Type type,
            Class<?> defaultType)
    {
        final Type actualType = extractActualType(containingClassType, declaringClass, type, defaultType);

        return asClass(actualType);
    }

    public static Class asClass(Type actualType)
    {
        if (actualType instanceof ParameterizedType)
        {
            final Type rawType = ((ParameterizedType) actualType).getRawType();
            if (rawType instanceof Class)
            {
                // The sun implementation returns Class<?>, but there is room in the interface for it to be
                // something else so to be safe ignore whatever "something else" might be.
                // TODO: consider logging for that day when "something else" causes some confusion
                return (Class) rawType;
            }
        }

        return (Class) actualType;
    }

    /**
     * Find the index of the TypeVariable in the classes parameters. The offset can be used on a subclass to find
     * the actual type.
     * 
     * @param clazz
     *            - the parameterized class
     * @param typeVar
     *            - the type variable in question.
     * @return the index of the type variable in the classes type parameters.
     */
    private static int getTypeVariableIndex(Class clazz, TypeVariable typeVar)
    {
        // the label from the class (the T in List<T>, the K and V in Map<K,V>, etc)
        String typeVarName = typeVar.getName();
        int typeArgumentIndex = 0;
        final TypeVariable[] typeParameters = clazz.getTypeParameters();
        for (; typeArgumentIndex < typeParameters.length; typeArgumentIndex++)
        {
            if (typeParameters[typeArgumentIndex].getName().equals(typeVarName))
                break;
        }
        return typeArgumentIndex;
    }
}
