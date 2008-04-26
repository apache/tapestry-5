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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.services.ComponentMethodAdvice;
import org.apache.tapestry.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used by {@link org.apache.tapestry.internal.services.InternalClassTransformationImpl} to manage adding method
 * invocation advice to arbitrary methods.
 */
public class InvocationBuilder
{
    private static final String FIELD_NAME = "_p";

    private static final MethodSignature OVERRIDE_SIGNATURE =
            new MethodSignature(void.class, "override", new Class[] { int.class, Object.class }, null);

    private static final MethodSignature GET_PARAMETER_SIGNATURE =
            new MethodSignature(Object.class, "getParameter", new Class[] { int.class }, null);

    private static final MethodSignature INVOKE_ADVISED_METHOD_SIGNATURE =
            new MethodSignature(void.class, "invokeAdvisedMethod", null, null);

    private final InternalClassTransformation _transformation;

    private final ClassFactory _classFactory;

    private final TransformMethodSignature _methodSignature;

    private final MethodInvocationInfo _info;

    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private static final int PROTECTED_FINAL = Modifier.PROTECTED | Modifier.FINAL;

    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private static String nextUID()
    {
        return Long.toHexString(UID_GENERATOR.getAndIncrement());
    }

    public InvocationBuilder(InternalClassTransformation transformation, ClassFactory classFactory,
                             ComponentClassCache componentClassCache, TransformMethodSignature methodSignature)
    {
        _transformation = transformation;
        _classFactory = classFactory;
        _methodSignature = methodSignature;

        _info = new MethodInvocationInfo(methodSignature, componentClassCache);
    }

    public void addAdvice(ComponentMethodAdvice advice)
    {
        _info.addAdvice(advice);
    }

    public void commit()
    {
        // The class name is the component class name plus the method name plus a unique uid. This places
        // the invocation in the same package as the component class; the original method will ultimately
        // be renamed and modified to be package private.

        String className =
                _transformation.getClassName() + "$" + _methodSignature.getMethodName() + "$invocation_" + nextUID();

        ClassFab invocationFab = _classFactory.newClass(className, AbstractComponentMethodInvocation.class);

        createConstructor(invocationFab);

        implementOverride(invocationFab);

        implementGetParameter(invocationFab);

        String renamed = copyAdvisedMethod();

        implementInvokeAdviseMethod(invocationFab, renamed);

        rebuildOriginalMethod(className);

        // Force the creation of the class now

        invocationFab.createClass();
    }

    private void rebuildOriginalMethod(String invocationClassName)
    {
        String methodInfoField = _transformation.addInjectedField(MethodInvocationInfo.class,
                                                                  _methodSignature.getMethodName() + "Info",
                                                                  _info);

        String componentResourcesField = _transformation.getResourcesFieldName();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s invocation = new %<s(%s, %s, $$);", invocationClassName, methodInfoField,
                      componentResourcesField);

        // Off into the first MethodAdvice

        builder.addln("invocation.proceed();");

        String[] exceptionTypes = _methodSignature.getExceptionTypes();
        int exceptionCount = exceptionTypes.length;

        if (exceptionCount > 0)
        {
            for (int i = 0; i < exceptionCount; i++)
            {
                String type = exceptionTypes[i];
                String name = "ex" + i;

                builder.addln("%s %s = (%1$s) invocation.getThrown(%s.getExceptionType(%d));",
                              type, name, methodInfoField, i);
                builder.addln("if (%s != null) throw %<s;", name);
            }
        }

        String returnType = _methodSignature.getReturnType();

        if (!returnType.equals("void"))
        {
            builder.addln("return %s;",
                          ClassFabUtils.castReference("invocation.getResult()", returnType));
        }


        builder.end();

        /** Replace the original method with the new implementation. */
        _transformation.addMethod(_methodSignature, builder.toString());
    }

    private void implementInvokeAdviseMethod(ClassFab classFab, String advisedMethodName)
    {
        BodyBuilder builder = new BodyBuilder().begin();

        boolean isVoid = _methodSignature.getReturnType().equals("void");

        builder.addln("%s component = (%<s) getComponentResources().getComponent();", _transformation.getClassName());

        String[] exceptionTypes = _methodSignature.getExceptionTypes();
        int exceptionCount = exceptionTypes.length;

        if (exceptionCount > 0)
            builder.add("try").begin();

        if (!isVoid) builder.add("overrideResult(($w) ");

        builder.add("component.%s(", advisedMethodName);

        for (int i = 0; i < _methodSignature.getParameterTypes().length; i++)
        {
            if (i > 0) builder.add(", ");

            builder.add("%s%d", FIELD_NAME, i);
        }

        builder.add(")");

        if (!isVoid) builder.add(")");

        builder.addln(";");

        if (exceptionCount > 0)
        {
            builder.end(); // try

            for (int i = 0; i < exceptionCount; i++)
            {
                builder.addln("catch (%s ex) { overrideThrown(ex); }", exceptionTypes[i]);
            }
        }

        builder.end();

        classFab.addMethod(PROTECTED_FINAL, INVOKE_ADVISED_METHOD_SIGNATURE, builder.toString());
    }

    private String copyAdvisedMethod()
    {
        String newName = _transformation.newMemberName("advised$" + _methodSignature.getMethodName());

        _transformation.copyMethod(_methodSignature, Modifier.FINAL, newName);

        return newName;
    }

    private void createConstructor(ClassFab classFab)
    {
        int parameterCount = _info.getParameterCount();

        Class[] parameterTypes = new Class[parameterCount + 2];

        parameterTypes[0] = MethodInvocationInfo.class;
        parameterTypes[1] = ComponentResources.class;

        BodyBuilder builder = new BodyBuilder().begin().addln("super($1,$2);");

        for (int i = 0; i < parameterCount; i++)
        {
            String name = FIELD_NAME + i;

            Class parameterType = _info.getParameterType(i);

            parameterTypes[2 + i] = parameterType;

            classFab.addField(name, _info.getParameterType(i));

            builder.addln("%s = $%d;", name, 3 + i);
        }

        builder.end();

        classFab.addConstructor(parameterTypes, null, builder.toString());
    }

    private void implementOverride(ClassFab classFab)
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = _methodSignature.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            String type = _methodSignature.getParameterTypes()[i];

            builder.addln("case %d: %s = %s; break;", i, FIELD_NAME + i, ClassFabUtils.castReference("$2", type));
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        classFab.addMethod(PUBLIC_FINAL, OVERRIDE_SIGNATURE, builder.toString());
    }

    private void implementGetParameter(ClassFab classFab)
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = _methodSignature.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            builder.addln("case %d: return ($w) %s;", i, FIELD_NAME + i);
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        classFab.addMethod(PUBLIC_FINAL, GET_PARAMETER_SIGNATURE, builder.toString());
    }

}
