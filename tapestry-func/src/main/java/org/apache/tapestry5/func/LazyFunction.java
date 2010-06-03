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

/**
 * A lazy function is used to populate a {@link Flow} incrementally. Lazy functions allow calculations to be
 * deferred. They also support infinitely large Flows, where successive values are computed just as needed.
 * 
 * @since 5.2.0
 */
public interface LazyFunction<T>
{
    /**
     * Calculates the next value for the function. The return value is tricky: it combines the next value in the
     * {@link Flow} with a function to compute the value after that as a {@link LazyContinuation}. Alternately, a
     * LazyFunction can return null to indicate that it has returned all the values it can.
     * 
     * @return continuation containing next value and next function, or null when no more values can be produced
     */
    LazyContinuation<T> next();
}
