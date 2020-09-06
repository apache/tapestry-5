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

package org.apache.tapestry5.internal.jpa;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.jpa.EntityManagerManager;

/**
 * Encapsulates a JPA entity name with an entity id.
 */
@ImmutableSessionPersistedObject
public class PersistedEntity implements Serializable
{
    private static final long serialVersionUID = 897120520279686518L;

    private final Class entityClass;

    private final Object id;

    private final String persistenceUnitName;

    public PersistedEntity(final Class entityClass, final Object id,
            final String persistenceUnitName)
    {
        this.entityClass = entityClass;
        this.id = id;
        this.persistenceUnitName = persistenceUnitName;
    }

    public Object restore(final EntityManagerManager entityManagerManager)
    {
        try
        {
            final EntityManager entityManager = entityManagerManager
                    .getEntityManager(persistenceUnitName);

            return entityManager.find(entityClass, id);
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(String.format(
                    "Failed to load session-persisted entity %s(%s): %s", entityClass.getName(),
                    id, ex));
        }
    }

    @Override
    public String toString()
    {
        return String.format("<PersistedEntity: %s(%s)>", entityClass.getName(), id);
    }
}
