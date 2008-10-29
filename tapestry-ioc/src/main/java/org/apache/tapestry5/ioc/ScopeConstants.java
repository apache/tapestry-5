//  Copyright 2008 The Apache Software Foundation
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


/**
 * Defines constants for built-in scopes (used with the {@link org.apache.tapestry5.ioc.annotations.Scope} annotation.
 */
public class ScopeConstants
{
    /**
     * The default scope is a singleton within the {@link org.apache.tapestry5.ioc.Registry}. A single instance will be
     * created on demand.  The lifespan of the instance lasts until the registry is {@linkplain Registry#shutdown() shut
     * down}. <p/>Some implementations will want to know when the Registry is shutdown; they should register for
     * notifications via the {@link org.apache.tapestry5.ioc.services.RegistryShutdownHub} service.
     */
    public static final String DEFAULT = "singleton";

    /**
     * An alternate scope provided with Tapestry; a per-thread instance is created on demand, behind a shared proxy.
     * Method invocations on the shared proxy are forwarded to the per-thread instance. Each per-thread instance lasts
     * until the {@linkplain Registry#cleanupThread() thread is cleaned up} (at the end of a request for a web
     * application). <p>Some implementations will want to be notified before being discarded and should register with
     * the {@link org.apache.tapestry5.ioc.services.PerthreadManager} to receive notifications.
     *
     * @see org.apache.tapestry5.ioc.internal.services.PerThreadServiceLifecycle
     */
    public static final String PERTHREAD = "perthread";
}
