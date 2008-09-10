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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.internal.util.Defense;

import java.lang.reflect.Method;

/**
 * Base class for {@link org.apache.tapestry5.ioc.Invocation}, which is extended with a dynamically generated class
 * generated for each individual class and method.
 */
public abstract class AbstractInvocation implements Invocation
{
    private final MethodInfo methodInfo;

    private final Method method;

    private Throwable thrown;

    private Object result;

    private int adviceIndex = 0;

    protected AbstractInvocation(MethodInfo methodInfo)
    {
        this.methodInfo = methodInfo;

        method = methodInfo.getMethod();
    }

    @Override
    public String toString()
    {
        return String.format("Invocation[%s]", method);
    }

    public String getMethodName()
    {
        return method.getName();
    }

    public Class getResultType()
    {
        return method.getReturnType();
    }

    public int getParameterCount()
    {
        return method.getParameterTypes().length;
    }

    public Class getParameterType(int index)
    {
        return method.getParameterTypes()[index];
    }

    public boolean isFail()
    {
        return thrown != null;
    }

    public <T extends Throwable> T getThrown(Class<T> throwableClass)
    {
        Defense.notNull(throwableClass, "throwableClass");

        if (throwableClass.isInstance(thrown)) return throwableClass.cast(thrown);

        return null;
    }

    public void overrideThrown(Exception thrown)
    {
        Defense.notNull(thrown, "thrown");

        for (Class t : method.getExceptionTypes())
        {
            if (t.isInstance(thrown))
            {
                this.thrown = thrown;
                return;
            }
        }

        throw new IllegalArgumentException(String.format("Exception %s is not a declared exception of method %s.",
                                                         thrown.getClass().getName(), method));
    }

    public Object getResult()
    {
        return result;
    }

    public void overrideResult(Object newResult)
    {
        result = newResult;
        thrown = null;
    }

    public void proceed()
    {
        if (adviceIndex >= methodInfo.getAdviceCount())
        {
            invokeDelegateMethod();
            return;
        }

        methodInfo.getAdvice(adviceIndex++).advise(this);
    }

    /**
     * This method is filled in, in the dynamically generated subclass.
     */
    protected abstract void invokeDelegateMethod();
}
