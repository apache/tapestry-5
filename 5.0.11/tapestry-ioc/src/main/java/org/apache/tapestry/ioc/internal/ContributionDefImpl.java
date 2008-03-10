// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import org.apache.tapestry.ioc.*;
import org.apache.tapestry.ioc.def.ContributionDef;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ContributionDefImpl implements ContributionDef
{
    private final String _serviceId;

    private final Method _contributorMethod;

    private final ClassFactory _classFactory;

    public ContributionDefImpl(String serviceId, Method contributorMethod, ClassFactory classFactory)
    {
        _serviceId = serviceId;
        _contributorMethod = contributorMethod;
        _classFactory = classFactory;
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(_contributorMethod, _classFactory);
    }

    public String getServiceId()
    {
        return _serviceId;
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           Configuration configuration)
    {
        invokeMethod(moduleBuilderSource, locator, Configuration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           OrderedConfiguration configuration)
    {
        invokeMethod(moduleBuilderSource, locator, OrderedConfiguration.class, configuration);
    }

    public void contribute(ModuleBuilderSource moduleBuilderSource, ObjectLocator locator,
                           MappedConfiguration configuration)
    {
        invokeMethod(moduleBuilderSource, locator, MappedConfiguration.class, configuration);
    }

    private <T> void invokeMethod(ModuleBuilderSource source, ObjectLocator locator,
                                  Class<T> parameterType, T parameterValue)
    {
        Map<Class, Object> parameterDefaults = newMap();

        // The way it works is: the method will take Configuration, OrderedConfiguration or
        // MappedConfiguration. So, if the method is for one type and the service is for a different
        // type, then we'll see an error putting together the parameter.

        parameterDefaults.put(parameterType, parameterValue);
        parameterDefaults.put(ObjectLocator.class, locator);

        Throwable fail = null;

        Object moduleBuilder = InternalUtils.isStatic(_contributorMethod) ? null : source
                .getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    _contributorMethod,
                    locator,
                    parameterDefaults);

            _contributorMethod.invoke(moduleBuilder, parameters);
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
                    .contributionMethodError(_contributorMethod, fail), fail);
    }
}
