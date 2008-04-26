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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.services.ComponentMethodAdvice;
import org.apache.tapestry.services.TransformMethodSignature;

import java.util.List;

/**
 * A companion to {@link org.apache.tapestry.internal.services.AbstractComponentMethodInvocation} that stores most of
 * the method and advice information needed.
 */
public class MethodInvocationInfo
{
    private final TransformMethodSignature _methodSignature;

    private final ComponentClassCache _componentClassCache;

    private final List<ComponentMethodAdvice> _advice = CollectionFactory.newList();

    private Class _effectiveResultType;

    public MethodInvocationInfo(TransformMethodSignature methodSignature, ComponentClassCache componentClassCache)
    {
        _methodSignature = methodSignature;
        _componentClassCache = componentClassCache;
    }

    public String getMethodName()
    {
        return _methodSignature.getMethodName();
    }

    public Class getResultType()
    {
        return _componentClassCache.forName(_methodSignature.getReturnType());
    }

    public synchronized Class getEffectiveResultType()
    {
        if (_effectiveResultType == null)
        {
            Class resultType = getResultType();

            _effectiveResultType = resultType.isPrimitive() ? ClassFabUtils.getWrapperType(resultType) : resultType;
        }

        return _effectiveResultType;
    }

    public int getParameterCount()
    {
        return _methodSignature.getParameterTypes().length;
    }

    public Class getParameterType(int index)
    {
        return _componentClassCache.forName(_methodSignature.getParameterTypes()[index]);
    }

    public int getAdviceCount()
    {
        return _advice.size();
    }

    public ComponentMethodAdvice getAdvice(int index)
    {
        return _advice.get(index);
    }

    public void addAdvice(ComponentMethodAdvice advice)
    {
        // Ultimately, the mutable portion of this object's lifecycle all occurs inside a synchronized block defined by
        // the class loader.  After that the _advice list is only accessed for reads.  I don't think there
        // are any concurrency issues with this approach.

        _advice.add(advice);
    }

    public Class[] getExceptionTypes()
    {
        String[] exceptionTypes = _methodSignature.getExceptionTypes();
        int count = exceptionTypes.length;

        Class[] result = new Class[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = _componentClassCache.forName(exceptionTypes[i]);
        }

        return result;
    }

    public Class getExceptionType(int index)
    {
        return _componentClassCache.forName(_methodSignature.getExceptionTypes()[index]);
    }
}
