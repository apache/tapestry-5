// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.tapestry5.services;

/**
 * Used by {@link ApplicationStateManager} to manage a specific kind of Session State Object (SSO)
 * persistence. The
 * strategy is responsible for managing SSO instances within its domain.
 * <p>
 * <em>NOTE: The term "Application" here is a hold-over from Tapestry 5.0, which used the @ApplicationState
 * (deprecated and deleted) annotation, and called them "ASOs" (Application State Objects). This service
 * would be better named "SessionStatePersistenceStrategy" (but renaming it would cause backwards
 * compatibility issues).</em>
 *
 * @see org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource
 */
public interface ApplicationStatePersistenceStrategy
{
    /**
     * Gets the SSO from the domain. If the SSO does not already exist, it is created and stored, then returned.
     */
    <T> T get(Class<T> ssoClass, ApplicationStateCreator<T> creator);

    /**
     * Stores a new SSO, possibly replacing the existing one.
     *
     * @param <T>
     * @param ssoClass
     * @param sso      instance to store, or null to delete existing
     */
    <T> void set(Class<T> ssoClass, T sso);

    /**
     * Returns true if the SSO already exists, false if null.
     */
    <T> boolean exists(Class<T> ssoClass);

    /**
     * Returns the SSO if it exists or null.
     */
    default <T> T getIfExists(Class<T> ssoClass) {
        return exists(ssoClass) ? get(ssoClass, () -> null) : null;
    }
}
