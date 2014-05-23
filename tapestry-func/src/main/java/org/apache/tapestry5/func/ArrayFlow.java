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

/**
 * Implementation of {@link Flow} based on an internal array of objects.
 * 
 * @since 5.2.0
 */
class ArrayFlow<T> extends AbstractFlow<T>
{
    private final T[] values;

    private final int start, count;

    // Guarded by this
    private Flow<T> rest;

    /** Creates an ArrayFlow from the values in the other flow. */
    ArrayFlow(Flow<T> flow)
    {
        this(toMutableList(flow));
    }

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

    @Override
    public Flow<T> each(Worker<? super T> worker)
    {
        for (int i = 0; i < count; i++)
            worker.work(values[start + i]);

        return this;
    }

    @Override
    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        assert reducer != null;

        A accumulator = initial;

        for (int i = 0; i < count; i++)
        {
            T value = values[start + i];

            accumulator = reducer.reduce(accumulator, value);
        }

        return accumulator;
    }

    @Override
    public List<T> toList()
    {
        return Arrays.asList(values).subList(start, start + count);
    }

    @Override
    public Flow<T> reverse()
    {
        if (values.length < 2)
            return this;

        List<T> newValues = new ArrayList<T>();

        newValues.addAll(Arrays.asList(values));

        Collections.reverse(newValues);

        return new ArrayFlow<T>(newValues);
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    protected List<T> toMutableList()
    {
        List<T> result = new ArrayList<T>(count);

        for (int i = 0; i < count; i++)
        {
            result.add(values[start + i]);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flow<T> sort()
    {
        if (values.length < 2)
            return this;

        List<Comparable> newValues = (List<Comparable>) toMutableList();

        Collections.sort(newValues);

        return new ArrayFlow<T>((List<T>) newValues);
    }

    @Override
    public Flow<T> sort(Comparator<T> comparator)
    {
        assert comparator != null;

        if (values.length < 2)
            return this;

        List<T> newValues = toMutableList();

        Collections.sort(newValues, comparator);

        return new ArrayFlow<T>(newValues);
    }

    @Override
    public Iterator<T> iterator()
    {
        return toList().iterator();
    }

    @Override
    public T first()
    {
        return values[start];
    }

    @Override
    public synchronized Flow<T> rest()
    {
        if (rest == null)
            rest = buildRest();

        return rest;
    }

    private Flow<T> buildRest()
    {
        if (count < 2)
            return F.emptyFlow();

        return new ArrayFlow<T>(values, start + 1, count - 1);
    }

    @Override
    public int count()
    {
        return count;
    }

    @Override
    public Flow<T> take(int length)
    {
        if (length < 1)
            return F.emptyFlow();

        return new ArrayFlow<T>(values, start, Math.min(count, length));
    }

    @Override
    public Flow<T> drop(int length)
    {
        assert length >= 0;

        if (length == 0)
            return this;

        if (length >= count)
            return F.emptyFlow();

        return new ArrayFlow<T>(values, start + length, count - length);
    }
}
