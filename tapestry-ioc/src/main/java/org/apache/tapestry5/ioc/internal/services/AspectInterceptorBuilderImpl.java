// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class AspectInterceptorBuilderImpl<T> implements AspectInterceptorBuilder<T>
{
    private final ClassFactory classFactory;

    private final Class<T> serviceInterface;

    private final ClassFab interceptorFab;

    private final ConstantInjectorImpl injector;

    private final String delegateFieldName;

    private final String description;

    private final OneShotLock lock = new OneShotLock();

    private final Set<Method> remainingMethods = CollectionFactory.newSet();

    private final Map<Method, AdvisedMethodInvocationBuilder> methodToBuilder = CollectionFactory.newMap();

    /**
     * Set to true if we ever see toString() as a method of the interface; either advised or pass thru. If false at the
     * end, we add our own implementation.
     */
    private boolean sawToString;

    public AspectInterceptorBuilderImpl(ClassFactory classFactory, Class<T> serviceInterface, T delegate,
                                        String description)
    {
        this.classFactory = classFactory;
        this.serviceInterface = serviceInterface;
        this.description = description;

        interceptorFab = this.classFactory.newClass(serviceInterface);

        injector = new ConstantInjectorImpl(interceptorFab);

        delegateFieldName = injector.inject(serviceInterface, delegate);

        remainingMethods.addAll(Arrays.asList(serviceInterface.getMethods()));
    }

    public void adviseMethod(Method method, MethodAdvice advice)
    {
        Defense.notNull(method, "method");
        Defense.notNull(advice, "advice");

        lock.check();

        AdvisedMethodInvocationBuilder builder = methodToBuilder.get(method);

        if (builder == null)
        {
            if (!remainingMethods.contains(method))
                throw new IllegalArgumentException(
                        String.format("Method %s is not defined for interface %s.", method, serviceInterface));

            // One less method to pass thru to the delegate

            remainingMethods.remove(method);

            sawToString |= ClassFabUtils.isToString(method);

            builder = new AdvisedMethodInvocationBuilder(classFactory, serviceInterface, method);

            methodToBuilder.put(method, builder);
        }

        builder.addAdvice(advice);
    }

    public void adviseAllMethods(MethodAdvice advice)
    {
        for (Method m : serviceInterface.getMethods())
            adviseMethod(m, advice);
    }

    public Class getInterface()
    {
        return serviceInterface;
    }

    public T build()
    {
        lock.lock();

        // Finish up each method that has been advised

        for (AdvisedMethodInvocationBuilder builder : methodToBuilder.values())
        {
            builder.commit(interceptorFab, delegateFieldName, injector);
        }

        // Hit all the methods that haven't been referenced so far.

        addPassthruMethods();

        // And if we haven't seen a toString(), we can add it now.

        if (!sawToString)
            interceptorFab.addToString(description);

        injector.implementConstructor();

        Class interceptorClass = interceptorFab.createClass();

        Object[] parameters = injector.getParameters();

        try
        {
            Constructor constructor = interceptorClass.getConstructors()[0];

            Object raw = constructor.newInstance(parameters);

            return serviceInterface.cast(raw);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void addPassthruMethods()
    {
        for (Method m : remainingMethods)
        {
            sawToString |= ClassFabUtils.isToString(m);

            MethodSignature sig = new MethodSignature(m);

            String body = String.format("return ($r) %s.%s($$);", delegateFieldName, m.getName());

            interceptorFab.addMethod(Modifier.PUBLIC, sig, body);
        }
    }
}
