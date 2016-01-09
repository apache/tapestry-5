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

package org.apache.tapestry5.hibernate.modules;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.hibernate.HibernateCore;
import org.apache.tapestry5.hibernate.HibernatePersistenceConstants;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.hibernate.HibernateSymbols;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.hibernate.CommitAfterWorker;
import org.apache.tapestry5.internal.hibernate.EntityApplicationStatePersistenceStrategy;
import org.apache.tapestry5.internal.hibernate.EntityPersistentFieldStrategy;
import org.apache.tapestry5.internal.hibernate.HibernateEntityValueEncoder;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.dashboard.DashboardManager;
import org.apache.tapestry5.services.dashboard.DashboardTab;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.hibernate.Session;

import java.util.Iterator;

/**
 * Supplements the services defined by {@link org.apache.tapestry5.hibernate.modules.HibernateCoreModule} with additional
 * services and configuration specific to Tapestry web application.
 */
public class HibernateModule
{
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateSymbols.PROVIDE_ENTITY_VALUE_ENCODERS, "true");
        configuration.add(HibernateSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED, "false");
    }

    /**
     * Contributes the package "&lt;root&gt;.entities" to the configuration, so that it will be scanned for annotated
     * entity classes.
     */
    public static void contributeHibernateEntityPackageManager(Configuration<String> configuration,

                                                               @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                               String appRootPackage)
    {
        configuration.add(appRootPackage + ".entities");
    }

    @Contribute(ServiceOverride.class)
    public static void provideInjectableSessionObject(MappedConfiguration<Class, Object> configuration, @HibernateCore
    Session session)
    {
        configuration.add(Session.class, session);
    }

    /**
     * Contributes {@link ValueEncoderFactory}s for all registered Hibernate entity classes. Encoding and decoding are
     * based on the id property value of the entity using type coercion. Hence, if the id can be coerced to a String and
     * back then the entity can be coerced.
     */
    @SuppressWarnings("unchecked")
    public static void contributeValueEncoderSource(MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                                    @Symbol(HibernateSymbols.PROVIDE_ENTITY_VALUE_ENCODERS)
                                                    boolean provideEncoders, final HibernateSessionSource sessionSource, final Session session,
                                                    final TypeCoercer typeCoercer, final PropertyAccess propertyAccess, final LoggerSource loggerSource)
    {
        if (!provideEncoders)
            return;

        for (ClassMetadata classMetadata : sessionSource.getSessionFactory().getAllClassMetadata().values())
        {
            final Class entityClass = classMetadata.getMappedClass();

            if (entityClass != null)
            {
                ValueEncoderFactory factory = new ValueEncoderFactory()
                {
                    @Override
                    public ValueEncoder create(Class type)
                    {
                        return new HibernateEntityValueEncoder(entityClass, classMetadata.getIdentifierPropertyName(), session, propertyAccess,
                                typeCoercer, loggerSource.getLogger(entityClass));
                    }
                };

                configuration.add(entityClass, factory);

            }
        }
    }

    /**
     * Contributes the following:
     * <dl>
     * <dt>entity</dt>
     * <dd>Stores the id of the entity and reloads from the {@link Session}</dd>
     * </dl>
     */
    public static void contributePersistentFieldManager(
            MappedConfiguration<String, PersistentFieldStrategy> configuration)
    {
        configuration.addInstance(HibernatePersistenceConstants.ENTITY, EntityPersistentFieldStrategy.class);
    }

    /**
     * Contributes the following strategy:
     * <dl>
     * <dt>entity</dt>
     * <dd>Stores the id of the entity and reloads from the {@link Session}</dd>
     * </dl>
     */
    public void contributeApplicationStatePersistenceStrategySource(
            MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration)
    {
        configuration
                .addInstance(HibernatePersistenceConstants.ENTITY, EntityApplicationStatePersistenceStrategy.class);
    }

    /**
     * Contributes {@link ApplicationStateContribution}s for all registered Hibernate entity classes.
     *
     * @param configuration
     *         Configuration to contribute
     * @param entitySessionStatePersistenceStrategyEnabled
     *         indicates if contribution should take place
     * @param sessionSource
     *         creates Hibernate session
     */
    public static void contributeApplicationStateManager(
            MappedConfiguration<Class, ApplicationStateContribution> configuration,
            @Symbol(HibernateSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED)
            boolean entitySessionStatePersistenceStrategyEnabled, HibernateSessionSource sessionSource)
    {

        if (!entitySessionStatePersistenceStrategyEnabled)
            return;
    
        for (ClassMetadata classMetadata : sessionSource.getSessionFactory().getAllClassMetadata().values())
        {
            final Class entityClass = classMetadata.getMappedClass();
            configuration.add(entityClass, new ApplicationStateContribution(HibernatePersistenceConstants.ENTITY));
        }
    }

    /**
     * Adds the CommitAfter annotation work, to process the
     * {@link org.apache.tapestry5.hibernate.annotations.CommitAfter} annotation.
     */
    @Contribute(ComponentClassTransformWorker2.class)
    @Primary
    public static void provideCommitAfterAnnotationSupport(
            OrderedConfiguration<ComponentClassTransformWorker2> configuration)
    {
        // If logging is enabled, we want logging to be the first advice, wrapping around the commit advice.

        configuration.addInstance("CommitAfter", CommitAfterWorker.class, "after:Log");
    }

    @Contribute(DashboardManager.class)
    public static void provideHibernateDashboardTab(OrderedConfiguration<DashboardTab> configuration)
    {
        configuration.add("HibernateStatistics", new DashboardTab("Hibernate", "core/HibernateStatistics"), "after:Services");
    }
}
