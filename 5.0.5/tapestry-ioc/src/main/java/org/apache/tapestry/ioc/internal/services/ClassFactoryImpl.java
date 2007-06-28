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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;

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

    public Class importClass(Class clazz)
    {
        return _pool.importClass(clazz);
    }

    public int getCreatedClassCount()
    {
        return _classSource.getCreatedClassCount();
    }

    public ClassLoader getClassLoader()
    {
        return _loader;
    }

    public Location getMethodLocation(Method method)
    {
        notNull(method, "method");

        // TODO: Is it worth caching this? Probably not as it usually is only
        // invoked perhaps at startup and in the event of errors.

        Class declaringClass = method.getDeclaringClass();
        Class effectiveClass = importClass(declaringClass);

        CtClass ctClass = _classSource.getCtClass(effectiveClass);

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

            String description = String.format(
                    "%s (at %s:%d)",
                    InternalUtils.asString(method),
                    sourceFile,
                    lineNumber);

            return new StringLocation(description, lineNumber);
        }
        catch (Exception ex)
        {
            return new StringLocation(InternalUtils.asString(method), 0);
        }
    }

    public Location getConstructorLocation(Constructor constructor)
    {
        notNull(constructor, "constructor");

        StringBuilder builder = new StringBuilder();

        Class declaringClass = constructor.getDeclaringClass();

        builder.append(declaringClass.getName());
        builder.append("(");

        CtClass ctClass = _classSource.getCtClass(declaringClass);

        StringBuilder descripton = new StringBuilder("(");

        Class[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            Class parameterType = parameterTypes[i];

            if (i > 0) builder.append(", ");

            builder.append(parameterType.getSimpleName());

            descripton.append(ClassFabUtils.getTypeCode(parameterType));
        }

        builder.append(")");

        // A constructor resembles a method of type void
        descripton.append(")V");

        int lineNumber = 0;

        try
        {
            CtConstructor ctConstructor = ctClass.getConstructor(descripton.toString());

            lineNumber = ctConstructor.getMethodInfo().getLineNumber(0);

            String sourceFile = ctConstructor.getDeclaringClass().getClassFile2().getSourceFile();

            builder.append(String.format(" (at %s:%d)", sourceFile, lineNumber));
        }
        catch (Exception ex)
        {
        }

        return new StringLocation(builder.toString(), lineNumber);
    }
}