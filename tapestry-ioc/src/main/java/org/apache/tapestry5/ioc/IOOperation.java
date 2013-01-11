// Copyright 2013 The Apache Software Foundation
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
 * An operation that, when performed, returns a value (like {@link Invokable}, but may throw an {@link java.io.IOException}.
 *
 * @since 5.4
 * @see OperationTracker#perform(String, IOOperation)
 */
public interface IOOperation<T>
{
    /** Perform an operation and return a value, or throw the exception. */
    T perform() throws IOException;
}
