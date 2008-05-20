// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.slf4j.Logger;

import java.io.Serializable;

public final class HibernateEntityValueEncoder<E> implements ValueEncoder<E>
{
    private final Class<E> entityClass;

    private final Session session;

    private final TypeCoercer typeCoercer;

    private final String idPropertyName;

    private final PropertyAdapter propertyAdapter;

    private final Logger logger;

    public HibernateEntityValueEncoder(Class<E> entityClass, PersistentClass persistentClass, Session session,
                                       PropertyAccess propertyAccess, TypeCoercer typeCoercer, Logger logger)
    {
        this.entityClass = entityClass;
        this.session = session;
        this.typeCoercer = typeCoercer;
        this.logger = logger;

        Property property = persistentClass.getIdentifierProperty();

        idPropertyName = property.getName();

        propertyAdapter = propertyAccess.getAdapter(this.entityClass).getPropertyAdapter(idPropertyName);
    }


    public String toClient(E value)
    {
        if (value == null) return null;

        Object id = propertyAdapter.get(value);

        if (id == null)
            throw new IllegalStateException(String.format(
                    "Entity %s has an %s property of null; this probably means that it has not been persisted yet.",
                    value, idPropertyName));

        return typeCoercer.coerce(id, String.class);
    }

    @SuppressWarnings("unchecked")
    public E toValue(String clientValue)
    {
        if (InternalUtils.isBlank(clientValue)) return null;

        Object id = typeCoercer.coerce(clientValue, propertyAdapter.getType());

        Serializable ser = Defense.cast(id, Serializable.class, "id");

        E result = (E) session.get(entityClass, ser);

        if (result == null)
        {
            // We don't identify the entity type in the message because the logger is based on the entity type.
            logger.error(String.format("Unable to convert client value '%s' into an entity instance.", clientValue));
        }

        return result;
    }

}
