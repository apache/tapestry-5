// Copyright 2010 The Apache Software Foundation
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
package org.apache.tapestry5.jmx;

import javax.management.ObjectName;

import org.apache.tapestry5.internal.jmx.MBeanSupportImpl;
import org.apache.tapestry5.internal.services.PagePool;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;

/**
 * Module for JMX services.
 *
 */
public class JmxModule
{
    public MBeanSupport buildMBeanSupport(RegistryShutdownHub shutdownHub, @Autobuild MBeanSupportImpl service)
    {
        shutdownHub.addRegistryShutdownListener(service);
        
        return service;
    }
    
    public static PagePool decoratePagePool(final PagePool pagePool, final MBeanSupport managedBeanSupport)
    { 
        final ObjectName objectName = buildObjectName("org.apache.tapestry5:service=PagePool");
        
        managedBeanSupport.register(pagePool, objectName);
        
        return pagePool;
    }
    
    private static ObjectName buildObjectName(String name)
    {
        try
        {
            return new ObjectName(name);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
