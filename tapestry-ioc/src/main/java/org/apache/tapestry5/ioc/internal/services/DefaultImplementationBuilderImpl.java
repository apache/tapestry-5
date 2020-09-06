// Copyright 2006, 2007, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.util.Map;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.DefaultImplementationBuilder;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;

public class DefaultImplementationBuilderImpl implements DefaultImplementationBuilder
{
    private final Map<Class, Object> cache = CollectionFactory.newConcurrentMap();

    private final PlasticProxyFactory proxyFactory;

    public DefaultImplementationBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public <S> S createDefaultImplementation(Class<S> serviceInterface)
    {
        S instance = serviceInterface.cast(cache.get(serviceInterface));

        if (instance == null)
        {
            instance = createInstance(serviceInterface);
            cache.put(serviceInterface, instance);
        }

        return instance;
    }

    /**
     * Creates a class and an instance of that class. Updates the cache and returns the instance.
     */
    private <S> S createInstance(final Class<S> serviceInterface)
    {
        ClassInstantiator instantiator = proxyFactory.createProxy(serviceInterface, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                plasticClass.addToString(String.format("<NoOp %s>", serviceInterface.getName()));
            }
        });

        return serviceInterface.cast(instantiator.newInstance());
    }
}
