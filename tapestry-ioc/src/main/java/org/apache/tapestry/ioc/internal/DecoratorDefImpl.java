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

package org.apache.tapestry.ioc.internal;

import org.apache.tapestry.ioc.IdMatcher;
import org.apache.tapestry.ioc.ModuleBuilderSource;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.lang.reflect.Method;
import java.util.List;

public class DecoratorDefImpl implements DecoratorDef
{
    private final String _decoratorId;

    private final Method _decoratorMethod;

    private final IdMatcher _idMatcher;

    private final String[] _constraints;

    private final ClassFactory _classFactory;

    public DecoratorDefImpl(String decoratorId, Method decoratorMethod, String[] patterns,
                            String[] constraints, ClassFactory classFactory)
    {
        _decoratorId = notBlank(decoratorId, "decoratorId");
        _decoratorMethod = notNull(decoratorMethod, "decoratorMethod");

        List<IdMatcher> matchers = CollectionFactory.newList();

        for (String pattern : notNull(patterns, "patterns"))
        {
            IdMatcher matcher = new IdMatcherImpl(pattern);
            matchers.add(matcher);
        }

        _idMatcher = new OrIdMatcher(matchers);

        _constraints = constraints != null ? constraints : new String[0];

        _classFactory = classFactory;
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(_decoratorMethod, _classFactory);
    }

    public String[] getConstraints()
    {
        return _constraints;
    }

    public String getDecoratorId()
    {
        return _decoratorId;
    }

    public ServiceDecorator createDecorator(ModuleBuilderSource moduleBuilderSource,
                                            ServiceResources resources)
    {
        return new ServiceDecoratorImpl(_decoratorMethod, moduleBuilderSource, resources,
                                        _classFactory);
    }

    /**
     * Returns true if <em>any</em> provided pattern matches the id of the service.
     */
    public boolean matches(ServiceDef serviceDef)
    {
        String serviceId = serviceDef.getServiceId();

        return _idMatcher.matches(serviceId);
    }

}
