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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

/**
 * A simple, streamlined implementation of {@link java.util.Stack}. The implementation is <em>not</em> threadsafe.
 *
 * @param <E> the type of elements stored in the map
 * @see CollectionFactory#newStack()
 */
public class Stack<E>
{
    private static final int MINIMUM_SIZE = 3;

    private static final int DEFAULT_ARRAY_SIZE = 20;

    private Object[] items;

    private int index = -1;

    /**
     * Normal constructor supporting an initial size of 20.
     */
    public Stack()
    {
        this(DEFAULT_ARRAY_SIZE);
    }

    /**
     * @param initialSize the initial size of the internal array (which will be expanded as necessary). For best
     *                    efficiency, set this to the maximum depth of the stack.
     */
    public Stack(int initialSize)
    {
        items = new Object[Math.max(initialSize, MINIMUM_SIZE)];
    }

    /**
     * Returns true if the stack is empty.
     */
    public boolean isEmpty()
    {
        return index < 0;
    }

    /**
     * Returns the number of items currently in the stack.
     */
    public int getDepth()
    {
        return index + 1;
    }

    /**
     * Clears the stack, the same as popping off all elements.
     */
    public void clear()
    {
        for (int i = 0; i <= index; i++) items[i] = null;

        index = -1;
    }

    /**
     * Pushes a new item onto the stack.
     */
    public void push(E item)
    {
        index++;

        if (index == items.length)
        {
            int newCapacity = (items.length * 3) / 2 + 1;
            Object[] newItems = new Object[newCapacity];
            System.arraycopy(items, 0, newItems, 0, items.length);

            items = newItems;
        }

        items[index] = item;
    }

    /**
     * Pops the top element off the stack and returns it.
     *
     * @return the top element of the stack
     * @throws IllegalStateException if the stack is empty
     */
    @SuppressWarnings("unchecked")
    public E pop()
    {
        checkIfEmpty();

        Object result = items[index];

        items[index] = null;

        index--;

        return (E) result;
    }

    private void checkIfEmpty()
    {
        if (index < 0) throw new IllegalStateException(UtilMessages.stackIsEmpty());
    }

    /**
     * Returns the top element of the stack without affecting the stack.
     *
     * @return top element on the stack
     * @throws IllegalStateException if the stack is empty
     */
    @SuppressWarnings("unchecked")
    public E peek()
    {
        checkIfEmpty();

        return (E) items[index];
    }

    /**
     * Describes the stack, listing the element in order of depth (top element first).
     *
     * @return string description of the stack
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Stack[");

        for (int i = index; i >= 0; i--)
        {
            if (i != index) builder.append(", ");

            builder.append(String.valueOf(items[i]));
        }

        builder.append("]");

        return builder.toString();
    }

    /**
     * Returns a snapshot of the current state of the stack as an array of objects. The first object is the deepest in
     * the stack, the last object is the most shallowest (most recently pushed onto the stack).  The returned array may
     * be manipulated (it is a copy).
     *
     * @return the stack as an object array
     */
    public Object[] getSnapshot()
    {
        Object[] result = new Object[index + 1];

        System.arraycopy(items, 0, result, 0, index + 1);

        return result;
    }
}
