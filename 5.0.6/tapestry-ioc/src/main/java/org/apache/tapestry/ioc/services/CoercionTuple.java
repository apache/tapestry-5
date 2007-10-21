// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.services;

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

/**
 * An immutable object that represents a mapping from one type to another. This is also the
 * contribution type when buildign the TypeCoercer service. Wraps a {@link Coercion} object that
 * performs the work with additional properties that describe the input and output types of the
 * coercion, needed when searching for an appropriate coercion (or combination of coercions).
 * 
 * @param <S>
 *            source (input) type
 * @param <T>
 *            target (output) type
 */
public final class CoercionTuple<S, T>
{
    private final Class<S> _sourceType;

    private final Class<T> _targetType;

    private final Coercion<S, T> _coercion;

    /**
     * Wraps an arbitrary coercion with an implementation of toString() that identifies the source
     * and target types.
     */
    private class CoercionWrapper<WS, WT> implements Coercion<WS, WT>
    {
        private final Coercion<WS, WT> _coercion;

        public CoercionWrapper(Coercion<WS, WT> coercion)
        {
            _coercion = coercion;
        }

        public WT coerce(WS input)
        {
            return _coercion.coerce(input);
        }

        @Override
        public String toString()
        {
            return String.format("%s --> %s", convert(_sourceType), convert(_targetType));
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
     * @param sourceType
     *            the source (or input) type of the coercion
     * @param targetType
     *            the target (or output) type of the coercion
     * @param coercion
     *            the object that performs the coercion
     * @param wrap
     *            if true, the coercion is wrapped to provide a useful toString()
     */
    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion,
            boolean wrap)
    {
        notNull(sourceType, "sourceType");
        notNull(targetType, "targetType");
        notNull(coercion, "coercion");

        _sourceType = sourceType;
        _targetType = targetType;
        _coercion = wrap ? new CoercionWrapper<S, T>(coercion) : coercion;
    }

    @Override
    public String toString()
    {
        return _coercion.toString();
    }

    public Coercion<S, T> getCoercion()
    {
        return _coercion;
    }

    public Class<S> getSourceType()
    {
        return _sourceType;
    }

    public Class<T> getTargetType()
    {
        return _targetType;
    }

}
