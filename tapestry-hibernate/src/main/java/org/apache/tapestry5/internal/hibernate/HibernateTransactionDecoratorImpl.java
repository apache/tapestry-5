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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

import java.lang.reflect.Method;

public class HibernateTransactionDecoratorImpl implements HibernateTransactionDecorator
{
    private final AspectDecorator aspectDecorator;

    private final HibernateSessionManager manager;

    /**
     * The rules for advice are the same for any method: commit on success or checked exception, abort on thrown
     * exception ... so we can use a single shared advice object.
     */
    private final MethodAdvice advice = new MethodAdvice()
    {
        public void advise(Invocation invocation)
        {
            try
            {
                invocation.proceed();
            }
            catch (RuntimeException ex)
            {
                manager.abort();

                throw ex;
            }

            // For success or checked exception, commit the transaction.

            manager.commit();
        }
    };

    public HibernateTransactionDecoratorImpl(AspectDecorator aspectDecorator, HibernateSessionManager manager)
    {
        this.aspectDecorator = aspectDecorator;

        this.manager = manager;
    }

    public <T> T build(Class<T> serviceInterface, T delegate, String serviceId)
    {
        Defense.notNull(serviceInterface, "serviceInterface");
        Defense.notNull(delegate, "delegate");
        Defense.notBlank(serviceId, "serviceId");

        String description = String.format("<Hibernate Transaction interceptor for %s(%s)>",
                                           serviceId,
                                           serviceInterface.getName());

        AspectInterceptorBuilder<T> builder = aspectDecorator.createBuilder(serviceInterface, delegate, description);

        for (Method m : serviceInterface.getMethods())
        {
            if (m.getAnnotation(CommitAfter.class) != null)
            {
                builder.adviseMethod(m, advice);
            }
        }

        return builder.build();
    }
}
