// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.commons.internal.services;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedList;

import org.apache.tapestry5.commons.services.GenericsResolver;

/**
 * Implementation copied from Tapestry 5.4's GenericUtils (commons package).
 */
@SuppressWarnings("rawtypes")
public class GenericsResolverImpl implements GenericsResolver
{
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
    public Class<?> extractGenericReturnType(Class<?> containingClass, Method method)
    {
        return asClass(resolve(method.getGenericReturnType(), containingClass));
    }


    /**
     * Analyzes the field in the context of containingClass and returns the Class that is represented by
     * the field's generic type. Any parameter information in the generic type is lost, if you want
     * to preserve the type parameters of the return type consider using
     * {@link #getTypeVariableIndex(java.lang.reflect.TypeVariable)}.
     *
     * @param containingClass class which either contains or inherited the field
     * @param field           field from which to extract the type
     * @return the class represented by the field's generic type, resolved based on the containingClass.
     * @see #extractActualType(java.lang.reflect.Type, java.lang.reflect.Field)
     * @see #resolve(java.lang.reflect.Type,java.lang.reflect.Type)
     * @see #asClass(java.lang.reflect.Type)
     */
    public Class extractGenericFieldType(Class containingClass, Field field)
    {
        return asClass(resolve(field.getGenericType(), containingClass));
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
    public Type extractActualType(Type containingType, Method method)
    {
        return resolve(method.getGenericReturnType(), containingType);
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
    public Type extractActualType(Type containingType, Field field)
    {
        return resolve(field.getGenericType(), containingType);
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
    public Type resolve(final Type type, final Type containingType)
    {
        // The type isn't generic. (String, Long, etc)
        if (type instanceof Class)
            return type;

        // List<T>, List<String>, List<T extends Number>
        if (type instanceof ParameterizedType)
            return resolve((ParameterizedType) type, containingType);

        // T[], List<String>[], List<T>[]
        if (type instanceof GenericArrayType)
            return resolve((GenericArrayType) type, containingType);

        // List<? extends T>, List<? extends Object & Comparable & Serializable>
        if (type instanceof WildcardType)
            return resolve((WildcardType) type, containingType);

        // T
        if (type instanceof TypeVariable)
            return resolve((TypeVariable) type, containingType);

        // I'm leaning towards an exception here.
        return type;
    }


    /**
     * Determines if the suspected super type is assignable from the suspected sub type.
     *
     * @param suspectedSuperType
     *          e.g. {@code GenericDAO<Pet, String>}
     * @param suspectedSubType
     *          e.g. {@code PetDAO extends GenericDAO<Pet,String>}
     * @return
     *          true if (sourceType)targetClass is a valid cast
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private boolean isAssignableFrom(Type suspectedSuperType, Type suspectedSubType)
    {
        final Class suspectedSuperClass = asClass(suspectedSuperType);
        final Class suspectedSubClass = asClass(suspectedSubType);

        // The raw types need to be compatible.
        if (!suspectedSuperClass.isAssignableFrom(suspectedSubClass))
        {
            return false;
        }

        // From this point we know that the raw types are assignable.
        // We need to figure out what the generic parameters in the targetClass are
        // as they pertain to the sourceType.

        if (suspectedSuperType instanceof WildcardType)
        {
            // ? extends Number
            // needs to match all the bounds (there will only be upper bounds or lower bounds
            for (Type t : ((WildcardType) suspectedSuperType).getUpperBounds())
            {
                if (!isAssignableFrom(t, suspectedSubType)) return false;
            }
            for (Type t : ((WildcardType) suspectedSuperType).getLowerBounds())
            {
                if (!isAssignableFrom(suspectedSubType, t)) return false;
            }
            return true;
        }

        Type curType = suspectedSubType;
        Class curClass;

        while (curType != null && !curType.equals(Object.class))
        {
            curClass = asClass(curType);

            if (curClass.equals(suspectedSuperClass))
            {
                final Type resolved = resolve(curType, suspectedSubType);

                if (suspectedSuperType instanceof Class)
                {
                    if ( resolved instanceof Class )
                        return suspectedSuperType.equals(resolved);

                    // They may represent the same class, but the suspectedSuperType is not parameterized. The parameter
                    // types default to Object so they must be a match.
                    // e.g. Pair p = new StringLongPair();
                    //      Pair p = new Pair<? extends Number, String>

                    return true;
                }

                if (suspectedSuperType instanceof ParameterizedType)
                {
                    if (resolved instanceof ParameterizedType)
                    {
                        final Type[] type1Arguments = ((ParameterizedType) suspectedSuperType).getActualTypeArguments();
                        final Type[] type2Arguments = ((ParameterizedType) resolved).getActualTypeArguments();
                        if (type1Arguments.length != type2Arguments.length) return false;

                        for (int i = 0; i < type1Arguments.length; ++i)
                        {
                            if (!isAssignableFrom(type1Arguments[i], type2Arguments[i])) return false;
                        }
                        return true;
                    }
                }
                else if (suspectedSuperType instanceof GenericArrayType)
                {
                    if (resolved instanceof GenericArrayType)
                    {
                        return isAssignableFrom(
                                ((GenericArrayType) suspectedSuperType).getGenericComponentType(),
                                ((GenericArrayType) resolved).getGenericComponentType()
                        );
                    }
                }

                return false;
            }

            final Type[] types = curClass.getGenericInterfaces();
            for (Type t : types)
            {
                final Type resolved = resolve(t, suspectedSubType);
                if (isAssignableFrom(suspectedSuperType, resolved))
                    return true;
            }

            curType = curClass.getGenericSuperclass();
        }
        return false;
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
    public Class asClass(Type actualType)
    {
        if (actualType instanceof Class) return (Class) actualType;

        if (actualType instanceof ParameterizedType)
        {
            final Type rawType = ((ParameterizedType) actualType).getRawType();
            // The sun implementation returns getRawType as Class<?>, but there is room in the interface for it to be
            // some other Type. We'll assume it's a Class.
            // TODO: consider logging or throwing our own exception for that day when "something else" causes some confusion
            return (Class) rawType;
        }

        if (actualType instanceof GenericArrayType)
        {
            final Type type = ((GenericArrayType) actualType).getGenericComponentType();
            return Array.newInstance(asClass(type), 0).getClass();
        }

        if (actualType instanceof TypeVariable)
        {
            // Support for List<T extends Number>
            // There is always at least one bound. If no bound is specified in the source then it will be Object.class
            return asClass(((TypeVariable) actualType).getBounds()[0]);
        }

        if (actualType instanceof WildcardType)
        {
            final WildcardType wildcardType = (WildcardType) actualType;
            final Type[] bounds = wildcardType.getLowerBounds();
            if (bounds != null && bounds.length > 0)
            {
                return asClass(bounds[0]);
            }
            // If there is no lower bounds then the only thing that makes sense is Object.
            return Object.class;
        }

        throw new RuntimeException(String.format("Unable to convert %s to Class.", actualType));
    }

    /**
     * Convert the type into a string. The string representation approximates the code that would be used to define the
     * type.
     *
     * @param type - the type.
     * @return a string representation of the type, similar to how it was declared.
     */
    public static String toString(Type type)
    {
        if ( type instanceof ParameterizedType ) return toString((ParameterizedType)type);
        if ( type instanceof WildcardType ) return toString((WildcardType)type);
        if ( type instanceof GenericArrayType) return toString((GenericArrayType)type);
        if ( type instanceof Class )
        {
            final Class theClass = (Class) type;
            return (theClass.isArray() ? theClass.getName() + "[]" : theClass.getName());
        }
        return type.toString();
    }

    /**
     * Method to resolve a TypeVariable to its most
     * <a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#112582">reifiable</a> form.
     *
     *
     * How to resolve a TypeVariable:<br/>
     * All of the TypeVariables defined by a generic class will be given a Type by any class that extends it. The Type
     * given may or may not be reifiable; it may be another TypeVariable for instance.
     *
     * Consider <br/>
     * <i>class Pair&gt;A,B> { A getA(){...}; ...}</i><br/>
     * <i>class StringLongPair extends Pair&gt;String, Long> { }</i><br/>
     *
     * To resolve the actual return type of Pair.getA() you must first resolve the TypeVariable "A".
     * We can do that by first finding the index of "A" in the Pair.class.getTypeParameters() array of TypeVariables.
     *
     * To get to the Type provided by StringLongPair you access the generics information by calling
     * StringLongPair.class.getGenericSuperclass; this will be a ParameterizedType. ParameterizedType gives you access
     * to the actual type arguments provided to Pair by StringLongPair. The array is in the same order as the array in
     * Pair.class.getTypeParameters so you can use the index we discovered earlier to extract the Type; String.class.
     *
     * When extracting Types we only have to consider the superclass hierarchy and not the interfaces implemented by
     * the class. When a class implements a generic interface it must provide types for the interface and any generic
     * methods implemented from the interface will be re-defined by the class with its generic type variables.
     *
     * @param typeVariable   - the type variable to resolve.
     * @param containingType - the shallowest class in the class hierarchy (furthest from Object) where typeVariable is defined.
     * @return a Type that has had all possible TypeVariables resolved that have been defined between the type variable
     *         declaration and the containingType.
     */
    private Type resolve(TypeVariable typeVariable, Type containingType)
    {
        // The generic declaration is either a Class, Method or Constructor
        final GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

        if (!(genericDeclaration instanceof Class))
        {
            // It's a method or constructor. The best we can do here is try to resolve the bounds
            // e.g. <T extends E> T getT(T param){} where E is defined by the class.
            final Type bounds0 = typeVariable.getBounds()[0];
            return resolve(bounds0, containingType);
        }

        final Class typeVariableOwner = (Class) genericDeclaration;

        // find the typeOwner in the containingType's hierarchy
        final LinkedList<Type> stack = new LinkedList<Type>();

        // If you pass a List<Long> as the containingType then the TypeVariable is going to be resolved by the
        // containingType and not the super class.
        if (containingType instanceof ParameterizedType)
        {
            stack.add(containingType);
        }

        Class theClass = asClass(containingType);
        Type genericSuperclass = theClass.getGenericSuperclass();
        while (genericSuperclass != null && // true for interfaces with no superclass
                !theClass.equals(Object.class) &&
                !theClass.equals(typeVariableOwner))
        {
            stack.addFirst(genericSuperclass);
            theClass = asClass(genericSuperclass);
            genericSuperclass = theClass.getGenericSuperclass();
        }

        int i = getTypeVariableIndex(typeVariable);
        Type resolved = typeVariable;
        for (Type t : stack)
        {
            if (t instanceof ParameterizedType)
            {
                resolved = ((ParameterizedType) t).getActualTypeArguments()[i];
                if (resolved instanceof Class) return resolved;
                if (resolved instanceof TypeVariable)
                {
                    // Need to look at the next class in the hierarchy
                    i = getTypeVariableIndex((TypeVariable) resolved);
                    continue;
                }
                return resolve(resolved, containingType);
            }
        }

        // the only way we get here is if resolved is still a TypeVariable, otherwise an
        // exception is thrown or a value is returned.
        return ((TypeVariable) resolved).getBounds()[0];
    }

    /**
     * @param type           - something like List&lt;T>[] or List&lt;? extends T>[] or T[]
     * @param containingType - the shallowest type in the hierarchy where type is defined.
     * @return either the passed type if no changes required or a copy with a best effort resolve of the component type.
     */
    private GenericArrayType resolve(GenericArrayType type, Type containingType)
    {
        final Type componentType = type.getGenericComponentType();

        if (!(componentType instanceof Class))
        {
            final Type resolved = resolve(componentType, containingType);
            return create(resolved);
        }

        return type;
    }

    /**
     * @param type           - something like List&lt;T>, List&lt;T extends Number>
     * @param containingType - the shallowest type in the hierarchy where type is defined.
     * @return the passed type if nothing to resolve or a copy of the type with the type arguments resolved.
     */
    private ParameterizedType resolve(ParameterizedType type, Type containingType)
    {
        // Use a copy because we're going to modify it.
        final Type[] types = type.getActualTypeArguments().clone();

        boolean modified = resolve(types, containingType);
        return modified ? create(type.getRawType(), type.getOwnerType(), types) : type;
    }

    /**
     * @param type           - something like List&lt;? super T>, List<&lt;? extends T>, List&lt;? extends T & Comparable&lt? super T>>
     * @param containingType - the shallowest type in the hierarchy where type is defined.
     * @return the passed type if nothing to resolve or a copy of the type with the upper and lower bounds resolved.
     */
    private WildcardType resolve(WildcardType type, Type containingType)
    {
        // Use a copy because we're going to modify them.
        final Type[] upper = type.getUpperBounds().clone();
        final Type[] lower = type.getLowerBounds().clone();

        boolean modified = resolve(upper, containingType);
        modified = modified || resolve(lower, containingType);

        return modified ? create(upper, lower) : type;
    }

    /**
     * @param types          - Array of types to resolve. The unresolved type is replaced in the array with the resolved type.
     * @param containingType - the shallowest type in the hierarchy where type is defined.
     * @return true if any of the types were resolved.
     */
    private boolean resolve(Type[] types, Type containingType)
    {
        boolean modified = false;
        for (int i = 0; i < types.length; ++i)
        {
            Type t = types[i];
            if (!(t instanceof Class))
            {
                modified = true;
                final Type resolved = resolve(t, containingType);
                if (!resolved.equals(t))
                {
                    types[i] = resolved;
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * @param rawType       - the un-parameterized type.
     * @param ownerType     - the outer class or null if the class is not defined within another class.
     * @param typeArguments - type arguments.
     * @return a copy of the type with the typeArguments replaced.
     */
    static ParameterizedType create(final Type rawType, final Type ownerType, final Type[] typeArguments)
    {
        return new ParameterizedType()
        {
            @Override
            public Type[] getActualTypeArguments()
            {
                return typeArguments;
            }

            @Override
            public Type getRawType()
            {
                return rawType;
            }

            @Override
            public Type getOwnerType()
            {
                return ownerType;
            }

            @Override
            public String toString()
            {
                return GenericsResolverImpl.toString(this);
            }
        };
    }

    static GenericArrayType create(final Type componentType)
    {
        return new GenericArrayType()
        {
            @Override
            public Type getGenericComponentType()
            {
                return componentType;
            }

            @Override
            public String toString()
            {
                return GenericsResolverImpl.toString(this);
            }
        };
    }

    /**
     * @param upperBounds - e.g. ? extends Number
     * @param lowerBounds - e.g. ? super Long
     * @return An new copy of the type with the upper and lower bounds replaced.
     */
    static WildcardType create(final Type[] upperBounds, final Type[] lowerBounds)
    {

        return new WildcardType()
        {
            @Override
            public Type[] getUpperBounds()
            {
                return upperBounds;
            }

            @Override
            public Type[] getLowerBounds()
            {
                return lowerBounds;
            }

            @Override
            public String toString()
            {
                return GenericsResolverImpl.toString(this);
            }
        };
    }

    static String toString(ParameterizedType pt)
    {
        String s = toString(pt.getActualTypeArguments());
        return String.format("%s<%s>", toString(pt.getRawType()), s);
    }

    static String toString(GenericArrayType gat)
    {
        return String.format("%s[]", toString(gat.getGenericComponentType()));
    }

    static String toString(WildcardType wt)
    {
        final boolean isSuper = wt.getLowerBounds().length > 0;
        return String.format("? %s %s",
                isSuper ? "super" : "extends",
                toString(wt.getLowerBounds()));
    }

    static String toString(Type[] types)
    {
        StringBuilder sb = new StringBuilder();
        for ( Type t : types )
        {
            sb.append(toString(t)).append(", ");
        }
        return sb.substring(0, sb.length() - 2);// drop last ,
    }

    /**
     * Find the index of the TypeVariable in the classes parameters. The offset can be used on a subclass to find
     * the actual type.
     *
     * @param typeVariable - the type variable in question.
     * @return the index of the type variable in its declaring class/method/constructor's type parameters.
     */
    private static int getTypeVariableIndex(final TypeVariable typeVariable)
    {
        // the label from the class (the T in List<T>, the K or V in Map<K,V>, etc)
        final String typeVarName = typeVariable.getName();
        final TypeVariable[] typeParameters = typeVariable.getGenericDeclaration().getTypeParameters();
        for (int typeArgumentIndex = 0; typeArgumentIndex < typeParameters.length; typeArgumentIndex++)
        {
            // The .equals for TypeVariable may not be compatible, a name check should be sufficient.
            if (typeParameters[typeArgumentIndex].getName().equals(typeVarName))
                return typeArgumentIndex;
        }

        // The only way this could happen is if the TypeVariable is hand built incorrectly, or it's corrupted.
        throw new RuntimeException(
                String.format("%s does not have a TypeVariable matching %s", typeVariable.getGenericDeclaration(), typeVariable));
    }
    
}
