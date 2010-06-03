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

package org.apache.tapestry5.func;

/**
 * The result of the evaluation of a {@link LazyFunction}.
 * 
 * @since 5.2.0
 */
public class LazyContinuation<T>
{
    private final LazyValue<T> nextValue;

    private final LazyFunction<T> nextFunction;

    public LazyContinuation(T nextValue, LazyFunction<T> nextFunction)
    {
        this(new StaticValue<T>(nextValue), nextFunction);
    }

    public LazyContinuation(LazyValue<T> nextValue, LazyFunction<T> nextFunction)
    {
        assert nextValue != null;
        assert nextFunction != null;

        this.nextValue = nextValue;
        this.nextFunction = nextFunction;
    }

    /**
     * Returns, indirectly, the next value computed by the lazy function. The LazyValue represents
     * a deferred computation.
     */
    public LazyValue<T> nextValue()
    {
        return nextValue;
    }

    /** Returns a new lazy function that will return the next continuation. */
    public LazyFunction<T> nextFunction()
    {
        return nextFunction;
    }

}
