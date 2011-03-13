// Copyright 2011 The Apache Software Foundation
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

import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformField;

public class PersistenceUnitWorker implements ComponentClassTransformWorker
{
    private final EntityManagerManager entityManagerManager;

    public PersistenceUnitWorker(final EntityManagerManager entityManagerManager)
    {
        super();
        this.entityManagerManager = entityManagerManager;
    }

    /**
     * {@inheritDoc}
     */
    public void transform(final ClassTransformation transformation,
            final MutableComponentModel model)
    {

        for (final TransformField field : transformation
                .matchFieldsWithAnnotation(PersistenceUnit.class))
        {
            final PersistenceUnit annotation = field.getAnnotation(PersistenceUnit.class);

            field.claim(annotation);

            field.replaceAccess(new FieldValueConduit()
            {

                public Object get()
                {
                    return JpaInternalUtils.getEntityManager(entityManagerManager, annotation);
                }

                public void set(final Object newValue)
                {
                    throw new UnsupportedOperationException(String.format(
                            "It is not possible to assign a new value to '%s' field",
                            field.getName()));

                }
            });
        }

    }

}
