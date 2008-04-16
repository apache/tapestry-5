// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.hibernate;

import org.apache.tapestry.hibernate.HibernateSessionManager;
import org.apache.tapestry.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry.hibernate.annotations.CommitAfter;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.ioc.util.BodyBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class HibernateTransactionDecoratorImpl implements HibernateTransactionDecorator
{
    private ClassFactory _classFactory;

    private HibernateSessionManager _manager;

    public HibernateTransactionDecoratorImpl(
            // Use the IOC's ClassFactory, not the component ClassFactory
            @Builtin ClassFactory classFactory,

            HibernateSessionManager manager)
    {
        _classFactory = classFactory;
        _manager = manager;
    }

    public <T> T build(Class<T> serviceInterface, T delegate, String serviceId)
    {
        Class interceptorClass = createInterceptorClass(serviceInterface, serviceId);
        Constructor cc = interceptorClass.getConstructors()[0];

        Object interceptor = null;
        Throwable fail = null;

        try
        {
            interceptor = cc.newInstance(delegate, _manager);
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
        cf.addField("_manager", Modifier.PRIVATE | Modifier.FINAL, HibernateSessionManager.class);

        cf.addConstructor(new Class[] { serviceInterface, HibernateSessionManager.class }, null,
                          "{ _delegate = $1; _manager = $2; }");

        addMethods(cf, serviceInterface, serviceId);

        return cf.createClass();
    }

    private void addMethods(ClassFab cf, Class serviceInterface, String serviceId)
    {
        Method[] methods = serviceInterface.getMethods();

        Method toString = null;

        for (Method method : methods)
        {
            CommitAfter annotation = method.getAnnotation(CommitAfter.class);

            MethodSignature signature = new MethodSignature(method);

            addMethod(cf, signature, annotation != null);

            if (ClassFabUtils.isToString(method))
            {
                toString = method;
            }
        }

        if (toString == null)
            cf.addToString(HibernateMessages.commitTransactionInterceptor(serviceId, serviceInterface));

    }

    private void addMethod(ClassFab cf, MethodSignature signature, boolean commit)
    {
        Class returnType = signature.getReturnType();
        boolean isVoid = returnType.equals(void.class);

        BodyBuilder builder = new BodyBuilder().begin();

        if (commit) builder.addln("try").begin();

        if (!isVoid) builder.add("%s result = ", ClassFabUtils.toJavaClassName(returnType));

        builder.addln("_delegate.%s($$);", signature.getName());

        if (commit)
            builder.addln("_manager.commit();");

        if (!isVoid) builder.addln("return result;");

        if (commit)
        {
            builder.end(); // try

            // Now to handle exceptions. All runtime exceptions cause an abort.

            builder.addln("catch (RuntimeException ex)");
            builder.begin().addln("_manager.abort(); throw ex;").end();

            // Next, each exception in the signature.  Declared exceptions
            // commit the transaction just like success.

            for (Class exceptionType : signature.getExceptionTypes())
            {
                builder.addln("catch (%s ex)", exceptionType.getName());
                builder.begin().addln("_manager.commit(); throw ex;").end();
            }
        }

        cf.addMethod(Modifier.PUBLIC, signature, builder.end().toString());
    }

}
