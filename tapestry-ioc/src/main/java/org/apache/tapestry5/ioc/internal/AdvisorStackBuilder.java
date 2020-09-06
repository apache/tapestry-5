// Copyright 2009, 2011 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.def.ServiceDef3;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

/**
 * Equivalent of {@link org.apache.tapestry5.ioc.internal.InterceptorStackBuilder}, but works using an
 * {@link org.apache.tapestry5.ioc.services.AspectInterceptorBuilder} that receives advice from
 * {@link org.apache.tapestry5.ioc.ServiceAdvisor}s.
 * 
 * @since 5.1.0.0
 */
public class AdvisorStackBuilder implements ObjectCreator
{
    private final ServiceDef3 serviceDef;

    private final ObjectCreator delegate;

    private final AspectDecorator aspectDecorator;

    private final InternalRegistry registry;

    /**
     * @param serviceDef
     *            the service that is ultimately being constructed
     * @param delegate
     *            responsible for creating the object to be decorated
     * @param aspectDecorator
     *            used to create the {@link org.apache.tapestry5.ioc.services.AspectInterceptorBuilder} passed to each
     *            {@link org.apache.tapestry5.ioc.ServiceAdvisor}
     * @param registry
     */
    public AdvisorStackBuilder(ServiceDef3 serviceDef, ObjectCreator delegate, AspectDecorator aspectDecorator,
            InternalRegistry registry)
    {
        this.serviceDef = serviceDef;
        this.delegate = delegate;
        this.registry = registry;
        this.aspectDecorator = aspectDecorator;
    }

    @Override
    public Object createObject()
    {
        Object service = delegate.createObject();

        List<ServiceAdvisor> advisors = registry.findAdvisorsForService(serviceDef);

        if (advisors.isEmpty())
            return service;

        final AspectInterceptorBuilder builder = aspectDecorator.createBuilder(serviceDef.getServiceInterface(),
                service, serviceDef, String.format("<AspectProxy for %s(%s)>", serviceDef.getServiceId(), serviceDef
                        .getServiceInterface().getName()));

        for (final ServiceAdvisor advisor : advisors)
        {
            registry.run("Invoking " + advisor, new Runnable()
            {
                @Override
                public void run()
                {
                    advisor.advise(builder);
                }
            });
        }

        return builder.build();
    }
}
