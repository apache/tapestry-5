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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.slf4j.Logger;

public class EntityManagerManagerImpl implements EntityManagerManager, ThreadCleanupListener
{
    private final EntityManagerSource entityManagerSource;

    private final Logger logger;

    private final Map<String, EntityManager> entityManagers = CollectionFactory.newMap();

    public EntityManagerManagerImpl(final EntityManagerSource entityManagerSource,
            final Logger logger)
    {
        super();
        this.entityManagerSource = entityManagerSource;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManager getEntityManager(final String persistenceUnitName)
    {
        return getOrCreateEntityManager(persistenceUnitName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, EntityManager> getEntityManagers()
    {
        createAllEntityManagers();
        
        return Collections.unmodifiableMap(entityManagers);
    }

    private void createAllEntityManagers()
    {
        for (final PersistenceUnitInfo info : entityManagerSource.getPersistenceUnitInfos())
        {
            getOrCreateEntityManager(info.getPersistenceUnitName());
        }
    }

    private EntityManager getOrCreateEntityManager(final String persistenceUnitName)
    {
        EntityManager em = entityManagers.get(persistenceUnitName);

        if (em == null)
        {
            em = entityManagerSource.create(persistenceUnitName);

            entityManagers.put(persistenceUnitName, em);
        }

        return em;
    }

    @Override
    public void threadDidCleanup()
    {
        for (final Entry<String, EntityManager> next : entityManagers.entrySet())
        {
            try
            {
                final EntityManager em = next.getValue();

                if (em.isOpen())
                {
                    em.close();
                }
            }
            catch (final Exception e)
            {
                logger.info(String.format(
                        "Failed to close EntityManager for persistence unit '%s'", next.getKey()));
            }
        }

        entityManagers.clear();

    }

}
