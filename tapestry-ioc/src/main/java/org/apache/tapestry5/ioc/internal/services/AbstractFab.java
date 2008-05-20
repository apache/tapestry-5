// Copyright 2005, 2006, 2007, 2008 The Apache Software Foundation
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

import javassist.CtClass;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.slf4j.Logger;

/**
 * Base class for {@link org.apache.tapestry5.ioc.internal.services.ClassFabImpl}. This code is a fork from HiveMind; it
 * is kept seperate from ClassFabImpl in case we want to re-introduce the idea of an InterfaceFab.
 */
public class AbstractFab
{
    protected final OneShotLock lock = new OneShotLock();

    private final CtClass ctClass;

    private final CtClassSource source;

    private final Logger logger;

    public AbstractFab(CtClassSource source, CtClass ctClass, Logger logger)
    {
        this.ctClass = ctClass;
        this.source = source;
        this.logger = logger;
    }

    public void addInterface(Class interfaceClass)
    {
        lock.check();

        CtClass ctInterfaceClass = source.toCtClass(interfaceClass);

        try
        {
            for (CtClass existing : ctClass.getInterfaces())
                if (existing == ctInterfaceClass) return;
        }
        catch (Exception ex)
        {
            // Don't think this code is actually reachable.
        }

        ctClass.addInterface(ctInterfaceClass);
    }

    protected CtClass[] toCtClasses(Class[] inputClasses)
    {
        if (inputClasses == null || inputClasses.length == 0) return null;

        int count = inputClasses.length;
        CtClass[] result = new CtClass[count];

        for (int i = 0; i < count; i++)
        {
            CtClass ctClass = toCtClass(inputClasses[i]);

            result[i] = ctClass;
        }

        return result;
    }

    protected CtClass toCtClass(Class inputClass)
    {
        return source.toCtClass(inputClass);
    }

    public Class createClass()
    {
        lock.lock();

        if (logger.isDebugEnabled()) logger.debug(String.format("Creating class from %s", this));

        return source.createClass(ctClass);
    }

    protected CtClass getCtClass()
    {
        return ctClass;
    }

    protected CtClassSource getSource()
    {
        return source;
    }

}
