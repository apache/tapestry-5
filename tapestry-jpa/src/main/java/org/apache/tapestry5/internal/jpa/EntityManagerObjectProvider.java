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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.jpa.EntityManagerManager;

public class EntityManagerObjectProvider implements ObjectProvider
{

    private EntityManager proxy;

    /**
     * {@inheritDoc}
     */
    public <T> T provide(final Class<T> objectType, final AnnotationProvider annotationProvider,
            final ObjectLocator locator)
    {
        if (objectType.equals(EntityManager.class))
            return objectType.cast(getOrCreateProxy(annotationProvider, locator));

        return null;
    }

    private synchronized EntityManager getOrCreateProxy(
            final AnnotationProvider annotationProvider, final ObjectLocator objectLocator)
    {
        if (proxy == null)
        {
            final ClassFactory classFactory = objectLocator.getService("ClassFactory",
                    ClassFactory.class);

             final PersistenceContext annotation = annotationProvider
                            .getAnnotation(PersistenceContext.class);

            proxy = classFactory.createProxy(EntityManager.class, new ObjectCreator()
            {
                public Object createObject()
                {
                    final EntityManagerManager entityManagerManager = objectLocator
                            .getService(EntityManagerManager.class);

                    return JpaInternalUtils.getEntityManager(entityManagerManager, annotation);
                }
            }, "<EntityManagerProxy>");
        }

        return proxy;
    }

}
