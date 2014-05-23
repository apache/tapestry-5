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

class LazyMappedValue<T, X> implements LazyValue<X>
{
    private final Flow<T> flow;

    private final Mapper<T, X> mapper;

    public LazyMappedValue(Flow<T> input, Mapper<T, X> mapper)
    {
        this.flow = input;
        this.mapper = mapper;
    }

    @Override
    public X get()
    {
        return mapper.map(flow.first());
    }
}
