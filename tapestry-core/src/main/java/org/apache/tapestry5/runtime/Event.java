// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.runtime;

/**
 * The core methods related to event handling. Events used in this way exist to gather data from user code, by invoking
 * user methods and capturing the response. Return values from methods, if non-null, are passed to a {@link
 * org.apache.tapestry5.ComponentEventCallback}. The {@link ComponentEvent} subinterface extends this by providing
 * access to a context, or set of information related to the event, along with additional data used, at runtime, to
 * match events to user code methods.
 */
public interface Event
{
    /**
     * Returns true if the event has been aborted (meaning that the return value from some event handler method was
     * accepted, and processing of the event was terminated).
     *
     * @return true if no further event handler methods should be invoked
     */
    boolean isAborted();

    /**
     * Invoke to identify, to the event, what component and method is being acted upon (used for some kinds of exception
     * reporting).
     *
     * @param methodDescription describes the location (i.e. file name, method name and line number) of the method
     */
    void setMethodDescription(String methodDescription);

    /**
     * Stores a result for the event. Storing a non-null result value may abort the event (at the discretion of the
     * {@link org.apache.tapestry5.ComponentEventCallback}).
     *
     * @param result the result obtained from a method invocations
     * @return true if the event is now aborted
     */
    boolean storeResult(Object result);
}
