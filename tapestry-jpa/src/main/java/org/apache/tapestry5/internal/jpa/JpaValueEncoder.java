// Copyright 2011-2013 The Apache Software Foundation
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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.slf4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class JpaValueEncoder<E> implements ValueEncoder<E>
{
    private final EntityType<E> entity;
    private final EntityManagerManager entityManagerManager;
    private final String persistenceUnitName;
    private final TypeCoercer typeCoercer;
    private final Logger logger;
    private final String idPropertyName;
    private final PropertyAdapter propertyAdapter;

    public JpaValueEncoder(final EntityType<E> entity,
            final EntityManagerManager entityManagerManager, final String persistenceUnitName,
            final PropertyAccess propertyAccess, final TypeCoercer typeCoercer, final Logger logger)
    {
        super();
        this.entity = entity;
        this.entityManagerManager = entityManagerManager;
        this.persistenceUnitName = persistenceUnitName;
        this.typeCoercer = typeCoercer;
        this.logger = logger;

        final Type<?> idType = this.entity.getIdType();

        final SingularAttribute<? super E, ?> idAttribute = this.entity.getId(idType.getJavaType());

        idPropertyName = idAttribute.getName();

        propertyAdapter = propertyAccess.getAdapter(entity.getJavaType()).getPropertyAdapter(
                idPropertyName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toClient(final E value)
    {
        if (value == null)
            return null;

        final Object id = propertyAdapter.get(value);

        if (id == null)
        {
            return null;
        }

        return typeCoercer.coerce(id, String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E toValue(final String clientValue)
    {
        if (InternalUtils.isBlank(clientValue))
            return null;

        Object id = null;
        final Class<E> entityClass = entity.getJavaType();

        try
        {

            id = typeCoercer.coerce(clientValue, propertyAdapter.getType());
        }
        catch (final Exception ex)
        {
            throw new RuntimeException(String.format(
                    "Exception converting '%s' to instance of %s (id type for entity %s): %s",
                    clientValue, propertyAdapter.getType().getName(), entityClass.getName(),
                    ExceptionUtils.toMessage(ex)), ex);
        }

        final EntityManager em = entityManagerManager.getEntityManager(persistenceUnitName);

        final E result = em.find(entityClass, id);

        if (result == null)
        {
            // We don't identify the entity type in the message because the logger is based on the
            // entity type.
            logger.error(String.format(
                    "Unable to convert client value '%s' into an entity instance.", clientValue));
        }

        return result;
    }
}
