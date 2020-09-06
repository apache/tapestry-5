// Copyright 2008-2014 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.internal;

import org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.hibernate.Session;

import java.io.Serializable;

/**
 * Encapsulates a Hibernate entity name with an entity id.
 */
@ImmutableSessionPersistedObject
public class PersistedEntity implements SessionRestorable
{
    private static final long serialVersionUID = 897120520279686518L;

    private final String entityName;

    private final Serializable id;

    public PersistedEntity(String entityName, Serializable id)
    {
        assert InternalUtils.isNonBlank(entityName);
        assert id != null;

        this.entityName = entityName;
        this.id = id;
    }

    @Override
    public Object restoreWithSession(Session session)
    {
        try
        {
            return session.get(entityName, id);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Failed to load session-persisted entity %s(%s): %s", entityName, id, ex),
                    ex);
        }
    }

    @Override
    public String toString()
    {
        return String.format("<PersistedEntity: %s(%s)>", entityName, id);
    }
}
