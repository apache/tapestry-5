// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import java.util.Arrays;
import java.util.Set;

import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticField;

@SuppressWarnings("all")
public class AspectInterceptorBuilderImpl<T> extends AbtractAspectInterceptorBuilder<T>
{
    private final Class<T> serviceInterface;

    private final Set<Method> allMethods = CollectionFactory.newSet();

    private final PlasticClassTransformation transformation;

    private final PlasticClass plasticClass;

    public AspectInterceptorBuilderImpl(AnnotationAccess annotationAccess, PlasticProxyFactory plasticProxyFactory,
            Class<T> serviceInterface, T delegate, String description)
    {
        super(annotationAccess);

        this.serviceInterface = serviceInterface;

        transformation = plasticProxyFactory.createProxyTransformation(serviceInterface);
        plasticClass = transformation.getPlasticClass();

        plasticClass.addToString(description);

        allMethods.addAll(Arrays.asList(serviceInterface.getMethods()));

        PlasticField delegateField = plasticClass.introduceField(serviceInterface, "delegate").inject(delegate);

        for (Method method : allMethods)
        {
            plasticClass.introduceMethod(method).delegateTo(delegateField);
        }
    }

    public void adviseMethod(Method method, MethodAdvice advice)
    {
        assert method != null;
        assert advice != null;

        AnnotationProvider methodAnnotationProvider = getMethodAnnotationProvider(method.getName(),
                method.getParameterTypes());

        adviseMethod(method, InternalUtils.toPlasticMethodAdvice(advice, methodAnnotationProvider));
    }

    public void adviseAllMethods(MethodAdvice advice)
    {
        for (Method m : serviceInterface.getMethods())
            adviseMethod(m, advice);
    }

    public void adviseMethod(Method method, org.apache.tapestry5.plastic.MethodAdvice advice)
    {
        assert method != null;
        assert advice != null;

        if (!allMethods.contains(method))
            throw new IllegalArgumentException(String.format("Method %s is not defined for interface %s.", method,
                    serviceInterface));

        plasticClass.introduceMethod(method).addAdvice(advice);
    }

    public void adviseAllMethods(org.apache.tapestry5.plastic.MethodAdvice advice)
    {
        for (Method m : serviceInterface.getMethods())
        {
            adviseMethod(m, advice);
        }
    }

    public Class getInterface()
    {
        return serviceInterface;
    }

    public T build()
    {
        return (T) transformation.createInstantiator().newInstance();
    }
}
