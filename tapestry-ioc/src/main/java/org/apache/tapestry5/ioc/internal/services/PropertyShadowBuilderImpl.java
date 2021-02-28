// Copyright 2006, 2007, 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.internal.services.ServiceMessages;
import org.apache.tapestry5.commons.services.*;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.plastic.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PropertyShadowBuilderImpl implements PropertyShadowBuilder
{
    private final PropertyAccess propertyAccess;

    private final PlasticProxyFactory proxyFactory;
    
    final private static MethodSignatureUniqueComparator METHOD_COMPARATOR = new MethodSignatureUniqueComparator();

    public PropertyShadowBuilderImpl(@Builtin
                                     PlasticProxyFactory proxyFactory,

                                     PropertyAccess propertyAccess)
    {
        this.proxyFactory = proxyFactory;
        this.propertyAccess = propertyAccess;
    }

    @Override
    public <T> T build(final Object source, final String propertyName, final Class<T> propertyType)
    {
        final Class sourceClass = source.getClass();
        final PropertyAdapter adapter = propertyAccess.getAdapter(sourceClass).getPropertyAdapter(propertyName);

        // TODO: Perhaps extend ClassPropertyAdapter to do these checks?

        if (adapter == null)
            throw new RuntimeException(ServiceMessages.noSuchProperty(sourceClass, propertyName));

        if (!adapter.isRead())
        {
            throw new RuntimeException(
                    String.format("Class %s does not provide an accessor ('getter') method for property '%s'.",
                            source.getClass().getName(), propertyName));
        }

        if (!propertyType.isAssignableFrom(adapter.getType()))
            throw new RuntimeException(ServiceMessages.propertyTypeMismatch(propertyName, sourceClass,
                    adapter.getType(), propertyType));

        ClassInstantiator instantiator = proxyFactory.createProxy(propertyType, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField sourceField = plasticClass.introduceField(sourceClass, "source").inject(source);

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(propertyType.getName(),
                        "readProperty", null, null);

                // You don't do this using MethodAdvice, because then we'd have to use reflection to access the read
                // method.

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    @Override
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(sourceField);
                        builder.invoke(sourceClass, propertyType, adapter.getReadMethod().getName());

                        // Now add the null check.

                        builder.dupe().when(Condition.NULL, new InstructionBuilderCallback()
                        {
                            @Override
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

                for (Method m : METHOD_COMPARATOR.getUniqueMethods(propertyType)) 
                {
                    final MethodDescription description = new MethodDescription(m);
                    if (Modifier.isStatic(description.modifiers)) {
                        continue;
                    }
                    plasticClass.introduceMethod(description).delegateTo(delegateMethod);
                }

                plasticClass.addToString(String.format("<Shadow: property %s of %s>", propertyName, source));
            }
        });

        return propertyType.cast(instantiator.newInstance());
    }
    
    private final static class MethodSignatureUniqueComparator implements Comparator<Method> {

        @Override
        public int compare(Method o1, Method o2) {

            int comparison = o1.getName().compareTo(o2.getName());

            if (comparison == 0) {
                comparison = o1.getParameterTypes().length - o2.getParameterTypes().length;
            }

            if (comparison == 0) {
                final int count = o1.getParameterTypes().length;
                for (int i = 0; i < count; i++) {
                    Class p1 = o1.getParameterTypes()[i];
                    Class p2 = o2.getParameterTypes()[i];
                    if (!p1.equals(p2)) {
                        comparison = p1.getName().compareTo(p2.getName());
                        break;
                    }
                }
            }

            return comparison;
        }

        public List<Method> getUniqueMethods(Class interfaceType) 
        {
            final List<Method> unique = new ArrayList<>(Arrays.asList(interfaceType.getMethods()));
            Collections.sort(unique, this);
            Method last = null;
            Iterator<Method> iterator = unique.iterator();
            while (iterator.hasNext()) 
            {
                Method m = iterator.next();
                if (last != null && compare(m, last) == 0) 
                {
                    iterator.remove();
                }
                last = m;
            }
            return unique;
        }

    }
}
