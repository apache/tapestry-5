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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;

/**
 * Optional interface that may be implemented by a service implementation (or even
 * a {@linkplain ObjectLocator#proxy(Class, Class) proxy} to give the service implementation
 * more control over its lifecyle.
 * 
 * @since 5.2.2
 */
public interface ReloadAware
{
    /**
     * Invoked when Tapestry {@linkplain UpdateListenerHub#fireCheckForUpdates() notices
     * that the implementation class has changed}. The existing instance is notified, so that
     * it can cleanly shutdown now, before being re-instantiated. This is necessary when the
     * service implementation retains some form of external resources.
     *
     * In addition, the implementation may request an immediate reload. Normally, reloading of the service is deferred
     * until a method of the proxy object is invoked (this causes the normal just-in-time instantiation of the
     * implementation). When this method returns true, the implementation is re-created immediately. This is most often
     * the case for services that are {@linkplain EagerLoad eagerly loaded} in the first place.
     * 
     * @return true if the service should be reloaded immediately, false if reload should be deferred
     */
    boolean shutdownImplementationForReload();
}
