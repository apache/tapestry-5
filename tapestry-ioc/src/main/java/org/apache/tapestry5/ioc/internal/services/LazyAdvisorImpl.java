// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.annotations.NotLazy;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.LazyAdvisor;
import org.apache.tapestry5.ioc.services.ThunkCreator;

import java.lang.reflect.Method;

public class LazyAdvisorImpl implements LazyAdvisor
{
    private final ThunkCreator thunkCreator;

    public LazyAdvisorImpl(ThunkCreator thunkCreator)
    {
        this.thunkCreator = thunkCreator;
    }

    public void addLazyMethodInvocationAdvice(MethodAdviceReceiver methodAdviceReceiver)
    {
        for (Method m : methodAdviceReceiver.getInterface().getMethods())
        {
            if (filter(m))
                addAdvice(m, methodAdviceReceiver);
        }
    }

    private void addAdvice(Method method, MethodAdviceReceiver receiver)
    {
        final Class thunkType = method.getReturnType();

        final String description = String.format("<%s Thunk for %s>",
                                                 thunkType.getName(),
                                                 InternalUtils.asString(method));

        MethodAdvice advice = new MethodAdvice()
        {
            /**
             * When the method is invoked, we don't immediately proceed. Intead, we return a thunk instance
             * that defers its behavior to the lazily invoked invocation.
             */
            public void advise(final Invocation invocation)
            {
                ObjectCreator deferred = new ObjectCreator()
                {
                    public Object createObject()
                    {
                        invocation.proceed();

                        return invocation.getResult();
                    }
                };

                ObjectCreator cachingObjectCreator = new CachingObjectCreator(deferred);

                Object thunk = thunkCreator.createThunk(thunkType, cachingObjectCreator, description);

                invocation.overrideResult(thunk);
            }
        };

        receiver.adviseMethod(method, advice);
    }

    private boolean filter(Method method)
    {
        if (method.getAnnotation(NotLazy.class) != null) return false;

        if (!method.getReturnType().isInterface()) return false;

        for (Class extype : method.getExceptionTypes())
        {
            if (!RuntimeException.class.isAssignableFrom(extype)) return false;
        }

        return true;
    }
}
