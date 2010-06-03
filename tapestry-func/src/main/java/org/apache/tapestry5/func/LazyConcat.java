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

@SuppressWarnings("unchecked")
class LazyConcat<T> implements LazyFunction<T>
{
    private final Flow<T> first, second;

    public LazyConcat(Flow<T> first, Flow<? extends T> second)
    {
        this.first = first;
        this.second = (Flow<T>) second;
    }

    public LazyContinuation<T> next()
    {
        if (first.isEmpty())
        {
            if (second.isEmpty())
                return null;

            return new LazyContinuation<T>(second.first(), new LazyWalk<T>(second.rest()));
        }

        return new LazyContinuation<T>(first.first(), new LazyConcat<T>(first.rest(), second));
    }

}
