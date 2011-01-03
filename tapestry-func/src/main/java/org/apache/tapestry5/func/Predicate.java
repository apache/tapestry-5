// Copyright 2010, 2011 The Apache Software Foundation
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

/**
 * Used when filtering a collection of objects of a given type; the predicate is passed
 * each object in turn, and returns true to include the object in the result collection.
 * <p>
 * The {@link F} class includes a number of Predicate factory methods.
 * 
 * @since 5.2.0
 * @see Flow#filter(Predicate)
 * @see Flow#remove(Predicate)
 */
public abstract class Predicate<T>
{
    /**
     * This method is overridden in subclasses to define which objects the Predicate will accept
     * and which it will reject.
     * 
     * @param element
     *            the element from the flow to be evaluated by the Predicate
     */
    public abstract boolean accept(T element);

    /**
     * Combines this Predicate with another compatible Predicate to form a new Predicate, which is
     * returned. The new Predicate is true only if both of the combined Predicates are true.
     */
    public final Predicate<T> and(final Predicate<? super T> other)
    {
        assert other != null;

        final Predicate<T> left = this;

        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return left.accept(object) && other.accept(object);
            };
        };
    }

    /**
     * Combines this Predicate with another compatible Predicate to form a new Predicate, which is
     * returned. The
     * new Predicate is true if either of the combined Predicates are true.
     */
    public final Predicate<T> or(final Predicate<? super T> other)
    {
        assert other != null;

        final Predicate<T> left = this;

        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return left.accept(object) || other.accept(object);
            };
        };
    }

    /**
     * Inverts this Predicate, returning a new Predicate that inverts the value returned from
     * {@link #accept}.
     */
    public final Predicate<T> invert()
    {
        final Predicate<T> normal = this;

        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return !normal.accept(object);
            };
        };
    }

}
