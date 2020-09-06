// Copyright 2007, 2008, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.Constructor;

/**
 * A service creator based on an implementation class' constructor, rather than a service builder method.
 */
public class ConstructorServiceCreator extends AbstractServiceCreator
{
    private final Constructor constructor;

    public ConstructorServiceCreator(ServiceBuilderResources resources, String creatorDescription,
                                     Constructor constructor)
    {
        super(resources, creatorDescription);

        this.constructor = constructor;
    }

    @Override
    public String toString()
    {
        return creatorDescription;
    }

    private ObjectCreator<?> plan;

    private  ObjectCreator<?> getPlan()
    {
        if (plan == null)
        {
            String description = String.format("Invoking constructor %s (for service '%s')", creatorDescription, resources.getServiceId());

            plan = InternalUtils.createConstructorConstructionPlan(resources.getTracker(), resources, createInjectionResources(), logger,
                    description, constructor);
        }

        return plan;
    }

    @Override
    public Object createObject()
    {
        return getPlan().createObject();
    }
}
