// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.commons.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.internal.util.InternalCommonsUtils;

/**
 * Used (as part of a {@link UnknownValueException} to identify what available values
 * are present.
 * 
 * @since 5.2.0
 */
public class AvailableValues
{
    private final String valueType;

    private final List<String> values;

    /**
     * @param valueType
     *            a word or phrase that describes what the values are such as "component types" or "service ids"
     *@param values
     *            a set of objects defining the values; the values will be converted to strings and sorted into
     *            ascending order
     */
    public AvailableValues(String valueType, Collection<?> values)
    {
        this.valueType = valueType;
        this.values = sortValues(values);
    }

    public AvailableValues(String valueType, Map<?, ?> map)
    {
        this(valueType, map.keySet());
    }

    private static List<String> sortValues(Collection<?> values)
    {
        List<String> result = CollectionFactory.newList();

        for (Object v : values)
        {
            result.add(String.valueOf(v));
        }

        Collections.sort(result);

        return Collections.unmodifiableList(result);
    }

    /** The type of value, i.e., "component types" or "service ids". */
    public String getValueType()
    {
        return valueType;
    }

    /** The values, as strings, in sorted order. */
    public List<String> getValues()
    {
        return values;
    }

    @Override
    public String toString()
    {
        return String.format("AvailableValues[%s: %s]", valueType, InternalCommonsUtils.join(values));
    }

}
