// Copyright 2011 The Apache Software Foundation
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassListener;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.slf4j.Logger;

public class PlasticProxyFactoryImpl implements PlasticProxyFactory
{
    private final ClassFactory classFactory;

    private final Logger logger;

    private final PlasticManager manager;

    public PlasticProxyFactoryImpl(ClassFactory classFactory, ClassLoader parentClassLoader, Logger logger)
    {
        this.classFactory = classFactory;
        this.logger = logger;

        manager = new PlasticManager(parentClassLoader);

        if (logger != null)
        {
            manager.addPlasticClassListener(new PlasticClassListenerLogger(logger));
        }
    }

    public ClassLoader getClassLoader()
    {
        return manager.getClassLoader();
    }

    public <T> ClassInstantiator<T> createProxy(Class<T> interfaceType, PlasticClassTransformer callback)
    {
        return manager.createProxy(interfaceType, callback);
    }

    public PlasticClassTransformation createProxyTransformation(Class interfaceType)
    {
        return manager.createProxyTransformation(interfaceType);
    }

    public <T> T createProxy(final Class<T> interfaceType, final ObjectCreator<T> creator, final String description)
    {
        assert creator != null;
        assert InternalUtils.isNonBlank(description);

        ClassInstantiator<T> instantiator = createProxy(interfaceType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField objectCreatorField = plasticClass.introduceField(ObjectCreator.class, "creator")
                        .inject(creator);

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(interfaceType.getName(), "delegate",
                        null, null);

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(objectCreatorField);
                        builder.invoke(ObjectCreator.class, Object.class, "createObject");
                        builder.checkcast(interfaceType).returnResult();
                    }
                });

                for (Method method : interfaceType.getMethods())
                {
                    plasticClass.introduceMethod(method).delegateTo(delegateMethod);
                }

                plasticClass.addToString(description);
            }
        });

        return interfaceType.cast(instantiator.newInstance());
    }

    public Location getMethodLocation(Method method)
    {
        return classFactory.getMethodLocation(method);
    }

    public Location getConstructorLocation(Constructor constructor)
    {
        return classFactory.getConstructorLocation(constructor);
    }

    public void addPlasticClassListener(PlasticClassListener listener)
    {
        manager.addPlasticClassListener(listener);
    }

    public void removePlasticClassListener(PlasticClassListener listener)
    {
        manager.removePlasticClassListener(listener);
    }

}
