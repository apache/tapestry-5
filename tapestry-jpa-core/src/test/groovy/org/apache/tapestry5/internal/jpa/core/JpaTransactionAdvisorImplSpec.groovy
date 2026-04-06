// Copyright 2011, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jpa.core

import java.sql.SQLException

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityTransaction
import jakarta.persistence.PersistenceContext

import org.apache.tapestry5.ioc.annotations.ImportModule
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.ioc.services.AspectDecorator
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder
import org.apache.tapestry5.jpa.core.EntityManagerManager
import org.apache.tapestry5.jpa.core.EntityTransactionManager
import org.apache.tapestry5.jpa.core.annotations.CommitAfter
import org.apache.tapestry5.jpa.core.modules.JpaCoreModule
import org.apache.tapestry5.internal.jpa.core.EntityTransactionManagerImpl
import org.slf4j.Logger

import spock.lang.Specification

@ImportModule(JpaCoreModule.class)
class JpaTransactionAdvisorImplSpec extends Specification
{
    static final String UNIT_NAME = "FooUnit";

    @Inject AspectDecorator aspectDecorator

    def "doesn't advise for methods not annotated with @CommitAfter"()
    {
        given: "a mock delegate whose undecorated() method has no @CommitAfter annotation"
        VoidService delegate = Mock()

        and: "mock dependencies — empty map is sufficient, constructor only needs the map size"
        EntityManagerManager manager = Mock {
            getEntityManagers() >> [:]
        }
        EntityTransactionManager transactionManager = Mock()

        and: "an interceptor built with advice from the class under test"
        VoidService interceptor = buildInterceptor(delegate, manager, transactionManager)

        when: "the unannotated method is called via the interceptor"
        interceptor.undecorated()

        then: "the call passes straight through to the delegate with no transaction involvement"
        1 * delegate.undecorated()
        0 * manager.getEntityManagers()
        0 * transactionManager._
    }

    def "throws when EntityManager cannot be resolved unambiguously with multiple persistence units"()
    {
        given: "two EntityManagers configured — the advisor cannot pick one without a unitName"
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [A: Mock(EntityManager), B: Mock(EntityManager)]
        }

        and: "a real EntityTransactionManager so transaction logic actually executes"
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "an interceptor built with advice from the class under test"
        VoidService interceptor = buildInterceptor(Mock(VoidService), manager, transactionManager)

        when: "a @CommitAfter-annotated method with an unresolvable EntityManager is called"
        interceptor."$method"()

        then: "an exception is thrown because the advisor cannot identify which EntityManager to use"
        Exception e = thrown()
        e.message.contains("Unable to locate a single EntityManager")

