// Copyright 2014 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.def.StartupDef;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class StartupDefImpl implements StartupDef
{
    private final Method startupMethod;

    public StartupDefImpl(Method contributorMethod)
    {
        this.startupMethod = contributorMethod;
    }

    @Override
    public void invoke(final ModuleBuilderSource moduleBuilderSource,
                       final OperationTracker tracker,
                       final ObjectLocator locator,
                       final Logger logger)
    {

        tracker.run(String.format("Invoking startup method %s.", InternalUtils.asString(startupMethod)),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Map<Class, Object> resourceMap = CollectionFactory.newMap();

                        resourceMap.put(ObjectLocator.class, locator);
                        resourceMap.put(Logger.class, logger);

                        InjectionResources injectionResources = new MapInjectionResources(resourceMap);

                        Throwable fail = null;

                        Object moduleInstance = InternalUtils.isStatic(startupMethod) ? null : moduleBuilderSource.getModuleBuilder();

                        try
                        {
                            ObjectCreator[] parameters = InternalUtils.calculateParametersForMethod(startupMethod, locator,
                                    injectionResources, tracker);

                            startupMethod.invoke(moduleInstance, InternalUtils.realizeObjects(parameters));
                        } catch (InvocationTargetException ex)
                        {
                            fail = ex.getTargetException();
                        } catch (RuntimeException ex)
                        {
                            throw ex;
                        } catch (Exception ex)
                        {
                            fail = ex;
                        }

                        if (fail != null)
                        {
                            throw new RuntimeException(fail);
                        }

                    }
                });
    }
}
