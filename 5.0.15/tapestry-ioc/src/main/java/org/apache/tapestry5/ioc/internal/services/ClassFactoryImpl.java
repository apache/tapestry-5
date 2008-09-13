// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import javassist.CtConstructor;
import javassist.CtMethod;
import org.apache.tapestry5.ioc.Location;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Implementation of {@link org.apache.tapestry5.ioc.services.ClassFactory}.
 */
public class ClassFactoryImpl implements ClassFactory
{
    private final Logger logger;

    /**
     * ClassPool shared by all modules (all CtClassSource instances).
     */
    private final ClassFactoryClassPool pool;

    private final CtClassSource classSource;

    private final ClassLoader loader;

    public ClassFactoryImpl(ClassLoader classLoader)
    {
        this(classLoader, LoggerFactory.getLogger(ClassFactoryImpl.class));
    }

    public ClassFactoryImpl()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Main constructor where a specific class loader and log is provided.
     */
    public ClassFactoryImpl(ClassLoader classLoader, Logger log)
    {
        this(classLoader, new ClassFactoryClassPool(classLoader), log);
    }

    /**
     * Special constructor used when the class pool is provided externally.
     */
    public ClassFactoryImpl(ClassLoader classLoader, ClassFactoryClassPool pool, Logger logger)
    {
        this(classLoader, pool, new CtClassSourceImpl(pool, classLoader), logger);
    }

    public ClassFactoryImpl(ClassLoader classLoader, ClassFactoryClassPool pool, CtClassSource classSource,
                            Logger logger)
    {
        loader = classLoader;

        this.pool = pool;

        this.classSource = classSource;

        this.logger = logger;
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
        if (logger.isDebugEnabled())
            logger.debug(String.format("Create ClassFab for %s (extends %s)", name, superClass
                    .getName()));

        try
        {
            CtClass ctNewClass = classSource.newClass(name, superClass);

            return new ClassFabImpl(classSource, ctNewClass, logger);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServiceMessages.unableToCreateClass(name, superClass, ex), ex);
        }
    }

    public Class importClass(Class clazz)
    {
        return pool.importClass(clazz);
    }

    public int getCreatedClassCount()
    {
        return classSource.getCreatedClassCount();
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }

    public Location getMethodLocation(Method method)
    {
        notNull(method, "method");

        // TODO: Is it worth caching this? Probably not as it usually is only
        // invoked perhaps at startup and in the event of errors.

        Class declaringClass = method.getDeclaringClass();
        Class effectiveClass = importClass(declaringClass);

        CtClass ctClass = classSource.toCtClass(effectiveClass);

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

            String description = String.format("%s (at %s:%d)", InternalUtils.asString(method), sourceFile, lineNumber);

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

        CtClass ctClass = classSource.toCtClass(declaringClass);

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
            // Leave the line number as 0 (aka "unknown").
        }

        return new StringLocation(builder.toString(), lineNumber);
    }
}
