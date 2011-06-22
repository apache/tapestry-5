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

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A wrapper around a decorator method.
 */
public class ServiceDecoratorImpl extends AbstractMethodInvokingInstrumenter implements ServiceDecorator
{

    public ServiceDecoratorImpl(Method method, ModuleBuilderSource moduleSource,
                                ServiceResources resources, ClassFactory classFactory)
    {
        super(moduleSource, method, resources, classFactory);
    }

    public Object createInterceptor(Object delegate)
    {
        // Create a copy of the parameters map so that Object.class points to the delegate instance.

        Map<Class, Object> resources = CollectionFactory.newMap(this.resourcesDefaults);

        resources.put(Object.class, delegate);
        resources.put(serviceInterface, delegate);

        InjectionResources injectionResources = new MapInjectionResources(resources);

        Object result = invoke(injectionResources);

        if (result != null && !serviceInterface.isInstance(result))
        {
            throw new RuntimeException(IOCMessages.decoratorReturnedWrongType(
                    method,
                    serviceId,
                    result,
                    serviceInterface));
        }

        return result;
    }

}
