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

import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.tapestry5.internal.services.AbstractSessionPersistentFieldStrategy;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.JpaConstants;
import org.apache.tapestry5.services.Request;

/**
 * Persists JPA entities by storing their id in the session.
 */
public class EntityPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy
{
    private final EntityManagerManager entityManagerManager;

    public EntityPersistentFieldStrategy(final EntityManagerManager entityManagerManager,
            final Request request)
    {
        super("entity:", request);

        this.entityManagerManager = entityManagerManager;
    }

    @Override
    protected Object convertApplicationValueToPersisted(final Object newValue)
    {
        final EntityManager em = getEntityManagerFactory(newValue);

        final EntityManagerFactory emf = em.getEntityManagerFactory();

        final Map<String, Object> properties = emf.getProperties();

        final String persistenceUnitName = (String) properties
                .get(JpaConstants.PERSISTENCE_UNIT_NAME);

        final Object id = emf.getPersistenceUnitUtil().getIdentifier(newValue);

        return new PersistedEntity(newValue.getClass(), id, persistenceUnitName);
    }

    @Override
    protected Object convertPersistedToApplicationValue(final Object persistedValue)
    {
        final PersistedEntity persisted = (PersistedEntity) persistedValue;

        return persisted.restore(entityManagerManager);
    }

    private EntityManager getEntityManagerFactory(final Object entity)
    {
        final Map<String, EntityManager> entityManagers = entityManagerManager.getEntityManagers();

        for (final EntityManager em : entityManagers.values())
        {
            final EntityManagerFactory emf = em.getEntityManagerFactory();

            final Metamodel metamodel = emf.getMetamodel();

            final Set<EntityType<?>> entities = metamodel.getEntities();

            for (final EntityType<?> entityType : entities)
            {
                if (entityType.getJavaType() == entity.getClass())
                {
                    if (em.contains(entity)) { return em; }
                }
            }
        }

        throw new IllegalArgumentException(
                String.format(
                        "Failed persisting an entity in the session. The entity '%s' does not belong to any of the existing persistence contexts.",
                        entity));
    }
}
