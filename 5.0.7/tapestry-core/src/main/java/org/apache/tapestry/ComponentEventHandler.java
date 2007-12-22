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

/**
 * Handler for a a {@linkplain org.apache.tapestry.runtime.Event render phase event) or
 * {@link org.apache.tapestry.runtime.ComponentEvent }, notified when a non-null value is returned from some event
 * handler method.
 */
public interface ComponentEventHandler<T>
{
    /**
     * Invoked to handle a non-null event handler method result. The handler should determine
     * whether the value is acceptible, and throw an exception if not.
     * <p/>
     * <p/>
     * Boolean values are <em>not</em> passed to the handler.  Booleans are used to indicate
     * that the event has been handled (true) or that a further search for handlers
     * should continue (true).  If a component event method returns true, then
     * {@link org.apache.tapestry.runtime.Event#isAborted()} will return true.
     *
     * @param result            the result value provided by a method
     * @param component         the component from which the result was obtained
     * @param methodDescription a string description of the class and method name (used when errors occur).
     * @return true if the event is aborted, false if the event may continue
     */
    boolean handleResult(T result, Component component, String methodDescription);
}
