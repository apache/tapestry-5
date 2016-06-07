/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tapestry5.internal.jpa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.jpa.EntityTransactionManager.VoidInvokable;
import org.slf4j.Logger;

public class PersistenceContextSpecificEntityTransactionManager
{

    private final Logger logger;
    private final EntityManager entityManager;

    private boolean transactionBeingCommitted;

    private Deque<Invokable<?>> invokableUnitsForSequentialTransactions = new ArrayDeque<Invokable<?>>();
    private Deque<Invokable<?>> invokableUnits = new ArrayDeque<Invokable<?>>();

    private List<Invokable<Boolean>> beforeCommitInvokables = new ArrayList<Invokable<Boolean>>();
    private List<Invokable<Boolean>> afterCommitInvokables = new ArrayList<Invokable<Boolean>>();

    public PersistenceContextSpecificEntityTransactionManager(Logger logger,
            EntityManager entityManager)
    {
        this.logger = logger;
        this.entityManager = entityManager;
    }

    private EntityTransaction getTransaction()
    {
        EntityTransaction transaction = entityManager.getTransaction();
        if (!transaction.isActive())
            transaction.begin();
        return transaction;
    }

    public void addBeforeCommitInvokable(Invokable<Boolean> invokable)
    {
        beforeCommitInvokables.add(invokable);
    }

    public void addAfterCommitInvokable(Invokable<Boolean> invokable)
    {
        afterCommitInvokables.add(invokable);
    }

    public <T> T invokeInTransaction(Invokable<T> invokable)
    {
        if (transactionBeingCommitted)
        {
            // happens for example if you try to run a transaction in @PostCommit hook. We can only
            // allow VoidInvokables
            // to be executed later
            if (invokable instanceof VoidInvokable)
            {
                invokableUnitsForSequentialTransactions.push(invokable);
                return null;
            }
            else
            {
                rollbackTransaction(getTransaction());
                throw new RuntimeException(
                        "Current transaction is already being committed. Transactions started @PostCommit are not allowed to return a value");
            }
        }

        final boolean topLevel = invokableUnits.isEmpty();
        invokableUnits.push(invokable);
        if (!topLevel)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Nested transaction detected, current depth = " + invokableUnits.size());
            }
        }

        final EntityTransaction transaction = getTransaction();
        try
        {
            T result = invokable.invoke();

            if (topLevel && invokableUnits.peek().equals(invokable))
            {
                // Success or checked exception:

                if (transaction.isActive())
                {
                    invokeBeforeCommit(transaction);
                }

                // FIXME check if we are still on top

                if (transaction.isActive())
                {
                    transactionBeingCommitted = true;
                    transaction.commit();
                    transactionBeingCommitted = false;
                    invokableUnits.clear();
                    invokeAfterCommit();
                    if (invokableUnitsForSequentialTransactions.size() > 0)
                        invokeInTransaction(invokableUnitsForSequentialTransactions.pop());
                }
            }

            return result;
        }
        catch (final RuntimeException e)
        {
            if (transaction != null && transaction.isActive())
            {
                rollbackTransaction(transaction);
            }

            throw e;
        }
        finally
        {
            invokableUnits.remove(invokable);
        }
    }

    private void invokeBeforeCommit(final EntityTransaction transaction)
    {
        for (Iterator<Invokable<Boolean>> i = beforeCommitInvokables.iterator(); i.hasNext();)
        {
            Invokable<Boolean> invokable = i.next();
            i.remove();
            Boolean beforeCommitSucceeded = tryInvoke(transaction, invokable);

            // Success or checked exception:
            if (beforeCommitSucceeded != null && !beforeCommitSucceeded.booleanValue())
            {
                rollbackTransaction(transaction);

                // Don't invoke further callbacks
                break;
            }
        }
    }

    private void invokeAfterCommit()
    {

        for (Iterator<Invokable<Boolean>> i = afterCommitInvokables.iterator(); i.hasNext();)
        {
            Invokable<Boolean> invokable = i.next();
            i.remove();
            Boolean afterCommitSucceeded = invokable.invoke();

            // Success or checked exception:
            if (afterCommitSucceeded != null && !afterCommitSucceeded.booleanValue())
            {
                if (invokableUnitsForSequentialTransactions.size() > 0) { throw new RuntimeException(
                        "After commit hook returned false but there are still uncommitted Invokables scheduled for the next transaction"); }
                return;
            }
        }
    }

    private static <T> T tryInvoke(final EntityTransaction transaction, Invokable<T> invokable)
            throws RuntimeException
    {
        T result;

        try
        {
            result = invokable.invoke();
        }
        catch (final RuntimeException e)
        {
            if (transaction != null && transaction.isActive())
            {
                rollbackTransaction(transaction);
            }

            throw e;
        }

        return result;
    }

    private static void rollbackTransaction(EntityTransaction transaction)
    {
        try
        {
            transaction.rollback();
        }
        catch (Exception e)
        { // Ignore
        }
    }
}
