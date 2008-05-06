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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.Invocation;
import org.apache.tapestry.ioc.MethodAdvice;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.ioc.util.BodyBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Perhaps some caching, would be useful if the same interface has multiple Aspects of interception.
 */
public class AspectInterceptorBuilderImpl<T> implements AspectInterceptorBuilder<T>
{
    private static final String PARAMETER_FIELD = "_p";

    private static final int PRIVATE_FINAL = Modifier.PRIVATE | Modifier.FINAL;

    private final ClassFactory classFactory;

    private final Class<T> serviceInterface;

    private final ClassFab interceptorFab;

    private final String delegateFieldName;

    private final String description;

    private boolean sawToString;

    private final OneShotLock lock = new OneShotLock();

    private static class Injection
    {
        final String _fieldName;
        final Class _fieldType;
        final Object _injectedValue;

        private Injection(String fieldName, Class fieldType, Object injectedValue)
        {
            _fieldName = fieldName;
            _fieldType = fieldType;
            _injectedValue = injectedValue;
        }
    }

    private final List<Injection> injections = CollectionFactory.newList();

    private final Map<Object, Injection> objectToInjection = CollectionFactory.newMap();

    private final Set<Method> remainingMethods = CollectionFactory.newSet();

    private final Set<Method> advisedMethods = CollectionFactory.newSet();

    public AspectInterceptorBuilderImpl(ClassFactory classFactory, Class<T> serviceInterface, T delegate,
                                        String description)
    {
        this.classFactory = classFactory;
        this.serviceInterface = serviceInterface;
        this.description = description;

        interceptorFab = this.classFactory.newClass(serviceInterface);

        delegateFieldName = inject(serviceInterface, delegate);

        remainingMethods.addAll(Arrays.asList(serviceInterface.getMethods()));
    }

    public void adviseMethod(Method method, MethodAdvice advice)
    {
        Defense.notNull(method, "method");
        Defense.notNull(advice, "advice");

        lock.check();

        if (advisedMethods.contains(method))
            throw new IllegalArgumentException(String.format("Method %s has already been advised.", method));

        if (!remainingMethods.contains(method))
            throw new IllegalArgumentException(
                    String.format("Method %s is not defined for interface %s.", method, serviceInterface));

        sawToString |= ClassFabUtils.isToString(method);

        String invocationClassName = createInvocationClass(method);

        BodyBuilder builder = new BodyBuilder().begin();

        String methodFieldName = inject(Method.class, method);
        String aspectFieldName = inject(MethodAdvice.class, advice);

        builder.addln("%s invocation = new %s(%s, %s, $$);", Invocation.class.getName(), invocationClassName,
                      methodFieldName, delegateFieldName);

        builder.addln("%s.advise(invocation);", aspectFieldName);

        Class[] exceptionTypes = method.getExceptionTypes();

        builder.addln("if (invocation.isFail())").begin();

        for (Class exceptionType : exceptionTypes)
        {
            String name = exceptionType.getSimpleName().toLowerCase();

            String exceptionTypeFieldName = inject(Class.class, exceptionType);

            builder.addln("%s %s = (%s) invocation.getThrown(%s);", exceptionType.getName(), name,
                          exceptionType.getName(), exceptionTypeFieldName);
            builder.addln("if (%s != null) throw %s;", name, name);
        }

        builder.addln(
                "throw new IllegalStateException(\"Impossible exception thrown from intercepted invocation.\");");

        builder.end(); // if fail

        builder.addln("return ($r) invocation.getResult();");

        builder.end();

        interceptorFab.addMethod(Modifier.PUBLIC, new MethodSignature(method), builder.toString());

        remainingMethods.remove(method);
        advisedMethods.add(method);
    }

    private String createInvocationClass(Method method)
    {
        String baseName = serviceInterface.getSimpleName() + "$" + method.getName();
        String className = ClassFabUtils.generateClassName(baseName);

        ClassFab invocationFab = classFactory.newClass(className, AbstractInvocation.class);

        List<Class> constructorTypes = CollectionFactory.newList();

        // The first two parameters are fixed:

        constructorTypes.add(Method.class); // And passed up to the super class

        invocationFab.addField("_delegate", PRIVATE_FINAL, serviceInterface);
        constructorTypes.add(serviceInterface);

        BodyBuilder constructorBuilder = new BodyBuilder().begin().addln("super($1);").addln("_delegate = $2;");

        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            Class type = method.getParameterTypes()[i];

            String name = PARAMETER_FIELD + i;

            invocationFab.addField(name, type);

            constructorTypes.add(type);

            // $0 is this
            // $1 is Method
            // $2 is delegate
            // $3 is first method parameter ...

            constructorBuilder.addln("%s = $%d;", name, i + 3);
        }

        addProceed(method, invocationFab);
        addGetParameter(method, invocationFab);
        addOverride(method, invocationFab);

