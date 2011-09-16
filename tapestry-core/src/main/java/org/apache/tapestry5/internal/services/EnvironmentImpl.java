// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.Environment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A non-threadsafe implementation (expects to use the "perthread" service lifecyle).
 */
public class EnvironmentImpl implements Environment, ThreadCleanupListener
{

    // My generics mojo breaks down when we talk about the key and the value being related
    // types.

    private Map<Class, LinkedList> typeToStack = CollectionFactory.newMap();

    // Support for cloak/decloak.  Cloaking pushes the current typeToStack map onto the stack
    // and creates a new, empyt, Map to replace it. Decloaking discards the current map
    // and replaces it with the top map on the stack.

    private final Stack<Map<Class, LinkedList>> cloakStack = CollectionFactory.newStack();

    private final OneShotLock lock = new OneShotLock();

    @SuppressWarnings("unchecked")
    private <T> LinkedList<T> stackFor(Class<T> type)
    {
        lock.check();

        LinkedList<T> result = typeToStack.get(type);

        if (result == null)
        {
            result = CollectionFactory.newLinkedList();
            typeToStack.put(type, result);
        }

        return result;
    }

    public <T> T peek(Class<T> type)
    {
        LinkedList<T> stack = stackFor(type);

        return stack.isEmpty() ? null : stack.getFirst();
    }

    public <T> T peekRequired(Class<T> type)
    {
        T result = peek(type);

        if (result == null)
        {
            List<Class> types = CollectionFactory.newList();
            for (Map.Entry<Class, LinkedList> e : typeToStack.entrySet())
            {
                LinkedList list = e.getValue();

                if (list != null && !list.isEmpty())
                    types.add(e.getKey());
            }

            throw new UnknownValueException(String.format("No object of type %s is available from the Environment.", type.getName()),
                    new AvailableValues("Environmentals",
                            F.flow(typeToStack.keySet()).map(new Mapper<Class, String>()
                            {
                                public String map(Class element)
                                {
                                    return element.getName();
                                }
                            }).toList()));
        }

        return result;
    }

    public <T> T pop(Class<T> type)
    {
        LinkedList<T> stack = stackFor(type);

        return stack.removeFirst();
    }

    public <T> T push(Class<T> type, T instance)
    {
        LinkedList<T> stack = stackFor(type);

        T result = stack.isEmpty() ? null : stack.getFirst();

        stack.addFirst(instance);

        return result;
    }

    public void clear()
    {
        throw new IllegalStateException("Environment.clear() is no longer supported.");
    }

    public void threadDidCleanup()
    {
        lock.lock();
    }

    public void cloak()
    {
        cloakStack.push(typeToStack);

        typeToStack = CollectionFactory.newMap();
    }

    public void decloak()
    {
        typeToStack = cloakStack.pop();
    }
}
