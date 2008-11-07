// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ContributionDefImpl implements ContributionDef
{
    private final String serviceId;

    private final Method contributorMethod;

    private final ClassFactory classFactory;

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

    public void contribute(ModuleBuilderSource moduleBuilderSource, ServiceResources resources,
                           Configuration configuration)
    {
        invokeMethod(moduleBuilderSource, resources, Configuration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ServiceResources resources,
                           OrderedConfiguration configuration)
    {
        invokeMethod(moduleBuilderSource, resources, OrderedConfiguration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ServiceResources resources,
                           MappedConfiguration configuration)
    {
        invokeMethod(moduleBuilderSource, resources, MappedConfiguration.class, configuration);
    }

    private <T> void invokeMethod(ModuleBuilderSource source, ServiceResources resources,
                                  Class<T> parameterType, T parameterValue)
    {
        Map<Class, Object> parameterDefaults = CollectionFactory.newMap();

        // The way it works is: the method will take Configuration, OrderedConfiguration or
        // MappedConfiguration. So, if the method is for one type and the service is for a different
        // type, then we'll see an error putting together the parameter.

        parameterDefaults.put(parameterType, parameterValue);
        parameterDefaults.put(ObjectLocator.class, resources);

        Throwable fail = null;

        Object moduleBuilder = InternalUtils.isStatic(contributorMethod) ? null : source
                .getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    contributorMethod,
                    resources,
                    parameterDefaults, resources.getTracker());

            contributorMethod.invoke(moduleBuilder, parameters);
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
