// Copyright 2007, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.lang.reflect.Method;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.EnvironmentalShadowBuilder;

public class EnvironmentalShadowBuilderImpl implements EnvironmentalShadowBuilder
{
    private final PlasticProxyFactory proxyFactory;

    private final Environment environment;

    /**
     * Construct using the default builtin factory, not the component layer version.
     */
    public EnvironmentalShadowBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory,

    Environment environment)
    {
        this.proxyFactory = proxyFactory;
        this.environment = environment;
    }

    public <T> T build(final Class<T> serviceType)
    {
        ClassInstantiator<T> instantiator = proxyFactory.createProxy(serviceType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(
                        PlasticUtils.toTypeName(serviceType), "delegate", null, null);

                delegateMethod.addAdvice(new MethodAdvice()
                {
                    public void advise(MethodInvocation invocation)
                    {
                        invocation.setReturnValue(environment.peekRequired(serviceType));
                    }
                });

                for (Method method : serviceType.getMethods())
                {
                    plasticClass.introduceMethod(method).delegateTo(delegateMethod);
                }

                plasticClass.addToString(String.format("<EnvironmentalProxy for %s>", serviceType.getName()));
            }
        });

        return instantiator.newInstance();
    }
}
