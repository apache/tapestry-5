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

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JpaTransactionAdvisorImplTest extends IOCTestCase
{
    private static final String UNIT_NAME = "FooUnit";

    private Registry registry;

    private AspectDecorator aspectDecorator;

    @BeforeClass
    public void setup()
    {
        registry = IOCUtilities.buildDefaultRegistry();

        aspectDecorator = registry.getService(AspectDecorator.class);
    }

    @AfterClass
    public void shutdown()
    {
        registry.shutdown();

        aspectDecorator = null;
        registry = null;
    }

    @Test
    public void undecorated()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        delegate.undecorated();

        replay();
        interceptor.undecorated();
        verify();
    }

    @Test
    public void persistence_unit_missing()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        delegate.persistenceUnitMissing();

        replay();
        interceptor.persistenceUnitMissing();
        verify();
    }

    @Test
    public void persistence_unit_name_missing()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        delegate.persistenceUnitNameMissing();

        replay();
        interceptor.persistenceUnitNameMissing();
        verify();
    }

    @Test
    public void transaction_inactive()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        train_getAndBeginTransaction(manager, entityManager, transaction);

        delegate.voidMethod();

        train_commitActiveTransaction(transaction);

        replay();
        interceptor.voidMethod();
        verify();
    }

    @Test
    public void void_method()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);

        delegate.voidMethod();

        train_commitActiveTransaction(transaction);

        replay();
        interceptor.voidMethod();
        verify();
    }

    @Test
    public void void_method_with_param()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);
        delegate.voidMethodWithParam(777);
        train_commitActiveTransaction(transaction);

        replay();
        interceptor.voidMethodWithParam(777);
        verify();
    }

    @Test
    public void runtime_exception_will_abort_transaction() throws Exception
    {
        final Performer delegate = newMock(Performer.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);
        final RuntimeException re = new RuntimeException("Unexpected.");

        final AspectInterceptorBuilder<Performer> builder = aspectDecorator.createBuilder(
                Performer.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final Performer interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);
        delegate.perform();
        TestBase.setThrowable(re);
        train_rollBackActiveTransaction(transaction);

        replay();
        try
        {
            interceptor.perform();
            TestBase.unreachable();
        }
        catch (final RuntimeException ex)
        {
            Assert.assertSame(ex, re);
        }

        verify();
    }

    @Test
    public void checked_exception_will_commit_transaction() throws Exception
    {
        final Performer delegate = newMock(Performer.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);
        final SQLException se = new SQLException("Checked.");

        final AspectInterceptorBuilder<Performer> builder = aspectDecorator.createBuilder(
                Performer.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final Performer interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);
        delegate.perform();
        TestBase.setThrowable(se);
        train_commitActiveTransaction(transaction);

        replay();
        try
        {
            interceptor.perform();
            TestBase.unreachable();
        }
        catch (final SQLException ex)
        {
            Assert.assertSame(ex, se);
        }

        verify();
    }

    @Test
    public void return_type_method()
    {
        final ReturnTypeService delegate = newTestService();
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);

        final AspectInterceptorBuilder<ReturnTypeService> builder = aspectDecorator.createBuilder(
                ReturnTypeService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final ReturnTypeService interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);
        delegate.returnTypeMethod();
        train_commitActiveTransaction(transaction);

        replay();
        Assert.assertEquals(interceptor.returnTypeMethod(), "Foo");
        verify();
    }

    @Test
    public void return_type_method_with_param()
    {
        final ReturnTypeService delegate = newTestService();
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);

        final AspectInterceptorBuilder<ReturnTypeService> builder = aspectDecorator.createBuilder(
                ReturnTypeService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final ReturnTypeService interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);
        delegate.returnTypeMethodWithParam(5, 3);
        train_commitActiveTransaction(transaction);

        replay();
        Assert.assertEquals(interceptor.returnTypeMethodWithParam(5, 3), 8);
        verify();
    }

    private void train_getAndBeginTransaction(final EntityManagerManager manager,
            final EntityManager entityManager, final EntityTransaction transaction)
    {
        train_getTransaction(manager, entityManager, transaction, false);
        transaction.begin();
    }

    private void train_getActiveTransaction(final EntityManagerManager manager,
            final EntityManager entityManager, final EntityTransaction transaction)
    {
        train_getTransaction(manager, entityManager, transaction, true);
    }

    private void train_getTransaction(final EntityManagerManager manager,
            final EntityManager entityManager, final EntityTransaction transaction,
            final boolean isActive)
    {
        expect(manager.getEntityManager(UNIT_NAME)).andReturn(entityManager);
        expect(entityManager.getTransaction()).andReturn(transaction);
        expect(transaction.isActive()).andReturn(isActive);
    }

    private void train_commitActiveTransaction(final EntityTransaction transaction)
    {
        expect(transaction.isActive()).andReturn(true);
        transaction.commit();
    }

    private void train_rollBackActiveTransaction(final EntityTransaction transaction)
    {
        expect(transaction.isActive()).andReturn(true);
        transaction.rollback();
    }

    private JpaTransactionAdvisor newJpaTransactionAdvisor(final EntityManagerManager manager)
    {
        return new JpaTransactionAdvisorImpl(manager);
    }

    private ReturnTypeService newTestService()
    {
        return new ReturnTypeService()
        {

            public String returnTypeMethod()
            {
                return "Foo";
            }

            public int returnTypeMethodWithParam(final int first, final int second)
            {
                return first + second;
            }

            @Override
            public String toString()
            {
                return "Baz";
            }
        };
    }

    public interface ReturnTypeService
    {
        @CommitAfter
        @PersistenceUnit(unitName = UNIT_NAME)
        String returnTypeMethod();

        @CommitAfter
        @PersistenceUnit(unitName = UNIT_NAME)
        int returnTypeMethodWithParam(int first, int second);

        String toString();
    }

    public interface VoidService
    {
        void undecorated();

        @CommitAfter
        @PersistenceUnit
        void persistenceUnitMissing();

        @CommitAfter
        void persistenceUnitNameMissing();

        @CommitAfter
        @PersistenceUnit(unitName = UNIT_NAME)
        void voidMethod();

        @CommitAfter
        @PersistenceUnit(unitName = UNIT_NAME)
        void voidMethodWithParam(long id);
    }

    public interface Performer
    {
        @CommitAfter
        @PersistenceUnit(unitName = UNIT_NAME)
        void perform() throws SQLException;
    }
}
