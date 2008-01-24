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

package org.apache.tapestry.hibernate;

import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.hibernate.DefaultHibernateConfigurer;
import org.apache.tapestry.internal.hibernate.HibernateSessionManagerImpl;
import org.apache.tapestry.internal.hibernate.HibernateSessionSourceImpl;
import org.apache.tapestry.internal.hibernate.PackageNameHibernateConfigurer;
import org.apache.tapestry.internal.services.ClassNameLocator;
import org.apache.tapestry.ioc.Configuration;
import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.services.AliasContribution;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

public class HibernateModule
{

    public static HibernateEntityPackageManager build(final Collection<String> packageNames)
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
     * Contributes the package "<em>root-package</em>.entities" to the configuration, so that it will be scanned for
     * annotated entity classes.
     */
    public static void contributeHibernateEntityPackageManager(Configuration<String> configuration,

                                                               @Inject
                                                               @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                               String appRootPackage)
    {
        configuration.add(appRootPackage + ".entities");
    }

    /**
     * The session manager manages sessions on a per-thread/per-request basis. A {@link Transaction} is created
     * initially, and is committed at the end of the request.
     */
    @Scope(PERTHREAD_SCOPE)
    public static HibernateSessionManager build(HibernateSessionSource sessionSource, ThreadCleanupHub threadCleanupHub)
    {
        HibernateSessionManagerImpl service = new HibernateSessionManagerImpl(sessionSource);

        threadCleanupHub.addThreadCleanupListener(service);

        return service;
    }

    public static Session build(HibernateSessionManager sessionManager, PropertyShadowBuilder propertyShadowBuilder)
    {
        // Here's the thing: the tapestry.hibernate.Session class doesn't have to be per-thread,
        // since it will invoke getSession() on the HibernateSessionManager service (which is per-thread).
        // On first invocation per request,
        // this forces the HSM into existence (which creates the session and begins the
        // transaction). Thus we don't actually create
        // a session until we first try to access it, then the session continues to exist for the
        // rest of the request.

        return propertyShadowBuilder.build(sessionManager, "session", Session.class);
    }

    /**
     * Contributes the {@link #build(HibernateSessionManager, PropertyShadowBuilder) Session} service.
     */
    public static void contributeAlias(Configuration<AliasContribution> configuration,

                                       @InjectService("Session")
                                       Session session)
    {
        configuration.add(AliasContribution.create(Session.class, session));
    }

    public static HibernateSessionSource build(Logger log, List<HibernateConfigurer> config)
    {
        return new HibernateSessionSourceImpl(log, config);
    }

    /**
     * Adds the following configurers: <dl> <dt>Default</dt> <dd>Performs default hibernate configuration</dd>
     * <dt>PackageName</dt> <dd>Loads entities by package name</dd> </ul>
     */
    public static void contributeHibernateSessionSource(OrderedConfiguration<HibernateConfigurer> config,
                                                        final ClassNameLocator classNameLocator,
                                                        final HibernateEntityPackageManager packageManager)
    {
        config.add("Default", new DefaultHibernateConfigurer());
        config.add("PackageName", new PackageNameHibernateConfigurer(packageManager, classNameLocator));
    }

}
