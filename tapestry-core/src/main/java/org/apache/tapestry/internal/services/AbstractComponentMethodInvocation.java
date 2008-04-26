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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.services.ComponentMethodAdvice;
import org.apache.tapestry.services.ComponentMethodInvocation;

public abstract class AbstractComponentMethodInvocation implements ComponentMethodInvocation
{
    private final MethodInvocationInfo _info;

    private final ComponentResources _resources;

    private int _adviceIndex = 0;

    private Throwable _thrown;

    private Object _result;

    public AbstractComponentMethodInvocation(MethodInvocationInfo info, ComponentResources resources)
    {
        _info = info;
        _resources = resources;
    }

    public ComponentResources getComponentResources()
    {
        return _resources;
    }

    public String getMethodName()
    {
        return _info.getMethodName();
    }

    public Class getResultType()
    {
        return _info.getResultType();
    }

    public int getParameterCount()
    {
        return _info.getParameterCount();
    }

    public Class getParameterType(int index)
    {
        return _info.getParameterType(index);
    }

    /**
     * This first call is to the first advice.  When we run out of advice, we re-invoke.
     */
    public void proceed()
    {
        if (_adviceIndex >= _info.getAdviceCount())
        {
            invokeAdvisedMethod();
            return;
        }

        ComponentMethodAdvice advice = _info.getAdvice(_adviceIndex++);

        // When this advice invokes proceed(), we can advance to the next advice,
        // and then ultimately to the advised method.

        advice.advise(this);
    }

    /**
     * Implemented to reinvoke the method on the advised method of the component.
     */
    protected abstract void invokeAdvisedMethod();

    public boolean isFail()
    {
        return _thrown != null;
    }

    public <T extends Throwable> T getThrown(Class<T> throwableClass)
    {
        if (throwableClass.isInstance(_thrown))
            return throwableClass.cast(_thrown);

        return null;
    }

    public void overrideThrown(Exception thrown)
    {
        for (Class type : _info.getExceptionTypes())
        {
            if (type.isInstance(thrown))
            {
                _thrown = thrown;
                return;
            }
        }

        throw new IllegalArgumentException(
                String.format("Exception class %s is not a declared exception type for method %s().",
                              thrown.getClass(),
                              _info.getMethodName()));
    }

    public Object getResult()
    {
        return _result;
    }

    public void overrideResult(Object newResult)
    {
        if (newResult != null)
        {
            Class expectedType = _info.getEffectiveResultType();

            if (!expectedType.isInstance(newResult))
            {
                throw new IllegalArgumentException(
                        String.format("Invalid result value (%s) does not match return type %s for method %s.",
                                      newResult,
                                      expectedType.getName(),
                                      _info.getMethodName()));
            }
        }

        _result = newResult;
        _thrown = null;
    }


}
