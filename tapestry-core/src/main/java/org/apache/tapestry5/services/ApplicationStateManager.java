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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Responsible for managing <em>session state objects</em>, objects which persist between requests, but are not tied to
 * any individual page or component. SSOs are also created on demand. SSOs are typically stored in the session, so that
 * they are specific to a particular client.
 * <p/>
 * The term "Application" is a hold-over from Tapestry 5.0, which used the {@link ApplicationState} annotation, and
 * called them "ASOs" (Application State Objects).
 * <p/>
 * Tapestry has a built-in default strategy for storing SSOs (in the session) and instantiating them. If desired,
 * contributions to the service configuration can override the default behavior, either specifying an alternate storage
 * strategy, or an alternate {@linkplain org.apache.tapestry5.services.ApplicationStateCreator creation strategy}.
 *
 * @see org.apache.tapestry5.annotations.ApplicationState
 */
@UsesMappedConfiguration(key = Class.class, value = ApplicationStateContribution.class)
public interface ApplicationStateManager
{
    /**
     * For a given class, find the SSO for the class, creating it if necessary. The manager has a configuration that
     * determines how an instance is stored and created as needed. A requested SSO not in the configuration is assumed
     * to be created via a no-args constructor, and stored in the session.
     *
     * @param <T>
     * @param ssoClass identifies the SSO to access or create
     * @return the SSO instance
     */
    <T> T get(Class<T> ssoClass);

    /**
     * For a given class, find the SSO for the class. The manager has a configuration that determines how an instance is
     * stored.
     *
     * @param <T>
     * @param ssoClass identifies the SSO to access or create
     * @return the SSO instance or null if it does not already exist
     */
    <T> T getIfExists(Class<T> ssoClass);

    /**
     * Returns true if the SSO already exists, false if it has not yet been created.
     *
     * @param ssoClass used to select the SSO
     * @return true if SSO exists, false if null
     */
    <T> boolean exists(Class<T> ssoClass);

    /**
     * Stores a new SSO, replacing the existing SSO (if any). Storing the value null will delete the SSO so that it may
     * be re-created later.
     *
     * @param <T>
     * @param ssoClass the type of SSO
     * @param SSO      the SSO instance
     */
    <T> void set(Class<T> ssoClass, T SSO);
}
