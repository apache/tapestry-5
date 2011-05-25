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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.JpaConstants;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.slf4j.Logger;

public class EntityManagerSourceImpl implements EntityManagerSource, RegistryShutdownListener
{
    private final Map<String, EntityManagerFactory> entityManagerFactories = CollectionFactory
            .newMap();

    private final Logger logger;

    private final List<TapestryPersistenceUnitInfo> persistenceUnitInfos;

    public EntityManagerSourceImpl(final Logger logger, final Resource persistenceDescriptor, PersistenceUnitConfigurer packageNamePersistenceUnitConfigurer, 
            final Map<String, PersistenceUnitConfigurer> configuration)
    {
        super();
        this.logger = logger;

        List<TapestryPersistenceUnitInfo> persistenceUnitInfos = null;
        final PersistenceParser parser = new PersistenceParser();

        InputStream inputStream = null;
        try
        {
            inputStream = persistenceDescriptor.openStream();
            persistenceUnitInfos = parser.parse(inputStream);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            InternalUtils.close(inputStream);
        }

         final Map<String, PersistenceUnitConfigurer> remainingConfigurations = configure(configuration, persistenceUnitInfos);

        configureRemaining(persistenceUnitInfos, remainingConfigurations);

        if(persistenceUnitInfos.size() == 1)
            packageNamePersistenceUnitConfigurer.configure(persistenceUnitInfos.get(0));

        this.persistenceUnitInfos = persistenceUnitInfos;
    }

    private Map<String, PersistenceUnitConfigurer> configure(Map<String, PersistenceUnitConfigurer> configuration, List<TapestryPersistenceUnitInfo> persistenceUnitInfos)
    {
        final Map<String, PersistenceUnitConfigurer> remainingConfigurations = CollectionFactory.newMap(configuration);

        for (final TapestryPersistenceUnitInfo info : persistenceUnitInfos)
        {
            final String unitName = info.getPersistenceUnitName();

            final PersistenceUnitConfigurer configurer = configuration.get(unitName);

            if (configurer != null)
            {
                configurer.configure(info);

                remainingConfigurations.remove(unitName) ;
            }
        }

        return remainingConfigurations;
    }


    private void configureRemaining(List<TapestryPersistenceUnitInfo> persistenceUnitInfos, Map<String, PersistenceUnitConfigurer> remainingConfigurations)
    {
        for(Entry<String, PersistenceUnitConfigurer> entry: remainingConfigurations.entrySet())
        {
            final PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl();
            info.setPersistenceUnitName(entry.getKey());

            final PersistenceUnitConfigurer configurer = entry.getValue();
            configurer.configure(info);

            persistenceUnitInfos.add(info);
        }
    }

    /**
     * {@inheritDoc}
     */
    public EntityManagerFactory getEntityManagerFactory(final String persistenceUnitName)
    {
        EntityManagerFactory emf = entityManagerFactories.get(persistenceUnitName);

        if (emf == null)
        {
            emf = createEntityManagerFactory(persistenceUnitName);

            entityManagerFactories.put(persistenceUnitName, emf);
        }

        return emf;
    }

    private EntityManagerFactory createEntityManagerFactory(final String persistenceUnitName)
    {
        final PersistenceProvider persistenceProvider = getPersistenceProvider();

        for (final TapestryPersistenceUnitInfo info : persistenceUnitInfos)
        {
            if (info.getPersistenceUnitName().equals(persistenceUnitName))
            {
                final Map<String, String> properties = CollectionFactory.newCaseInsensitiveMap();
                properties.put(JpaConstants.PERSISTENCE_UNIT_NAME, persistenceUnitName);

                return persistenceProvider.createContainerEntityManagerFactory(info, properties);
            }
        }

        throw new IllegalStateException(String.format(
                "Failed to create EntityManagerFactory for persistence unit '%s'",
                persistenceUnitName));
    }

    private PersistenceProvider getPersistenceProvider()
    {
        final PersistenceProviderResolver resolver = PersistenceProviderResolverHolder
                .getPersistenceProviderResolver();

        final List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        if (providers.isEmpty())
            throw new IllegalStateException(
                    "No PersistenceProvider implementation available in the runtime environment.");

        return providers.get(0);
    }

    public EntityManager create(final String persistenceUnitName)
    {
        return getEntityManagerFactory(persistenceUnitName).createEntityManager();
    }

    public void registryDidShutdown()
    {
        final Set<Entry<String, EntityManagerFactory>> entrySet = entityManagerFactories.entrySet();

        for (final Entry<String, EntityManagerFactory> entry : entrySet)
        {
            final EntityManagerFactory emf = entry.getValue();
            try
            {
                emf.close();
            }
            catch (final Exception e)
            {
                logger.error(String.format(
                        "Failed to close EntityManagerFactory for persistence unit '%s'",
                        entry.getKey()), e);
            }
        }

        entityManagerFactories.clear();

    }

    public List<PersistenceUnitInfo> getPersistenceUnitInfos()
    {
        return Collections.<PersistenceUnitInfo> unmodifiableList(persistenceUnitInfos);
    }

}
