// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.internal.util.*;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

public class ContributionDefImpl implements ContributionDef
{
    private final String serviceId;

    private final Method contributorMethod;

    private final ClassFactory classFactory;

    private static final Class[] CONFIGURATION_TYPES = new Class[] {Configuration.class, MappedConfiguration.class,
            OrderedConfiguration.class};

    public ContributionDefImpl(String serviceId, Method contributorMethod, ClassFactory classFactory)
    {
        this.serviceId = serviceId;
        this.contributorMethod = contributorMethod;
        this.classFactory = classFactory;
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(contributorMethod, classFactory);
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           Configuration configuration)
    {
        invokeMethod(moduleSource, resources, Configuration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           OrderedConfiguration configuration)
    {
        invokeMethod(moduleSource, resources, OrderedConfiguration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           MappedConfiguration configuration)
    {
        invokeMethod(moduleSource, resources, MappedConfiguration.class, configuration);
    }

    private <T> void invokeMethod(ModuleBuilderSource source, ServiceResources resources,
                                  Class<T> parameterType, T parameterValue)
    {
        Map<Class, Object> resourceMap = CollectionFactory.newMap();

        resourceMap.put(parameterType, parameterValue);
        resourceMap.put(ObjectLocator.class, resources);
        resourceMap.put(Logger.class, resources.getLogger());

        InjectionResources injectionResources = new MapInjectionResources(resourceMap);

        // For each of the other configuration types that is not expected, add a guard.

        for (Class t : CONFIGURATION_TYPES)
        {
            if (parameterType != t)
            {
                injectionResources = new DelegatingInjectionResources(
                        new WrongConfigurationTypeGuard(resources.getServiceId(), t, parameterType),
                        injectionResources);
            }
        }


        Throwable fail = null;

        Object moduleInstance = InternalUtils.isStatic(contributorMethod) ? null : source
                .getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    contributorMethod,
                    resources,
                    injectionResources, resources.getTracker());

            contributorMethod.invoke(moduleInstance, parameters);
        }
        catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }

        if (fail != null)
            throw new RuntimeException(IOCMessages
                    .contributionMethodError(contributorMethod, fail), fail);
    }
}
