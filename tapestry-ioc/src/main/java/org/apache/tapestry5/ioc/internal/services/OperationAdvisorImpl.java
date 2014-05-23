// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Operation;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.OperationAdvisor;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

import java.lang.reflect.Method;

@PreventServiceDecoration
public class OperationAdvisorImpl implements OperationAdvisor
{
    private final OperationTracker tracker;

    public OperationAdvisorImpl(OperationTracker tracker)
    {
        this.tracker = tracker;
    }

    private Runnable toRunnable(final MethodInvocation invocation)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                invocation.proceed();
            }
        };
    }

    private class SimpleAdvice implements MethodAdvice
    {
        private final String description;

        SimpleAdvice(String description)
        {
            this.description = description;
        }

        @Override
        public void advise(MethodInvocation invocation)
        {
            tracker.run(description, toRunnable(invocation));
        }
    }

    private class FormattedAdvice implements MethodAdvice
    {
        private final String format;

        FormattedAdvice(String format)
        {
            this.format = format;
        }

        @Override
        public void advise(MethodInvocation invocation)
        {
            Object[] parameters = extractParameters(invocation);

            String description = String.format(format, parameters);

            tracker.run(description, toRunnable(invocation));
        }

        private Object[] extractParameters(MethodInvocation invocation)
        {
            int count = invocation.getMethod().getParameterTypes().length;

            Object[] result = new Object[count];

            for (int i = 0; i < count; i++)
            {
                result[i] = invocation.getParameter(i);
            }

            return result;
        }
    }

    @Override
    public void addOperationAdvice(MethodAdviceReceiver receiver)
    {
        for (Method m : receiver.getInterface().getMethods())
        {

            Operation annotation = receiver.getMethodAnnotation(m, Operation.class);

            if (annotation != null)
            {
                String value = annotation.value();

                receiver.adviseMethod(m, createAdvice(value));
            }
        }
    }

    @Override
    public MethodAdvice createAdvice(String description)
    {
        assert InternalUtils.isNonBlank(description);

        if (description.contains("%"))
        {
            return new FormattedAdvice(description);
        }

        return new SimpleAdvice(description);
    }
}
