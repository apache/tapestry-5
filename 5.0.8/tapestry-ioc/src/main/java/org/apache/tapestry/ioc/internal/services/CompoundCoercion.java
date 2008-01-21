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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.services.Coercion;

/**
 * Combines two coercions to create a coercsion through an intermediate type.
 *
 * @param <S>
 * The source (input) type
 * @param <I>
 * The intermediate type
 * @param <T>
 * The target (output) type
 */
public class CompoundCoercion<S, I, T> implements Coercion<S, T>
{
    private final Coercion<S, I> _op1;

    private final Coercion<I, T> _op2;

    public CompoundCoercion(Coercion<S, I> op1, Coercion<I, T> op2)
    {
        _op1 = op1;
        _op2 = op2;
    }

    public T coerce(S input)
    {
        // Run the input through the first operation (S --> I), then run the result of that through
        // the second operation (I --> T).

        I intermediate = _op1.coerce(input);

        return _op2.coerce(intermediate);
    }

    @Override
    public String toString()
    {
        return String.format("%s, %s", _op1, _op2);
    }
}
