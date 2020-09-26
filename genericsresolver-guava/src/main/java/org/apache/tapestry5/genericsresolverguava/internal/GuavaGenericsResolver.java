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

package org.apache.tapestry5.genericsresolverguava.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.tapestry5.commons.services.GenericsResolver;
import org.apache.tapestry5.genericsresolverguava.internal.GuavaGenericsResolver;

import com.google.common.reflect.TypeToken;

/**
 * {@link GuavaGenericsResolver} implementation using Guava.
 */
public class GuavaGenericsResolver implements GenericsResolver {

    @Override
    public Class<?> extractGenericReturnType(Class<?> containingClass, Method method) 
    {
        return TypeToken.of(containingClass).resolveType(method.getGenericReturnType()).getRawType();
    }

    @Override
    public Class extractGenericFieldType(Class containingClass, Field field) 
    {
        return TypeToken.of(containingClass).resolveType(field.getGenericType()).getRawType();
    }

    @Override
    public Type extractActualType(Type containingType, Method method) 
    {
        return TypeToken.of(containingType).resolveType(method.getGenericReturnType()).getType();
    }

    @Override
    public Type extractActualType(Type containingType, Field field) 
    {
        return TypeToken.of(containingType).resolveType(field.getGenericType()).getType();
    }

    @Override
    public Type resolve(Type type, Type containingType) 
    {
        return TypeToken.of(containingType).resolveType(type).getType();
    }

    @Override
    public Class asClass(Type actualType) {
        return TypeToken.of(actualType).getRawType();
    }

}
