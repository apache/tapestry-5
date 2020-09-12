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
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.jpa.EntityManagerManager;
import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.jpa.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.apache.tapestry5.test.ioc.TestBase;
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);

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
    public void persistence_unit_name_missing()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
        Map<String, EntityManager> managers = CollectionFactory.newMap();
        managers.put("A", newMock(EntityManager.class));
        managers.put("B", newMock(EntityManager.class));

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        expect(manager.getEntityManagers()).andReturn(managers);

        replay();

        try
        {
            interceptor.persistenceUnitNameMissing();
            TestBase.unreachable();
        } catch (Exception e)
        {
            Assert.assertEquals(e.getMessage(), "Unable to locate a single EntityManager. " +
                    "You must provide the persistence unit name as defined in the persistence.xml using the @PersistenceContext annotation.");
        }
        verify();
    }

    @Test
    public void persistence_unit_name_missing_single_unit_configured()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
        final EntityTransaction transaction = newMock(EntityTransaction.class);
        EntityManager em = newMock(EntityManager.class);
        Map<String, EntityManager> managers = CollectionFactory.newMap();
        managers.put("A", em);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        expect(manager.getEntityManagers()).andReturn(managers);
        train_getTransaction(em, transaction, true);
        delegate.persistenceUnitNameMissing();
        train_commitActiveTransaction(transaction);

        replay();
        interceptor.persistenceUnitNameMissing();
        verify();
    }

    @Test
    public void persistence_unit_missing()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
        Map<String, EntityManager> managers = CollectionFactory.newMap();
        managers.put("A", newMock(EntityManager.class));
        managers.put("B", newMock(EntityManager.class));

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        expect(manager.getEntityManagers()).andReturn(managers);

        replay();

        try
        {
            interceptor.persistenceUnitMissing();
            TestBase.unreachable();
        } catch (Exception e)
        {
            assertMessageContains(e, "Unable to locate a single EntityManager");
        }

        verify();
    }

    @Test
    public void persistence_unit_missing_single_unit_configured()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
        final EntityTransaction transaction = newMock(EntityTransaction.class);
        EntityManager em = newMock(EntityManager.class);
        Map<String, EntityManager> managers = CollectionFactory.newMap();
        managers.put("A", em);

        final AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(
                VoidService.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final VoidService interceptor = builder.build();

        expect(manager.getEntityManagers()).andReturn(managers);
        train_getTransaction(em, transaction, true);
        delegate.persistenceUnitMissing();
        train_commitActiveTransaction(transaction);

        replay();
        interceptor.persistenceUnitMissing();
        verify();
    }

    @Test
    public void transaction_inactive()
    {
        final VoidService delegate = newMock(VoidService.class);
        final EntityManagerManager manager = newMock(EntityManagerManager.class);
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        } catch (final RuntimeException ex)
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
        final EntityManager entityManager = newMock(EntityManager.class);
        final EntityTransaction transaction = newMock(EntityTransaction.class);
        final SQLException se = new SQLException("Checked.");

        final AspectInterceptorBuilder<Performer> builder = aspectDecorator.createBuilder(
                Performer.class, delegate, "foo.Bar");

        advisor.addTransactionCommitAdvice(builder);

        final Performer interceptor = builder.build();

        train_getActiveTransaction(manager, entityManager, transaction);

        delegate.perform();
        setThrowable(se);

        train_commitActiveTransaction(transaction);

        replay();


        try
        {
            interceptor.perform();

            unreachable();
        } catch (final SQLException ex)
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        final EntityTransactionManager transactionManager = newMock(EntityTransactionManager.class);
        final JpaTransactionAdvisor advisor = newJpaTransactionAdvisor(manager, transactionManager);
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
        train_getTransaction(entityManager, transaction, isActive);
    }

    private void train_getTransaction(
            final EntityManager entityManager, final EntityTransaction transaction,
            final boolean isActive)
    {
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

    private JpaTransactionAdvisor newJpaTransactionAdvisor(final EntityManagerManager manager,
            EntityTransactionManager transactionManager)
    {
        return new JpaTransactionAdvisorImpl(manager, transactionManager);
    }

    private ReturnTypeService newTestService()
    {
        return new ReturnTypeService()
        {

            @Override
            public String returnTypeMethod()
            {
                return "Foo";
            }

            @Override
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
        @PersistenceContext(unitName = UNIT_NAME)
        String returnTypeMethod();

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        int returnTypeMethodWithParam(int first, int second);

        @Override
        String toString();
    }

    public interface VoidService
    {
        void undecorated();

        @CommitAfter
        @PersistenceContext
        void persistenceUnitNameMissing();

        @CommitAfter
        void persistenceUnitMissing();

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void voidMethod();

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void voidMethodWithParam(long id);
    }

    public interface Performer
    {
        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void perform() throws SQLException;
    }

    public interface Service
    {
    	void perform();
    }

    public class ServiceImpl implements Service {
    	@Override
    	@CommitAfter
    	@PersistenceContext(unitName = UNIT_NAME)
        public void perform()
    	{

    	}
    }

}
