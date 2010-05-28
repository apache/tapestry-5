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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

class FlowImpl<T> implements Flow<T>
{
    private final List<T> values;

    FlowImpl(Collection<T> values)
    {
        this(new ArrayList<T>(values));
    }

    FlowImpl(List<T> values)
    {
        this.values = values;
    }

    public Flow<T> each(Worker<? super T> worker)
    {
        for (T t : values)
        {
            worker.work(t);
        }

        return this;
    }

    public Flow<T> filter(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        List<T> result = new ArrayList<T>(values.size());

        for (T item : values)
        {
            if (predicate.accept(item))
                result.add(item);
        }

        return new FlowImpl<T>(result);
    }

    public Flow<T> remove(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        return filter(predicate.invert());
    }

    public <X> Flow<X> map(Mapper<T, X> mapper)
    {
        Defense.notNull(mapper, "mapper");

        if (values.isEmpty())
        {
            List<X> empty = Collections.emptyList();
            return new FlowImpl<X>(empty);
        }

        List<X> newValues = new ArrayList<X>(values.size());

        for (T value : values)
        {
            newValues.add(mapper.map(value));
        }

        return new FlowImpl<X>(newValues);
    }

    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        Defense.notNull(reducer, "reducer");

        A accumulator = initial;

        for (T value : values)
        {
            accumulator = reducer.reduce(accumulator, value);
        }

        return accumulator;
    }

    /** Returns the values in the flow as an unmodifiable List. */
    public List<T> toList()
    {
        if (values.isEmpty())
            return Collections.emptyList();

        return Collections.unmodifiableList(values);
    }

    public Flow<T> reverse()
    {
        if (values.size() < 2)
            return this;

        List<T> newValues = CollectionFactory.newList(values);

        Collections.reverse(newValues);

        return new FlowImpl<T>(newValues);
    }

    public boolean isEmpty()
    {
        return values.isEmpty();
    }

    public Flow<T> concat(Flow<? extends T> other)
    {
        Defense.notNull(other, "other");

        if (other.isEmpty())
            return this;

        List<T> newValues = new ArrayList<T>(values);
        newValues.addAll(other.toList());

        return new FlowImpl<T>(newValues);
    }

    public Flow<T> concat(List<? extends T> list)
    {
        return concat(F.flow(list));
    }

    public <V extends T> Flow<T> append(V... values)
    {
        return concat(F.flow(values));
    }

}
