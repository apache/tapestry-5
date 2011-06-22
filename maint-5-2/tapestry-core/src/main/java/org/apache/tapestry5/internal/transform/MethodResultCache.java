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

package org.apache.tapestry5.internal.transform;

/**
 * Manages a cache value as the result of invoking a no-arguments method.
 */
public interface MethodResultCache
{
    /** Returns true if the cache contains a cached value. May also check to see if the cached value is valid. */
    boolean isCached();

    /** Stores a new cached value for later reference. */
    void set(Object cachedValue);

    /** Returns the previously cached value, if any. */
    Object get();

    /** Resets the cache, discarding the cached value. */
    void reset();
}
