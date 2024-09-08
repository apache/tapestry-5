// Copyright 2011 The Apache Software Foundation
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

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;

public class DummyEntityManager implements EntityManager
{

    public DummyEntityManager()
    {
        
    }
    
    @Override
    public void persist(Object entity)
    {
        
    }

    @Override
    public <T> T merge(T entity)
    {
        return null;
    }

    @Override
    public void remove(Object entity)
    {
        
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey)
    {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
    {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
    {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
            Map<String, Object> properties)
    {
        return null;
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey)
    {
        return null;
    }

    @Override
    public void flush()
    {
        
    }

    @Override
    public void setFlushMode(FlushModeType flushMode)
    {
        
    }

    @Override
    public FlushModeType getFlushMode()
    {
        return null;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode)
    {
        
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        
    }

    @Override
    public void refresh(Object entity)
    {
        
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties)
    {
        
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode)
    {
        
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        
    }

    @Override
    public void clear()
    {
        
    }

    @Override
    public void detach(Object entity)
    {
        
    }

    @Override
    public boolean contains(Object entity)
    {
        return false;
    }

    @Override
    public LockModeType getLockMode(Object entity)
    {
        return null;
    }

    @Override
    public void setProperty(String propertyName, Object value)
    {
        
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return null;
    }

    @Override
    public Query createQuery(String qlString)
    {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
    {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
    {
        return null;
    }

    @Override
    public Query createNamedQuery(String name)
    {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
    {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString)
    {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Query createNativeQuery(String sqlString, Class resultClass)
    {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping)
    {
        return null;
    }

    @Override
    public void joinTransaction()
    {
        
    }

    @Override
    public <T> T unwrap(Class<T> cls)
    {
        return null;
    }

    @Override
    public Object getDelegate()
    {
        return null;
    }

    @Override
    public void close()
    {
        
    }

    @Override
    public boolean isOpen()
    {
        return false;
    }

    @Override
    public EntityTransaction getTransaction()
    {
        return null;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory()
    {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder()
    {
        return null;
    }

    @Override
    public Metamodel getMetamodel()
    {
        return null;
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) 
    {
        return null;
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) 
    {
        return null;
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) 
    {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) 
    {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) 
    {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) 
    {
        return null;
    }

    @Override
    public boolean isJoinedToTransaction() 
    {
        return false;
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) 
    {
        return null;
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) 
    {
        return null;
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) 
    {
        return null;
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) 
    {
        return null;
    }
    
}
