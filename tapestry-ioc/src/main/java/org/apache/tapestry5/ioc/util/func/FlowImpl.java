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

package org.apache.tapestry5.ioc.util.func;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

class FlowImpl<T> implements Flow<T>
{
    private final List<T> values;

    public FlowImpl(List<T> values)
    {
        this.values = values;
    }

    public Flow<T> each(Worker<? super T> worker)
    {
        F.each(worker, values);

        return this;
    }

    public Flow<T> filter(Predicate<? super T> predicate)
    {
        return new FlowImpl<T>(F.filter(predicate, values));
    }

    public Flow<T> remove(Predicate<? super T> predicate)
    {
        return new FlowImpl<T>(F.remove(predicate, values));
    }

    public <X> Flow<X> map(Mapper<T, X> mapper)
    {
        return new FlowImpl<X>(F.map(mapper, values));
    }

    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        return F.reduce(reducer, initial, values);
    }

    public List<T> toList()
    {
        return Collections.unmodifiableList(values);
    }

    public Flow<T> reverse()
    {
        List<T> newValues = CollectionFactory.newList(values);

        Collections.reverse(newValues);

        return new FlowImpl<T>(newValues);
    }

}
