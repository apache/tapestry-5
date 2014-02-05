// Copyright 2009, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.internal.services.SessionApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.Request;
import org.hibernate.HibernateException;

import java.io.Serializable;

/**
 * Persists Hibernate entities as SSOs by storing their primary key in the {@link org.apache.tapestry5.services.Session}.
 *
 * @see org.apache.tapestry5.internal.hibernate.PersistedEntity
 */
public class EntityApplicationStatePersistenceStrategy extends SessionApplicationStatePersistenceStrategy
{

    private final org.hibernate.Session hibernateSession;

    public EntityApplicationStatePersistenceStrategy(Request request, org.hibernate.Session hibernateSession)
    {
        super(request);
        this.hibernateSession = hibernateSession;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> ssoClass, ApplicationStateCreator<T> creator)
    {
        final Object persistedValue = getOrCreate(ssoClass, creator);

        if (persistedValue instanceof PersistedEntity)
        {
            final PersistedEntity persisted = (PersistedEntity) persistedValue;

            Object restored = persisted.restore(this.hibernateSession);

            //shall we maybe throw an exception instead?
            if (restored == null)
            {
                set(ssoClass, null);
                return (T) getOrCreate(ssoClass, creator);
            }

            return (T) restored;
        }

        return (T) persistedValue;
    }

    public <T> void set(Class<T> ssoClass, T sso)
    {
        final String key = buildKey(ssoClass);
        Object entity;

        if (sso != null)
        {
            try
            {
                final String entityName = this.hibernateSession.getEntityName(sso);
                final Serializable id = this.hibernateSession.getIdentifier(sso);

                entity = new PersistedEntity(entityName, id);
            } catch (final HibernateException ex)
            {
                // if entity not attached to a Hibernate Session yet, store it as usual sso
                entity = sso;
            }
        } else
        {
            entity = sso;
        }

        getSession().setAttribute(key, entity);
    }

}