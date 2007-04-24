// Copyright 2007 The Apache Software Foundation
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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.tapestry.internal.hibernate.HibernateSessionManagerImpl;
import org.apache.tapestry.internal.hibernate.HibernateSessionSourceImpl;
import org.apache.tapestry.internal.services.ClassNameLocator;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Lifecycle;
import org.apache.tapestry.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.services.AliasContribution;
import org.apache.tapestry.services.ApplicationGlobals;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateModule
{
    /**
     * The HibernateSessionSource is responsible for creating Hibernate sessions, which includes the
     * initial configuration of Hibernate.
     */
    public static HibernateSessionSource build(Log log,

    @InjectService("ClassNameLocator")
    ClassNameLocator classNameLocator,

    Collection<String> packageNames)
    {
        return new HibernateSessionSourceImpl(log, packageNames, classNameLocator);
    }

    /**
     * Contributes the package "&lt;root&gt;.entities" to the configuration, so that it will be
     * scanned for annotated entity classes.
     */
    public static void contributeHibernateSessionSource(Configuration<String> configuration,
            @Inject("alias:ApplicationGlobals")
            ApplicationGlobals globals)
    {
        configuration.add(globals.getApplicationRootPackage() + ".entities");
    }

    /**
     * The session manager manages sessions on a per-thread/per-request basis. A {@link Transaction}
     * is created initially, and is committed at the end of the request.
     */
    @Lifecycle("perthread")
    public static HibernateSessionManager build(@InjectService("HibernateSessionSource")
    HibernateSessionSource sessionSource,

    @InjectService("ThreadCleanupHub")
    ThreadCleanupHub threadCleanupHub)
    {
        HibernateSessionManagerImpl service = new HibernateSessionManagerImpl(sessionSource);

        threadCleanupHub.addThreadCleanupListener(service);

        return service;
    }

    public static Session build(@InjectService("HibernateSessionManager")
    HibernateSessionManager sessionManager,

    @InjectService("PropertyShadowBuilder")
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
     * Contributes the "session" property (type {@link Session}), the Hibernate Session for the
     * current request.
     */
    public static void contributeAlias(Configuration<AliasContribution> configuration,

    @InjectService("Session")
    Session session)
    {
        configuration.add(new AliasContribution("session", session));
    }
}
