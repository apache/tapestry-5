// Copyright 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

public class CommitAfterMethodAdvice implements MethodAdvice
{
    private final EntityManagerManager manager;

    private final PersistenceContext annotation;

    public CommitAfterMethodAdvice(final EntityManagerManager manager, PersistenceContext annotation)
    {
        this.manager = manager;
        this.annotation = annotation;
    }

    public void advise(final MethodInvocation invocation)
    {
        final EntityTransaction transaction = getTransaction();

        if (transaction != null && !transaction.isActive())
        {
            transaction.begin();
        }

        try
        {
            invocation.proceed();
        } catch (final RuntimeException e)
        {
            if (transaction != null && transaction.isActive())
            {
                rollbackTransaction(transaction);
            }

            throw e;
        }

        // Success or checked exception:

        if (transaction != null && transaction.isActive())
        {
            transaction.commit();
        }

    }

    private void rollbackTransaction(EntityTransaction transaction)
    {
        try
        {
            transaction.rollback();
        } catch (Exception e)
        { // Ignore
        }
    }

    private EntityTransaction getTransaction()
    {
        EntityManager em = JpaInternalUtils.getEntityManager(manager, annotation);

        if (em == null)
            return null;

        return em.getTransaction();
    }

}
