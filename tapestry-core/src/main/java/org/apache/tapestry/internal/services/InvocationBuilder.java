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

import javassist.*;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.internal.services.CtClassSource;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.services.ComponentMethodAdvice;
import org.apache.tapestry.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used by {@link org.apache.tapestry.internal.services.InternalClassTransformationImpl} to manage adding method
 * invocation advice to arbitrary component methods.
 */
public class InvocationBuilder
{
    private static final String FIELD_NAME = "_p";

    private static final int PROTECTED_FINAL = Modifier.PROTECTED | Modifier.FINAL;

    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private final InternalClassTransformation _transformation;

    private final CtClassSource _classSource;

    private final TransformMethodSignature _advisedMethod;

    private final MethodInvocationInfo _info;

    private final CtClass _invocationCtClass;

    private final String _invocationClassName;

    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private static String nextUID()
    {
        return Long.toHexString(UID_GENERATOR.getAndIncrement());
    }

    public InvocationBuilder(InternalClassTransformation transformation,
                             ComponentClassCache componentClassCache, TransformMethodSignature advisedMethod,
                             CtClassSource classSource)
    {
        _transformation = transformation;
        _advisedMethod = advisedMethod;
        _classSource = classSource;

        _info = new MethodInvocationInfo(advisedMethod, componentClassCache);

        _invocationClassName = _transformation.getClassName() + "$" + _advisedMethod.getMethodName() + "$invocation_" + nextUID();

        _invocationCtClass = _classSource.newClass(_invocationClassName, AbstractComponentMethodInvocation.class);

    }

    public void addAdvice(ComponentMethodAdvice advice)
    {
        _info.addAdvice(advice);
    }

    /**
     * Commit the changes, creating the new class for the invocation, and renaming and rewriting the advised method.
     */
    public void commit()
    {
        // The class name is the component class name plus the method name plus a unique uid. This places
        // the invocation in the same package as the component class; the original method will ultimately
        // be renamed and modified to be package private.

        try
        {
            createConstructor();

            implementOverride();

            implementGetParameter();

            String renamed = copyAdvisedMethod();

            implementInvokeAdvisedMethod(renamed);

            _classSource.createClass(_invocationCtClass);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        rebuildOriginalMethod();
    }

    private void rebuildOriginalMethod()
    {
        String methodInfoField = _transformation.addInjectedField(MethodInvocationInfo.class,
                                                                  _advisedMethod.getMethodName() + "Info",
                                                                  _info);

        String componentResourcesField = _transformation.getResourcesFieldName();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s invocation = new %<s(%s, %s, $$);", _invocationClassName, methodInfoField,
                      componentResourcesField);

        // Off into the first MethodAdvice

        builder.addln("invocation.proceed();");

        String[] exceptionTypes = _advisedMethod.getExceptionTypes();
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

        String returnType = _advisedMethod.getReturnType();

        if (!returnType.equals("void"))
        {
            builder.addln("return %s;",
                          ClassFabUtils.castReference("invocation.getResult()", returnType));
        }


        builder.end();

        /** Replace the original method with the new implementation. */
        _transformation.addMethod(_advisedMethod, builder.toString());
    }

    private void implementInvokeAdvisedMethod(String advisedMethodName) throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        boolean isVoid = _advisedMethod.getReturnType().equals("void");

        builder.addln("%s component = (%<s) getComponentResources().getComponent();", _transformation.getClassName());

        String[] exceptionTypes = _advisedMethod.getExceptionTypes();
        int exceptionCount = exceptionTypes.length;

        if (exceptionCount > 0)
            builder.add("try").begin();

        if (!isVoid) builder.add("overrideResult(($w) ");

        builder.add("component.%s(", advisedMethodName);

        for (int i = 0; i < _advisedMethod.getParameterTypes().length; i++)
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

        CtMethod method = new CtMethod(CtClass.voidType, "invokeAdvisedMethod",
                                       new CtClass[0], _invocationCtClass);

        method.setModifiers(PROTECTED_FINAL);
        method.setBody(builder.toString());

        _invocationCtClass.addMethod(method);
    }

    private String copyAdvisedMethod()
    {
        String newName = _transformation.newMemberName("advised$" + _advisedMethod.getMethodName());

        _transformation.copyMethod(_advisedMethod, Modifier.FINAL, newName);

        return newName;
    }

    private void createConstructor() throws CannotCompileException
    {
        int parameterCount = _info.getParameterCount();

        CtClass[] parameterTypes = new CtClass[parameterCount + 2];

        parameterTypes[0] = toCtClass(MethodInvocationInfo.class);
        parameterTypes[1] = toCtClass(ComponentResources.class);

        BodyBuilder builder = new BodyBuilder().begin().addln("super($1,$2);");

        for (int i = 0; i < parameterCount; i++)
        {
            String name = FIELD_NAME + i;

            String parameterTypeName = _advisedMethod.getParameterTypes()[i];

            CtClass parameterType = _classSource.toCtClass(parameterTypeName);

            CtField field = new CtField(parameterType, name, _invocationCtClass);
            field.setModifiers(Modifier.PRIVATE);
            _invocationCtClass.addField(field);

            parameterTypes[2 + i] = parameterType;

            builder.addln("%s = $%d;", name, 3 + i);
        }

        builder.end();

        CtConstructor constructor = new CtConstructor(parameterTypes, _invocationCtClass);
        constructor.setBody(builder.toString());

        _invocationCtClass.addConstructor(constructor);

    }

    private CtClass toCtClass(Class input)
    {
        return _classSource.toCtClass(input);
    }

    private void implementOverride() throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = _advisedMethod.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            String type = _advisedMethod.getParameterTypes()[i];

            builder.addln("case %d: %s = %s; break;", i, FIELD_NAME + i, ClassFabUtils.castReference("$2", type));
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        CtMethod method = new CtMethod(CtClass.voidType, "override",
                                       new CtClass[] { CtClass.intType, toCtClass(Object.class) }, _invocationCtClass);

        method.setModifiers(PUBLIC_FINAL);
        method.setBody(builder.toString());

        _invocationCtClass.addMethod(method);
    }

    private void implementGetParameter() throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = _advisedMethod.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            builder.addln("case %d: return ($w) %s;", i, FIELD_NAME + i);
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        CtMethod method = new CtMethod(toCtClass(Object.class), "getParameter",
                                       new CtClass[] { CtClass.intType }, _invocationCtClass);

        method.setModifiers(PUBLIC_FINAL);
        method.setBody(builder.toString());

        _invocationCtClass.addMethod(method);
    }

}
