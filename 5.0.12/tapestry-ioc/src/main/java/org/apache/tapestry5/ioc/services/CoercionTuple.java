// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;

/**
 * An immutable object that represents a mapping from one type to another. This is also the contribution type when
 * building the {@link org.apache.tapestry5.ioc.services.TypeCoercer} service. Wraps a {@link
 * org.apache.tapestry5.ioc.services.Coercion} object that performs the work with additional properties that describe
 * the input and output types of the coercion, needed when searching for an appropriate coercion (or sequence of
 * coercions).
 *
 * @param <S> source (input) type
 * @param <T> target (output) type
 */
public final class CoercionTuple<S, T>
{
    private final Class<S> sourceType;

    private final Class<T> targetType;

    private final Coercion<S, T> coercion;

    /**
     * Wraps an arbitrary coercion with an implementation of toString() that identifies the source and target types.
     */
    private class CoercionWrapper<WS, WT> implements Coercion<WS, WT>
    {
        private final Coercion<WS, WT> coercion;

        public CoercionWrapper(Coercion<WS, WT> coercion)
        {
            this.coercion = coercion;
        }

        public WT coerce(WS input)
        {
            return coercion.coerce(input);
        }

        @Override
        public String toString()
        {
            return String.format("%s --> %s", convert(sourceType), convert(targetType));
        }
    }

    private String convert(Class type)
    {
        if (void.class.equals(type)) return "null";

        String name = ClassFabUtils.toJavaClassName(type);

        int dotx = name.lastIndexOf('.');

        // Strip off a package name of "java.lang"

        if (dotx > 0 && name.substring(0, dotx).equals("java.lang"))
            return name.substring(dotx + 1);

        return name;
    }

    /**
     * Standard constructor, which defaults wrap to true.
     */
    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion)
    {
        this(sourceType, targetType, coercion, true);
    }

    /**
     * Internal-use constructor.
     *
     * @param sourceType the source (or input) type of the coercion
     * @param targetType the target (or output) type of the coercion
     * @param coercion   the object that performs the coercion
     * @param wrap       if true, the coercion is wrapped to provide a useful toString()
     */
    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion,
                         boolean wrap)
    {
        notNull(sourceType, "sourceType");
        notNull(targetType, "targetType");
        notNull(coercion, "coercion");

        this.sourceType = sourceType;
        this.targetType = targetType;
        this.coercion = wrap ? new CoercionWrapper<S, T>(coercion) : coercion;
    }

    @Override
    public String toString()
    {
        return coercion.toString();
    }

    public Coercion<S, T> getCoercion()
    {
        return coercion;
    }

    public Class<S> getSourceType()
    {
        return sourceType;
    }

    public Class<T> getTargetType()
    {
        return targetType;
    }

}
