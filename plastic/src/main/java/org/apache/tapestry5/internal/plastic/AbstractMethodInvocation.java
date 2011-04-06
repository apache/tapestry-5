// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.MethodInvocation;

public abstract class AbstractMethodInvocation implements MethodInvocation
{
    private final Object instance;

    private final InstanceContext instanceContext;

    private final MethodInvocationBundle bundle;

    private int adviceIndex;

    protected AbstractMethodInvocation(Object instance, InstanceContext instanceContext, MethodInvocationBundle bundle)
    {
        this.instance = instance;
        this.instanceContext = instanceContext;
        this.bundle = bundle;
    }

    private Exception checkedException;

    public void rethrow()
    {
        if (checkedException != null)
            throw new RuntimeException(checkedException);
    }

    public boolean didThrowCheckedException()
    {
        return checkedException != null;
    }

    public <T extends Throwable> T getCheckedException(Class<T> exceptionType)
    {
        assert exceptionType != null;

        if (exceptionType.isInstance(checkedException))
            return exceptionType.cast(checkedException);

        return null;
    }

    public Object getInstance()
    {
        return instance;
    }

    public InstanceContext getInstanceContext()
    {
        return instanceContext;
    }

    public MethodInvocation proceed()
    {
        if (adviceIndex == bundle.advice.length)
            proceedToAdvisedMethod();
        else
            bundle.advice[adviceIndex++].advise(this);

        return this;
    }

    public MethodInvocation setCheckedException(Exception exception)
    {
        checkedException = exception;

        return this;
    }

    public String getMethodName()
    {
        return bundle.methodDescription.methodName;
    }

    protected abstract void proceedToAdvisedMethod();
}
