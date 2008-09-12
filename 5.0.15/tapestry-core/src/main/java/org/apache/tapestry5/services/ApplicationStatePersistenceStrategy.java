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

/**
 * Used by {@link ApplicationStateManager} to manage a specific kind of ASO persistence. The stategy is responsible for
 * managing ASO instances within its domain.
 *
 * @see org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource
 */
public interface ApplicationStatePersistenceStrategy
{
    /**
     * Gets the ASO from the domain. If the ASO does not already exist, it is created and stored, then returned.
     */
    <T> T get(Class<T> asoClass, ApplicationStateCreator<T> creator);

    /**
     * Stores a new ASO, possibly replacing the existing one.
     *
     * @param <T>
     * @param asoClass
     * @param aso      instance to store, or null to delete existing
     */
    <T> void set(Class<T> asoClass, T aso);

    /**
     * Returns true if the ASO already exists, false if null.
     */
    <T> boolean exists(Class<T> asoClass);
}
