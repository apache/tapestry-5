// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static java.lang.String.format;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;

import java.util.Map;

import org.apache.tapestry.ioc.services.Builtin;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.DefaultImplementationBuilder;
import org.apache.tapestry.ioc.services.MethodIterator;
import org.apache.tapestry.ioc.services.MethodSignature;

/**
 * 
 */
public class DefaultImplementationBuilderImpl implements DefaultImplementationBuilder
{
    private final Map<Class, Object> _cache = newConcurrentMap();

    private final ClassFactory _classFactory;

    public DefaultImplementationBuilderImpl(@Builtin
    ClassFactory classFactory)
    {
        _classFactory = classFactory;
    }

    public <S> S createDefaultImplementation(Class<S> serviceInterface)
    {
        S instance = serviceInterface.cast(_cache.get(serviceInterface));

        if (instance == null)
        {
            instance = createInstance(serviceInterface);
            _cache.put(serviceInterface, instance);
        }

        return instance;
    }

    /**
     * Creates a class and an instance of that class. Updates the cache and returns the instance.
     */
    private <S> S createInstance(Class<S> serviceInterface)
    {
        // In rare race conditions, we may end up creating two (or more)
        // NOOP class/instance pairs for the same interface. You need multiple threads
        // asking for a NOOP class for the same interface pretty much simulataneously.
        // We just let this happen.

        Class<S> noopClass = createClass(serviceInterface);

        try
        {
            S instance = noopClass.newInstance();

            _cache.put(serviceInterface, instance);

            return instance;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <S> Class<S> createClass(Class<S> serviceInterface)
    {
        ClassFab cf = _classFactory.newClass(serviceInterface);

        MethodIterator mi = new MethodIterator(serviceInterface);

        while (mi.hasNext())
        {
            MethodSignature sig = mi.next();

            cf.addNoOpMethod(sig);
        }

        if (!mi.getToString()) cf.addToString(format("<NoOp %s>", serviceInterface.getName()));

        return cf.createClass();
    }
}