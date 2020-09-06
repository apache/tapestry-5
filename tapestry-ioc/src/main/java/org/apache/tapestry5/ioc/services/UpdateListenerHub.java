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

import org.apache.tapestry5.commons.ObjectLocator;

/**
 * Manages a set of {@link org.apache.tapestry5.ioc.services.UpdateListener}s. Periodically (say, every request during
 * development, or every minute or so during production), request processing is locked down so that only a single thread
 * is active, and the active thread invokes {@link #fireCheckForUpdates()}. Various services that are dependent on
 * external resource files (such as classes or template files) can check to see if any file they've used has changed. If
 * so, the service can invalidate its internal cache, or notify other services (typically via
 * {@link org.apache.tapestry5.commons.services.InvalidationListener} that they should do the same.
 *
 * Note that this interface has moved from module tapestry-core to tapestry-ioc, but has kept the same package (for
 * backwards compatibility reasons).
 *
 * A <em>weak reference</em> to the listener is kept; this ensures that registering as a listener will not prevent a
 * listener instance from being reclaimed by the garbage collector (this is useful as proxies created by
 * {@link ObjectLocator#proxy(Class, Class)} may register as listeners, but still be ephemeral).
 *
 * Starting in Tapestry 5.3, this services does <em>nothing</em> in production mode.
 * 
 * @since 5.1.0.0
 */
public interface UpdateListenerHub
{
    /**
     * Adds a listener.
     */
    void addUpdateListener(UpdateListener listener);

    /**
     * Invoked periodically to allow services to check if underlying state has changed. For example, a template file may
     * have changed. Listeners will typically notify applicable listeners of their own (they usually implement
     * {@link org.apache.tapestry5.commons.services.InvalidationEventHub}) when such a change occurs.
     */
    void fireCheckForUpdates();
}
