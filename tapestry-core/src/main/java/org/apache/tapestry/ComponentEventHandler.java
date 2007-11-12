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

package org.apache.tapestry;

import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentEvent;

/**
 * Handler for a {@link ComponentEvent}, notified when a non-null value is returned from some event
 * handler method.
 * <p/>
 * TODO: Multiple handlers for different result types / strategy pattern?
 */
public interface ComponentEventHandler<T>
{
    /**
     * Invoked to handle a non-null event handler method result. The handler should determine
     * whether the value is acceptible, and throw an exception if not.
     *
     * @param result            the result value provided by a method
     * @param component         the component from which the result was obtained
     * @param methodDescription a string description of the class and method name (used when errors occur).
     * @return true if the event is aborted, false if the event may continue
     */
    boolean handleResult(T result, Component component, String methodDescription);
}
