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
 * Responsible for managing <em>application state objects</em>, objects which persist between requests, but are not tied
 * to any individual page or component. ASOs are also created on demand. ASOs are typically stored in the session, so
 * that they are specific to a particular client.
 * <p/>
 * <em>Application</em> is a bit of a misnomer here, as it implies global state shared by all users of the application.
 * Although that is a possibility, the supported ASO strategies are user-specific, usually storing data in the {@link
 * org.apache.tapestry5.services.Session}.
 * <p/>
 * Tapestry has a built-in default strategy for storing ASOs (in the session) and instantiating them. If desired,
 * contributions to the service configuration can override the default behavior, either specifying an alternate storage
 * strategy, or an alternate {@linkplain org.apache.tapestry5.services.ApplicationStateCreator creation strategy}.
 *
 * @see org.apache.tapestry5.annotations.ApplicationState
 */
@UsesMappedConfiguration(key = Class.class, value = ApplicationStateContribution.class)
public interface ApplicationStateManager
{
    /**
     * For a given class, find the ASO for the class, creating it if necessary. The manager has a configuration that
     * determines how an instance is stored and created as needed. A requested ASO not in the configuration is assumed
     * to be created via a no-args constructor, and stored in the session.
     *
     * @param <T>
     * @param asoClass identifies the ASO to access or create
     * @return the ASO instance
     */
    <T> T get(Class<T> asoClass);

    /**
     * For a given class, find the ASO for the class. The manager has a configuration that determines how an instance is
     * stored.
     *
     * @param <T>
     * @param asoClass identifies the ASO to access or create
     * @return the ASO instance or null if it does not already exist
     */
    <T> T getIfExists(Class<T> asoClass);

    /**
     * Returns true if the ASO already exists, false if it has not yet been created.
     *
     * @param asoClass used to select the ASO
     * @return true if ASO exists, false if null
     */
    <T> boolean exists(Class<T> asoClass);

    /**
     * Stores a new ASO, replacing the existing ASO (if any). Storing the value null will delete the ASO so that it may
     * be re-created later.
     *
     * @param <T>
     * @param asoClass the type of ASO
     * @param aso      the ASO instance
     */
    <T> void set(Class<T> asoClass, T aso);
}
