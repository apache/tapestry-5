// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.integration.app5;

import java.util.Map;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

final class DummyEntityManagerFactory implements EntityManagerFactory
{
    @Override
    public boolean isOpen()
    {
        return false;
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return null;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil()
    {
        return null;
    }

    @Override
    public Metamodel getMetamodel()
    {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder()
    {
        return null;
    }

    @Override
    public Cache getCache()
    {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public EntityManager createEntityManager(Map map)
    {
        return new DummyEntityManager();
    }

    @Override
    public EntityManager createEntityManager()
    {
        return new DummyEntityManager();
    }

    @Override
    public void close()
    {

    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType)
    {
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map)
    {
        return null;
    }

    @Override
    public void addNamedQuery(String name, Query query)
    {

    }

    @Override
    public <T> T unwrap(Class<T> cls)
    {
        return null;
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph)
    {

    }
}
