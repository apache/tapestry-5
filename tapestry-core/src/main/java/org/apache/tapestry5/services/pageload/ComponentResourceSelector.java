// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.pageload;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.commons.util.CollectionFactory;

/**
 * Encapsulates the information that is used when locating a template or message catalog associated with a component.
 * The selector is combined with the component class name to locate the other resources. The selector defines one or
 * more <em>axes</em> that are combined with a {@link ComponentResourceLocator} implementation to enforce a naming
 * convention for locating resources. The primary axis is {@link Locale} (Tapestry 5.2 and earlier used a Locale
 * instance as the selector), but Tapestry 5.3 adds support for additional axes.
 *
 * @since 5.3
 */
public final class ComponentResourceSelector
{
    public final Locale locale;

    private final Map<Class, Object> axis;

    public ComponentResourceSelector(Locale locale)
    {
        this(locale, Collections.<Class, Object>emptyMap());
    }

    private ComponentResourceSelector(Locale locale, Map<Class, Object> axis)
    {
        assert locale != null;

        this.locale = locale;
        this.axis = axis;
    }

    /**
     * Returns a <em>new</em> selector with the given axis data. It is not allowed to redefine an existing axis type.
     * Typically, the axis type is an enum type. Axis values are expected to be immutable, and to implement
     * {@code equals()} and {@code hashCode()}.
     *
     * @param axisType  non-blank axis key
     * @param axisValue non-null axis value
     * @return new selector including axis value
     */
    public <T> ComponentResourceSelector withAxis(Class<T> axisType, T axisValue)
    {
        assert axisType != null;
        assert axisValue != null;

        if (axis.containsKey(axisType))
            throw new IllegalArgumentException(String.format("Axis type %s is already specified as %s.",
                    axisType.getName(), axis.get(axisType)));

        Map<Class, Object> updated = CollectionFactory.newMap(axis);
        updated.put(axisType, axisValue);

        return new ComponentResourceSelector(locale, updated);
    }

    /**
     * Returns a previously stored axis value, or null if no axis value of the specified type has been stored.
     *
     * @param <T>
     * @param axisType
     * @return value or null
     */
    public <T> T getAxis(Class<T> axisType)
    {
        return axisType.cast(axis.get(axisType));
    }

    /**
     * Returns true if the object is another selector with the same locale and set of axis.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (!(obj instanceof ComponentResourceSelector))
            return false;

        ComponentResourceSelector other = (ComponentResourceSelector) obj;

        return locale.equals(other.locale) && axis.equals(other.axis);
    }

    @Override
    public int hashCode()
    {
        return 37 * locale.hashCode() + axis.hashCode();
    }

    @Override
    public String toString()
    {
        return String.format("ComponentResourceSelector[%s]", toShortString());
    }

    /**
     * Returns a string identifying the locale, and any additional axis types and values.  Example,
     * "en" or "fr com.example.Skin=RED".
     */
    public String toShortString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(locale.toString());

        String sep = " ";
        for (Map.Entry<Class, Object> e : axis.entrySet())
        {
            builder.append(sep);
            builder.append(e.getKey().getName());
            builder.append('=');
            builder.append(e.getValue().toString());

            sep = ", ";
        }

        return builder.toString();
    }
}
