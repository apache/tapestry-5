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

import org.apache.tapestry5.internal.transform.ReadOnlyComponentFieldConduit;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import javax.persistence.PersistenceContext;

public class PersistenceContextWorker implements ComponentClassTransformWorker2
{
    private final EntityManagerManager entityManagerManager;

    public PersistenceContextWorker(final EntityManagerManager entityManagerManager)
    {
        this.entityManagerManager = entityManagerManager;
    }

    @Override
    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (final PlasticField field : plasticClass
                .getFieldsWithAnnotation(PersistenceContext.class))
        {
            final PersistenceContext annotation = field.getAnnotation(PersistenceContext.class);

            field.claim(annotation);

            field.setConduit(new ReadOnlyComponentFieldConduit(plasticClass.getClassName(), field.getName())
            {
                @Override
                public Object get(Object instance, InstanceContext context)
                {
                    return JpaInternalUtils.getEntityManager(entityManagerManager, annotation);
                }
            });
        }
    }
}