        where:
        method                       | _
        "persistenceUnitNameMissing" | _ // @PersistenceContext present but unitName not set
        "persistenceUnitMissing"     | _ // no @PersistenceContext at all
    }

    def "succeeds when EntityManager can be resolved unambiguously with a single persistence unit"()
    {
        given: "a single EntityManager — the advisor can pick it without a unitName"
        EntityManager em = Mock()
        EntityTransaction transaction = Mock()

        and: "manager returns the single EntityManager for any lookup"
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [A: em]
        }

        and: "a real EntityTransactionManager so transaction logic actually executes"
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "a mock delegate and an interceptor built with advice from the class under test"
        VoidService delegate = Mock()
        VoidService interceptor = buildInterceptor(delegate, manager, transactionManager)

        when: "a @CommitAfter-annotated method with no explicit unitName is called"
        interceptor."$method"()

        then: "the single EntityManager is resolved, the delegate is called, and the transaction is committed"
        1 * em.getTransaction() >> transaction
        _ * transaction.isActive() >> true
        1 * delegate."$method"()
        1 * transaction.commit()

        where:
        method                       | _
        "persistenceUnitNameMissing" | _ // @PersistenceContext present but unitName not set
        "persistenceUnitMissing"     | _ // no @PersistenceContext at all
    }

    def "commits transaction after calling an annotated void method [#delegateMethod, #initiallyActive]"()
    {
        given: "a single EntityManager — UNIT_NAME must be a key in the map so the advisor wires up the correct MethodAdvice"
        EntityManager em = Mock()
        EntityTransaction transaction = Mock()

        and: "manager returns the EntityManager both during construction and during advice execution"
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [(UNIT_NAME): em]
            getEntityManager(UNIT_NAME) >> em
        }

        and: "a real EntityTransactionManager so transaction logic actually executes"
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "a mock delegate and an interceptor built with advice from the class under test"
        VoidService delegate = Mock()
        VoidService interceptor = buildInterceptor(delegate, manager, transactionManager)

        when: "an annotated method is called"
        invocation(interceptor)

        then: "begin() is called only when no transaction was active, the delegate is called, and the transaction is committed"
        1 * em.getTransaction() >> transaction
        1 * transaction.isActive() >> initiallyActive  // if false, begin() will be called to start a new transaction
        (initiallyActive ? 0 : 1) * transaction.begin()
        1 * delegate."$delegateMethod"(*_)
        _ * transaction.isActive() >> true             // subsequent isActive() checks after the delegate call
        1 * transaction.commit()

        where:
        initiallyActive | delegateMethod        | invocation
        false           | "voidMethod"          | { it.voidMethod() }            // no active transaction: begin() is called first
        true            | "voidMethod"          | { it.voidMethod() }            // active transaction: begin() is skipped
        true            | "voidMethodWithParam" | { it.voidMethodWithParam(777) } // same but with a method parameter
    }

    def "rolls back transaction and rethrows when delegate throws a RuntimeException"()
    {
        given: "a RuntimeException the delegate will throw"
        def re = new RuntimeException("Unexpected.")

        and: "a single EntityManager with an active transaction"
        EntityManager em = Mock()
        EntityTransaction transaction = Mock()
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [(UNIT_NAME): em]
            getEntityManager(UNIT_NAME) >> em
        }
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "a mock Performer delegate and an interceptor built with advice from the class under test"
        Performer delegate = Mock()
        Performer interceptor = buildPerformerInterceptor(delegate, manager, transactionManager)

        when: "the annotated method is called and the delegate throws a RuntimeException"
        interceptor.perform()

        then: "the transaction is rolled back and the original exception instance is rethrown"
        1 * em.getTransaction() >> transaction
        _ * transaction.isActive() >> true
        1 * delegate.perform() >> { throw re }
        1 * transaction.rollback()
        RuntimeException ex = thrown()
        ex.is(re)
    }

    def "commits transaction and rethrows when delegate throws a checked exception"()
    {
        given: "a checked exception the delegate will throw"
        def se = new SQLException("Checked.")

        and: "a single EntityManager with an active transaction"
        EntityManager em = Mock()
        EntityTransaction transaction = Mock()
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [(UNIT_NAME): em]
            getEntityManager(UNIT_NAME) >> em
        }
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "a mock Performer delegate and an interceptor built with advice from the class under test"
        Performer delegate = Mock()
        Performer interceptor = buildPerformerInterceptor(delegate, manager, transactionManager)

        when: "the annotated method is called and the delegate throws a checked exception"
        interceptor.perform()

        then: "the transaction is committed (not rolled back) and the original exception instance is rethrown"
        1 * em.getTransaction() >> transaction
        _ * transaction.isActive() >> true
        1 * delegate.perform() >> { throw se }
        1 * transaction.commit()
        SQLException ex = thrown()
        ex.is(se)
    }

    def "commits transaction and returns value after calling an annotated method with a return type [#delegateMethod]"()
    {
        given: "a single EntityManager with an active transaction"
        EntityManager em = Mock()
        EntityTransaction transaction = Mock()
        EntityManagerManager manager = Stub {
            getEntityManagers() >> [(UNIT_NAME): em]
            getEntityManager(UNIT_NAME) >> em
        }
        EntityTransactionManager transactionManager = new EntityTransactionManagerImpl(Stub(Logger), manager)

        and: "a real ReturnTypeService delegate (returnTypeMethod returns 'Foo', returnTypeMethodWithParam returns the sum)"
        ReturnTypeService delegate = [returnTypeMethod: { "Foo" }, returnTypeMethodWithParam: { a, b -> a + b }] as ReturnTypeService

        and: "an interceptor built with advice from the class under test"
        def advisor = newJpaTransactionAdvisor(manager, transactionManager)
        AspectInterceptorBuilder<ReturnTypeService> builder = aspectDecorator.createBuilder(ReturnTypeService.class, delegate, "foo.Bar")
        advisor.addTransactionCommitAdvice(builder)
        ReturnTypeService interceptor = builder.build()

        when: "an annotated method with a return type is called"
        def result = invocation(interceptor)

        then: "the transaction is committed and the return value from the delegate is preserved"
        1 * em.getTransaction() >> transaction
        _ * transaction.isActive() >> true
        1 * transaction.commit()
        result == expectedResult

        where:
        delegateMethod              | invocation                               || expectedResult
        "returnTypeMethod"          | ({ it.returnTypeMethod() })              || "Foo"  // String return type
        "returnTypeMethodWithParam" | ({ it.returnTypeMethodWithParam(5, 3) }) || 8      // int return type, result is 5 + 3
    }

    // ---- helpers ----

    def newJpaTransactionAdvisor(manager, transactionManager)
    {
        return new JpaTransactionAdvisorImpl(manager, transactionManager)
    }

    VoidService buildInterceptor(VoidService delegate, EntityManagerManager manager, EntityTransactionManager transactionManager)
    {
        def advisor = newJpaTransactionAdvisor(manager, transactionManager)
        AspectInterceptorBuilder<VoidService> builder = aspectDecorator.createBuilder(VoidService.class, delegate, "foo.Bar")
        advisor.addTransactionCommitAdvice(builder)
        return builder.build()
    }

    Performer buildPerformerInterceptor(Performer delegate, EntityManagerManager manager, EntityTransactionManager transactionManager)
    {
        def advisor = newJpaTransactionAdvisor(manager, transactionManager)
        AspectInterceptorBuilder<Performer> builder = aspectDecorator.createBuilder(Performer.class, delegate, "foo.Bar")
        advisor.addTransactionCommitAdvice(builder)
        return builder.build()
    }


    public interface VoidService
    {
        void undecorated()

        @CommitAfter
        @PersistenceContext
        void persistenceUnitNameMissing()

        @CommitAfter
        void persistenceUnitMissing()

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void voidMethod()

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void voidMethodWithParam(long id)
    }

    public interface Performer
    {
        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        void perform() throws SQLException
    }

    public interface ReturnTypeService
    {
        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        String returnTypeMethod()

        @CommitAfter
        @PersistenceContext(unitName = UNIT_NAME)
        int returnTypeMethodWithParam(int first, int second)
    }

}