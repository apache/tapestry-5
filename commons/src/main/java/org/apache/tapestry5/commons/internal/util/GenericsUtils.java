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

package org.apache.tapestry5.commons.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.tapestry5.commons.services.GenericsResolver;

/**
 * Static methods related to the use of JDK 1.5 generics. From Tapestry 5.5.0,
 * this class just delegates to {@link GenericsResolver}.
 */
public class GenericsUtils
{
    final private static GenericsResolver GENERICS_RESOLVER = GenericsResolver.Provider.getInstance();
    
    /**
     * Analyzes the method in the context of containingClass and returns the Class that is represented by
     * the method's generic return type. Any parameter information in the generic return type is lost. If you want
     * to preserve the type parameters of the return type consider using
     * {@link #extractActualType(java.lang.reflect.Type, java.lang.reflect.Method)}.
     *
     * @param containingClass class which either contains or inherited the method
     * @param method          method from which to extract the return type
     * @return the class represented by the methods generic return type, resolved based on the context .
     * @see #extractActualType(java.lang.reflect.Type, java.lang.reflect.Method)
     * @see #resolve(java.lang.reflect.Type,java.lang.reflect.Type)
     * @see #asClass(java.lang.reflect.Type)
     */
    public static Class<?> extractGenericReturnType(Class<?> containingClass, Method method)
    {
        return GENERICS_RESOLVER.extractGenericReturnType(containingClass, method);
    }

    /**
     * Analyzes the field in the context of containingClass and returns the Class that is represented by
     * the field's generic type. Any parameter information in the generic type is lost, if you want
     * to preserve the type parameters of the return type consider using
     * #getTypeVariableIndex(java.lang.reflect.TypeVariable).
     *
     * @param containingClass class which either contains or inherited the field
     * @param field           field from which to extract the type
     * @return the class represented by the field's generic type, resolved based on the containingClass.
     * @see #extractActualType(java.lang.reflect.Type, java.lang.reflect.Field)
     * @see #resolve(java.lang.reflect.Type,java.lang.reflect.Type)
     * @see #asClass(java.lang.reflect.Type)
     */
    public static Class extractGenericFieldType(Class containingClass, Field field)
    {
        return GENERICS_RESOLVER.extractGenericFieldType(containingClass, field);
    }

    /**
     * Analyzes the method in the context of containingClass and returns the Class that is represented by
     * the method's generic return type. Any parameter information in the generic return type is lost.
     *
     * @param containingType Type which is/represents the class that either contains or inherited the method
     * @param method         method from which to extract the generic return type
     * @return the generic type represented by the methods generic return type, resolved based on the containingType.
     * @see #resolve(java.lang.reflect.Type,java.lang.reflect.Type)
     */
    public static Type extractActualType(Type containingType, Method method)
    {
        return GENERICS_RESOLVER.extractActualType(containingType, method);
    }

    /**
     * Analyzes the method in the context of containingClass and returns the Class that is represented by
     * the method's generic return type. Any parameter information in the generic return type is lost.
     *
     * @param containingType Type which is/represents the class that either contains or inherited the field
     * @param field          field from which to extract the generic return type
     * @return the generic type represented by the methods generic return type, resolved based on the containingType.
     * @see #resolve(java.lang.reflect.Type,java.lang.reflect.Type)
     */
    public static Type extractActualType(Type containingType, Field field)
    {
        return GENERICS_RESOLVER.extractActualType(containingType, field);
    }

    /**
     * Resolves the type parameter based on the context of the containingType.
     *
     * {@link java.lang.reflect.TypeVariable} will be unwrapped to the type argument resolved form the class
     * hierarchy. This may be something other than a simple Class if the type argument is a ParameterizedType for
     * instance (e.g. {@code List<E>; List<Map<Long, String>>}, E would be returned as a ParameterizedType with the raw
     * type Map and type arguments Long and String.
     *
     *
     * @param type
     *          the generic type (ParameterizedType, GenericArrayType, WildcardType, TypeVariable) to be resolved
     * @param containingType
     *          the type which his
     * @return
     *          the type resolved to the best of our ability.
     * @since 5.2.?
     */
    public static Type resolve(final Type type, final Type containingType)
    {
        return GENERICS_RESOLVER.resolve(type, containingType);
    }
    
    /**
     * Get the class represented by the reflected type.
     * This method is lossy; You cannot recover the type information from the class that is returned.
     *
     * {@code TypeVariable} the first bound is returned. If your type variable extends multiple interfaces that information
     * is lost.
     *
     * {@code WildcardType} the first lower bound is returned. If the wildcard is defined with upper bounds
     * then {@code Object} is returned.
     *
     * @param actualType
     *           a Class, ParameterizedType, GenericArrayType
     * @return the un-parameterized class associated with the type.
     */
    public static Class asClass(Type actualType)
    {
        return GENERICS_RESOLVER.asClass(actualType);
    }
    
}
