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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;


public class AbstractInvocationTest extends TestBase
{
    class TestInvocation extends AbstractInvocation
    {
        protected TestInvocation(Method method)
        {
            this(new MethodInfo(method));
        }

        protected TestInvocation(MethodInfo method)
        {
            super(method);
        }

        public Object getParameter(int index)
        {
            return null;
        }

        public void override(int index, Object newParameter)
        {
        }

        protected void invokeDelegateMethod()
        {
        }
    }

    interface Subject
    {
        void go() throws SQLException;

        int count() throws SQLException;

        void execute(Runnable runnable);
    }

    @Test
    public void to_string() throws Exception
    {
        Invocation iv = new TestInvocation(Runnable.class.getMethod("run"));

        assertEquals(iv.toString(), "Invocation[public abstract void java.lang.Runnable.run()]");
    }

    @Test
    public void override_exception() throws Exception
    {
        SQLException se = new SQLException();

        Invocation iv = new TestInvocation(Subject.class.getMethod("go"));

        iv.overrideThrown(se);

        assertTrue(iv.isFail());
        assertSame(iv.getThrown(Exception.class), se);
    }

    @Test
    public void get_thrown_returns_null_if_not_a_match() throws Exception
    {
        SQLException se = new SQLException();

        Invocation iv = new TestInvocation(Subject.class.getMethod("go"));

        iv.overrideThrown(se);

        assertNull(iv.getThrown(RuntimeException.class));
    }

    @Test
    public void override_result_clears_exception() throws Exception
    {
        SQLException se = new SQLException();
        Integer override = new Integer(23);

        Invocation iv = new TestInvocation(Subject.class.getMethod("count"));

        iv.overrideThrown(se);

        assertTrue(iv.isFail());

        iv.overrideResult(override);
        assertFalse(iv.isFail());
        assertSame(iv.getResult(), override);
    }

    @Test
    public void invalid_exception_for_override() throws Exception
    {
        SQLException se = new SQLException();

        Invocation iv = new TestInvocation(Runnable.class.getMethod("run"));

        try
        {
            iv.overrideThrown(se);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Exception java.sql.SQLException is not a declared exception of method public abstract void java.lang.Runnable.run().");
        }
    }

    @Test
    public void get_parameter_type() throws Exception
    {
        Invocation iv = new TestInvocation(Subject.class.getMethod("execute", Runnable.class));

        assertEquals(iv.getParameterCount(), 1);
        assertSame(iv.getParameterType(0), Runnable.class);
    }
}
