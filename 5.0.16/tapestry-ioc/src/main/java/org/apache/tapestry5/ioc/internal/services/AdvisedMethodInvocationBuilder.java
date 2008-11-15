//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.util.BodyBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a single method of an advised service, responsible for constructing a subclass of {@link
 * org.apache.tapestry5.ioc.internal.services.AbstractInvocation}.
 */
public class AdvisedMethodInvocationBuilder
{
    /**
     * Parameters of the invocation are stored as fields name "p0", "p1", etc.
     */
    private static final String PARAMETER_FIELD = "p";

    private static final String DELEGATE_FIELD_NAME = "delegate";

    private static final int PRIVATE_FINAL = Modifier.PRIVATE | Modifier.FINAL;

    private static final MethodSignature GET_PARAMETER_SIGNATURE = new MethodSignature(Object.class, "getParameter",
                                                                                       new Class[] {int.class}, null);

    private static final MethodSignature OVERRIDE_SIGNATURE = new MethodSignature(void.class, "override",
                                                                                  new Class[] {int.class, Object.class},
                                                                                  null);

    private static final MethodSignature INVOKE_DELEGATE_METHOD_SIGNATURE = new MethodSignature(void.class,
                                                                                                "invokeDelegateMethod",
                                                                                                null, null);

    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private final Class serviceInterface;

    private final Method method;

    private final MethodInfo info;

    private final ClassFab classFab;

    public AdvisedMethodInvocationBuilder(ClassFactory classFactory, Class serviceInterface, Method method)
    {
        this.serviceInterface = serviceInterface;
        this.method = method;

        info = new MethodInfo(method);

        String name = "Invocation$" + serviceInterface.getSimpleName() +
                "$" + method.getName() +
                "$" + Long.toHexString(UID_GENERATOR.getAndIncrement());

        classFab = classFactory.newClass(name, AbstractInvocation.class);

        addInfrastructure();
        addGetParameter();
        addOverride();
        addInvokeDelegateMethod();

        classFab.addToString(String.format("<Method invocation %s>", method));
    }

    private void addInfrastructure()
    {
        List<Class> constructorTypes = CollectionFactory.newList();

        // First two parameters are fixed:

        // Passed to the AbstractInvocation base class
        constructorTypes.add(MethodInfo.class);
        BodyBuilder constructorBuilder = new BodyBuilder().begin().addln("super($1);");

        // Stored for chaining purposes.
        classFab.addField(DELEGATE_FIELD_NAME, PRIVATE_FINAL, serviceInterface);
        constructorTypes.add(serviceInterface);
        constructorBuilder.addln("%s = $2;", DELEGATE_FIELD_NAME);

        // Now, a field for each method parameter. 
        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            Class type = method.getParameterTypes()[i];

            String name = PARAMETER_FIELD + i;

            classFab.addField(name, type);

            constructorTypes.add(type);

            // $0 is this
            // $1 is MethodInfo
            // $2 is delegate
            // $3 is first method parameter ...

            constructorBuilder.addln("%s = $%d;", name, i + 3);
        }

        constructorBuilder.end();

        Class[] typesArray = constructorTypes.toArray(new Class[constructorTypes.size()]);

        classFab.addConstructor(typesArray, null, constructorBuilder.toString());
    }

    private void addGetParameter()
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

        classFab.addMethod(Modifier.PUBLIC, GET_PARAMETER_SIGNATURE, builder.toString());
    }

    private void addOverride()
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

        classFab.addMethod(Modifier.PUBLIC, OVERRIDE_SIGNATURE, builder.toString());
    }

    private void addInvokeDelegateMethod()
    {
        Class returnType = method.getReturnType();
        Class[] exceptionTypes = method.getExceptionTypes();

        boolean isNonVoid = !returnType.equals(void.class);
        boolean hasChecked = exceptionTypes.length > 0;

        BodyBuilder builder = new BodyBuilder().begin();

        if (hasChecked) builder.addln("try").begin();

        if (isNonVoid)
            builder.add("%s result = ", ClassFabUtils.toJavaClassName(returnType));

        builder.add("%s.%s(", DELEGATE_FIELD_NAME, method.getName());

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

        classFab.addMethod(Modifier.PUBLIC, INVOKE_DELEGATE_METHOD_SIGNATURE, builder.toString());
    }

    public void addAdvice(MethodAdvice advice)
    {
        info.addAdvice(advice);
    }

    /**
     * Invoked at the end of construction of the interceptor to intercept the method invocation and hook it into the
     * advice.
     *
     * @param interceptorClassFab classfab for the service interceptor under construction
     * @param injector            allows constant values to be injected into the interceptor class as final fields
     */
    public void commit(ClassFab interceptorClassFab, String delegateFieldName, ConstantInjector injector)
    {
        Class invocationClass = classFab.createClass();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s invocation = new %<s(%s, %s, $$);",
                      invocationClass.getName(),
                      injector.inject(MethodInfo.class, info),
                      delegateFieldName);

        builder.addln("invocation.proceed();");

        Class[] exceptionTypes = method.getExceptionTypes();

        builder.addln("if (invocation.isFail())").begin();

        for (Class exceptionType : exceptionTypes)
        {
            String name = exceptionType.getSimpleName().toLowerCase();

            String exceptionTypeFieldName = injector.inject(Class.class, exceptionType);

            builder.addln("%s %s = (%s) invocation.getThrown(%s);", exceptionType.getName(), name,
                          exceptionType.getName(), exceptionTypeFieldName);
            builder.addln("if (%s != null) throw %s;", name, name);
        }

        builder.addln(
                "throw new IllegalStateException(\"Impossible exception thrown from intercepted invocation.\");");

        builder.end(); // if fail

        builder.addln("return ($r) invocation.getResult();");

        builder.end();

        interceptorClassFab.addMethod(Modifier.PUBLIC, new MethodSignature(method), builder.toString());
    }
}
