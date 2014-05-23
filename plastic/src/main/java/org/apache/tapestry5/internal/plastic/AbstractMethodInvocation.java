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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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

    /**
     * Invoked from the implementation of {@link MethodInvocation#setReturnValue(Object)}.
     */
    protected void clearCheckedException()
    {
        checkedException = null;
    }

    @Override
    public void rethrow()
    {
        if (checkedException != null)
            throw new RuntimeException(checkedException);
    }

    @Override
    public boolean didThrowCheckedException()
    {
        return checkedException != null;
    }

    @Override
    public <T extends Throwable> T getCheckedException(Class<T> exceptionType)
    {
        assert exceptionType != null;

        if (exceptionType.isInstance(checkedException))
            return exceptionType.cast(checkedException);

        return null;
    }

    @Override
    public Object getInstance()
    {
        return instance;
    }

    @Override
    public InstanceContext getInstanceContext()
    {
        return instanceContext;
    }

    @Override
    public MethodInvocation proceed()
    {
        if (adviceIndex == bundle.advice.length)
            proceedToAdvisedMethod();
        else
            bundle.advice[adviceIndex++].advise(this);

        return this;
    }

    @Override
    public MethodInvocation setCheckedException(Exception exception)
    {
        checkedException = exception;

        return this;
    }

    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
    {
        return getAnnotation(annotationType) != null;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        return getMethod().getAnnotation(annotationType);
    }

    @Override
    public Method getMethod()
    {
        return bundle.getMethod(getInstance());
    }

    /** This is implemented in a runtime-generated subclass. */
    protected abstract void proceedToAdvisedMethod();
}
