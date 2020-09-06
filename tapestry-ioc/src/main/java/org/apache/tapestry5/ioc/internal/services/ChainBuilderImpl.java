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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.WhenCallback;

public class ChainBuilderImpl implements ChainBuilder
{
    private final PlasticProxyFactory proxyFactory;

    public ChainBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T build(final Class<T> commandInterface, List<T> commands)
    {
        // Jump through some hoops to convert the list into an array of the proper type

        Object[] array = (Object[]) Array.newInstance(commandInterface, commands.size());

        final Object[] commandsArray = commands.toArray(array);

        ClassInstantiator<T> instantiator = proxyFactory.createProxy(commandInterface, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                PlasticField commandsField = plasticClass.introduceField(commandsArray.getClass(), "commands").inject(
                        commandsArray);

                for (Method method : commandInterface.getMethods())
                {
                    if (!Modifier.isStatic(method.getModifiers()))
                    {
                        implementMethod(plasticClass, method, commandsField);
                    }
                }

                plasticClass.addToString(String.format("<Command chain of %s>", commandInterface.getName()));
            }
        });

        return instantiator.newInstance();
    }

    private void implementMethod(PlasticClass plasticClass, final Method method, final PlasticField commandsField)
    {
        plasticClass.introduceMethod(method).changeImplementation(new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                builder.loadThis().getField(commandsField).iterateArray(new InstructionBuilderCallback()
                {
                    @Override
                    public void doBuild(InstructionBuilder builder)
                    {
                        // The command is on the stack; add the elements and invoke the method.

                        builder.loadArguments().invoke(method);

                        Class returnType = method.getReturnType();

                        if (returnType == void.class)
                            return;

                        final boolean wide = returnType == long.class || returnType == double.class;

                        if (wide)
                            builder.dupeWide();
                        else
                            builder.dupe();

                        if (returnType == float.class)
                        {
                            builder.loadConstant(0f).compareSpecial("float");
                        }

                        if (returnType == long.class)
                        {
                            builder.loadConstant(0l).compareSpecial("long");
                        }

                        if (returnType == double.class)
                        {
                            builder.loadConstant(0d).compareSpecial("double");
                        }

                        Condition condition = returnType.isPrimitive() ? Condition.NON_ZERO : Condition.NON_NULL;

                        builder.when(condition, new WhenCallback()
                        {
                            @Override
                            public void ifTrue(InstructionBuilder builder)
                            {
                                builder.returnResult();
                            }

                            @Override
                            public void ifFalse(InstructionBuilder builder)
                            {
                                if (wide)
                                    builder.popWide();
                                else
                                    builder.pop();
                            }
                        });
                    }
                });

                builder.returnDefaultValue();
            }
        });
    }
}
