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

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.internal.util.Invokable;

import java.util.Collections;
import java.util.List;

/**
 * Responsible for constructing the interceptor stack, on demand, by invoking an ordered series of decorators ({@link
 * org.apache.tapestry5.ioc.def.DecoratorDef} (which are converted into {@link ServiceDecorator}s).
 */
public class InterceptorStackBuilder implements ObjectCreator
{
    private final String serviceId;

    private final ObjectCreator coreServiceCreator;

    private final Module module;

    private final OperationTracker tracker;

    /**
     * @param module             the module containing the decorator method
     * @param serviceId          identifies the service to be decorated
     * @param coreServiceCreator responsible for creating the core service which is then decorated with a stack of
     * @param tracker            used to track which decorator is being invoked
     */
    public InterceptorStackBuilder(Module module, String serviceId, ObjectCreator coreServiceCreator,
                                   OperationTracker tracker)
    {
        this.module = module;
        this.serviceId = serviceId;
        this.coreServiceCreator = coreServiceCreator;
        this.tracker = tracker;
    }

    public Object createObject()
    {
        Object current = coreServiceCreator.createObject();

        List<ServiceDecorator> decorators = module.findDecoratorsForService(serviceId);

        // We get the decorators ordered according to their dependencies. However, we want to
        // process from the last interceptor to the first, so we reverse the list.

        Collections.reverse(decorators);

        for (final ServiceDecorator decorator : decorators)
        {
            final Object delegate = current;

            Object interceptor =
                    tracker.invoke("Invoking " + decorator, new Invokable<Object>()
                    {
                        public Object invoke()
                        {
                            return decorator.createInterceptor(delegate);
                        }
                    });

            // Decorator methods may return null; this indicates that the decorator chose not to
            // decorate.

            if (interceptor != null) current = interceptor;
        }

        // The stack of interceptors (plus the core service implementation) are "represented" to the
        // outside world
        // as the outermost interceptor. That will still be buried inside the service proxy.

        return current;
    }
}
