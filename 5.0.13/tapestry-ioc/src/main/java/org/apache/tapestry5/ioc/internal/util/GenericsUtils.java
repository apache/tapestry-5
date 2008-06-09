// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Static methods related to the use of JDK 1.5 generics.
 */
public class GenericsUtils
{
    /**
     * Analyzes the method (often defined in a base class) in the context of a particular concrete implementation of the
     * class to establish the generic type of a property. This works when the property type is defined as a class
     * generic parameter.
     *
     * @param type   base type for evaluation
     * @param method method (possibly from a base class of type) to extract
     * @return the generic type if it may be determined, or the raw type (that is, with type erasure, most often
     *         Object)
     */
    public static Class extractGenericReturnType(Class type, Method method)
    {
        Class defaultType = method.getReturnType();

        Type genericType = method.getGenericReturnType();

        // We can only handle the case where you "lock down" a generic type to a specific type.

        if (genericType instanceof TypeVariable)
        {

            // An odd name for the method that gives you access to the type parameters
            // used when implementing this class.  When you say Bean<String>, the first
            // type variable of the generic superclass is class String.

            Type superType = type.getGenericSuperclass();

            if (superType instanceof ParameterizedType)
            {
                ParameterizedType superPType = (ParameterizedType) superType;

                TypeVariable tv = (TypeVariable) genericType;

                String name = tv.getName();

                TypeVariable[] typeVariables = tv.getGenericDeclaration().getTypeParameters();

                for (int i = 0; i < typeVariables.length; i++)
                {
                    TypeVariable stv = typeVariables[i];

                    // We're trying to match the name of the type variable that is used as the return type
                    // of the method.  With that name, we find the corresponding index in the
                    // type declarations.  With the index, we check superPType for the Class instance
                    // that defines it. Generics has lots of other options that we simply can't handle.

                    if (stv.getName().equals(name))
                    {
                        Type actualType = superPType.getActualTypeArguments()[i];

                        if (actualType instanceof Class) return (Class) actualType;

                        break;
                    }
                }

            }
        }


        return defaultType;

        // P.S. I wrote this and I barely understand it.  Fortunately, I have tests ...
    }
}
