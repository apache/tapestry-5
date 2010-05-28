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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

class FlowImpl<T> implements Flow<T>
{
    private final T[] values;

    @SuppressWarnings("unchecked")
    FlowImpl(Collection<T> values)
    {
        this.values = (T[]) values.toArray();
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

        if (isEmpty())
            return this;

        List<T> result = new ArrayList<T>(values.length);

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

        if (isEmpty())
        {
            List<X> empty = Collections.emptyList();
            return new FlowImpl<X>(empty);
        }

        List<X> newValues = new ArrayList<X>(values.length);

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
        if (isEmpty())
            return Collections.emptyList();

        return Arrays.asList(values);
    }

    public Flow<T> reverse()
    {
        if (values.length < 2)
            return this;

        List<T> newValues = CollectionFactory.newList(values);

        Collections.reverse(newValues);

        return new FlowImpl<T>(newValues);
    }

    public boolean isEmpty()
    {
        return values.length == 0;
    }

    public Flow<T> concat(Flow<? extends T> other)
    {
        Defense.notNull(other, "other");

        if (other.isEmpty())
            return this;

        List<T> newValues = copy();
        newValues.addAll(other.toList());

        return new FlowImpl<T>(newValues);
    }

    private List<T> copy()
    {
        return new ArrayList<T>(Arrays.asList(values));
    }

    public Flow<T> concat(List<? extends T> list)
    {
        return concat(F.flow(list));
    }

    public <V extends T> Flow<T> append(V... values)
    {
        return concat(F.flow(values));
    }

    @SuppressWarnings("unchecked")
    public Flow<T> sort()
    {
        if (values.length < 2)
            return this;

        List<Comparable> newValues = (List<Comparable>) copy();

        Collections.sort(newValues);

        return new FlowImpl<T>((List<T>) newValues);
    }

    public Flow<T> sort(Comparator<? super T> comparator)
    {
        Defense.notNull(comparator, "comparator");

        if (values.length < 2)
            return this;

        List<T> newValues = copy();

        Collections.sort(newValues, comparator);

        return new FlowImpl<T>(newValues);
    }

    public Iterator<T> iterator()
    {
        // Kind of inefficient but it works.

        return toList().iterator();
    }

}
