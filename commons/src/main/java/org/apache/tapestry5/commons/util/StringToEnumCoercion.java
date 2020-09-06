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

package org.apache.tapestry5.commons.util;

import java.util.Map;

import org.apache.tapestry5.commons.services.Coercion;

/**
 * A {@link org.apache.tapestry5.commons.services.Coercion} for converting strings into an instance of a particular
 * enumerated type. The {@link Enum#name() name} is used as the key to identify the enum instance, in a case-insensitive
 * fashion.
 *
 * Moved from tapestry-core to tapestry-ioc in release 5.3, but kept in same package for compatibility.
 * Moved tapestry-ioc to commons in release 5.4, but kept in same package for compatibility.
 * 
 * @param <T>
 *            the type of enumeration
 */
public final class StringToEnumCoercion<T extends Enum> implements Coercion<String, T>
{
    private final Class<T> enumClass;

    private final Map<String, T> stringToEnum = CollectionFactory.newCaseInsensitiveMap();

    public StringToEnumCoercion(Class<T> enumClass)
    {
        this(enumClass, enumClass.getEnumConstants());
    }

    public StringToEnumCoercion(Class<T> enumClass, T... values)
    {
        this.enumClass = enumClass;

        for (T value : values)
            stringToEnum.put(value.name(), value);
    }

    @Override
    public T coerce(String input)
    {
        if (CommonsUtils.isBlank(input))
            return null;

        T result = stringToEnum.get(input);

        if (result == null)
        {
            String message = String.format("Input '%s' does not identify a value from enumerated type %s.", input,
                    enumClass.getName());

            throw new UnknownValueException(message, new AvailableValues(enumClass.getName() + " enum constants",
                    stringToEnum));
        }

        return result;
    }

    /**
     * Allows an alias value (alternate) string to reference a value.
     * 
     * @since 5.2.2
     */
    public StringToEnumCoercion<T> addAlias(String alias, T value)
    {
        stringToEnum.put(alias, value);

        return this;
    }

    public static <T extends Enum> StringToEnumCoercion<T> create(Class<T> enumClass)
    {
        return new StringToEnumCoercion<T>(enumClass);
    }

}
