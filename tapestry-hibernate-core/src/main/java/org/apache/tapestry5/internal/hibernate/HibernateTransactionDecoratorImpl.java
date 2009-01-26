// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.hibernate.HibernateTransactionAdvisor;
import org.apache.tapestry5.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

public class HibernateTransactionDecoratorImpl implements HibernateTransactionDecorator
{
    private final AspectDecorator aspectDecorator;

    private final HibernateTransactionAdvisor advisor;

    public HibernateTransactionDecoratorImpl(AspectDecorator aspectDecorator, HibernateTransactionAdvisor advisor)
    {
        this.aspectDecorator = aspectDecorator;
        this.advisor = advisor;
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

        advisor.addTransactionCommitAdvice(builder);

        return builder.build();
    }
}
