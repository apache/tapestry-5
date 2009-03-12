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
import org.apache.tapestry5.internal.hibernate.CommitAfterWorker;
import org.apache.tapestry5.internal.hibernate.EntityPersistentFieldStrategy;
import org.apache.tapestry5.internal.hibernate.HibernateEntityValueEncoder;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.AliasContribution;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;

import java.util.Iterator;

/**
 * Supplements the services defined by {@link org.apache.tapestry5.hibernate.HibernateCoreModule} with additional
 * services and configuration specific to Tapestry web application.
 */
@SuppressWarnings({"JavaDoc"})
public class HibernateModule
{
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateConstants.PROVIDE_ENTITY_VALUE_ENCODERS_SYMBOL, "true");
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


    public static void contributeAlias(Configuration<AliasContribution> configuration, @HibernateCore Session session)
    {
        configuration.add(AliasContribution.create(Session.class, session));
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
            MappedConfiguration<String, PersistentFieldStrategy> configuration)
    {
        configuration.addInstance("entity", EntityPersistentFieldStrategy.class);
    }

    /**
     * Adds the CommitAfter annotation work, to process the {@link org.apache.tapestry5.hibernate.annotations.CommitAfter}
     * annotation.
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration)
    {
        // If logging is enabled, we want logging to be the first advice, wrapping around the commit advice.

        configuration.addInstance("CommitAfter", CommitAfterWorker.class, "after:Log");
    }
    
    /**
     * Contribution to the {@link org.apache.tapestry5.services.ComponentClassResolver} service configuration.
     */
    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("hibernate", "org.apache.tapestry5.hibernate"));
    }

}
