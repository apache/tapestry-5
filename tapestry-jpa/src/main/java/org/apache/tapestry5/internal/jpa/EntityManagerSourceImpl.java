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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.JpaConstants;
import org.apache.tapestry5.jpa.JpaSymbols;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.slf4j.Logger;

public class EntityManagerSourceImpl implements EntityManagerSource
{
    private final Map<String, EntityManagerFactory> entityManagerFactories = CollectionFactory
            .newMap();

    private final Logger logger;

    private final List<TapestryPersistenceUnitInfo> persistenceUnitInfos;

    public EntityManagerSourceImpl(Logger logger, @Symbol(JpaSymbols.PERSISTENCE_DESCRIPTOR)
    final Resource persistenceDescriptor, @Local
    PersistenceUnitConfigurer packageNamePersistenceUnitConfigurer,
                                   Map<String, PersistenceUnitConfigurer> configuration)
    {
        this.logger = logger;

        List<TapestryPersistenceUnitInfo> persistenceUnitInfos = parsePersistenceUnitInfos(persistenceDescriptor);

        final Map<String, PersistenceUnitConfigurer> remainingConfigurations = configure(configuration, persistenceUnitInfos);

        configureRemaining(persistenceUnitInfos, remainingConfigurations);

        if (persistenceUnitInfos.size() == 1)
        {
            packageNamePersistenceUnitConfigurer.configure(persistenceUnitInfos.get(0));
        } else
        {
            validateUnitInfos(persistenceUnitInfos);
        }

        this.persistenceUnitInfos = persistenceUnitInfos;
    }

