// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newLinkedList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;
import org.apache.tapestry.ioc.services.ClassFabUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Used to search from a particular class up the inheritance hierarchy of extended classes and
 * implemented interfaces.
 * <p/>
 * The search starts with the initial class (provided in the constructor). It progresses up the
 * inheritance chain, but skips java.lang.Object.
 * <p/>
 * Once classes are exhausted, the inheritance hiearchy is searched. This is a breadth-first search,
 * rooted in the interfaces implemented by the initial class at its super classes.
 * <p/>
 * Once all interfaces are exhausted, java.lang.Object is returned (it is always returned last).
 * <p/>
 * Two minor tweak to normal inheritance rules:
 * <ul>
 * <li> Normally, the parent class of an <em>object</em> array is java.lang.Object, which is odd
 * because Foo[] is assignable to Object[]. Thus, we tweak the search so that the effective super
 * class of Foo[] is Object[].
 * <li> The "super class" of a primtive type is its <em>wrapper type</em>, with the exception of
 * void, whose "super class" is left at its normal value (Object.class)
 * </ul>
 * <p/>
 * This class implements the {@link Iterable} interface, so it can be used directly in a for loop:
 * <code>
 * for (Class search : new InheritanceSearch(startClass)) {
 * ...
 * }
 * </code>
 * <p/>
 * This class is not threadsafe.
 */
public class InheritanceSearch implements Iterator<Class>, Iterable<Class>
{
    private Class _searchClass;

    private final Set<Class> _addedInterfaces = newSet();

    private final LinkedList<Class> _interfaceQueue = newLinkedList();

    private enum State
    {
        CLASS, INTERFACE, DONE
    }

    private State _state;

    public InheritanceSearch(Class searchClass)
    {
        _searchClass = searchClass;

        queueInterfaces(searchClass);

        _state = searchClass == Object.class ? State.INTERFACE : State.CLASS;
    }

    private void queueInterfaces(Class searchClass)
    {
        for (Class intf : searchClass.getInterfaces())
        {
            if (_addedInterfaces.contains(intf)) continue;

            _interfaceQueue.addLast(intf);
            _addedInterfaces.add(intf);
        }
    }

    public Iterator<Class> iterator()
    {
        return this;
    }

    public boolean hasNext()
    {
        return _state != State.DONE;
    }

    public Class next()
    {
        switch (_state)
        {
            case CLASS:

                Class result = _searchClass;

                _searchClass = parentOf(_searchClass);

                if (_searchClass == null) _state = State.INTERFACE;
                else queueInterfaces(_searchClass);

                return result;

            case INTERFACE:

                if (_interfaceQueue.isEmpty())
                {
                    _state = State.DONE;
                    return Object.class;
                }

                Class intf = _interfaceQueue.removeFirst();

                queueInterfaces(intf);

                return intf;

            default:
                throw new IllegalStateException();
        }

    }

    /**
     * Returns the parent of the given class. Tweaks inheritance for object arrays. Returns null
     * instead of Object.class.
     */
    private Class parentOf(Class clazz)
    {
        if (clazz != void.class && clazz.isPrimitive()) return ClassFabUtils.getWrapperType(clazz);

        if (clazz.isArray() && clazz != Object[].class)
        {
            Class componentType = clazz.getComponentType();

            while (componentType.isArray()) componentType = componentType.getComponentType();

            if (!componentType.isPrimitive()) return Object[].class;
        }

        Class parent = clazz.getSuperclass();

        return parent != Object.class ? parent : null;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

}
