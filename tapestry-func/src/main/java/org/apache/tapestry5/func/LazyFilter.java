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

class LazyFilter<T> implements LazyFunction<T>
{
    private final Predicate<? super T> predicate;

    private final Flow<T> flow;

    public LazyFilter(Predicate<? super T> predicate, Flow<T> flow)
    {
        this.predicate = predicate;
        this.flow = flow;
    }

    @Override
    public LazyContinuation<T> next()
    {
        Flow<T> cursor = flow;

        while (!cursor.isEmpty())
        {
            T potential = cursor.first();

            if (predicate.accept(potential))
                return new LazyContinuation<T>(potential, new LazyFilter<T>(predicate, cursor.rest()));
            
            cursor = cursor.rest();
        }

        return null;
    }
}
