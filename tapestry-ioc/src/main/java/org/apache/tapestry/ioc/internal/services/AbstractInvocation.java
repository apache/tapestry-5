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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.Invocation;
import org.apache.tapestry.ioc.internal.util.Defense;

import java.lang.reflect.Method;

/**
 * Base class for {@link org.apache.tapestry.ioc.Invocation}, which is extended with a dynamically generated class
 * generated for each individual class and method.
 */
public abstract class AbstractInvocation implements Invocation
{
    private final Method _method;

    private Throwable _thrown;

    private Object _result;

    @Override
    public String toString()
    {
        return String.format("Invocation[%s]", _method);
    }

    protected AbstractInvocation(Method method)
    {
        _method = method;
    }

    public String getMethodName()
    {
        return _method.getName();
    }

    public Class getResultType()
    {
        return _method.getReturnType();
    }

    public int getParameterCount()
    {
        return _method.getParameterTypes().length;
    }

    public Class getParameterType(int index)
    {
        return _method.getParameterTypes()[index];
    }

    public boolean isFail()
    {
        return _thrown != null;
    }

    public <T extends Throwable> T getThrown(Class<T> throwableClass)
    {
        Defense.notNull(throwableClass, "throwableClass");

        if (throwableClass.isInstance(_thrown)) return throwableClass.cast(_thrown);

        return null;
    }

    public void overrideThrown(Exception thrown)
    {
        Defense.notNull(thrown, "thrown");

        for (Class t : _method.getExceptionTypes())
        {
            if (t.isInstance(thrown))
            {
                _thrown = thrown;
                return;
            }
        }

        throw new IllegalArgumentException(String.format("Exception %s is not a declared exception of method %s.",
                                                         thrown.getClass().getName(), _method));
    }

    public Object getResult()
    {
        return _result;
    }

    public void overrideResult(Object newResult)
    {
        _result = newResult;
        _thrown = null;
    }
}
