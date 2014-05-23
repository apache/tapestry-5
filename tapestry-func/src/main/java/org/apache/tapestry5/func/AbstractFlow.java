// Copyright 2010, 2011, 2012 The Apache Software Foundation
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

import java.lang.reflect.Array;
import java.util.*;

/**
 * Abstract base class for implementations of {@link Flow}. Subclasses typically override some
 * methods for either efficiency, or for the concern they embrace.
 * 
 * @since 5.2.0
 */
abstract class AbstractFlow<T> implements Flow<T>
{
    /**
     * Method limited to just AbstractFlow and its subclasses. Forces a resolve of the entire Flow,
     * and results in a mutable list of the values in the flow.
     */
    protected List<T> toMutableList()
    {
        return toMutableList(this);
    }

    protected static <T> List<T> toMutableList(Flow<T> flow)
    {
        List<T> result = new ArrayList<T>();

        for (T value : flow)
        {
            result.add(value);
        }

        return result;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private Flow<T> current = AbstractFlow.this;

            @Override
            public boolean hasNext()
            {
                return !current.isEmpty();
            }

            @Override
            public T next()
            {
                T next = current.first();

                current = current.rest();

                return next;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("Flows are immutable.");
            }

        };
    }

    @Override
    public Flow<T> concat(Collection<? extends T> collection)
    {
        return concat(F.flow(collection));
    }

    @Override
    public <V extends T> Flow<T> append(V... values)
    {
        return concat(F.flow(values));
    }

    @Override
    public Flow<T> concat(Flow<? extends T> other)
    {
        return F.lazy(new LazyConcat<T>(this, other));
    }

    /** Subclasses may override this for efficiency. */
    @Override
    public Flow<T> each(Worker<? super T> worker)
    {
        assert worker != null;

        for (T value : this)
        {
            worker.work(value);
        }

        return this;
    }

    @Override
    public Flow<T> filter(Predicate<? super T> predicate)
    {
        assert predicate != null;

        return F.lazy(new LazyFilter<T>(predicate, this));
    }

    @Override
    public <X> Flow<X> map(Mapper<T, X> mapper)
    {
        assert mapper != null;

        return F.lazy(new LazyMapper<T, X>(mapper, this));
    }

    @Override
    public <X, Y> Flow<Y> map(Mapper2<T, X, Y> mapper, Flow<? extends X> flow)
    {
        assert mapper != null;
        assert flow != null;

        if (this.isEmpty() || flow.isEmpty())
            return F.emptyFlow();

        return F.lazy(new LazyMapper2<T, X, Y>(mapper, this, flow));
    }

    @Override
    public <A> A reduce(Reducer<A, T> reducer, A initial)
    {
        assert reducer != null;

        A accumulator = initial;

        Flow<T> cursor = this;

        while (!cursor.isEmpty())
        {
            accumulator = reducer.reduce(accumulator, cursor.first());
            cursor = cursor.rest();
        }

        return accumulator;
    }

    @Override
    public <X> Flow<X> mapcat(Mapper<T, Flow<X>> mapper)
    {
        Flow<Flow<X>> flows = map(mapper);

        if (flows.isEmpty())
            return F.emptyFlow();

        return flows.rest().reduce(new Reducer<Flow<X>, Flow<X>>()
        {
            @Override
            public Flow<X> reduce(Flow<X> accumulator, Flow<X> value)
            {
                return accumulator.concat(value);
            }
        }, flows.first());
    }

    @Override
    public Flow<T> remove(Predicate<? super T> predicate)
    {
        assert predicate != null;

        return filter(F.not(predicate));
    }

    @Override
    public Flow<T> reverse()
    {
        if (isEmpty())
            return F.emptyFlow();

        return new ArrayFlow<T>(this).reverse();
    }

    @Override
    public Flow<T> sort()
    {
        if (isEmpty())
            return F.emptyFlow();

        return new ArrayFlow<T>(this).sort();
    }

    @Override
    public Flow<T> sort(Comparator<T> comparator)
    {
        if (isEmpty())
            return F.emptyFlow();

        return new ArrayFlow<T>(this).sort(comparator);
    }

    @Override
    public List<T> toList()
    {
        if (isEmpty())
            return Collections.emptyList();

        return Collections.unmodifiableList(toMutableList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] toArray(Class<T> type)
    {
        assert type != null;

        List<T> list = toMutableList();

        Object array = Array.newInstance(type, list.size());

        return list.toArray((T[]) array);
    }

    @Override
    public int count()
    {
        if (isEmpty()){
            return 0;
        }
        int count = 0;
        for(Flow<T> flow = this; flow != null && !flow.isEmpty(); flow = flow.rest()){
            count++;
        }
        return count;
    }

    @Override
    public Flow<T> take(int length)
    {
        return F.lazy(new LazyTake<T>(length, this));
    }

    @Override
    public Flow<T> drop(int length)
    {
        assert length >= 0;

        if (length == 0)
            return this;

        return F.lazy(new LazyDrop<T>(length, this));
    }

    @Override
    public Set<T> toSet()
    {
        Set<T> set = new HashSet<T>();

        each(F.addToCollection(set));

        return Collections.unmodifiableSet(set);
    }

    @Override
    public <X> ZippedFlow<T, X> zipWith(Flow<X> otherFlow)
    {
        assert otherFlow != null;

        Flow<Tuple<T, X>> tupleFlow = F.lazy(new LazyZip<T, X>(this, otherFlow));

        return ZippedFlowImpl.create(tupleFlow);
    }

    @Override
    public Flow<T> removeNulls()
    {
        return remove(F.isNull());
    }

    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public T first()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flow<T> rest()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flow<T> interleave(Flow<T>... otherFlows)
    {
        List<Flow<T>> allFlows = new ArrayList<Flow<T>>(otherFlows.length + 1);
        allFlows.add(this);

        for (Flow<T> otherFlow : otherFlows)
        {
            allFlows.add(otherFlow);
        }

        return F.lazy(new Interleaver<T>(allFlows));
    }
}
