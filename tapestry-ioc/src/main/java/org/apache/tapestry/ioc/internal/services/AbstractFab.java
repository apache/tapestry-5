// Copyright 2005, 2006, 2007 The Apache Software Foundation
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

import javassist.CtClass;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Base class for {@link org.apache.tapestry.ioc.internal.services.ClassFabImpl}. This code is a
 * fork from HiveMind; it is kept seperate from ClassFabImpl in case we want to re-introduce the
 * idea of an InterfaceFab.
 */
public class AbstractFab
{
    protected final OneShotLock _lock = new OneShotLock();

    private final CtClass _ctClass;

    private final CtClassSource _source;

    private final Logger _logger;

    public AbstractFab(CtClassSource source, CtClass ctClass, Logger logger)
    {
        _ctClass = ctClass;
        _source = source;
        _logger = logger;
    }

    /**
     * Map from Class to CtClass.
     */
    private final Map<Class, CtClass> _ctClassCache = newMap();

    public void addInterface(Class interfaceClass)
    {
        _lock.check();

        CtClass ctInterfaceClass = _source.getCtClass(interfaceClass);

        try
        {
            for (CtClass existing : _ctClass.getInterfaces())
                if (existing == ctInterfaceClass) return;
        }
        catch (Exception ex)
        {
            // Don't think this code is actually reachable.
        }

        _ctClass.addInterface(ctInterfaceClass);
    }

    protected CtClass[] convertClasses(Class[] inputClasses)
    {
        if (inputClasses == null || inputClasses.length == 0) return null;

        int count = inputClasses.length;
        CtClass[] result = new CtClass[count];

        for (int i = 0; i < count; i++)
        {
            CtClass ctClass = convertClass(inputClasses[i]);

            result[i] = ctClass;
        }

        return result;
    }

    protected CtClass convertClass(Class inputClass)
    {
        CtClass result = _ctClassCache.get(inputClass);

        if (result == null)
        {
            result = _source.getCtClass(inputClass);
            _ctClassCache.put(inputClass, result);
        }

        return result;
    }

    public Class createClass()
    {
        _lock.lock();

        if (_logger.isDebugEnabled()) _logger.debug(String.format("Creating class from %s", this));

        return _source.createClass(_ctClass);
    }

    protected CtClass getCtClass()
    {
        return _ctClass;
    }

    protected CtClassSource getSource()
    {
        return _source;
    }

}