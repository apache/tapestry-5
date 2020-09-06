// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.StrategyRegistry;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.StrategyBuilder;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;

public class StrategyBuilderImpl implements StrategyBuilder
{
    private final PlasticProxyFactory proxyFactory;

    public StrategyBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public <S> S build(StrategyRegistry<S> registry)
    {
        return createProxy(registry.getAdapterType(), registry);
    }

    @Override
    public <S> S build(Class<S> adapterType, Map<Class, S> registrations)
    {
        StrategyRegistry<S> registry = StrategyRegistry.newInstance(adapterType, registrations);

        return build(registry);
    }

    private <S> S createProxy(final Class<S> interfaceType, final StrategyRegistry<S> registry)
    {
        ClassInstantiator instantiator = proxyFactory.createProxy(interfaceType, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField registryField = plasticClass.introduceField(StrategyRegistry.class, "registry")
                        .inject(registry);
                Class<?> interfaceSelectorType = null;

                for (final Method method : interfaceType.getMethods())
                {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0)
                    {
                        throw new IllegalArgumentException("Invalid method "  + method
                            + ", when using the strategy pattern, every method must take at least the selector as its parameter");
                    }
                    Class<?> methodSelectorType = parameterTypes[0];
                    if (interfaceSelectorType == null)
                    {
                        interfaceSelectorType = methodSelectorType;
                    } else if (!interfaceSelectorType.equals(methodSelectorType))
                    {
                        throw new IllegalArgumentException("Conflicting method definitions,"
                            + " expecting the first argument of every method to have the same type");

                    }
                    plasticClass.introduceMethod(new MethodDescription(method), new InstructionBuilderCallback()
                    {
                        @Override
                        public void doBuild(InstructionBuilder builder)
                        {
                            Class returnType = method.getReturnType();

                            builder.loadThis().getField(registryField);

                            // Argument 0 is the selector used to find the adapter and should be an object reference,
                            // not a primitive.

                            builder.loadArgument(0);

                            // Use the StrategyRegistry to get the adapter to re-invoke the method on
                            builder.invoke(StrategyRegistry.class, Object.class, "getByInstance", Object.class)
                                    .checkcast(interfaceType);

                            // That leaves the correct adapter on top of the stack. Get the
                            // selector and the rest of the arguments in place and invoke the method.

                            builder.loadArguments().invoke(interfaceType, returnType, method.getName(),
                                    method.getParameterTypes());

                            builder.returnResult();
                        }
                    });
                }

                plasticClass.addToString(String.format("<Strategy for %s>", interfaceType.getName()));
            }
        });

        return interfaceType.cast(instantiator.newInstance());
    }
}
