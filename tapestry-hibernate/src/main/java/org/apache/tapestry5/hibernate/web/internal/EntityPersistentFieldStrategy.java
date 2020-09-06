// Copyright 2008-2014 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.internal;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.AbstractSessionPersistentFieldStrategy;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Persists Hibernate entities by storing their id in the session.
 *
 * @see org.apache.tapestry5.hibernate.web.internal.PersistedEntity
 */
public class EntityPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy
{
    private final Session session;

    public EntityPersistentFieldStrategy(Session session, Request request)
    {
        super("entity:", request);

        this.session = session;
    }

    @Override
    public Object convertApplicationValueToPersisted(Object newValue)
    {
        assert newValue != null;

        if (!session.contains(newValue))
        {
            return new PersistedTransientEntity(newValue);
        }

        try
        {
            String entityName = session.getEntityName(newValue);
            Serializable id = session.getIdentifier(newValue);

            return new PersistedEntity(entityName, id);
        } catch (HibernateException ex)
        {
            throw new IllegalArgumentException(String.format("Failed persisting an entity in the session. entity: %s", newValue), ex);
        }
    }

    @Override
    public Object convertPersistedToApplicationValue(Object persistedValue)
    {
        assert persistedValue != null;

        SessionRestorable persisted = (SessionRestorable) persistedValue;

        return persisted.restoreWithSession(session);
    }
}
