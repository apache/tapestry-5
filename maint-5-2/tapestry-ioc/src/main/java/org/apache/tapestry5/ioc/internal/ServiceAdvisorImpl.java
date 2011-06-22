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

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class ServiceAdvisorImpl extends AbstractMethodInvokingInstrumenter implements ServiceAdvisor
{
    public ServiceAdvisorImpl(ModuleBuilderSource moduleSource, Method method, ServiceResources resources,
                              ClassFactory classFactory)
    {
        super(moduleSource, method, resources, classFactory);
    }

    /**
     * Invokes the configured method, passing the builder. The method will always take, as a parameter, a
     * MethodAdvisor.
     */
    public void advise(MethodAdviceReceiver methodAdviceReceiver)
    {
        Map<Class, Object> resources = CollectionFactory.newMap(this.resourcesDefaults);

        resources.put(MethodAdviceReceiver.class, methodAdviceReceiver);

        InjectionResources injectionResources = new MapInjectionResources(resources);

        // By design, advise methods return void, so we know that the return value is null.

        invoke(injectionResources);
    }
}
