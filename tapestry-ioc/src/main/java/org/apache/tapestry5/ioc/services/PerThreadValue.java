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

package org.apache.tapestry5.ioc.services;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides access to per-thread (and, by extension, per-request) data, managed by the {@link PerthreadManager}.
 * A PerThreadValue stores a particular type of information.
 *
 * @see org.apache.tapestry5.ioc.services.PerthreadManager#createValue()
 * @since 5.2.0
 */
public interface PerThreadValue<T>
{
    /**
     * Is a value stored (even null)?
     */
    boolean exists();

    /**
     * Reads the current per-thread value, or returns null if no value has been stored.
     */
    T get();

    /**
     * Gets the current per-thread value if it exists (even if null), or the defaultValue
     * if no value has been stored.
     */
    T get(T defaultValue);

    /**
     * Sets the current per-thread value, then returns that value.
     */
    T set(T newValue);

    /**
     * If no value is currently stored (checked by {@link #exists()}), the value
     * provided by the supplier function is set and return.
     * Otherwise, the current value is returned.
     *
     * @param fn the value supplier function
     * @return The current (existing or computed) value
     * @throws NullPointerException if the supplier function is null
     * @since 5.8.3
     */
    default T computeIfAbsent(Supplier<? extends T> fn) {
        Objects.requireNonNull(fn);
        if (exists()) {
            return get();
        }

        T newValue = fn.get();
        set(newValue);

        return newValue;
    }

    /**
     * If a value is currently stored (checked by {@link #exists()}), this value
     * is used to compute a new one with the given mapping function.
     * Otherwise, null is returned.
     *
     * @param fn the mapping function to compute the new value
     * @return The new computed value, or null if none was present
     * @throws NullPointerException if the mapping function is null
     * @since 5.8.3
     */
    default T computeIfPresent(Function<? super T, ? extends T> fn) {
        Objects.requireNonNull(fn);
        if (!exists()) {
            return null;
        }
        
        T newValue = fn.apply(get());
        set(newValue);

        return newValue;
    }

    /**
     * Computes a new value with the help of the current one, which is returned.
     *
     * @param fn the mapping function to compute the new value
     * @return The new computed value
     * @throws NullPointerException if the mapping function is null
     * @since 5.8.3
     */
    default T compute(Function<? super T, ? extends T> fn) {
        Objects.requireNonNull(fn);

        T newValue = fn.apply(get());
        set(newValue);

        return newValue;
    }

    /**
     * If a value is set, performs the given action with it, otherwise it
     * does nothing.
     *
     * @param action performed action if a value is set
     * @throws NullPointerException if the action is null
     * @since 5.8.3
     */
    default void ifSet(Consumer<? super T> action) {
        Objects.requireNonNull(action);

        if (!exists()) {
            return;
        }

        action.accept(get());
    }
}
