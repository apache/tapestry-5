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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;

public abstract class AbstractComponentMethodInvocation implements ComponentMethodInvocation
{
    private final ComponentMethodInvocationInfo info;

    private final ComponentResources resources;

    private int adviceIndex = 0;

    private Throwable thrown;

    private Object result;

    public AbstractComponentMethodInvocation(ComponentMethodInvocationInfo info, ComponentResources resources)
    {
        this.info = info;
        this.resources = resources;
    }

    public ComponentResources getComponentResources()
    {
        return resources;
    }

    public String getMethodName()
    {
        return info.getMethodName();
    }

    public Class getResultType()
    {
        return info.getResultType();
    }

    public int getParameterCount()
    {
        return info.getParameterCount();
    }

    public Class getParameterType(int index)
    {
        return info.getParameterType(index);
    }

    /**
     * This first call is to the first advice.  When we run out of advice, we re-invoke.
     */
    public void proceed()
    {
        if (adviceIndex >= info.getAdviceCount())
        {
            invokeAdvisedMethod();
            return;
        }

        ComponentMethodAdvice advice = info.getAdvice(adviceIndex++);

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
        return thrown != null;
    }

    public <T extends Throwable> T getThrown(Class<T> throwableClass)
    {
        if (throwableClass.isInstance(thrown))
            return throwableClass.cast(thrown);

        return null;
    }

    public void overrideThrown(Exception thrown)
    {
        for (Class type : info.getExceptionTypes())
        {
            if (type.isInstance(thrown))
            {
                this.thrown = thrown;
                return;
            }
        }

        throw new IllegalArgumentException(
                String.format("Exception class %s is not a declared exception type for method %s().",
                              thrown.getClass(),
                              info.getMethodName()));
    }

    public Object getResult()
    {
        return result;
    }

    public void overrideResult(Object newResult)
    {
        if (newResult != null)
        {
            Class expectedType = info.getEffectiveResultType();

            if (!expectedType.isInstance(newResult))
            {
                throw new IllegalArgumentException(
                        String.format("Invalid result value (%s) does not match return type %s for method %s.",
                                      newResult,
                                      expectedType.getName(),
                                      info.getMethodName()));
            }
        }

        result = newResult;
        thrown = null;
    }
}
