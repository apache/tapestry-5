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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

public class DummyEntityManager implements EntityManager
{

    public DummyEntityManager()
    {
        
    }
    
    public void persist(Object entity)
    {
        
    }

    public <T> T merge(T entity)
    {
        return null;
    }

    public void remove(Object entity)
    {
        
    }

    public <T> T find(Class<T> entityClass, Object primaryKey)
    {
        return null;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
    {
        return null;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
    {
        return null;
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
            Map<String, Object> properties)
    {
        return null;
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey)
    {
        return null;
    }

    public void flush()
    {
        
    }

    public void setFlushMode(FlushModeType flushMode)
    {
        
    }

    public FlushModeType getFlushMode()
    {
        return null;
    }

    public void lock(Object entity, LockModeType lockMode)
    {
        
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        
    }

    public void refresh(Object entity)
    {
        
    }

    public void refresh(Object entity, Map<String, Object> properties)
    {
        
    }

    public void refresh(Object entity, LockModeType lockMode)
    {
        
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        
    }

    public void clear()
    {
        
    }

    public void detach(Object entity)
    {
        
    }

    public boolean contains(Object entity)
    {
        return false;
    }

    public LockModeType getLockMode(Object entity)
    {
        return null;
    }

    public void setProperty(String propertyName, Object value)
    {
        
    }

    public Map<String, Object> getProperties()
    {
        return null;
    }

    public Query createQuery(String qlString)
    {
        return null;
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
    {
        return null;
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
    {
        return null;
    }

    public Query createNamedQuery(String name)
    {
        return null;
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
    {
        return null;
    }

    public Query createNativeQuery(String sqlString)
    {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Query createNativeQuery(String sqlString, Class resultClass)
    {
        return null;
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping)
    {
        return null;
    }

    public void joinTransaction()
    {
        
    }

    public <T> T unwrap(Class<T> cls)
    {
        return null;
    }

    public Object getDelegate()
    {
        return null;
    }

    public void close()
    {
        
    }

    public boolean isOpen()
    {
        return false;
    }

    public EntityTransaction getTransaction()
    {
        return null;
    }

    public EntityManagerFactory getEntityManagerFactory()
    {
        return null;
    }

    public CriteriaBuilder getCriteriaBuilder()
    {
        return null;
    }

    public Metamodel getMetamodel()
    {
        return null;
    }
    
}
