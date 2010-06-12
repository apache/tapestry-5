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
package org.apache.tapestry5.ioc.internal;

import static java.lang.String.format;
import static org.apache.tapestry5.ioc.internal.util.InternalUtils.asString;
import static org.apache.tapestry5.ioc.internal.util.InternalUtils.toMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.def.StartupDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.slf4j.Logger;


public class StartupDefImpl implements StartupDef
{
    private final Method startupMethod;

    public StartupDefImpl(Method startupMethod)
    {
        this.startupMethod = startupMethod;
    }

    public void startup(ModuleBuilderSource moduleSource, ObjectLocator locator, OperationTracker tracker, Logger logger)
    {
        Map<Class, Object> resourceMap = CollectionFactory.newMap();

        resourceMap.put(ObjectLocator.class, locator);
        resourceMap.put(Logger.class, logger);

        InjectionResources injectionResources = new MapInjectionResources(resourceMap);
        
        Throwable fail = null;

        Object moduleInstance = InternalUtils.isStatic(startupMethod) ? null : moduleSource
                .getModuleBuilder();

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    startupMethod,
                    locator,
                    injectionResources, tracker);

            startupMethod.invoke(moduleInstance, parameters);
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
            throw new RuntimeException(
                    format("Error invoking startup method %s: %s", asString(startupMethod), toMessage(fail)), fail);
    }

}
