// Copyright 2007-2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;

import java.util.List;

public class HibernateSessionSourceImpl implements HibernateSessionSource
{
    private final SessionFactory sessionFactory;

    private final Configuration configuration;

    public HibernateSessionSourceImpl(Logger logger, List<HibernateConfigurer> hibernateConfigurers)
    {
        long startTime = System.currentTimeMillis();

        configuration = new Configuration();

        for (HibernateConfigurer configurer : hibernateConfigurers)
            configurer.configure(configuration);

        long configurationComplete = System.currentTimeMillis();

        sessionFactory = configuration.buildSessionFactory();

        long factoryCreated = System.currentTimeMillis();

        logger.info(String.format("Hibernate startup: %,d ms to configure, %,d ms overall.", configurationComplete - startTime, factoryCreated - startTime));

        logger.info(String.format("Configured Hibernate entities: %s", InternalUtils.joinSorted(sessionFactory.getAllClassMetadata().keySet())));
    }

    @PostInjection
    public void listenForShutdown(RegistryShutdownHub hub)
    {
        hub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                sessionFactory.close();
            }
        });
    }

    @Override
    public Session create()
    {
        return sessionFactory.openSession();
    }

    @Override
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    @Override
    public Configuration getConfiguration()
    {
        return configuration;
    }

}
