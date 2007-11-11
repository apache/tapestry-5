// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.spring;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * A wrapper that converts a Spring {@link ApplicationContext} into a set of service definitions,
 * compatible with Tapestry 5 IoC, for the beans defined in the context, as well as the context
 * itself.
 */
public class SpringModuleDef implements ModuleDef
{
    private static final String CONTEXT_SERVICE_ID = WebApplicationContext.class.getSimpleName();

    private final ApplicationContext _context;

    private final Map<String, ServiceDef> _serviceDefs = newCaseInsensitiveMap();

    public SpringModuleDef(final ApplicationContext context)
    {
        _context = context;

        for (final String beanName : BeanFactoryUtils.beanNamesIncludingAncestors(_context))
        {
            ServiceDef serviceDef = new ServiceDef()
            {
                private Object getBean()
                {
                    return _context.getBean(beanName);
                }

                private Class getBeanType()
                {
                    return _context.getType(beanName);
                }

                public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
                {
                    return new ObjectCreator()
                    {
                        public Object createObject()
                        {
                            return getBean();
                        }
                    };
                }

                public String getServiceId()
                {
                    return beanName;
                }

                public Class getServiceInterface()
                {
                    return getBeanType();
                }

                public String getServiceScope()
                {
                    return IOCConstants.DEFAULT_SCOPE;
                }

                public boolean isEagerLoad()
                {
                    return false;
                }

                /** Returns an empty set, Spring has no concept of a marker annotation. */
                public Set<Class> getMarkers()
                {
                    return Collections.emptySet();
                }

            };

            _serviceDefs.put(beanName, serviceDef);
        }

        // And add one service that is the Spring WebApplicationContext.

        ServiceDef serviceDef = new ServiceDef()
        {

            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return new ObjectCreator()
                {
                    public Object createObject()
                    {
                        return _context;
                    }
                };
            }

            public String getServiceId()
            {
                return CONTEXT_SERVICE_ID;
            }

            public Class getServiceInterface()
            {
                return WebApplicationContext.class;
            }

            public String getServiceScope()
            {
                return IOCConstants.DEFAULT_SCOPE;
            }

            public boolean isEagerLoad()
            {
                return false;
            }

            /** Returns null. */
            public Set<Class> getMarkers()
            {
                return Collections.emptySet();
            }

        };

        _serviceDefs.put(CONTEXT_SERVICE_ID, serviceDef);
    }

    public Class getBuilderClass()
    {
        return null;
    }

    /** Returns an empty set. */
    public Set<ContributionDef> getContributionDefs()
    {
        return Collections.emptySet();
    }

    /** Returns an empty set. */
    public Set<DecoratorDef> getDecoratorDefs()
    {
        return Collections.emptySet();
    }

    public String getLoggerName()
    {
        return "Spring";
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return _serviceDefs.get(serviceId);
    }

    public Set<String> getServiceIds()
    {
        return _serviceDefs.keySet();
    }

}
