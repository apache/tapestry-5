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

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.lang.reflect.Method;

import javassist.CtClass;
import javassist.CtMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodLocation;

/**
 * Implementation of {@link org.apache.tapestry.ioc.services.ClassFactory}.
 */
public class ClassFactoryImpl implements ClassFactory
{
    private final Log _log;

    /**
     * ClassPool shared by all modules (all CtClassSource instances).
     */
    private final ClassFactoryClassPool _pool;

    private final CtClassSource _classSource;

    private final ClassLoader _loader;

    public ClassFactoryImpl(ClassLoader classLoader)
    {
        this(classLoader, LogFactory.getLog(ClassFactoryImpl.class));
    }

    public ClassFactoryImpl()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    /** Main constructor where a specific class loader and log is provided. */
    public ClassFactoryImpl(ClassLoader classLoader, Log log)
    {
        this(classLoader, new ClassFactoryClassPool(classLoader), log);
    }

    /** Special constructor used when the class pool is provided externally. */
    public ClassFactoryImpl(ClassLoader classLoader, ClassFactoryClassPool pool, Log log)
    {
        _loader = classLoader;

        _pool = pool;

        _classSource = new CtClassSource(_pool, classLoader);

        _log = log;
    }

    public ClassFab newClass(Class serviceInterface)
    {
        String name = ClassFabUtils.generateClassName(serviceInterface);

        ClassFab cf = newClass(name, Object.class);

        cf.addInterface(serviceInterface);

        return cf;
    }

    public ClassFab newClass(String name, Class superClass)
    {
        if (_log.isDebugEnabled())
            _log.debug(String.format("Create ClassFab for %s (extends %s)", name, superClass
                    .getName()));

        try
        {
            CtClass ctNewClass = _classSource.newClass(name, superClass);

            return new ClassFabImpl(_classSource, ctNewClass, _log);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServiceMessages.unableToCreateClass(name, superClass, ex),
                    ex);
        }
    }

    public int getCreatedClassCount()
    {
        return _classSource.getCreatedClassCount();
    }

    public ClassLoader getClassLoader()
    {
        return _loader;
    }

    public MethodLocation getMethodLocation(Method method)
    {
        notNull(method, "method");

        // TODO: Is it worth caching this? Probably not as it usually is only
        // invoked perhaps at startup and in the event of errors.

        CtClass ctClass = _classSource.getCtClass(method.getDeclaringClass());

        StringBuilder builder = new StringBuilder("(");

        for (Class parameterType : method.getParameterTypes())
        {
            builder.append(ClassFabUtils.getTypeCode(parameterType));
        }

        builder.append(")");
        builder.append(ClassFabUtils.getTypeCode(method.getReturnType()));

        try
        {
            CtMethod ctMethod = ctClass.getMethod(method.getName(), builder.toString());

            int lineNumber = ctMethod.getMethodInfo().getLineNumber(0);

            String sourceFile = ctMethod.getDeclaringClass().getClassFile2().getSourceFile();

            return new MethodLocation(method, sourceFile, lineNumber);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

}