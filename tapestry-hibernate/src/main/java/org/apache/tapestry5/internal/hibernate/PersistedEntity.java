//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.annotations.ImmutableSessionPersistedObject;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Encapsulates a Hibernate entity name with an entity id.
 */
@ImmutableSessionPersistedObject
public class PersistedEntity implements Serializable
{
    private final String entityName;

    private final Serializable id;

    public PersistedEntity(String entityName, Serializable id)
    {
        this.entityName = entityName;
        this.id = id;
    }

    Object restore(Session session)
    {
        try
        {
            return session.get(entityName, id);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(HibernateMessages.sessionPersistedEntityLoadFailure(entityName, id, ex));
        }
    }

    @Override
    public String toString()
    {
        return String.format("<PersistedEntity: %s(%s)>", entityName, id);
    }
}
