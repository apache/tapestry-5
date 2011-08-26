// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Invokable;

/**
 * Manages per-thread data, and provides a way for listeners to know when such data should be cleaned up. Typically,
 * data is cleaned up at the end of the request (in a web application). Tapestry IoC has any number of objects that need
 * to know when this event occurs, so that they can clean up any per-thread/per-request state.
 * <p/>
 * Due to <a href="https://issues.apache.org/jira/browse/TAPESTRY-2141">TAPESTRY-2141<a> (and the underlying JDK 1.5 bug
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5025230">5025230</a>), this service has expanded to
 * manage per-thread data (not just end-of-request listeners).
 */
public interface PerthreadManager
{
    /**
     * Adds a listener to the hub. All listeners are discarded at the {@link #cleanup()}.
     *
     * @param listener to add
     */
    void addThreadCleanupListener(ThreadCleanupListener listener);

    /**
     * Immediately performs a cleanup of the thread, notifying all listeners, then discarding all per-thread data
     * stored by the manager.
     */
    void cleanup();

    /**
     * Creates a value using a unique internal key.
     *
     * @since 5.2.0
     */
    <T> PerThreadValue<T> createValue();

    /**
     * Invokes {@link Runnable#run()}, providing a try...finally to {@linkplain #cleanup() cleanup} after.
     *
     * @since 5.2.0
     */
    void run(Runnable runnable);

    /**
     * Returns the result from the invocation, providing a try...finally to {@linkplain #cleanup() cleanup} after.
     */
    <T> T invoke(Invokable<T> invokable);
}
