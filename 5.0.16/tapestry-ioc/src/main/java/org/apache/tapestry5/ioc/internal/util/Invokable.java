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

package org.apache.tapestry5.ioc.internal.util;


/**
 * Similiar to {@link Runnable} execpt that it returns a value. Used by {@link ConcurrentBarrier} to identify the block
 * of code to execute with read/write lock protection.
 *
 * @param <T> the return value type
 */
public interface Invokable<T>
{
    /**
     * Called to produce a value.
     */
    T invoke();
}
