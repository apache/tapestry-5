// Copyright 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.AdvisorDef2;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

public class AdvisorDefImpl extends AbstractServiceInstrumenter implements AdvisorDef2
{
    private final String advisorId;

    public AdvisorDefImpl(Method method, String[] patterns, String[] constraints, PlasticProxyFactory proxyFactory,
            String advisorId, Class serviceInterface, Set<Class> markers)
    {
        super(method, patterns, constraints, serviceInterface, markers, proxyFactory);
        assert InternalUtils.isNonBlank(advisorId);

        this.advisorId = advisorId;
    }

    @Override
    public ServiceAdvisor createAdvisor(ModuleBuilderSource moduleSource, ServiceResources resources)
    {
        return new ServiceAdvisorImpl(moduleSource, method, resources, proxyFactory);
    }

    @Override
    public String getAdvisorId()
    {
        return advisorId;
    }
}
