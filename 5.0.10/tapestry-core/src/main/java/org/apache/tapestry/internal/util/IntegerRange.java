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

package org.apache.tapestry.internal.util;

import java.util.Iterator;

/**
 * Represents a sequence of integer values, either ascending or descending. The sequence is always
 * inclusive (of the finish value).
 */
public final class IntegerRange implements Iterable<Integer>
{
    private final int _start;

    private final int _finish;

    private class RangeIterator implements Iterator<Integer>
    {
        private final int _increment;

        private int _value = _start;

        private boolean _hasNext = true;

        RangeIterator()
        {
            _increment = _start < _finish ? +1 : -1;
        }

        public boolean hasNext()
        {
            return _hasNext;
        }

        public Integer next()
        {
            if (!_hasNext) throw new IllegalStateException();

            int result = _value;

            _hasNext = _value != _finish;

            _value += _increment;

            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

    public IntegerRange(final int start, final int finish)
    {
        _start = start;
        _finish = finish;
    }

    public int getFinish()
    {
        return _finish;
    }

    public int getStart()
    {
        return _start;
    }

    @Override
    public String toString()
    {
        return String.format("%d..%d", _start, _finish);
    }

    /**
     * The main puprose of a range object is to produce an Iterator. Since IntegerRange is iterable,
     * it is useful with the Tapestry Loop component, but also with the Java for loop!
     */
    public Iterator<Integer> iterator()
    {
        return new RangeIterator();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;

        int result = PRIME + _finish;

        result = PRIME * result + _start;

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
        if (_finish != other._finish) return false;

        return _start == other._start;
    }

}
