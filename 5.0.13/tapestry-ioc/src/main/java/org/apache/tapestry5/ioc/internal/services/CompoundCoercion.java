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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.Coercion;

/**
 * Combines two coercions to create a coercsion through an intermediate type.
 *
 * @param <S> The source (input) type
 * @param <I> The intermediate type
 * @param <T> The target (output) type
 */
public class CompoundCoercion<S, I, T> implements Coercion<S, T>
{
    private final Coercion<S, I> op1;

    private final Coercion<I, T> op2;

    public CompoundCoercion(Coercion<S, I> op1, Coercion<I, T> op2)
    {
        this.op1 = op1;
        this.op2 = op2;
    }

    public T coerce(S input)
    {
        // Run the input through the first operation (S --> I), then run the result of that through
        // the second operation (I --> T).

        I intermediate = op1.coerce(input);

        return op2.coerce(intermediate);
    }

    @Override
    public String toString()
    {
        return String.format("%s, %s", op1, op2);
    }
}
