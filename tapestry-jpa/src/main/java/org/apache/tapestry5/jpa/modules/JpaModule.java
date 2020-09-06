// Copyright 2011-2014 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.modules;

import java.util.Collection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.internal.jpa.CommitAfterWorker;
import org.apache.tapestry5.internal.jpa.EntityApplicationStatePersistenceStrategy;
import org.apache.tapestry5.internal.jpa.EntityManagerManagerImpl;
import org.apache.tapestry5.internal.jpa.EntityManagerObjectProvider;
import org.apache.tapestry5.internal.jpa.EntityManagerSourceImpl;
import org.apache.tapestry5.internal.jpa.EntityPersistentFieldStrategy;
import org.apache.tapestry5.internal.jpa.EntityTransactionManagerImpl;
import org.apache.tapestry5.internal.jpa.JpaTransactionAdvisorImpl;
import org.apache.tapestry5.internal.jpa.JpaValueEncoder;
import org.apache.tapestry5.internal.jpa.PackageNamePersistenceUnitConfigurer;
import org.apache.tapestry5.internal.jpa.PersistenceContextWorker;
import org.apache.tapestry5.internal.services.PersistentFieldManager;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.jpa.JpaEntityPackageManager;
import org.apache.tapestry5.jpa.JpaPersistenceConstants;
import org.apache.tapestry5.jpa.JpaSymbols;
import org.apache.tapestry5.jpa.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.slf4j.Logger;

/**
 * Defines core services for JPA support.
 *
 * @since 5.3
 */
public class JpaModule
{
    public static void bind(final ServiceBinder binder)
    {
        binder.bind(JpaTransactionAdvisor.class, JpaTransactionAdvisorImpl.class);
        binder.bind(PersistenceUnitConfigurer.class, PackageNamePersistenceUnitConfigurer.class).withSimpleId();
        binder.bind(EntityManagerSource.class, EntityManagerSourceImpl.class);
        binder.bind(EntityTransactionManager.class, EntityTransactionManagerImpl.class);

    }

    public static JpaEntityPackageManager buildJpaEntityPackageManager(final Collection<String> packageNames)
    {
        return new JpaEntityPackageManager()
        {
            @Override
            public Collection<String> getPackageNames()
            {
                return packageNames;
            }
        };
    }

    @Scope(ScopeConstants.PERTHREAD)
    public static EntityManagerManager buildEntityManagerManager(final EntityManagerSource entityManagerSource,
                                                                 final PerthreadManager perthreadManager, final Logger logger)
    {
        final EntityManagerManagerImpl service = new EntityManagerManagerImpl(entityManagerSource, logger);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    @Contribute(JpaEntityPackageManager.class)
    public static void provideEntityPackages(Configuration<String> configuration,

                                             @Symbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                             String appRootPackage)
    {
        configuration.add(appRootPackage + ".entities");
    }

    @Contribute(PersistentFieldManager.class)
    public static void provideEntityPersistentFieldStrategies(
            final MappedConfiguration<String, PersistentFieldStrategy> configuration)
    {
        configuration.addInstance(JpaPersistenceConstants.ENTITY, EntityPersistentFieldStrategy.class);
    }

    @Contribute(ApplicationStatePersistenceStrategySource.class)
    public void provideApplicationStatePersistenceStrategies(
            final MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration)
    {
        configuration.addInstance(JpaPersistenceConstants.ENTITY, EntityApplicationStatePersistenceStrategy.class);
    }

    @Contribute(ComponentClassTransformWorker2.class)
    @Primary
    public static void provideClassTransformWorkers(OrderedConfiguration<ComponentClassTransformWorker2> configuration)
    {
        configuration.addInstance("PersistenceContext", PersistenceContextWorker.class, "after:Property");
        configuration.addInstance("JPACommitAfter", CommitAfterWorker.class, "after:Log");
    }

    @Contribute(MasterObjectProvider.class)
    public static void provideObjectProviders(final OrderedConfiguration<ObjectProvider> configuration)
    {
        configuration.addInstance("EntityManager", EntityManagerObjectProvider.class,
                "before:AnnotationBasedContributions");
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void provideFactoryDefaults(final MappedConfiguration<String, String> configuration)
    {
        configuration.add(JpaSymbols.PROVIDE_ENTITY_VALUE_ENCODERS, "true");
        configuration.add(JpaSymbols.EARLY_START_UP, "true");
        configuration.add(JpaSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED, "true");
        configuration.add(JpaSymbols.PERSISTENCE_DESCRIPTOR, "/META-INF/persistence.xml");
    }

    @Contribute(ValueEncoderSource.class)
    public static void provideValueEncoders(final MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                            @Symbol(JpaSymbols.PROVIDE_ENTITY_VALUE_ENCODERS)
                                            final boolean provideEncoders, final EntityManagerSource entityManagerSource,
                                            final EntityManagerManager entityManagerManager, final TypeCoercer typeCoercer,
                                            final PropertyAccess propertyAccess, final LoggerSource loggerSource)
    {

        if (!provideEncoders)
            return;

        for (final PersistenceUnitInfo info : entityManagerSource.getPersistenceUnitInfos())
        {
            final EntityManagerFactory emf = entityManagerSource.getEntityManagerFactory(info.getPersistenceUnitName());

            final Metamodel metamodel = emf.getMetamodel();

            for (final EntityType<?> entity : metamodel.getEntities())
            {
                final Class<?> javaType = entity.getJavaType();

                final ValueEncoderFactory factory = new ValueEncoderFactory()
                {
                    @Override
                    public ValueEncoder create(final Class type)
                    {
                        return new JpaValueEncoder(entity, entityManagerManager, info.getPersistenceUnitName(),
                                propertyAccess, typeCoercer, loggerSource.getLogger(javaType));
                    }
                };

                configuration.add(javaType, factory);
            }
        }
    }

    @Contribute(ApplicationStateManager.class)
    public static void provideApplicationStateContributions(
            final MappedConfiguration<Class, ApplicationStateContribution> configuration,
            final EntityManagerSource entityManagerSource,
            @Symbol(JpaSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED)
            final boolean entitySessionStatePersistenceStrategyEnabled)
    {

        if (!entitySessionStatePersistenceStrategyEnabled)
            return;

        for (final PersistenceUnitInfo info : entityManagerSource.getPersistenceUnitInfos())
        {
            final EntityManagerFactory emf = entityManagerSource.getEntityManagerFactory(info.getPersistenceUnitName());

            final Metamodel metamodel = emf.getMetamodel();

            for (EntityType<?> entity : metamodel.getEntities())
            {
                configuration.add(entity.getJavaType(), new ApplicationStateContribution(JpaPersistenceConstants.ENTITY));
            }

        }
    }

    @Startup
    public static void startupEarly(final EntityManagerManager entityManagerManager, @Symbol(JpaSymbols.EARLY_START_UP)
    final boolean earlyStartup)
    {
        if (earlyStartup)
        {
            entityManagerManager.getEntityManagers();
        }
    }
}
