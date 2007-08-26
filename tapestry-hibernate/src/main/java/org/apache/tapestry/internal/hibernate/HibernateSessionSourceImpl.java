// Copyright 2007 The Apache Software Foundation
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

import java.util.Collection;

import org.apache.tapestry.hibernate.HibernateSessionSource;
import org.apache.tapestry.internal.services.ClassNameLocator;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;

public class HibernateSessionSourceImpl implements HibernateSessionSource
{
    private SessionFactory _sessionFactory;

    public HibernateSessionSourceImpl(Logger logger, Collection<String> packageNames,

    @InjectService("ClassNameLocator")
    ClassNameLocator classNameLocator)
    {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        long startTime = System.currentTimeMillis();

        AnnotationConfiguration configuration = new AnnotationConfiguration();

        // Perform normal configuration.

        configuration.configure();

        for (String packageName : packageNames)
        {
            configuration.addPackage(packageName);

            for (String className : classNameLocator.locateClassNames(packageName))
            {
                try
                {
                    Class entityClass = contextClassLoader.loadClass(className);

                    configuration.addAnnotatedClass(entityClass);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }

        long configurationComplete = System.currentTimeMillis();

        _sessionFactory = configuration.buildSessionFactory();

        long factoryCreated = System.currentTimeMillis();

        logger.info(HibernateMessages.startupTiming(
                configurationComplete - startTime,
                factoryCreated - startTime));

        logger
                .info(HibernateMessages.entityCatalog(_sessionFactory.getAllClassMetadata()
                        .keySet()));

    }

    public Session create()
    {
        return _sessionFactory.openSession();
    }

    public SessionFactory getSessionFactory()
    {
        return _sessionFactory;
    }
}
