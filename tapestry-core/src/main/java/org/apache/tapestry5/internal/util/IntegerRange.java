// Copyright 2006 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.util;

import java.util.Iterator;

/**
 * Represents a sequence of integer values, either ascending or descending. The sequence is always inclusive (of the
 * finish value).
 */
public final class IntegerRange implements Iterable<Integer>
{
    private final int start;

    private final int finish;

    private class RangeIterator implements Iterator<Integer>
    {
        private final int increment;

        private int value = start;

        private boolean hasNext = true;

        RangeIterator()
        {
            increment = start < finish ? +1 : -1;
        }

        public boolean hasNext()
        {
            return hasNext;
        }

        public Integer next()
        {
            if (!hasNext) throw new IllegalStateException();

            int result = value;

            hasNext = value != finish;

            value += increment;

            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

    public IntegerRange(final int start, final int finish)
    {
        this.start = start;
        this.finish = finish;
    }

    public int getFinish()
    {
        return finish;
    }

    public int getStart()
    {
        return start;
    }

    @Override
    public String toString()
    {
        return String.format("%d..%d", start, finish);
    }

    /**
     * The main puprose of a range object is to produce an Iterator. Since IntegerRange is iterable, it is useful with
     * the Tapestry Loop component, but also with the Java for loop!
     */
    public Iterator<Integer> iterator()
    {
        return new RangeIterator();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;

        int result = PRIME + finish;

        result = PRIME * result + start;

        return result;
    }

    /**
     * Returns true if the other object is an IntegerRange with the same start and finish values.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final IntegerRange other = (IntegerRange) obj;
        if (finish != other.finish) return false;

        return start == other.start;
    }

}
