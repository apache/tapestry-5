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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.IdMatcher;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;
import java.util.List;

public class DecoratorDefImpl implements DecoratorDef
{
    private final String decoratorId;

    private final Method decoratorMethod;

    private final IdMatcher idMatcher;

    private final String[] constraints;

    private final ClassFactory classFactory;

    public DecoratorDefImpl(String decoratorId, Method decoratorMethod, String[] patterns,
                            String[] constraints, ClassFactory classFactory)
    {
        this.decoratorId = notBlank(decoratorId, "decoratorId");
        this.decoratorMethod = notNull(decoratorMethod, "decoratorMethod");

        List<IdMatcher> matchers = CollectionFactory.newList();

        for (String pattern : notNull(patterns, "patterns"))
        {
            IdMatcher matcher = new IdMatcherImpl(pattern);
            matchers.add(matcher);
        }

        idMatcher = new OrIdMatcher(matchers);

        this.constraints = constraints != null ? constraints : new String[0];

        this.classFactory = classFactory;
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(decoratorMethod, classFactory);
    }

    public String[] getConstraints()
    {
        return constraints;
    }

    public String getDecoratorId()
    {
        return decoratorId;
    }

    public ServiceDecorator createDecorator(ModuleBuilderSource moduleBuilderSource,
                                            ServiceResources resources)
    {
        return new ServiceDecoratorImpl(decoratorMethod, moduleBuilderSource, resources,
                                        classFactory);
    }

    /**
     * Returns true if <em>any</em> provided pattern matches the id of the service.
     */
    public boolean matches(ServiceDef serviceDef)
    {
        String serviceId = serviceDef.getServiceId();

        return idMatcher.matches(serviceId);
    }

}
