// Copyright 2006, 2007, 2009, 2010, 2011 The Apache Software Foundation
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

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.def.DecoratorDef2;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

public class DecoratorDefImpl extends AbstractServiceInstrumenter implements DecoratorDef2
{
    private final String decoratorId;

    public DecoratorDefImpl(Method decoratorMethod, String[] patterns, String[] constraints,
            PlasticProxyFactory proxyFactory, String decoratorId, Class serviceInterface, Set<Class> markers)
    {
        super(decoratorMethod, patterns, constraints, serviceInterface, markers, proxyFactory);
        assert InternalUtils.isNonBlank(decoratorId);

        this.decoratorId = decoratorId;

    }

    @Override
    public ServiceDecorator createDecorator(ModuleBuilderSource moduleSource, ServiceResources resources)
    {
        return new ServiceDecoratorImpl(method, moduleSource, resources, proxyFactory);
    }

    @Override
    public String getDecoratorId()
    {
        return decoratorId;
    }

}
