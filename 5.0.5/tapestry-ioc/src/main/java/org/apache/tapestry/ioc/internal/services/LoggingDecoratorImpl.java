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
import static org.apache.tapestry.ioc.services.ClassFabUtils.toJavaClassName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.ExceptionTracker;
import org.apache.tapestry.ioc.services.LoggingDecorator;
import org.apache.tapestry.ioc.services.MethodIterator;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.util.BodyBuilder;

public class LoggingDecoratorImpl implements LoggingDecorator
{
    private final ClassFactory _classFactory;

    private final ExceptionTracker _exceptionTracker;

    public LoggingDecoratorImpl(@InjectService("ClassFactory")
    ClassFactory classFactory,

    ExceptionTracker exceptionTracker)
    {
        _classFactory = classFactory;
        _exceptionTracker = exceptionTracker;
    }

    public <T> T build(Class<T> serviceInterface, T delegate, String serviceId, Log serviceLog)
    {
        Class interceptorClass = createInterceptorClass(serviceInterface, serviceId);

        ServiceLogger logger = new ServiceLogger(serviceLog, _exceptionTracker);

        Constructor cc = interceptorClass.getConstructors()[0];

        Object interceptor = null;
        Throwable fail = null;

        try
        {
            interceptor = cc.newInstance(delegate, logger);
        }
        catch (InvocationTargetException ite)
        {
            fail = ite.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }

        if (fail != null) throw new RuntimeException(fail);

        return serviceInterface.cast(interceptor);
    }

    private Class createInterceptorClass(Class serviceInterface, String serviceId)
    {
        ClassFab cf = _classFactory.newClass(serviceInterface);

        cf.addField("_delegate", Modifier.PRIVATE | Modifier.FINAL, serviceInterface);
        cf.addField("_logger", Modifier.PRIVATE | Modifier.FINAL, ServiceLogger.class);

        cf.addConstructor(new Class[]
        { serviceInterface, ServiceLogger.class }, null, "{ _delegate = $1; _logger = $2; }");

        addMethods(cf, serviceInterface, serviceId);

        return cf.createClass();
    }

    private void addMethods(ClassFab cf, Class serviceInterface, String serviceId)
    {
        MethodIterator mi = new MethodIterator(serviceInterface);

        while (mi.hasNext())
            addMethod(cf, mi.next());

        if (!mi.getToString())
            cf.addToString(ServiceMessages.loggingInterceptor(serviceId, serviceInterface));
    }

    private void addMethod(ClassFab cf, MethodSignature signature)
    {
        String name = '"' + signature.getName() + '"';
        Class returnType = signature.getReturnType();
        boolean isVoid = returnType.equals(void.class);

        // We'll see how well Javassist handles void methods with this setup

        BodyBuilder builder = new BodyBuilder();
        builder.begin();
        builder.addln("boolean debug = _logger.isDebugEnabled();");

        builder.addln("if (debug)");
        builder.addln("  _logger.entry(%s, $args);", name);

        builder.addln("try");
        builder.begin();

        if (!isVoid) builder.add("%s result = ", toJavaClassName(returnType));

        builder.addln("_delegate.%s($$);", signature.getName());

        if (isVoid)
        {
            builder.addln("if (debug)");
            builder.addln(format("  _logger.voidExit(%s);", name));
            builder.addln("return;");
        }
        else
        {
            builder.addln("if (debug)");
            builder.addln(format("  _logger.exit(%s, ($w)result);", name));
            builder.addln("return result;");
        }

        builder.end(); // try

        // Now, a catch for each declared exception (if any)

        if (signature.getExceptionTypes() != null)
            for (Class exceptionType : signature.getExceptionTypes())
                addExceptionHandler(builder, name, exceptionType);

        // And a catch for RuntimeException

        addExceptionHandler(builder, name, RuntimeException.class);

        builder.end();

        cf.addMethod(Modifier.PUBLIC, signature, builder.toString());
    }

    private void addExceptionHandler(BodyBuilder builder, String quotedMethodName,
            Class exceptionType)
    {
        builder.addln("catch (%s ex)", exceptionType.getName());
        builder.begin();
        builder.addln("if (debug)");
        builder.addln("  _logger.fail(%s, ex);", quotedMethodName);
        builder.addln("throw ex;");
        builder.end();
    }
}
