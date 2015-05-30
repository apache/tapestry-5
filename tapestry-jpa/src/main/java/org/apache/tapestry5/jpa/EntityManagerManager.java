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
import java.util.Map;

/**
 * Manages <code>EntityManager</code>s for the current thread.
 * An <code>EntityManager</code> is created as needed and closed at the end of each request.
 *
 *
 *
 * The implementation of this service is per-thread.
 *
 * @since 5.3
 */
public interface EntityManagerManager
{
    /**
     * Gets the active EntityManager for this request, creating it as necessary.
     *
     * @param persistenceUnitName the name of a persistence unit as defined in {@code persistence.xml}
     * @return EntityManager for the persistence unit,
     */
    EntityManager getEntityManager(String persistenceUnitName);

    /**
     * Gets all active EntityManagers for this request, creating them as necessary.
     *
     * @return Map in which persistence unit names are associated with EntityManagers
     */
    Map<String, EntityManager> getEntityManagers();
}
