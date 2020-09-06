// Copyright 2008, 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.modules;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.hibernate.*;
import org.apache.tapestry5.hibernate.internal.*;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.hibernate.Session;

import java.util.Collection;

/**
 * Defines core services that support initialization of Hibernate and access to the Hibernate {@link
 * org.hibernate.Session}.
 */
@SuppressWarnings({"JavaDoc"})
@Marker(HibernateCore.class)
public class HibernateCoreModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(HibernateTransactionDecorator.class, HibernateTransactionDecoratorImpl.class);
        binder.bind(HibernateTransactionAdvisor.class, HibernateTransactionAdvisorImpl.class);
        binder.bind(HibernateConfigurer.class, DefaultHibernateConfigurer.class).withSimpleId();
        binder.bind(HibernateSessionSource.class, HibernateSessionSourceImpl.class);
    }


    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateSymbols.DEFAULT_CONFIGURATION, "true");
        configuration.add(HibernateSymbols.EARLY_START_UP, "false");
    }

    public static void contributeRegistryStartup(OrderedConfiguration<Runnable> configuration,

                                                 @Symbol(HibernateSymbols.EARLY_START_UP)
                                                 final boolean earlyStartup,

                                                 final HibernateSessionSource sessionSource)
    {
        configuration.add("HibernateStartup", new Runnable()
        {
            @Override
            public void run()
            {
                if (earlyStartup)
                    sessionSource.getConfiguration();
            }
        });
    }

    public static HibernateEntityPackageManager buildHibernateEntityPackageManager(
            final Collection<String> packageNames)
    {
        return new HibernateEntityPackageManager()
        {
            @Override
            public Collection<String> getPackageNames()
            {
                return packageNames;
            }
        };
    }

    /**
     * The session manager manages sessions on a per-thread/per-request basis. Any active transaction will be rolled
     * back at {@linkplain org.apache.tapestry5.ioc.Registry#cleanupThread() thread cleanup time}.  The thread is
     * cleaned up automatically in a Tapestry web application.
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

    /**
     * Adds the following configurers: <dl> <dt>Default <dd> performs default hibernate configuration <dt>PackageName
     * <dd> loads entities by package name</dl>
     */
    public static void contributeHibernateSessionSource(OrderedConfiguration<HibernateConfigurer> config,

                                                        @Local
                                                        HibernateConfigurer defaultHibernateConfigurer)
    {
        config.add("Default", defaultHibernateConfigurer);
        config.addInstance("PackageName", PackageNameHibernateConfigurer.class);
    }
}
