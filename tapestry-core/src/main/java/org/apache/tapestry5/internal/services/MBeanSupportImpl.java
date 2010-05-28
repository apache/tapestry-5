// Copyright 20010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.slf4j.Logger;

public class MBeanSupportImpl implements MBeanSupport, RegistryShutdownListener
{
    private Logger logger;

    private MBeanServer server;
    
    private final OneShotLock lock = new OneShotLock();

    private final Set<ObjectName> registeredBeans = CollectionFactory.newSet();

    public MBeanSupportImpl(Logger logger)
    {
        this.logger = logger;
        
        // TODO: Agent Id should be configurable
        final List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

        if (servers != null && 0 <  servers.size())
        {
            this.server = servers.get(0);
        }

        if (this.server == null)
        {
            this.server = ManagementFactory.getPlatformMBeanServer();
        }
    }

    public void register(final Object object, final ObjectName objectName)
    {
        lock.check();
        
        try
        {
            this.server.registerMBean(object, objectName);

            this.registeredBeans.add(objectName);

            this.logger.info(format("Registered MBean '%s' with server", objectName));
        }
        catch (final Exception e)
        {
            this.logger.error(format("Failed to register MBean '%s' with server", objectName), e);
        }
    }

    public void unregister(final ObjectName objectName)
    {
        lock.check();
        
        if (this.server.isRegistered(objectName))
        {
            try
            {
                this.server.unregisterMBean(objectName);

                this.logger.info(format("Unegistered MBean '%s' from server", objectName));
                
                if(registeredBeans.contains(objectName))
                    registeredBeans.remove(objectName);
            }
            catch (final Exception e)
            {
                this.logger.error(String.format("Failed to unregister MBean '%s' from server", objectName), e);
            }
        }
    }

    public void registryDidShutdown()
    {
        lock.lock();
        
        for (final ObjectName name : this.registeredBeans)
        {
            unregister(name);
        }

        this.registeredBeans.clear();

    }
}
