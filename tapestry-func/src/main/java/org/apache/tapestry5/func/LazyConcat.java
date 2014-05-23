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
    private final Flow<T> left, right;

    public LazyConcat(Flow<T> first, Flow<? extends T> second)
    {
        this.left = first;
        this.right = (Flow<T>) second;
    }

    @Override
    public LazyContinuation<T> next()
    {
        if (left.isEmpty())
        {
            if (right.isEmpty())
                return null;

            return new LazyContinuation<T>(new LazyFirst<T>(right), new LazyWalk<T>(right.rest()));
        }

        return new LazyContinuation<T>(new LazyFirst<T>(left), new LazyConcat<T>(left.rest(), right));
    }

}
