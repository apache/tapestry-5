// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.util.List;

/**
 * A companion to {@link org.apache.tapestry5.internal.services.AbstractComponentMethodInvocation} that stores most of
 * the method and advice information needed.
 */
public class ComponentMethodInvocationInfo
{
    private final TransformMethodSignature methodSignature;

    private final ComponentClassCache componentClassCache;

    private final List<ComponentMethodAdvice> advice = CollectionFactory.newList();

    private Class effectiveResultType;

    public ComponentMethodInvocationInfo(TransformMethodSignature methodSignature,
                                         ComponentClassCache componentClassCache)
    {
        this.methodSignature = methodSignature;
        this.componentClassCache = componentClassCache;
    }

    public String getMethodName()
    {
        return methodSignature.getMethodName();
    }

    public Class getResultType()
    {
        return componentClassCache.forName(methodSignature.getReturnType());
    }

    public synchronized Class getEffectiveResultType()
    {
        if (effectiveResultType == null)
        {
            Class resultType = getResultType();

            effectiveResultType = resultType.isPrimitive() ? ClassFabUtils.getWrapperType(resultType) : resultType;
        }

        return effectiveResultType;
    }

    public int getParameterCount()
    {
        return methodSignature.getParameterTypes().length;
    }

    public Class getParameterType(int index)
    {
        return componentClassCache.forName(methodSignature.getParameterTypes()[index]);
    }

    public int getAdviceCount()
    {
        return advice.size();
    }

    public ComponentMethodAdvice getAdvice(int index)
    {
        return advice.get(index);
    }

    public void addAdvice(ComponentMethodAdvice advice)
    {
        // Ultimately, the mutable portion of this object's lifecycle all occurs inside a synchronized block defined by
        // the class loader.  After that the advice list is only accessed for reads.  I don't think there
        // are any concurrency issues with this approach.

        this.advice.add(advice);
    }

    public Class[] getExceptionTypes()
    {
        String[] exceptionTypes = methodSignature.getExceptionTypes();
        int count = exceptionTypes.length;

        Class[] result = new Class[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = componentClassCache.forName(exceptionTypes[i]);
        }

        return result;
    }

    public Class getExceptionType(int index)
    {
        return componentClassCache.forName(methodSignature.getExceptionTypes()[index]);
    }
}
