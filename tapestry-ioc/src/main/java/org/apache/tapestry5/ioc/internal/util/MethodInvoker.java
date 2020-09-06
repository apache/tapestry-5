// Copyright 2011-2013 The Apache Software Foundation
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


package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.Invokable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @since 5.3
 */
public class MethodInvoker<T> implements Invokable<T>
{
    private final Object instance;

    private final Method method;

    private final ObjectCreator[] methodParameters;

    public MethodInvoker(Object instance, Method method, ObjectCreator[] methodParameters)
    {
        this.instance = instance;
        this.method = method;
        this.methodParameters = methodParameters;
    }

    @Override
    public T invoke()
    {
        Throwable fail;

        Object[] realized = InternalUtils.realizeObjects(methodParameters);

        try
        {
            Object result = method.invoke(instance, realized);

            return (T) result;
        } catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        } catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(String.format("Error invoking method %s: %s",
                method, ExceptionUtils.toMessage(fail)), fail);
    }
}
