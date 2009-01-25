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

import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

@SuppressWarnings({ "ThrowableInstanceNeverThrown" })
public class HibernateTransactionDecoratorImplTest extends IOCTestCase
{
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
        VoidService delegate = newMock(VoidService.class);
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.undecorated();

        replay();
        interceptor.undecorated();
        verify();

        assertToString(interceptor);
    }

    @Test
    public void void_method()
    {
        VoidService delegate = newMock(VoidService.class);
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.voidMethod();
        manager.commit();

        replay();
        interceptor.voidMethod();
        verify();

        assertToString(interceptor);
    }

    @Test
    public void void_method_with_param()
    {
        VoidService delegate = newMock(VoidService.class);
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        VoidService interceptor = decorator.build(VoidService.class, delegate, "foo.Bar");

        delegate.voidMethodWithParam(777);
        manager.commit();

        replay();
        interceptor.voidMethodWithParam(777);
        verify();

        assertToString(interceptor);
    }

    @Test
    public void runtime_exception_will_abort_transaction() throws Exception
    {
        Performer delegate = newMock(Performer.class);
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        RuntimeException re = new RuntimeException("Unexpected.");

        delegate.perform();
        TestBase.setThrowable(re);
        manager.abort();

        replay();

        Performer interceptor = decorator.build(Performer.class, delegate, "foo.Bar");

        try
        {
            interceptor.perform();
            TestBase.unreachable();
        }
        catch (RuntimeException ex)
        {
            Assert.assertSame(ex, re);
        }

        verify();
    }

    @Test
    public void checked_exception_will_commit_transaction() throws Exception
    {
        Performer delegate = newMock(Performer.class);
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        SQLException se = new SQLException("Checked.");

        delegate.perform();
        TestBase.setThrowable(se);
        manager.commit();

        replay();

        Performer interceptor = decorator.build(Performer.class, delegate, "foo.Bar");

        try
        {
            interceptor.perform();
            TestBase.unreachable();
        }
        catch (SQLException ex)
        {
            Assert.assertSame(ex, se);
        }

        verify();
    }

    @Test
    public void return_type_method()
    {
        ReturnTypeService delegate = newTestService();
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        ReturnTypeService interceptor = decorator.build(ReturnTypeService.class, delegate, "foo.Bar");

        delegate.returnTypeMethod();

        manager.commit();

        replay();
        Assert.assertEquals(interceptor.returnTypeMethod(), "Foo");
        verify();
    }

    @Test
    public void return_type_method_with_param()
    {
        ReturnTypeService delegate = newTestService();
        HibernateSessionManager manager = newMock(HibernateSessionManager.class);
        HibernateTransactionDecorator decorator = newHibernateSessionManagerDecorator(manager);
        ReturnTypeService interceptor = decorator.build(ReturnTypeService.class, delegate, "foo.Bar");

        delegate.returnTypeMethodWithParam(5, 3);

        manager.commit();

        replay();
        Assert.assertEquals(interceptor.returnTypeMethodWithParam(5, 3), 8);
        verify();

        Assert.assertEquals(
                interceptor.toString(),
                "Baz");
    }

    private HibernateTransactionDecorator newHibernateSessionManagerDecorator(HibernateSessionManager manager)
    {
        return new HibernateTransactionDecoratorImpl(aspectDecorator, new HibernateTransactionAdvisorImpl(manager));
    }

    private void assertToString(VoidService interceptor)
    {
        Assert.assertEquals(
                interceptor.toString(),
                "<Hibernate Transaction interceptor for foo.Bar(" + getClass().getName() + "$VoidService)>");
    }

    private ReturnTypeService newTestService()
    {
        return new ReturnTypeService()
        {

            public String returnTypeMethod()
            {
                return "Foo";
            }

            public int returnTypeMethodWithParam(int first, int second)
            {
                return first + second;
            }

            public String toString()
            {
                return "Baz";
            }
        };
    }

    public interface ReturnTypeService
    {
        @CommitAfter
        String returnTypeMethod();

        @CommitAfter
        int returnTypeMethodWithParam(int first, int second);

        String toString();
    }

    public interface VoidService
    {
        void undecorated();

        @CommitAfter
        void voidMethod();

        @CommitAfter
        void voidMethodWithParam(long id);
    }

    public interface Performer
    {
        @CommitAfter
        void perform() throws SQLException;
    }
}
