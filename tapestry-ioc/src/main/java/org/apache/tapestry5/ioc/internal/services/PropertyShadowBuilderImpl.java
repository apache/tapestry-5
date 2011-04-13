// Copyright 2006, 2007, 2010, 2011 The Apache Software Foundation
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
import java.lang.reflect.Modifier;

import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.WhenCallback;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;

public class PropertyShadowBuilderImpl implements PropertyShadowBuilder
{
    private final PropertyAccess propertyAccess;

    private final PlasticProxyFactory proxyFactory;

    public PropertyShadowBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory,

    PropertyAccess propertyAccess)
    {
        this.proxyFactory = proxyFactory;
        this.propertyAccess = propertyAccess;
    }

    public <T> T build(final Object source, final String propertyName, final Class<T> propertyType)
    {
        final Class sourceClass = source.getClass();
        final PropertyAdapter adapter = propertyAccess.getAdapter(sourceClass).getPropertyAdapter(propertyName);

        // TODO: Perhaps extend ClassPropertyAdapter to do these checks?

        if (adapter == null)
            throw new RuntimeException(ServiceMessages.noSuchProperty(sourceClass, propertyName));

        if (!adapter.isRead())
            throw new RuntimeException(ServiceMessages.readNotSupported(source, propertyName));

        if (!propertyType.isAssignableFrom(adapter.getType()))
            throw new RuntimeException(ServiceMessages.propertyTypeMismatch(propertyName, sourceClass,
                    adapter.getType(), propertyType));

        ClassInstantiator instantiator = proxyFactory.createProxy(propertyType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField sourceField = plasticClass.introduceField(sourceClass, "source").inject(source);

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(propertyType.getName(),
                        "readProperty", null, null);

                // You don't do this using MethodAdvice, because then we'd have to use reflection to access the read
                // method.

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(sourceField);
                        builder.invoke(sourceClass, propertyType, adapter.getReadMethod().getName());

                        // Now add the null check.

                        builder.dupe().when(Condition.NULL, new InstructionBuilderCallback()
                        {
                            public void doBuild(InstructionBuilder builder)
                            {
                                builder.throwException(
                                        NullPointerException.class,
                                        String.format(
                                                "Unable to delegate method invocation to property '%s' of %s, because the property is null.",
                                                propertyName, source));
                            }
                        });

                        builder.returnResult();
                    }
                });

                for (Method m : propertyType.getMethods())
                {
                    plasticClass.introduceMethod(m).delegateTo(delegateMethod);
                }

                plasticClass.addToString(String.format("<Shadow: property %s of %s>", propertyName, source));
            }
        });

        return propertyType.cast(instantiator.newInstance());
    }
}
