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

class LazyTake<T> implements LazyFunction<T>
{
    private final int length;

    private final Flow<T> flow;

    public LazyTake(int length, Flow<T> flow)
    {
        this.length = length;
        this.flow = flow;
    }

    @Override
    public LazyContinuation<T> next()
    {
        if (flow.isEmpty() || length < 1)
            return null;

        return new LazyContinuation<T>(new LazyFirst<T>(flow), new LazyTake<T>(length - 1, flow.rest()));
    }

}