        constructorBuilder.end(); // constructor

        Class[] typesArray = constructorTypes.toArray(new Class[constructorTypes.size()]);

        invocationFab.addConstructor(typesArray, null, constructorBuilder.toString());

        invocationFab.createClass();

        return className;
    }

    private void addProceed(Method method, ClassFab fab)
    {
        Class returnType = method.getReturnType();
        Class[] exceptionTypes = method.getExceptionTypes();

        boolean isNonVoid = !returnType.equals(void.class);
        boolean hasChecked = exceptionTypes.length > 0;

        BodyBuilder builder = new BodyBuilder().begin();

        if (hasChecked) builder.addln("try").begin();

        if (isNonVoid)
            builder.add("%s result = ", returnType.getName());

        builder.add("_delegate.%s(", method.getName());

        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            if (i > 0) builder.add(", ");

            builder.add(PARAMETER_FIELD + i);
        }

        builder.addln(");"); // Call on delegate

        if (isNonVoid)
        {
            builder.add("overrideResult(($w) result);");
        }

        if (hasChecked)
        {
            builder.end();   // try

            for (Class exception : exceptionTypes)
            {
                builder.addln("catch (%s ex) { overrideThrown(ex); }", exception.getName());
            }
        }

        builder.end(); // method

        MethodSignature sig = new MethodSignature(void.class, "proceed", null, null);

        fab.addMethod(Modifier.PUBLIC, sig, builder.toString());
    }

    private void addGetParameter(Method method, ClassFab fab)
    {
        Class[] parameterTypes = method.getParameterTypes();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            // ($w) will wrap a primitive as a wrapper type
            builder.addln("case %d: return ($w) %s%d;", i, PARAMETER_FIELD, i);
        }

        builder.addln("default: throw new IllegalArgumentException(\"Parameter index out of range.\");");

        builder.end().end(); // switch and method

        fab.addMethod(Modifier.PUBLIC,
                      new MethodSignature(Object.class, "getParameter", new Class[] { int.class }, null),
                      builder.toString());

    }

    private void addOverride(Method method, ClassFab fab)
    {
        Class[] parameterTypes = method.getParameterTypes();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            Class type = parameterTypes[i];
            String typeName = ClassFabUtils.toJavaClassName(type);

            builder.addln("case %d: %s%d = %s; return;",
                          i, PARAMETER_FIELD, i,
                          ClassFabUtils.castReference("$2", typeName));
        }

        builder.addln("default: throw new IllegalArgumentException(\"Parameter index out of range.\");");

        builder.end().end(); // switch and method

        fab.addMethod(Modifier.PUBLIC,
                      new MethodSignature(void.class, "override", new Class[] { int.class, Object.class }, null),
                      builder.toString());
    }

    public T build()
    {
        lock.lock();

        // Hit all the methods that haven't been referenced so far.

        addPassthruMethods();

        // And if we haven't seend a toString(), we can add it now.

        if (!sawToString)
            interceptorFab.addToString(description);

        Object[] parameters = createConstructor();

        try
        {
            Class c = interceptorFab.createClass();

            // There's only ever the one constructor.

            Constructor cc = c.getConstructors()[0];

            Object interceptor = cc.newInstance(parameters);

            return serviceInterface.cast(interceptor);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Object[] createConstructor()
    {
        // Time to add the constructor.

        Class[] parameterTypes = new Class[injections.size()];
        Object[] parameters = new Object[injections.size()];

        BodyBuilder builder = new BodyBuilder().begin();

        for (int i = 0; i < injections.size(); i++)
        {
            Injection injection = injections.get(i);

            builder.addln("%s = $%d;", injection._fieldName, i + 1);

            parameterTypes[i] = injection._fieldType;
            parameters[i] = injection._injectedValue;
        }

        builder.end();

        interceptorFab.addConstructor(parameterTypes, null, builder.toString());

        return parameters;
    }

    private void addPassthruMethods()
    {
        for (Method m : remainingMethods)
        {
            sawToString |= ClassFabUtils.isToString(m);

            MethodSignature sig = new MethodSignature(m);

            String body = String.format("return ($r) %s.%s($$);", delegateFieldName, m.getName());

            interceptorFab.addMethod(Modifier.PUBLIC, sig, body);
        }
    }

    private <T> String inject(Class<T> fieldType, T injectedValue)
    {
        Injection injection = objectToInjection.get(injectedValue);

        if (injection == null)
        {
            String name = "_" + fieldType.getSimpleName().toLowerCase() + "_" + injections.size();

            interceptorFab.addField(name, PRIVATE_FINAL, fieldType);

            injection = new Injection(name, fieldType, injectedValue);

            injections.add(injection);
            objectToInjection.put(injectedValue, injection);
        }

        return injection._fieldName;
    }

}
