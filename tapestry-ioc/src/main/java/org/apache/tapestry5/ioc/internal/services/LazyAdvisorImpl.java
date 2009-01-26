// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.annotations.NotLazy;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class LazyAdvisorImpl implements LazyAdvisor
{
    /**
     * Map from an interface type to a corresponding "thunk" class that implements the interface.
     */
    private final Map<Class, Class> interfaceToThunkClass = CollectionFactory.newConcurrentMap();

    private final ClassFactory classFactory;

    private final MethodSignature toStringSignature = new MethodSignature(String.class, "toString", null, null);

    private static final int PRIVATE_FINAL = Modifier.FINAL + Modifier.PRIVATE;

    private static final String DESCRIPTION_FIELD = "_$description";
    private static final String CREATOR_FIELD = "_$creator";
    private static final String DELEGATE_METHOD = "_$delegate";

    public LazyAdvisorImpl(@Builtin ClassFactory classFactory)
    {
        this.classFactory = classFactory;
    }

    public void addLazyMethodInvocationAdvice(MethodAdviceReceiver methodAdviceReceiver)
    {
        for (Method m : methodAdviceReceiver.getInterface().getMethods())
        {
            if (filter(m))
                addAdvice(m, methodAdviceReceiver);
        }
    }

    private void addAdvice(Method method, MethodAdviceReceiver receiver)
    {
        final Class thunkClass = getThunkClass(method.getReturnType());

        final String description = String.format("<%s Thunk for %s>",
                                                 method.getReturnType().getName(),
                                                 InternalUtils.asString(method));

        MethodAdvice advice = new MethodAdvice()
        {
            /**
             * When the method is invoked, we don't immediately proceed. Intead, we return a thunk instance
             * that defers its behavior to the lazyily invoked invocation.
             */
            public void advise(final Invocation invocation)
            {
                ObjectCreator deferred = new ObjectCreator()
                {
                    public Object createObject()
                    {
                        invocation.proceed();

                        return invocation.getResult();
                    }
                };

                ObjectCreator oneShot = new OneShotObjectCreator(deferred);

                Object thunk = createThunk(thunkClass, description, oneShot);

                invocation.overrideResult(thunk);
            }
        };

        receiver.adviseMethod(method, advice);
    }

    private Object createThunk(Class thunkClass, String description, ObjectCreator creator)
    {
        Throwable failure = null;

        try
        {
            return thunkClass.getConstructors()[0].newInstance(description, creator);
        }
        catch (InvocationTargetException ex)
        {
            failure = ex.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        throw new RuntimeException(String.format("Exception instantiating thunk class %s: %s",
                                                 thunkClass.getName(),
                                                 InternalUtils.toMessage(failure)),
                                   failure);
    }

    private Class getThunkClass(Class type)
    {
        Class result = interfaceToThunkClass.get(type);

        if (result == null)
        {
            result = constructThunkClass(type);
            interfaceToThunkClass.put(type, result);
        }

        return result;
    }

    private Class constructThunkClass(Class interfaceType)
    {
        ClassFab classFab = classFactory.newClass(interfaceType);

        classFab.addField(DESCRIPTION_FIELD, PRIVATE_FINAL, String.class);

        classFab.addField(CREATOR_FIELD, PRIVATE_FINAL, ObjectCreator.class);

        classFab.addConstructor(new Class[] { String.class, ObjectCreator.class }, null,
                                String.format("{ %s = $1; %s = $2; }", DESCRIPTION_FIELD, CREATOR_FIELD));

        MethodSignature sig = new MethodSignature(interfaceType, DELEGATE_METHOD, null, null);

        classFab.addMethod(Modifier.PRIVATE, sig, String.format("return ($r) %s.createObject();", CREATOR_FIELD));

        MethodIterator mi = new MethodIterator(interfaceType);

        while (mi.hasNext())
        {
            sig = mi.next();

            classFab.addMethod(Modifier.PUBLIC, sig,
                               String.format("return ($r) %s().%s($$);", DELEGATE_METHOD, sig.getName()));
        }

        if (!mi.getToString())
            classFab.addMethod(Modifier.PUBLIC, toStringSignature, String.format("return %s;", DESCRIPTION_FIELD));

        return classFab.createClass();
    }

    private boolean filter(Method method)
    {
        if (method.getAnnotation(NotLazy.class) != null) return false;

        if (!method.getReturnType().isInterface()) return false;

        for (Class extype : method.getExceptionTypes())
        {
            if (!RuntimeException.class.isAssignableFrom(extype)) return false;
        }

        return true;
    }
}
