// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;

public class DecoratorDefImpl extends AbstractServiceInstrumenter implements DecoratorDef
{
    private final String decoratorId;

    public DecoratorDefImpl(Method decoratorMethod, String[] patterns, String[] constraints, ClassFactory classFactory,
                            String decoratorId
    )
    {
        super(decoratorMethod, patterns, constraints, classFactory);

        this.decoratorId = Defense.notBlank(decoratorId, "decoratorId");


    }

    public ServiceDecorator createDecorator(ModuleBuilderSource moduleSource,
                                            ServiceResources resources)
    {
        return new ServiceDecoratorImpl(method, moduleSource, resources, classFactory);
    }

    public String getDecoratorId()
    {
        return decoratorId;
    }

}
