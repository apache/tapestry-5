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

class LazyMapper<T, X> implements LazyFunction<X>
{
    private final Mapper<T, X> mapper;

    private final Flow<T> flow;

    public LazyMapper(Mapper<T, X> mapper, Flow<T> flow)
    {
        this.mapper = mapper;
        this.flow = flow;
    }

    @Override
    public LazyContinuation<X> next()
    {
        if (flow.isEmpty())
            return null;

        LazyValue<X> nextValue = new LazyMappedValue<T, X>(flow, mapper);

        return new LazyContinuation<X>(nextValue, new LazyMapper<T, X>(mapper, flow.rest()));
    }

}
