// Copyright 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.jpa;

/**
 * Used to configure a {@linkplain javax.persistence.spi.PersistenceUnitInfo}, which is used to
 * create an {@linkplain javax.persistence.EntityManagerFactory}.
 * 
 * @since 5.3
 */
public interface PersistenceUnitConfigurer
{
    /**
     * Configures a persistence unit.
     * 
     * @param unitInfo
     *            represents a persistence unit to configure
     */
    void configure(TapestryPersistenceUnitInfo unitInfo);
}
