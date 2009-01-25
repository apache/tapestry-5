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

import org.apache.tapestry5.ioc.AdvisorDef;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;

public class AdvisorDefImpl extends AbstractServiceInstrumenter implements AdvisorDef
{
    private final String advisorId;

    public AdvisorDefImpl(Method method, String[] patterns, String[] constraints, ClassFactory classFactory,
                          String advisorId)
    {
        super(method, patterns, constraints, classFactory);

        this.advisorId = Defense.notBlank(advisorId, "advisorId");
    }

    public ServiceAdvisor createAdvisor(ModuleBuilderSource moduleSource, ServiceResources resources)
    {
        return new ServiceAdvisorImpl(moduleSource, method, resources, classFactory);
    }

    public String getAdvisorId()
    {
        return advisorId;
    }
}
