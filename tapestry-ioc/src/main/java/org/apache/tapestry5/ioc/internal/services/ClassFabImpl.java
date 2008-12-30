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

import javassist.*;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newSet;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.MethodIterator;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.lang.reflect.Modifier;
import java.util.Formatter;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link org.apache.tapestry5.ioc.services.ClassFab}. Hides, as much as possible, the underlying
 * library (Javassist).
 */
public class ClassFabImpl extends AbstractFab implements ClassFab
{
    private static final Map<Class, String> DEFAULT_RETURN = newMap();

    static
    {
        DEFAULT_RETURN.put(boolean.class, "false");
        DEFAULT_RETURN.put(long.class, "0L");
        DEFAULT_RETURN.put(float.class, "0.0f");
        DEFAULT_RETURN.put(double.class, "0.0d");
    }

    /**
     * Add fields, methods, and constructors are added, their psuedo-code is appended to this description, which is used
     * by toString().
     */
    private final StringBuilder description = new StringBuilder();

    private final Formatter formatter = new Formatter(description);

    private final Set<MethodSignature> addedSignatures = newSet();

    public ClassFabImpl(CtClassSource source, CtClass ctClass, Logger logger)
    {
        super(source, ctClass, logger);
    }

    /**
     * Returns a representation of the fabricated class, including inheritance, fields, constructors, methods and method
     * bodies.
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder("ClassFab[\n");

        try
        {
            buffer.append(buildClassAndInheritance());

            buffer.append(description.toString());
        }
        catch (Exception ex)
        {
            buffer.append(" *** ");
            buffer.append(ex);
        }

        buffer.append("\n]");

        return buffer.toString();
    }

    private String buildClassAndInheritance() throws NotFoundException
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(Modifier.toString(getCtClass().getModifiers()));
        buffer.append(" class ");
        buffer.append(getName());
        buffer.append(" extends ");
        buffer.append(getCtClass().getSuperclass().getName());
        buffer.append("\n");

        CtClass[] interfaces = getCtClass().getInterfaces();

        if (interfaces.length > 0)
        {
            buffer.append("  implements ");

            for (int i = 0; i < interfaces.length; i++)
            {
                if (i > 0) buffer.append(", ");

                buffer.append(interfaces[i].getName());
            }

            buffer.append("\n\n");
        }

        return buffer.toString();
    }

    /**
     * Returns the name of the class fabricated by this instance.
     */
    String getName()
    {
        return getCtClass().getName();
    }

    public void addField(String name, Class type)
    {
        addField(name, Modifier.PRIVATE, type);
    }

    public void addField(String name, int modifiers, Class type)
    {
        lock.check();

        CtClass ctType = toCtClass(type);

        try
        {
            CtField field = new CtField(ctType, name, getCtClass());
            field.setModifiers(modifiers);

            getCtClass().addField(field);
        }
        catch (CannotCompileException ex)
        {
            // Have yet to find a way to make this happen!
            throw new RuntimeException(ServiceMessages.unableToAddField(name, getCtClass(), ex), ex);
        }

        formatter.format("%s %s %s;\n\n", Modifier.toString(modifiers), ClassFabUtils
                .toJavaClassName(type), name);
    }

    public void proxyMethodsToDelegate(Class serviceInterface, String delegateExpression,
                                       String toString)
    {
        lock.check();

        addInterface(serviceInterface);

        MethodIterator mi = new MethodIterator(serviceInterface);

        while (mi.hasNext())
        {
            MethodSignature sig = mi.next();

            // ($r) properly handles void methods for us, which keeps this simple.

            String body = format("return ($r) %s.%s($$);", delegateExpression, sig.getName());

            addMethod(Modifier.PUBLIC, sig, body);
        }

        if (!mi.getToString()) addToString(toString);
    }

