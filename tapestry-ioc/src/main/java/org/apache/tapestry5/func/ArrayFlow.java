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

/**
 * Implementation of {@link Flow} based on an internal array of objects.
 */
class ArrayFlow<T> implements Flow<T>
{
    private final T[] values;

    private final int start, count;

    // Guarded by this
    private Flow<T> rest;

    @SuppressWarnings("unchecked")
    ArrayFlow(Collection<T> values)
    {
        this((T[]) values.toArray());
    }

    ArrayFlow(T[] values)
    {
        this(values, 0, values.length);
    }

    ArrayFlow(T[] values, int start, int count)
    {
        this.values = values;
        this.start = start;
        this.count = count;
    }

    public Flow<T> each(Worker<? super T> worker)
    {
        for (int i = 0; i < count; i++)
            worker.work(values[start + i]);

        return this;
    }

    public Flow<T> filter(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        if (isEmpty())
            return this;

        List<T> result = new ArrayList<T>(values.length);

        for (int i = 0; i < count; i++)
        {
            T value = values[start + i];

            if (predicate.accept(value))
                result.add(value);
        }

        return new ArrayFlow<T>(result);
    }

    public Flow<T> remove(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        return filter(predicate.invert());
    }

    @SuppressWarnings("unchecked")
    public <X> Flow<X> map(Mapper<T, X> mapper)
    {
        Defense.notNull(mapper, "mapper");

        if (isEmpty())
        {
            List<X> empty = Collections.emptyList();
            return new ArrayFlow<X>(empty);
        }

        X[] newValues = (X[]) new Object[values.length];

        for (int i = 0; i < count; i++)
        {
            T value = values[start + i];
            newValues[i] = mapper.map(value);
        }

        return new ArrayFlow<X>(newValues);
    }

    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        Defense.notNull(reducer, "reducer");

        A accumulator = initial;

        for (int i = 0; i < count; i++)
        {
            T value = values[start + i];

            accumulator = reducer.reduce(accumulator, value);
        }

        return accumulator;
    }

    public List<T> toList()
    {
        if (isEmpty())
            return Collections.emptyList();

        return Arrays.asList(values).subList(start, start + count);
    }

    public Flow<T> reverse()
    {
        if (values.length < 2)
            return this;

        List<T> newValues = CollectionFactory.newList(values);

        Collections.reverse(newValues);

        return new ArrayFlow<T>(newValues);
    }

    public boolean isEmpty()
    {
        return count == 0;
    }

    public Flow<T> concat(Flow<? extends T> other)
    {
        Defense.notNull(other, "other");

        if (other.isEmpty())
            return this;

        List<T> newValues = copy();
        newValues.addAll(other.toList());

        return new ArrayFlow<T>(newValues);
    }

    private List<T> copy()
    {
        List<T> result = new ArrayList<T>(count);

        for (int i = 0; i < count; i++)
        {
            result.add(values[start + i]);
        }

        return result;
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

        return new ArrayFlow<T>((List<T>) newValues);
    }

    public Flow<T> sort(Comparator<? super T> comparator)
    {
        Defense.notNull(comparator, "comparator");

        if (values.length < 2)
            return this;

        List<T> newValues = copy();

        Collections.sort(newValues, comparator);

        return new ArrayFlow<T>(newValues);
    }

    public Iterator<T> iterator()
    {
        // Kind of inefficient but it works.

        return toList().iterator();
    }

    public T first()
    {
        return isEmpty() ? null : values[start];
    }

    public synchronized Flow<T> rest()
    {
        if (rest == null)
            rest = buildRest();

        return rest;
    }

    private Flow<T> buildRest()
    {
        // TODO: A special implementation of an empty FlowImpl would be cool.
        if (isEmpty())
            return this;

        return new ArrayFlow<T>(values, start + 1, count - 1);
    }

}
