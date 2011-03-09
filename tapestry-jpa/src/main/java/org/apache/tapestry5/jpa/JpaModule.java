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

package org.apache.tapestry5.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.internal.jpa.CommitAfterWorker;
import org.apache.tapestry5.internal.jpa.EntityManagerManagerImpl;
import org.apache.tapestry5.internal.jpa.EntityManagerObjectProvider;
import org.apache.tapestry5.internal.jpa.EntityManagerSourceImpl;
import org.apache.tapestry5.internal.jpa.EntityPersistentFieldStrategy;
import org.apache.tapestry5.internal.jpa.JpaValueEncoder;
import org.apache.tapestry5.internal.jpa.PersistenceUnitWorker;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.slf4j.Logger;

public class JpaModule
{
    public static EntityManagerSource buildEntityManagerSource(final Logger logger,
            final Map<String, PersistenceUnitConfigurer> configuration,
            final RegistryShutdownHub hub)
    {
        final EntityManagerSourceImpl hss = new EntityManagerSourceImpl(logger, configuration);

        hub.addRegistryShutdownListener(hss);

        return hss;
    }

    @Scope(ScopeConstants.PERTHREAD)
    public static EntityManagerManager buildEntityManagerManager(
            final EntityManagerSource entityManagerSource, final PerthreadManager perthreadManager,
            final Logger logger)
    {
        final EntityManagerManagerImpl service = new EntityManagerManagerImpl(entityManagerSource,
                logger);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    @Contribute(PersistentFieldManager.class)
    public static void provideEntityPersistentFieldStrategies(
            final MappedConfiguration<String, PersistentFieldStrategy> configuration)
    {
        configuration.addInstance("entity", EntityPersistentFieldStrategy.class);
    }

    @Contribute(ComponentClassTransformWorker.class)
    public static void provideComponentClassTransformWorkers(
            final OrderedConfiguration<ComponentClassTransformWorker> configuration)
    {

        configuration.addInstance("JPACommitAfter", CommitAfterWorker.class, "after:Log");
    }

    @Contribute(MasterObjectProvider.class)
    public static void provideObjectProviders(
            final OrderedConfiguration<ObjectProvider> configuration)
    {
        configuration.addInstance("EntityManager", EntityManagerObjectProvider.class,
                "before:AnnotationBasedContributions");
    }

    @Contribute(ComponentClassTransformWorker.class)
    public static void perovideComponentClassTransformWorker(
            final OrderedConfiguration<ComponentClassTransformWorker> configuration)
    {
        configuration.addInstance("PersistenceUnit", PersistenceUnitWorker.class, "after:Property");
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void provideFactoryDefaults(
            final MappedConfiguration<String, String> configuration)
    {
        configuration.add(JpaSymbols.PROVIDE_ENTITY_VALUE_ENCODERS, "true");
        configuration.add(JpaSymbols.EARLY_START_UP, "true");
    }

    @Contribute(ValueEncoderSource.class)
    public static void provideValueEncoders(
            final MappedConfiguration<Class, ValueEncoderFactory> configuration,
            @Symbol(JpaSymbols.PROVIDE_ENTITY_VALUE_ENCODERS)
            final boolean provideEncoders, final EntityManagerSource entityManagerSource,
            final EntityManagerManager entityManagerManager, final TypeCoercer typeCoercer,
            final PropertyAccess propertyAccess, final LoggerSource loggerSource)
    {

        if (!provideEncoders)
            return;

        for (final PersistenceUnitInfo info : entityManagerSource.getPersistenceUnitInfos())
        {
            final EntityManagerFactory emf = entityManagerSource.getEntityManagerFactory(info
                    .getPersistenceUnitName());

            for (final String className : info.getManagedClassNames())
            {
                final Metamodel metamodel = emf.getMetamodel();

                final Class<?> clazz = loadClass(info, className);

                final EntityType<?> entity = metamodel.entity(clazz);

                final ValueEncoderFactory factory = new ValueEncoderFactory()
                {
                    public ValueEncoder create(final Class type)
                    {
                        return new JpaValueEncoder(entity, entityManagerManager,
                                info.getPersistenceUnitName(), propertyAccess, typeCoercer,
                                loggerSource.getLogger(clazz));
                    }
                };

                configuration.add(clazz, factory);
            }
        }
    }

    @Startup
    public static void startupEarly(final EntityManagerSource entityManagerSource,
            @Symbol(JpaSymbols.EARLY_START_UP)
            final boolean earlyStartup)
    {
        if (!earlyStartup)
            return;

        for (final PersistenceUnitInfo info : entityManagerSource.getPersistenceUnitInfos())
        {
            entityManagerSource.create(info.getPersistenceUnitName());
        }

    }

    private static Class loadClass(final PersistenceUnitInfo info, final String className)
    {
        try
        {
            return info.getClassLoader().loadClass(className);
        }
        catch (final ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
