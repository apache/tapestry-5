// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ThunkCreator;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;

@SuppressWarnings("all")
public class ThunkCreatorImpl implements ThunkCreator
{
    private final Map<Class, ClassInstantiator> interfaceToInstantiator = CollectionFactory.newConcurrentMap();

    private final PlasticProxyFactory proxyFactory;

    private static final Method CREATE_OBJECT = PlasticUtils.getMethod(ObjectCreator.class, "createObject");

    public ThunkCreatorImpl(@Builtin
    PlasticProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public <T> T createThunk(Class<T> proxyType, ObjectCreator objectCreator, String description)
    {
        assert proxyType != null;
        assert objectCreator != null;
        assert InternalUtils.isNonBlank(description);

        if (!proxyType.isInterface())
            throw new IllegalArgumentException(String.format(
                    "Thunks may only be created for interfaces; %s is a class.",
                    PlasticUtils.toTypeName(proxyType)));

        return getInstantiator(proxyType).with(ObjectCreator.class, objectCreator).with(String.class, description)
                .newInstance();

    }

    private <T> ClassInstantiator<T> getInstantiator(Class<T> interfaceType)
    {
        ClassInstantiator<T> result = interfaceToInstantiator.get(interfaceType);

        if (result == null)
        {
            result = createInstantiator(interfaceType);
            interfaceToInstantiator.put(interfaceType, result);
        }

        return result;
    }

    private <T> ClassInstantiator<T> createInstantiator(final Class<T> interfaceType)
    {
        return proxyFactory.createProxy(interfaceType, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField objectCreatorField = plasticClass.introduceField(ObjectCreator.class, "creator")
                        .injectFromInstanceContext();

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(interfaceType.getName(), "delegate",
                        null, null);

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    @Override
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(objectCreatorField);
                        builder.invoke(CREATE_OBJECT);
                        builder.checkcast(interfaceType).returnResult();
                    }
                });

                for (Method method : interfaceType.getMethods())
                {
                    plasticClass.introduceMethod(method).delegateTo(delegateMethod);
                }

                if (!plasticClass.isMethodImplemented(PlasticUtils.TO_STRING_DESCRIPTION))
                {
                    final PlasticField descriptionField = plasticClass.introduceField(String.class, "description")
                            .injectFromInstanceContext();

                    plasticClass.introduceMethod(PlasticUtils.TO_STRING_DESCRIPTION, new InstructionBuilderCallback()
                    {
                        @Override
                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.loadThis().getField(descriptionField).returnResult();
                        }
                    });
                }
            }
        });
    }
}
