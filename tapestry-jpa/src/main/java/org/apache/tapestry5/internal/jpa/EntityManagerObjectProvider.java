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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.jpa.EntityManagerManager;

public class EntityManagerObjectProvider implements ObjectProvider
{
    private Map<String, EntityManager> emProxyByName = new HashMap<String, EntityManager>();

    @Override
    public <T> T provide(final Class<T> objectType, final AnnotationProvider annotationProvider,
                         final ObjectLocator locator)
    {
        if (objectType.equals(EntityManager.class))
            return objectType.cast(getOrCreateProxy(annotationProvider, locator));

        return null;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private EntityManager getOrCreateProxy(final AnnotationProvider annotationProvider,
            final ObjectLocator objectLocator)
    {
        final PersistenceContext annotation = annotationProvider
                .getAnnotation(PersistenceContext.class);
        final String unitName = annotation == null ? null : annotation.unitName();
        EntityManager proxy = emProxyByName.get(unitName);
        if (proxy == null)
            synchronized (this)
            {
                final PlasticProxyFactory proxyFactory = objectLocator.getService(
                        "PlasticProxyFactory", PlasticProxyFactory.class);

                proxy = proxyFactory.createProxy(EntityManager.class, new ObjectCreator()
                {
                    @Override
                    public Object createObject()
                    {
                        final EntityManagerManager entityManagerManager = objectLocator
                                .getService(EntityManagerManager.class);

                        return JpaInternalUtils.getEntityManager(entityManagerManager, annotation);
                    }
                }, "<EntityManagerProxy>");
                emProxyByName.put(unitName, proxy);
            }

        return proxy;
    }

}
