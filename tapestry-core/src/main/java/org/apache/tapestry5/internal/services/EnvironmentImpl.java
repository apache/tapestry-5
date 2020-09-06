// Copyright 2006-2014 The Apache Software Foundation
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

import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.services.Environment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A non-threadsafe implementation (expects to use the "perthread" service lifecyle).
 */
public class EnvironmentImpl implements Environment
{

    // My generics mojo breaks down when we talk about the key and the value being related
    // types.

    private Map<Class, LinkedList> typeToStack = CollectionFactory.newMap();

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
                            F.flow(typeToStack.entrySet()).remove(new Predicate<Entry<Class, LinkedList>>()
                            {
                                public boolean accept(Entry<Class, LinkedList> element)
                                {
                                    return element.getValue().isEmpty();
                                }
                            }).map(new Mapper<Entry<Class, LinkedList>, String>()
                            {
                                public String map(Entry<Class, LinkedList> element)
                                {
                                    return element.getKey().getName();
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

    public void threadDidCleanup()
    {
        lock.lock();
    }

    public void cloak()
    {
        throw new UnsupportedOperationException("cloak() is no longer available in Tapestry 5.4.");
    }

    public void decloak()
    {
        throw new UnsupportedOperationException("decloak() is no longer available in Tapestry 5.4.");
    }
}
