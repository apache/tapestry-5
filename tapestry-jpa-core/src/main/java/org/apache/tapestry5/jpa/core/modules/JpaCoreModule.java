// Copyright 2011-2014, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.core.modules;

import java.util.Collection;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.spi.PersistenceUnitInfo;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.jpa.core.EntityManagerManagerImpl;
import org.apache.tapestry5.internal.jpa.core.EntityManagerObjectProvider;
import org.apache.tapestry5.internal.jpa.core.EntityManagerSourceImpl;
import org.apache.tapestry5.internal.jpa.core.EntityTransactionManagerImpl;
import org.apache.tapestry5.internal.jpa.core.JpaTransactionAdvisorImpl;
import org.apache.tapestry5.internal.jpa.core.PackageNamePersistenceUnitConfigurer;
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
import org.apache.tapestry5.jpa.core.EntityManagerManager;
import org.apache.tapestry5.jpa.core.EntityManagerSource;
import org.apache.tapestry5.jpa.core.EntityTransactionManager;
import org.apache.tapestry5.jpa.core.JpaEntityPackageManager;
import org.apache.tapestry5.jpa.core.JpaCoreSymbols;
import org.apache.tapestry5.jpa.core.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.core.PersistenceUnitConfigurer;
import org.slf4j.Logger;

/**
 * Defines core services for JPA support.
 *
 * @since 5.3
 */
public class JpaCoreModule
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
        configuration.add(JpaCoreSymbols.EARLY_START_UP, "true");
        configuration.add(JpaCoreSymbols.PERSISTENCE_DESCRIPTOR, "/META-INF/persistence.xml");
    }

    @Startup
    public static void startupEarly(final EntityManagerManager entityManagerManager, @Symbol(JpaCoreSymbols.EARLY_START_UP)
    final boolean earlyStartup)
    {
        if (earlyStartup)
        {
            entityManagerManager.getEntityManagers();
        }
    }
}
