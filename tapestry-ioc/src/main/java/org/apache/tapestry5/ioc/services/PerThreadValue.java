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
}
