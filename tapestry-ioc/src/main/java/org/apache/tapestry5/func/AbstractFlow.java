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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * Abstract base class for implementations of {@link Flow}. Subclasses typically override some methods
 * for either efficiency, or for the concern they embrace.
 * 
 * @since 5.2.0
 */
abstract class AbstractFlow<T> implements Flow<T>
{
    private <X> X nyi()
    {
        throw new RuntimeException("Not yet implemented.");
    }

    /**
     * Method limited to just AbstractFlow and its subclasses. Forces a resolve of the entire Flow,
     * and results in a mutable list of the values in the flow.
     */
    List<T> toMutableList()
    {
        ArrayList<T> result = new ArrayList<T>();

        Flow<T> cursor = this;

        while (!cursor.isEmpty())
        {
            result.add(cursor.first());
            cursor = cursor.rest();
        }

        return result;
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private Flow<T> current = AbstractFlow.this;

            public boolean hasNext()
            {
                return !current.isEmpty();
            }

            public T next()
            {
                T next = current.first();

                current = current.rest();

                return next;
            }

            public void remove()
            {
                throw new UnsupportedOperationException("Flows are immutable.");
            }

        };
    }

    public Flow<T> concat(List<? extends T> list)
    {
        return concat(F.flow(list));
    }

    public <V extends T> Flow<T> append(V... values)
    {
        return concat(F.flow(values));
    }

    public Flow<T> concat(Flow<? extends T> other)
    {
        // Possible optimization is to check for EmptyFlow here (but not isEmpty(),
        // so as not to prematurely realize values in the Flow).
        return new ConcatFlow<T>(this, other);
    }

    /** Subclasses may override this for efficiency. */
    public Flow<T> each(Worker<? super T> worker)
    {
        Flow<T> cursor = this;
        while (!cursor.isEmpty())
        {
            worker.work(cursor.first());
            cursor = cursor.rest();
        }

        return this;
    }

    public Flow<T> filter(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        return new FilteredFlow<T>(predicate, this);
    }

    public <X> Flow<X> map(Mapper<T, X> mapper)
    {
        Defense.notNull(mapper, "mapper");

        return new MappedFlow<T, X>(mapper, this);
    }

    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        Defense.notNull(reducer, "reducer");

        A accumulator = initial;

        Flow<T> cursor = this;

        while (!cursor.isEmpty())
        {
            accumulator = reducer.reduce(accumulator, cursor.first());
            cursor = cursor.rest();
        }

        return accumulator;
    }

    public Flow<T> remove(Predicate<? super T> predicate)
    {
        Defense.notNull(predicate, "predicate");

        return filter(predicate.invert());
    }

    public Flow<T> reverse()
    {
        return nyi();
    }

    public Flow<T> sort()
    {
        return nyi();
    }

    public Flow<T> sort(Comparator<? super T> comparator)
    {
        return nyi();
    }

    public List<T> toList()
    {
        if (isEmpty())
            return Collections.emptyList();

        return Collections.unmodifiableList(toMutableList());
    }
}