    @PostInjection
    public void listenForShutdown(RegistryShutdownHub hub)
    {
        hub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                registryDidShutdown();
            }
        });
    }

    private void validateUnitInfos(List<TapestryPersistenceUnitInfo> persistenceUnitInfos)
    {
        final List<String> affectedUnits = F.flow(persistenceUnitInfos).filter(new Predicate<TapestryPersistenceUnitInfo>()
        {
            @Override
            public boolean accept(TapestryPersistenceUnitInfo info)
            {
                return !info.excludeUnlistedClasses();
            }
        }).map(new Mapper<TapestryPersistenceUnitInfo, String>()
        {
            @Override
            public String map(TapestryPersistenceUnitInfo info)
            {
                return info.getPersistenceUnitName();
            }
        }).toList();

        if (0 < affectedUnits.size())
        {
            throw new RuntimeException(
                    String.format(
                            "Persistence units '%s' are configured to include managed classes that have not been explicitly listed. " +
                                    "This is forbidden when multiple persistence units are used in the same application. " +
                                    "Please configure persistence units to exclude unlisted managed classes (e.g. by removing <exclude-unlisted-classes> element) " +
                                    "and include them explicitly.",
                            InternalUtils.join(affectedUnits)));
        }
    }

    private List<TapestryPersistenceUnitInfo> parsePersistenceUnitInfos(Resource persistenceDescriptor)
    {
        List<TapestryPersistenceUnitInfo> persistenceUnitInfos = CollectionFactory.newList();

        if (persistenceDescriptor.exists())
        {
            final PersistenceParser parser = new PersistenceParser();

            InputStream inputStream = null;
            try
            {
                inputStream = persistenceDescriptor.openStream();
                persistenceUnitInfos = parser.parse(inputStream);
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            } finally
            {
                InternalUtils.close(inputStream);
            }

        }
        return persistenceUnitInfos;
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

                remainingConfigurations.remove(unitName);
            }
        }

        return remainingConfigurations;
    }


    private void configureRemaining(List<TapestryPersistenceUnitInfo> persistenceUnitInfos, Map<String, PersistenceUnitConfigurer> remainingConfigurations)
    {
        for (Entry<String, PersistenceUnitConfigurer> entry : remainingConfigurations.entrySet())
        {
            final PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl(entry.getKey());

            final PersistenceUnitConfigurer configurer = entry.getValue();
            configurer.configure(info);

            persistenceUnitInfos.add(info);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManagerFactory getEntityManagerFactory(final String persistenceUnitName)
    {
        EntityManagerFactory emf = entityManagerFactories.get(persistenceUnitName);

        if (emf == null)
            synchronized (this)
            {
                emf = entityManagerFactories.get(persistenceUnitName);

                if (emf == null)
                {

                    emf = createEntityManagerFactory(persistenceUnitName);

                    entityManagerFactories.put(persistenceUnitName, emf);
                }
            }

        return emf;
    }

    @SuppressWarnings("unchecked")
    EntityManagerFactory createEntityManagerFactory(final String persistenceUnitName)
    {

        for (final TapestryPersistenceUnitInfo info : persistenceUnitInfos)
        {
            if (info.getPersistenceUnitName().equals(persistenceUnitName))
            {
                final Map properties = info.getEntityManagerProperties() == null ? CollectionFactory.newCaseInsensitiveMap() : info.getEntityManagerProperties();
                properties.put(JpaConstants.PERSISTENCE_UNIT_NAME, persistenceUnitName);

                String providerClassName = info.getPersistenceProviderClassName();

                final PersistenceProvider persistenceProvider = getPersistenceProvider(persistenceUnitName, providerClassName);

                return persistenceProvider.createContainerEntityManagerFactory(info, properties);
            }
        }

        throw new IllegalStateException(String.format(
                "Failed to create EntityManagerFactory for persistence unit '%s'",
                persistenceUnitName));
    }

    private PersistenceProvider getPersistenceProvider(final String persistenceUnitName, final String providerClassName)
    {
        final PersistenceProviderResolver resolver = PersistenceProviderResolverHolder
                .getPersistenceProviderResolver();

        final List<PersistenceProvider> providers = resolver.getPersistenceProviders();

        if (providers.isEmpty())
        {
            throw new IllegalStateException(
                    "No PersistenceProvider implementation available in the runtime environment.");
        }

        if(1 < providers.size() && providerClassName == null)
        {
            throw new IllegalStateException(
                    String.format("Persistence providers [%s] are available in the runtime environment " +
                            "but no provider class is defined for the persistence unit %s.", InternalUtils.join(toProviderClasses(providers)), persistenceUnitName));
        }

        if(providerClassName != null)
        {
            return findPersistenceProviderByName(providers, providerClassName);
        }

        return providers.get(0);
    }

    private PersistenceProvider findPersistenceProviderByName(final List<PersistenceProvider> providers, final String providerClassName)
    {
        PersistenceProvider provider = F.flow(providers).filter(new Predicate<PersistenceProvider>() {
            @Override
            public boolean accept(PersistenceProvider next) {
                return next.getClass().getName().equals(providerClassName);
            }
        }).first();

        if(provider == null)
        {
            throw new IllegalStateException(
                    String.format("No persistence provider of type %s found in the runtime environment. " +
                            "Following providers are available: [%s]", providerClassName, InternalUtils.join(toProviderClasses(providers))));
        }

        return provider;
    }

    private List<Class> toProviderClasses(final List<PersistenceProvider> providers)
    {
       return F.flow(providers).map(new Mapper<PersistenceProvider, Class>() {
           @Override
           public Class map(PersistenceProvider element) {
               return element.getClass();
           }
       }).toList();
    }

    @Override
    public EntityManager create(final String persistenceUnitName)
    {
        return getEntityManagerFactory(persistenceUnitName).createEntityManager();
    }

    private void registryDidShutdown()
    {
        final Set<Entry<String, EntityManagerFactory>> entrySet = entityManagerFactories.entrySet();

        for (final Entry<String, EntityManagerFactory> entry : entrySet)
        {
            final EntityManagerFactory emf = entry.getValue();
            try
            {
                emf.close();
            } catch (final Exception e)
            {
                logger.error(String.format(
                        "Failed to close EntityManagerFactory for persistence unit '%s'",
                        entry.getKey()), e);
            }
        }

        entityManagerFactories.clear();

    }

    @Override
    public List<PersistenceUnitInfo> getPersistenceUnitInfos()
    {
        return Collections.<PersistenceUnitInfo>unmodifiableList(persistenceUnitInfos);
    }

}
