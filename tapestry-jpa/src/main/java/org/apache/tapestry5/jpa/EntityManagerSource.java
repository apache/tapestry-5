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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.List;

/**
 * Responsible for creating an EntityManager as needed.
 *
 * @since 5.3
 */
public interface EntityManagerSource
{
    /**
     * Creates an EntityManager for the given persistence unit name.
     *
     * @param persistenceUnitName the name of a persistence unit as defined in {@code persistence.xml}
     * @return  EntityManager for the given persistence unit name
     */
    EntityManager create(String persistenceUnitName);

    /**
     * Gets the EntityManagerFactory for the given persistence unit name, creating it as necessary.
     *
     * @param persistenceUnitName the name of a persistence unit as defined in {@code persistence.xml}
     *
     * @return EntityManagerFactory for the given persistence unit name
     */
    EntityManagerFactory getEntityManagerFactory(String persistenceUnitName);

    /**
     * Get the list of {@linkplain PersistenceUnitInfo} parsed from {@code persistence.xml}.
     *
     * @return list of PersistenceUnitInfos
     */
    List<PersistenceUnitInfo> getPersistenceUnitInfos();
}
