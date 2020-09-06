// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jmx;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.jmx.MBeanSupport;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public class MBeanSupportImpl implements MBeanSupport
{
    private final Logger logger;

    private final MBeanServer server;

    private final OneShotLock lock = new OneShotLock();

    private final Set<ObjectName> registeredBeans = CollectionFactory.newSet();

    public MBeanSupportImpl(Logger logger)
    {
        this.logger = logger;

        // TODO: Agent Id should be configurable
        final List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

        MBeanServer server = null;

        if (servers != null && 0 < servers.size())
        {
            server = servers.get(0);
        }

        if (server == null)
        {
            server = ManagementFactory.getPlatformMBeanServer();
        }

        this.server = server;
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

    @Override
    public void register(Object bean, String name)
    {
        register(bean, toObjectName(name));
    }

    private static ObjectName toObjectName(String name)
    {
        try
        {
            return new ObjectName(name);
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void register(final Object object, final ObjectName objectName)
    {
        lock.check();

        if (this.server.isRegistered(objectName))
            return;

        try
        {
            this.server.registerMBean(object, objectName);

            this.registeredBeans.add(objectName);

            this.logger.info(format("Registered MBean '%s' with server", objectName));
        } catch (final Exception e)
        {
            this.logger.error(format("Failed to register MBean '%s' with server", objectName), e);
        }
    }

    @Override
    public void unregister(final ObjectName objectName)
    {
        lock.check();

        doUnregister(objectName);
    }

    private void doUnregister(final ObjectName objectName)
    {
        if (this.server.isRegistered(objectName))
        {
            try
            {
                this.server.unregisterMBean(objectName);

                this.logger.info(format("Unregistered MBean '%s' from server", objectName));

                if (registeredBeans.contains(objectName))
                    registeredBeans.remove(objectName);
            } catch (final Exception e)
            {
                this.logger.error(String.format("Failed to unregister MBean '%s' from server", objectName), e);
            }
        }
    }

    private void registryDidShutdown()
    {
        lock.lock();
        // store into new data structure so we can remove them from registered beans
        ObjectName[] objects = registeredBeans.toArray(new ObjectName[registeredBeans.size()]);
        for (final ObjectName name : objects)
        {
            doUnregister(name);
        }

        this.registeredBeans.clear();

    }
}
