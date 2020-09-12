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

package org.apache.tapestry5.commons.services;

import java.util.Map;

/**
 * An object which manages a list of {@link org.apache.tapestry5.commons.services.InvalidationListener}s. There are multiple
 * event hub services implementing this interface, each with a specific marker annotation; each can register listeners
 * and fire events; these are based on the type of resource that has been invalidated. Tapestry has built-in support
 * for:
 * <dl>
 * <dt>message catalog resources
 * <dd><a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/services/ComponentMessages.html">ComponentMessages</a> marker annotation
 * <dt>component templates
 * <dd><a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/services/ComponentTemplates.html">ComponentTemplates</a> marker annotation
 * <dt>component classes
 * <dd><a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/services/ComponentClasses.html">ComponentClasses</a> marker annotation
 * </dl>
 *
 * Starting in Tapestry 5.3, these services are disabled in production (it does nothing).
 *
 * @since 5.1.0.0
 */
public interface InvalidationEventHub
{
    /**
     * Adds a listener, who needs to know when an underlying resource of a given category has changed (so that the
     * receiver may discard any cached data that may have been invalidated). Does nothing in production mode.
     *
     * @deprecated in 5.4, use {@link #addInvalidationCallback(Runnable)} instead}
     */
    void addInvalidationListener(InvalidationListener listener);

    /**
     * Adds a callback that is invoked when an underlying tracked resource has changed. Does nothing in production mode.
     *
     * @since  5.4
     */
    void addInvalidationCallback(Runnable callback);

    /**
     * Adds a callback that clears the map.
     *
     * @since 5.4
     */
    void clearOnInvalidation(Map<?,?> map);
}
