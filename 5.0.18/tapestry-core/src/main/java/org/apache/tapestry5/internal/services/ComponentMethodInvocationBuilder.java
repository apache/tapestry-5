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

package org.apache.tapestry5.internal.services;

import javassist.*;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used by {@link org.apache.tapestry5.internal.services.InternalClassTransformationImpl} to manage adding method
 * invocation advice to arbitrary component methods.
 *
 * @see org.apache.tapestry5.ioc.MethodAdvice
 */
class ComponentMethodInvocationBuilder
{
    private static final String FIELD_NAME = "_p";

    private static final int PROTECTED_FINAL = Modifier.PROTECTED | Modifier.FINAL;

    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private final InternalClassTransformation transformation;

    private final CtClassSource classSource;

    private final TransformMethodSignature advisedMethod;

    private final ComponentMethodInvocationInfo info;

    private final CtClass invocationCtClass;

    private final String invocationClassName;

    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private static String nextUID()
    {
        return Long.toHexString(UID_GENERATOR.getAndIncrement());
    }

    public ComponentMethodInvocationBuilder(InternalClassTransformation transformation,
                                            ComponentClassCache componentClassCache,
                                            TransformMethodSignature advisedMethod,
                                            CtClassSource classSource)
    {
        this.transformation = transformation;
        this.advisedMethod = advisedMethod;
        this.classSource = classSource;

        info = new ComponentMethodInvocationInfo(advisedMethod, componentClassCache);

        invocationClassName = this.transformation.getClassName() + "$" + this.advisedMethod.getMethodName() + "$invocation_" + nextUID();

        invocationCtClass = this.classSource.newClass(invocationClassName, AbstractComponentMethodInvocation.class);
    }

    public void addAdvice(ComponentMethodAdvice advice)
    {
        info.addAdvice(advice);
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

            classSource.createClass(invocationCtClass);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        rebuildOriginalMethod();
    }

    private void rebuildOriginalMethod()
    {
        String methodInfoField = transformation.addInjectedField(ComponentMethodInvocationInfo.class,
                                                                 advisedMethod.getMethodName() + "Info",
                                                                 info);

        String componentResourcesField = transformation.getResourcesFieldName();

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s invocation = new %<s(%s, %s, $$);", invocationClassName, methodInfoField,
                      componentResourcesField);

        // Off into the first MethodAdvice

        builder.addln("invocation.proceed();");

        String[] exceptionTypes = advisedMethod.getExceptionTypes();
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

        String returnType = advisedMethod.getReturnType();

        if (!returnType.equals("void"))
        {
            builder.addln("return %s;",
                          ClassFabUtils.castReference("invocation.getResult()", returnType));
        }


        builder.end();

        /** Replace the original method with the new implementation. */
        transformation.addMethod(advisedMethod, builder.toString());
    }

    private void implementInvokeAdvisedMethod(String advisedMethodName) throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        boolean isVoid = advisedMethod.getReturnType().equals("void");

        builder.addln("%s component = (%<s) getComponentResources().getComponent();", transformation.getClassName());

        String[] exceptionTypes = advisedMethod.getExceptionTypes();
        int exceptionCount = exceptionTypes.length;

        if (exceptionCount > 0)
            builder.add("try").begin();

        if (!isVoid) builder.add("overrideResult(($w) ");

        builder.add("component.%s(", advisedMethodName);

        for (int i = 0; i < advisedMethod.getParameterTypes().length; i++)
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
                                       new CtClass[0], invocationCtClass);

        method.setModifiers(PROTECTED_FINAL);
        method.setBody(builder.toString());

        invocationCtClass.addMethod(method);
    }

    private String copyAdvisedMethod()
    {
        String newName = transformation.newMemberName("advised$" + advisedMethod.getMethodName());

        transformation.copyMethod(advisedMethod, Modifier.FINAL, newName);

        return newName;
    }

    private void createConstructor() throws CannotCompileException
    {
        int parameterCount = info.getParameterCount();

        CtClass[] parameterTypes = new CtClass[parameterCount + 2];

        parameterTypes[0] = toCtClass(ComponentMethodInvocationInfo.class);
        parameterTypes[1] = toCtClass(ComponentResources.class);

        BodyBuilder builder = new BodyBuilder().begin().addln("super($1,$2);");

        for (int i = 0; i < parameterCount; i++)
        {
            String name = FIELD_NAME + i;

            String parameterTypeName = advisedMethod.getParameterTypes()[i];

            CtClass parameterType = classSource.toCtClass(parameterTypeName);

            CtField field = new CtField(parameterType, name, invocationCtClass);
            field.setModifiers(Modifier.PRIVATE);
            invocationCtClass.addField(field);

            parameterTypes[2 + i] = parameterType;

            builder.addln("%s = $%d;", name, 3 + i);
        }

        builder.end();

        CtConstructor constructor = new CtConstructor(parameterTypes, invocationCtClass);
        constructor.setBody(builder.toString());

        invocationCtClass.addConstructor(constructor);
    }

    private CtClass toCtClass(Class input)
    {
        return classSource.toCtClass(input);
    }

    private void implementOverride() throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = advisedMethod.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            String type = advisedMethod.getParameterTypes()[i];

            builder.addln("case %d: %s = %s; break;", i, FIELD_NAME + i, ClassFabUtils.castReference("$2", type));
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        CtMethod method = new CtMethod(CtClass.voidType, "override",
                                       new CtClass[] {CtClass.intType, toCtClass(Object.class)}, invocationCtClass);

        method.setModifiers(PUBLIC_FINAL);
        method.setBody(builder.toString());

        invocationCtClass.addMethod(method);
    }

    private void implementGetParameter() throws CannotCompileException
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("switch ($1)").begin();

        int count = advisedMethod.getParameterTypes().length;

        for (int i = 0; i < count; i++)
        {
            builder.addln("case %d: return ($w) %s;", i, FIELD_NAME + i);
        }

        builder.addln("default: throw new IllegalArgumentException(\"Index out of range.\");");

        builder.end().end();

        CtMethod method = new CtMethod(toCtClass(Object.class), "getParameter",
                                       new CtClass[] {CtClass.intType}, invocationCtClass);

        method.setModifiers(PUBLIC_FINAL);
        method.setBody(builder.toString());

        invocationCtClass.addMethod(method);
    }
}