    public void addToString(String toString)
    {
        lock.check();

        MethodSignature sig = new MethodSignature(String.class, "toString", null, null);

        // TODO: Very simple quoting here, will break down if the string itself contains
        // double quotes or various other characters that need escaping.

        addMethod(Modifier.PUBLIC, sig, format("return \"%s\";", toString));
    }

    public void addMethod(int modifiers, MethodSignature ms, String body)
    {
        lock.check();

        if (addedSignatures.contains(ms))
            throw new RuntimeException(ServiceMessages.duplicateMethodInClass(ms, this));

        CtClass ctReturnType = toCtClass(ms.getReturnType());

        CtClass[] ctParameters = toCtClasses(ms.getParameterTypes());
        CtClass[] ctExceptions = toCtClasses(ms.getExceptionTypes());

        CtMethod method = new CtMethod(ctReturnType, ms.getName(), ctParameters, getCtClass());

        try
        {
            method.setModifiers(modifiers);
            method.setBody(body);
            method.setExceptionTypes(ctExceptions);

            getCtClass().addMethod(method);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServiceMessages.unableToAddMethod(ms, getCtClass(), ex), ex);
        }

        addedSignatures.add(ms);

        // modifiers, return type, name

        formatter.format("%s %s %s", Modifier.toString(modifiers), ClassFabUtils
                .toJavaClassName(ms.getReturnType()), ms.getName());

        // parameters, exceptions and body from this:
        addMethodDetailsToDescription(ms.getParameterTypes(), ms.getExceptionTypes(), body);

        description.append("\n\n");
    }

    public void addNoOpMethod(MethodSignature signature)
    {
        lock.check();

        Class returnType = signature.getReturnType();

        if (returnType.equals(void.class))
        {
            addMethod(Modifier.PUBLIC, signature, "return;");
            return;
        }

        String value = "null";
        if (returnType.isPrimitive())
        {
            value = DEFAULT_RETURN.get(returnType);
            if (value == null) value = "0";
        }

        addMethod(Modifier.PUBLIC, signature, "return " + value + ";");
    }

    public void addConstructor(Class[] parameterTypes, Class[] exceptions, String body)
    {
        Defense.notBlank(body, "body");

        lock.check();

        CtClass[] ctParameters = toCtClasses(parameterTypes);
        CtClass[] ctExceptions = toCtClasses(exceptions);

        try
        {
            CtConstructor constructor = new CtConstructor(ctParameters, getCtClass());
            constructor.setExceptionTypes(ctExceptions);
            constructor.setBody(body);

            getCtClass().addConstructor(constructor);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServiceMessages.unableToAddConstructor(getCtClass(), ex), ex);
        }

        description.append("public ");

        // This isn't quite right; we should strip the package portion off of the name.
        // However, fabricated classes are almost always in the "default" package, so
        // this is OK.

        description.append(getName());

        addMethodDetailsToDescription(parameterTypes, exceptions, body);

        description.append("\n\n");
    }

    /**
     * Adds a listing of method (or constructor) parameters and thrown exceptions, and the body, to the description
     *
     * @param parameterTypes types of method parameters, or null
     * @param exceptions     types of throw exceptions, or null
     * @param body           body of method or constructor
     */
    private void addMethodDetailsToDescription(Class[] parameterTypes, Class[] exceptions,
                                               String body)
    {
        description.append("(");

        int count = InternalUtils.size(parameterTypes);
        for (int i = 0; i < count; i++)
        {
            if (i > 0) description.append(", ");

            description.append(ClassFabUtils.toJavaClassName(parameterTypes[i]));

            description.append(" $");
            description.append(i + 1);
        }

        description.append(")");

        count = InternalUtils.size(exceptions);
        for (int i = 0; i < count; i++)
        {
            if (i == 0)
                description.append("\n  throws ");
            else
                description.append(", ");

            // Since this can never be an array type, we don't need to use getJavaClassName

            description.append(exceptions[i].getName());
        }

        description.append("\n");
        description.append(body);
    }
}
