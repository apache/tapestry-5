// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.clojure;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import org.apache.tapestry5.clojure.ClojureBuilder;
import org.apache.tapestry5.clojure.MethodToFunctionSymbolMapper;
import org.apache.tapestry5.clojure.Namespace;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.*;

import java.lang.reflect.Method;

public class ClojureBuilderImpl implements ClojureBuilder
{
    private final PlasticProxyFactory proxyFactory;

    private final MethodToFunctionSymbolMapper mapper;

    private final OperationTracker tracker;

    private final Var REQUIRE = RT.var("clojure.core", "require");

    public ClojureBuilderImpl(@Builtin PlasticProxyFactory proxyFactory, MethodToFunctionSymbolMapper mapper, OperationTracker tracker)
    {
        this.proxyFactory = proxyFactory;
        this.mapper = mapper;
        this.tracker = tracker;
    }

    public <T> T build(final Class<T> interfaceType)
    {
        assert interfaceType != null;
        assert interfaceType.isInterface();

        Namespace annotation = interfaceType.getAnnotation(Namespace.class);

        if (annotation == null)
        {
            throw new IllegalArgumentException(String.format("Interface type %s does not have the Namespace annotation.",
                    interfaceType.getName()));
        }

        final String namespace = annotation.value();

        ClassInstantiator<T> instantiator = proxyFactory.createProxy(interfaceType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                for (final Method m : interfaceType.getMethods())
                {
                    bridgeToClojure(plasticClass, m);
                }
            }

            private void bridgeToClojure(final PlasticClass plasticClass, final Method method)
            {
                final MethodDescription desc = new MethodDescription(method);

                if (method.getReturnType() == void.class)
                {
                    throw new IllegalArgumentException(String.format("Method %s may not be void when bridging to Clojure functions.",
                            desc));
                }

                final Symbol symbol = mapper.mapMethod(namespace, method);

                tracker.run(String.format("Mapping %s method %s to Clojure function %s",
                        interfaceType.getName(),
                        desc.toShortString(),
                        symbol.toString()), new Runnable()
                {
                    public void run()
                    {
                        Symbol namespaceSymbol = Symbol.create(symbol.getNamespace());

                        REQUIRE.invoke(namespaceSymbol);

                        Var var = Var.find(symbol);

                        final PlasticField varField = plasticClass.introduceField(Var.class, method.getName() + "Var").inject(var);

                        plasticClass.introduceMethod(desc).changeImplementation(new InstructionBuilderCallback()
                        {
                            public void doBuild(InstructionBuilder builder)
                            {
                                bridgeToClojure(builder, desc, varField);
                            }
                        });

                    }
                });

            }

            private void bridgeToClojure(InstructionBuilder builder, MethodDescription description, PlasticField varField)
            {
                builder.loadThis().getField(varField);

                int count = description.argumentTypes.length;

                Class[] invokeParameterTypes = new Class[count];

                for (int i = 0; i < count; i++)
                {
                    invokeParameterTypes[i] = Object.class;

                    builder.loadArgument(i).boxPrimitive(description.argumentTypes[i]);
                }

                Method ifnMethod = null;

                try
                {
                    ifnMethod = IFn.class.getMethod("invoke", invokeParameterTypes);
                } catch (NoSuchMethodException ex)
                {
                    throw new RuntimeException(String.format("Unable to find correct IFn.invoke() method: %s",
                            InternalUtils.toMessage(ex)), ex);
                }

                builder.invoke(ifnMethod);

                builder.castOrUnbox(description.returnType);
                builder.returnResult();
            }
        });

        return instantiator.newInstance();
    }
}
