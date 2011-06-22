// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

import java.util.List;

/**
 * Equivalent of {@link org.apache.tapestry5.ioc.internal.InterceptorStackBuilder}, but works using an {@link
 * org.apache.tapestry5.ioc.services.AspectInterceptorBuilder} that receives advice from {@link
 * org.apache.tapestry5.ioc.ServiceAdvisor}s.
 *
 * @since 5.1.0.0
 */
public class AdvisorStackBuilder implements ObjectCreator
{
    private final ServiceDef serviceDef;

    private final ObjectCreator delegate;

    private final AspectDecorator aspectDecorator;

    private final InternalRegistry registry;

    /**
     * @param serviceDef      the service that is ultimately being constructed
     * @param delegate        responsible for creating the object to be decorated
     * @param aspectDecorator used to create the {@link org.apache.tapestry5.ioc.services.AspectInterceptorBuilder}
     *                        passed to each {@link org.apache.tapestry5.ioc.ServiceAdvisor}
     * @param registry
     */
    public AdvisorStackBuilder(ServiceDef serviceDef, ObjectCreator delegate,
                               AspectDecorator aspectDecorator, InternalRegistry registry)
    {
        this.serviceDef = serviceDef;
        this.delegate = delegate;
        this.registry = registry;
        this.aspectDecorator = aspectDecorator;
    }

    public Object createObject()
    {
        Object service = delegate.createObject();

        List<ServiceAdvisor> advisors = registry.findAdvisorsForService(serviceDef);

        if (advisors.isEmpty())
            return service;

        final AspectInterceptorBuilder builder = aspectDecorator.createBuilder(serviceDef.getServiceInterface(),
                                                                               service,
                                                                               String.format("<AspectProxy for %s(%s)>",
                                                                                             serviceDef.getServiceId(),
                                                                                             serviceDef.getServiceInterface().getName()));
        for (final ServiceAdvisor advisor : advisors)
        {
            registry.run("Invoking " + advisor, new Runnable()
            {
                public void run()
                {
                    advisor.advise(builder);
                }
            });
        }

        return builder.build();
    }
}
