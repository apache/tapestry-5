//  Copyright 2008, 2009, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import java.io.IOException;

/**
 * Used to track some set of operations in such a way that a failure (a thrown RuntimeException) will be logged along
 * with a trace of the stack of operations.
 */
public interface OperationTracker
{
    /**
     * Executes the operation.  If the operation throws a {@link RuntimeException} it will be logged and rethrown
     * wrapped as a {@link org.apache.tapestry5.ioc.internal.OperationException}.
     *
     * @param description
     *         used if there is an exception
     * @param operation
     *         to execute
     */
    void run(String description, Runnable operation);

    /**
     * As with {@link #run(String, Runnable)}, but the operation may return a value.
     *
     * @param description
     *         used if there is an exception
     * @param operation
     *         to invoke
     * @return result of operation
     */
    <T> T invoke(String description, Invokable<T> operation);

    /**
     * As with {@link #invoke(String, Invokable)}, but the operation may throw an {@link java.io.IOException}.
     *
     * @param description
     *         used if there is an exception (outside of IOException)
     * @param operation
     *         to perform
     * @return result of operation
     * @since 5.4
     */
    <T> T perform(String description, IOOperation<T> operation) throws IOException;
}
