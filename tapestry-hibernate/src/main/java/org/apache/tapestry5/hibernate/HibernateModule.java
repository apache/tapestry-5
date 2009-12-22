// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.hibernate.*;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.services.AliasContribution;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"JavaDoc"})
public class HibernateModule
{

    public static void bind(ServiceBinder binder)
    {
        binder.bind(HibernateTransactionDecorator.class, HibernateTransactionDecoratorImpl.class);
        binder.bind(HibernateConfigurer.class, DefaultHibernateConfigurer.class).withId("DefaultHibernateConfigurer");
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateConstants.PROVIDE_ENTITY_VALUE_ENCODERS_SYMBOL, "true");
        configuration.add(HibernateConstants.DEFAULT_CONFIGURATION, "true");
    }

    public static HibernateEntityPackageManager buildHibernateEntityPackageManager(
            final Collection<String> packageNames)
    {
        return new HibernateEntityPackageManager()
        {
            public Collection<String> getPackageNames()
            {
                return packageNames;
            }
        };
    }

    /**
     * Contributes the package "&lt;root&gt;.entities" to the configuration, so that it will be scanned for annotated
     * entity classes.
     */
    public static void contributeHibernateEntityPackageManager(Configuration<String> configuration,

                                                               @Inject
                                                               @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                               String appRootPackage)
    {
        configuration.add(appRootPackage + ".entities");
    }

    /**
     * The session manager manages sessions on a per-thread/per-request basis. A {@link org.hibernate.Transaction} is
     * created initially, and is committed at the end of the request.
     */
    @Scope(ScopeConstants.PERTHREAD)
    public static HibernateSessionManager buildHibernateSessionManager(HibernateSessionSource sessionSource,
                                                                       PerthreadManager perthreadManager)
    {
        HibernateSessionManagerImpl service = new HibernateSessionManagerImpl(sessionSource);

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    public static Session buildSession(HibernateSessionManager sessionManager,
                                       PropertyShadowBuilder propertyShadowBuilder)
    {
        // Here's the thing: the tapestry.hibernate.Session class doesn't have to be per-thread,
        // since
        // it will invoke getSession() on the HibernateSessionManager service (which is per-thread).
        // On
        // first invocation per request,
        // this forces the HSM into existence (which creates the session and begins the
        // transaction).
        // Thus we don't actually create
        // a session until we first try to access it, then the session continues to exist for the
        // rest
        // of the request.

        return propertyShadowBuilder.build(sessionManager, "session", Session.class);
    }

    public static void contributeAlias(Configuration<AliasContribution> configuration, @Local Session session)
    {
        configuration.add(AliasContribution.create(Session.class, session));
    }

    public static HibernateSessionSource buildHibernateSessionSource(Logger logger, List<HibernateConfigurer> config,
                                                                     RegistryShutdownHub hub)
    {
        HibernateSessionSourceImpl hss = new HibernateSessionSourceImpl(logger, config);

        hub.addRegistryShutdownListener(hss);

        return hss;
    }

    /**
     * Adds the following configurers: <dl> <dt>Default <dd> performs default hibernate configuration <dt>PackageName
     * <dd> loads entities by package name</dl>
     */
    public static void contributeHibernateSessionSource(OrderedConfiguration<HibernateConfigurer> config,

                                                        @Local HibernateConfigurer defaultHibernateConfigurer,

                                                        ObjectLocator locator)
    {
        config.add("Default", defaultHibernateConfigurer);
        config.add("PackageName", locator.autobuild(PackageNameHibernateConfigurer.class));
    }

    /**
     * Contributes {@link ValueEncoderFactory}s for all registered Hibernate entity classes. Encoding and decoding are
     * based on the id property value of the entity using type coercion. Hence, if the id can be coerced to a String and
     * back then the entity can be coerced.
     */
    @SuppressWarnings("unchecked")
    public static void contributeValueEncoderSource(MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                                    @Symbol(HibernateConstants.PROVIDE_ENTITY_VALUE_ENCODERS_SYMBOL)
                                                    boolean provideEncoders,
                                                    final HibernateSessionSource sessionSource,
                                                    final Session session,
                                                    final TypeCoercer typeCoercer,
                                                    final PropertyAccess propertyAccess,
                                                    final LoggerSource loggerSource)
    {
        if (!provideEncoders) return;

        org.hibernate.cfg.Configuration config = sessionSource.getConfiguration();
        Iterator<PersistentClass> mappings = config.getClassMappings();
        while (mappings.hasNext())
        {
            final PersistentClass persistentClass = mappings.next();
            final Class entityClass = persistentClass.getMappedClass();

            ValueEncoderFactory factory = new ValueEncoderFactory()
            {
                public ValueEncoder create(Class type)
                {
                    return new HibernateEntityValueEncoder(entityClass, persistentClass, session, propertyAccess,
                                                           typeCoercer, loggerSource.getLogger(entityClass));
                }
            };

            configuration.add(entityClass, factory);
        }
    }

    /**
     * Contributes the following: <dl> <dt>entity</dt> <dd>Stores the id of the entity and reloads from the {@link
     * Session}</dd> </dl>
     */
    public static void contributePersistentFieldManager(
            MappedConfiguration<String, PersistentFieldStrategy> configuration,
            ObjectLocator locator)
    {
        configuration.add("entity", locator.autobuild(EntityPersistentFieldStrategy.class));
    }

    /**
     * Adds the CommitAfter annotation work, to process the {@link org.apache.tapestry5.hibernate.annotations.CommitAfter}
     * annotation.
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration,
            ObjectLocator locator)
    {
        // If logging is enabled, we want logging to be the first advice, wrapping around the commit advice.

        configuration.add("CommitAfter", locator.autobuild(CommitAfterWorker.class), "after:Log");
    }
}
