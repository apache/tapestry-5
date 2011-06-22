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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ServiceDef2;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.Set;

public class SpringBeanServiceDef implements ServiceDef2
{
    private final String beanName;

    private final ApplicationContext context;

    public SpringBeanServiceDef(String beanName, ApplicationContext context)
    {
        this.beanName = beanName;
        this.context = context;
    }

    public boolean isPreventDecoration()
    {
        return true;
    }

    public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
    {
        return new ObjectCreator()
        {
            public Object createObject()
            {
                return context.getBean(beanName);
            }

            @Override
            public String toString()
            {
                return String.format("ObjectCreator<Spring Bean '%s'>", beanName);
            }
        };
    }

    public String getServiceId()
    {
        return beanName;
    }

    public Set<Class> getMarkers()
    {
        return Collections.emptySet();
    }

    public Class getServiceInterface()
    {
        return context.getType(beanName);
    }

    public String getServiceScope()
    {
        return ScopeConstants.DEFAULT;
    }

    public boolean isEagerLoad()
    {
        return false;
    }
}
