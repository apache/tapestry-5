// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newStack;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

public class StackTest extends TestBase
{
    @Test
    public void peek_in_empty_stack_is_failure()
    {
        Stack<Integer> stack = newStack();

        try
        {
            stack.peek();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(), "Stack is empty.");
        }
    }

    @Test
    public void pop_in_empty_stack_is_failure()
    {
        Stack<Integer> stack = newStack();

        try
        {
            stack.pop();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(), "Stack is empty.");
        }
    }

    @Test
    public void basic_operations()
    {
        Stack<String> stack = newStack();

        assertTrue(stack.isEmpty());

        final String fred = "fred";
        final String barney = "barney";

        stack.push(fred);
        assertEquals(stack.peek(), fred);
        assertFalse(stack.isEmpty());

        stack.push(barney);
        assertEquals(stack.peek(), barney);

        assertEquals(stack.toString(), "Stack[barney, fred]");

        assertEquals(stack.getDepth(), 2);

        Object[] snapshot = stack.getSnapshot();

        assertArraysEqual(snapshot, new Object[] {fred, barney});

        assertEquals(stack.pop(), barney);
        assertEquals(stack.peek(), fred);

        assertEquals(stack.pop(), fred);
        assertTrue(stack.isEmpty());
    }

    @Test
    public void expansion_of_inner_data()
    {
        final int LIMIT = 1000;

        Stack<Integer> stack = newStack(10);

        for (int i = 0; i < LIMIT; i++)
        {
            stack.push(i);
        }

        for (int i = LIMIT - 1; i >= 0; i--)
        {
            assertEquals(stack.pop().intValue(), i);
        }
    }

    @Test
    public void clear()
    {
        Stack<Integer> stack = newStack();

        for (int i = 0; i < 10; i++) stack.push(i);

        assertEquals(stack.isEmpty(), false);

        stack.clear();

        assertEquals(stack.isEmpty(), true);
    }
}
