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

package org.apache.tapestry5.internal.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.jpa.EntityManagerManager;

public class CommitAfterMethodAdvice implements MethodAdvice
{

    private final EntityManagerManager manager;

    public CommitAfterMethodAdvice(final EntityManagerManager manager)
    {
        super();
        this.manager = manager;
    }

    public void advise(final Invocation invocation)
    {
        final EntityTransaction transaction = getTransaction(invocation);

        if (transaction != null && !transaction.isActive())
        {
            transaction.begin();
        }

        try
        {
            invocation.proceed();
        }
        catch (final RuntimeException e)
        {
            if (transaction != null && transaction.isActive())
            {
                transaction.rollback();
            }

            throw e;
        }

        // Success or checked exception:

        if (transaction != null && transaction.isActive())
        {
            transaction.commit();
        }

    }

    private EntityTransaction getTransaction(final Invocation invocation)
    {
        final PersistenceUnit persistenceUnit = invocation
                .getMethodAnnotation(PersistenceUnit.class);

        EntityManager em = JpaInternalUtils.getEntityManager(manager, persistenceUnit);

        if (em == null)
            return null;

        return em.getTransaction();
    }

}
