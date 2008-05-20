// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.EagerLoad;

/**
 * Public access to the IoC service registry.
 */
public interface Registry extends ObjectLocator
{
    /**
     * Invoked at the end of a request to discard any thread-specific information accumulated during the current
     * request.
     *
     * @see org.apache.tapestry5.ioc.services.PerthreadManager
     * @see org.apache.tapestry5.ioc.services.ThreadCleanupListener
     */
    void cleanupThread();

    /**
     * Shuts down a Registry instance. Notifies all listeners that the registry has shutdown. Further method invocations
     * on the Registry are no longer allowed, and the Registry instance should be discarded.
     *
     * @see org.apache.tapestry5.ioc.services.RegistryShutdownHub
     * @see org.apache.tapestry5.ioc.services.RegistryShutdownListener
     */
    void shutdown();

    /**
     * Invoked to eagerly load services marked with the {@link EagerLoad} annotation, and to execute all contributions
     * to the Startup service.
     */
    void performRegistryStartup();
}
