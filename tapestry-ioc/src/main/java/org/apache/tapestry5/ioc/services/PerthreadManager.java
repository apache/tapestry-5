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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.Invokable;

/**
 * Manages per-thread data, and provides a way for listeners to know when such data should be cleaned up. Typically,
 * data is cleaned up at the end of the request (in a web application). Tapestry IoC has any number of objects that need
 * to know when this event occurs, so that they can clean up any per-thread/per-request state.
 */
public interface PerthreadManager
{
    /**
     * Adds a listener to the hub. All listeners are discarded at the {@link #cleanup()}.
     *
     * @param listener
     *         to add
     * @deprecated Deprecated in 5.4, use {@link #addThreadCleanupCallback(Runnable)} instead.
     */
    void addThreadCleanupListener(ThreadCleanupListener listener);

    /**
     * Adds a callback to be invoked when {@link #cleanup()} is invoked; callbacks are then removed.
     *
     * @param callback
     * @since 5.4
     */
    void addThreadCleanupCallback(Runnable callback);

    /**
     * Immediately performs a cleanup of the thread, invoking all callback, then discarding all per-thread data
     * stored by the manager (including the list of callbacks).
     */
    void cleanup();

    /**
     * Creates a value using a unique internal key.
     *
     * @since 5.2.0
     */
    <T> PerThreadValue<T> createValue();

    /**
     * Return {@link ObjectCreator}, which for each thread,
     * the first call will use the delegate {@link ObjectCreator} to create
     * an instance, and later calls will reuse the same per-thread instance. The instance is stored in the
     * {@link org.apache.tapestry5.ioc.services.PerthreadManager} and will be released at the end of the request.
     *
     * @since 5.4
     */
    <T> ObjectCreator<T> createValue(ObjectCreator<T> delegate);

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
