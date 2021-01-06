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

import java.util.Objects;

/**
 * A Tuple holds two values of two different types.
 * 
 * @param <A> first type
 * @param <B> second type
 * @since 5.3
 */
public class Tuple<A, B>
{
    public final A first;

    public final B second;

    public Tuple(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public static <X, Y> Tuple<X, Y> create(X first, Y second)
    {
        return new Tuple<X, Y>(first, second);
    }

    /**
     * Returns the values of the tuple, separated by commas, enclosed in parenthesis. Example:
     * <code>("Ace", "Spades")</code>.
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("(");

        builder.append(String.valueOf(first));
        builder.append(", ");
        builder.append(String.valueOf(second));

        extendDescription(builder);

        return builder.append(')').toString();
    }

    /**
     * Overriden in subclasses to write additional values into the
     * description.
     * 
     * @param builder
     */
    protected void extendDescription(StringBuilder builder)
    {
    }

    /** Utility for comparing two values, either of which may be null. */
    static boolean isEqual(Object left, Object right)
    {
        return left == right || (left != null && left.equals(right));
    }

    /**
     * Compares this Tuple to another object. Equality is defined by: other object is not null,
     * is same class as this Tuple, and all values are themselves equal.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj.getClass() == getClass()))
            return false;

        return isMatch(obj);
    }

    /**
     * Returns a hash code value for the tuple, based on its values.
     *
     * @return a hash code value for this tuple.
     * @since 5.7
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.first, this.second);
    }

    /**
     * The heart of {@link #equals(Object)}; the other object is the same class as this object.
     * 
     * @param other
     *            other tuple to compare
     * @return true if all values stored in tuple match
     */
    protected boolean isMatch(Object other)
    {
        Tuple<?, ?> tuple = (Tuple<?, ?>) other;

        return isEqual(first, tuple.first) && isEqual(second, tuple.second);
    }
}
