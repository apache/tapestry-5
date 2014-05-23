// Copyright 2010, 2012 The Apache Software Foundation
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

import java.util.*;

/**
 * The single implementation of {@link ZippedFlow}, that operates by wrapping around an ordinary {@link Flow} of the
 * {@link Tuples} of the zipped flow. In the future, we may create an
 * EmptyZippedFlow implementation as well.
 * 
 * @param <A>
 *            type of first value in tuple
 * @param <B>
 *            type of second value in tuple
 */
class ZippedFlowImpl<A, B> implements ZippedFlow<A, B>
{
    private final Flow<Tuple<A, B>> tupleFlow;

    private ZippedFlowImpl(Flow<Tuple<A, B>> tupleFlow)
    {
        this.tupleFlow = tupleFlow;
    }

    @Override
    public Tuple<Flow<A>, Flow<B>> unzip()
    {
        return Tuple.create(firsts(), seconds());
    }

    static <X, Y> ZippedFlow<X, Y> create(Flow<Tuple<X, Y>> wrappedTupleFlow)
    {
        assert wrappedTupleFlow != null;

        return new ZippedFlowImpl<X, Y>(wrappedTupleFlow);
    }

    @Override
    public ZippedFlow<A, B> filter(Predicate<? super Tuple<A, B>> predicate)
    {
        return create(tupleFlow.filter(predicate));
    }

    @Override
    public ZippedFlow<A, B> remove(Predicate<? super Tuple<A, B>> predicate)
    {
        return create(tupleFlow.remove(predicate));
    }

    @Override
    public ZippedFlow<A, B> each(Worker<? super Tuple<A, B>> worker)
    {
        tupleFlow.each(worker);

        return this;
    }

    @Override
    public List<Tuple<A, B>> toList()
    {
        return tupleFlow.toList();
    }

    @Override
    public Set<Tuple<A, B>> toSet()
    {
        return tupleFlow.toSet();
    }

    @Override
    public ZippedFlow<A, B> reverse()
    {
        return create(tupleFlow.reverse());
    }

    @Override
    public boolean isEmpty()
    {
        return tupleFlow.isEmpty();
    }

    @Override
    public Tuple<A, B> first()
    {
        return tupleFlow.first();
    }

    @Override
    public ZippedFlow<A, B> rest()
    {
        return create(tupleFlow.rest());
    }

    @Override
    public int count()
    {
        return tupleFlow.count();
    }

    @Override
    public ZippedFlow<A, B> sort(Comparator<Tuple<A, B>> comparator)
    {
        return create(tupleFlow.sort(comparator));
    }

    @Override
    public ZippedFlow<A, B> take(int length)
    {
        return create(tupleFlow.take(length));
    }

    @Override
    public ZippedFlow<A, B> drop(int length)
    {
        return create(tupleFlow.drop(length));
    }

    @Override
    public ZippedFlow<A, B> concat(Collection<? extends Tuple<A, B>> collection)
    {
        return create(tupleFlow.concat(collection));
    }

    @Override
    public Iterator<Tuple<A, B>> iterator()
    {
        return tupleFlow.iterator();
    }

    @Override
    public <O> O reduce(Reducer<O, Tuple<A, B>> reducer, O initial)
    {
        return tupleFlow.reduce(reducer, initial);
    }

    @Override
    public <X, Y> ZippedFlow<X, Y> mapTuples(Mapper<Tuple<A, B>, Tuple<X, Y>> mapper)
    {
        return create(tupleFlow.map(mapper));
    }

    @Override
    public Flow<A> firsts()
    {
        return tupleFlow.map(new Mapper<Tuple<A, B>, A>()
        {
            @Override
            public A map(Tuple<A, B> value)
            {
                return value.first;
            }
        });
    }

    @Override
    public ZippedFlow<A, B> removeNulls()
    {
        return create(tupleFlow.removeNulls());
    }

    @Override
    public Flow<B> seconds()
    {
        return tupleFlow.map(new Mapper<Tuple<A, B>, B>()
        {
            @Override
            public B map(Tuple<A, B> value)
            {
                return value.second;
            }
        });
    }

    @Override
    public ZippedFlow<A, B> filterOnFirst(final Predicate<? super A> predicate)
    {
        assert predicate != null;

        return filter(new Predicate<Tuple<A, B>>()
        {
            @Override
            public boolean accept(Tuple<A, B> element)
            {
                return predicate.accept(element.first);
            }
        });
    }

    @Override
    public ZippedFlow<A, B> filterOnSecond(final Predicate<? super B> predicate)
    {
        assert predicate != null;

        return filter(new Predicate<Tuple<A, B>>()
        {
            @Override
            public boolean accept(Tuple<A, B> element)
            {
                return predicate.accept(element.second);
            }
        });
    }

    @Override
    public ZippedFlow<A, B> removeOnFirst(Predicate<? super A> predicate)
    {
        assert predicate != null;

        return filterOnFirst(F.not(predicate));
    }

    @Override
    public ZippedFlow<A, B> removeOnSecond(Predicate<? super B> predicate)
    {
        assert predicate != null;

        return filterOnSecond(F.not(predicate));
    }

    @Override
    public Map<A, B> toMap()
    {
        final Map<A, B> result = new HashMap<A, B>();

        tupleFlow.each(new Worker<Tuple<A, B>>()
        {
            @Override
            public void work(Tuple<A, B> value)
            {
                result.put(value.first, value.second);
            }
        });

        return result;
    }
}
