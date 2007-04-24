// Copyright 2006 The Apache Software Foundation
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

/**
 * An immutable object that represents a mapping from one type to another. This is also the
 * contribution type when buildign the TypeCoercer service. Wraps a {@link Coercion} object that
 * performs the work with additional properties that describe the input and output types of the
 * coercion, needed when searching for an appropriate coercion (or combination of coercions).
 * 
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

    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercer)
    {
        _sourceType = sourceType;
        _targetType = targetType;
        _coercion = coercer;
    }

    @Override
    public String toString()
    {
        return String.format("CoercionTuple[%s --> %s]", _sourceType.getName(), _targetType
                .getName());
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
