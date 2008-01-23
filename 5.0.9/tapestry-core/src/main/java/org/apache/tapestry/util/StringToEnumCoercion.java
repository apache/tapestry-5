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

package org.apache.tapestry.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.Coercion;

import java.util.Map;

/**
 * A {@link Coercion} for converting strings into an instance of a particular enumerated type. The
 * {@link Enum#name() name} is used as the key to identify the enum instance, in a case-insensitive
 * fashion.
 *
 * @param <T>
 * the type of enumeration
 */
public final class StringToEnumCoercion<T extends Enum> implements Coercion<String, T>
{
    private final Class<T> _enumClass;

    private final Map<String, T> _stringToEnum = newCaseInsensitiveMap();

    public StringToEnumCoercion(Class<T> enumClass)
    {
        this(enumClass, enumClass.getEnumConstants());
    }

    public StringToEnumCoercion(Class<T> enumClass, T... values)
    {
        _enumClass = enumClass;

        for (T value : values)
            _stringToEnum.put(value.name(), value);
    }

    public T coerce(String input)
    {
        if (InternalUtils.isBlank(input))
            return null;

        T result = _stringToEnum.get(input);

        if (result == null)
            throw new RuntimeException(UtilMessages.missingEnumValue(
                    input,
                    _enumClass,
                    _stringToEnum.keySet()));

        return result;
    }

}
