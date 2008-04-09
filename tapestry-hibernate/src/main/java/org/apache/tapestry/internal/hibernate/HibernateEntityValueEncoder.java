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

package org.apache.tapestry.internal.hibernate;

import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;

import java.io.Serializable;

public final class HibernateEntityValueEncoder<E> implements ValueEncoder<E>
{
    private final Class<E> _entityClass;
    private final Session _session;
    private final TypeCoercer _typeCoercer;
    private final String _idPropertyName;
    private final PropertyAdapter _propertyAdapter;

    public HibernateEntityValueEncoder(Class<E> entityClass, PersistentClass persistentClass, Session session,
                                       PropertyAccess propertyAccess, TypeCoercer typeCoercer)
    {
        _entityClass = entityClass;
        _session = session;
        _typeCoercer = typeCoercer;

        Property property = persistentClass.getIdentifierProperty();

        _idPropertyName = property.getName();

        _propertyAdapter = propertyAccess.getAdapter(_entityClass).getPropertyAdapter(_idPropertyName);
    }


    public String toClient(E value)
    {
        if (value == null) return null;

        Object id = _propertyAdapter.get(value);

        if (id == null)
            throw new IllegalStateException(String.format(
                    "Entity %s has an %s property of null; this probably means that it has not been persisted yet.",
                    value, _idPropertyName));

        return _typeCoercer.coerce(id, String.class);
    }

    @SuppressWarnings("unchecked")
    public E toValue(String clientValue)
    {
        if (InternalUtils.isBlank(clientValue)) return null;

        Object id = _typeCoercer.coerce(clientValue, _propertyAdapter.getType());

        Serializable ser = Defense.cast(id, Serializable.class, "id");

        return (E) _session.get(_entityClass, ser);
    }

}
