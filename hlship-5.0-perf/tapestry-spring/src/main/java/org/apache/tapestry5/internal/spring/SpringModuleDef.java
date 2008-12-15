// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper that converts a Spring {@link ApplicationContext} into a set of service definitions, compatible with
 * Tapestry 5 IoC, for the beans defined in the context, as well as the context itself.
 */
public class SpringModuleDef implements ModuleDef
{
    private static final String CONTEXT_SERVICE_ID = WebApplicationContext.class.getSimpleName();

    private final Map<String, ServiceDef> serviceDefs = newCaseInsensitiveMap();

    public SpringModuleDef(final ApplicationContext context)
    {
        for (final String beanName : BeanFactoryUtils.beanNamesIncludingAncestors(context))
        {
            ServiceDef serviceDef = new ServiceDef()
            {
                private Object getBean()
                {
                    return context.getBean(beanName);
                }

                private Class getBeanType()
                {
                    return context.getType(beanName);
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
                    return ScopeConstants.DEFAULT;
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

            serviceDefs.put(beanName, serviceDef);
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
                        return context;
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
                return ScopeConstants.DEFAULT;
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

        serviceDefs.put(CONTEXT_SERVICE_ID, serviceDef);
    }

    public Class getBuilderClass()
    {
        return null;
    }

    /**
     * Returns an empty set.
     */
    public Set<ContributionDef> getContributionDefs()
    {
        return Collections.emptySet();
    }

    /**
     * Returns an empty set.
     */
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
        return serviceDefs.get(serviceId);
    }

    public Set<String> getServiceIds()
    {
        return serviceDefs.keySet();
    }
}
