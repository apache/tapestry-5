// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import static java.lang.String.format;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.MemberValue;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.MethodIterator;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.slf4j.Logger;

/**
 * Implementation of {@link org.apache.tapestry5.ioc.services.ClassFab}. Hides, as much as possible, the underlying
 * library (Javassist).
 */
@SuppressWarnings("all")
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
                if (i > 0)
                    buffer.append(", ");

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

        formatter.format("%s %s %s;\n\n", Modifier.toString(modifiers), ClassFabUtils.toJavaClassName(type), name);
    }

    public void proxyMethodsToDelegate(Class serviceInterface, String delegateExpression, String toString)
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

        if (!mi.getToString())
            addToString(toString);
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

        formatter.format("%s %s %s", Modifier.toString(modifiers), ClassFabUtils.toJavaClassName(ms.getReturnType()),
                ms.getName());

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
            if (value == null)
                value = "0";
        }

        addMethod(Modifier.PUBLIC, signature, "return " + value + ";");
    }

    public void addConstructor(Class[] parameterTypes, Class[] exceptions, String body)
    {
        assert InternalUtils.isNonBlank(body);
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
     * @param parameterTypes
     *            types of method parameters, or null
     * @param exceptions
     *            types of throw exceptions, or null
     * @param body
     *            body of method or constructor
     */
    private void addMethodDetailsToDescription(Class[] parameterTypes, Class[] exceptions, String body)
    {
        description.append("(");

        int count = InternalUtils.size(parameterTypes);
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
                description.append(", ");

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
    
    public void copyClassAnnotationsFromDelegate(Class delegateClass)
    {
        lock.check();
        
        for (Annotation annotation : delegateClass.getAnnotations())
        {
            try
            {
                addAnnotation(annotation);
            }
            catch (RuntimeException ex) 
            {
                //Annotation processing may cause exceptions thrown by Javassist. 
                //To provide backward compatibility we have to continue even though copying a particular annotation failed.
                getLogger().error(String.format("Failed to copy annotation '%s' from '%s'", annotation.annotationType(), delegateClass.getName()));
            }
        }   
    }
    
    public void copyMethodAnnotationsFromDelegate(Class serviceInterface, Class delegateClass)
    {
        lock.check();
        
        for(MethodSignature sig: addedSignatures)
        {   
            if(getMethod(sig, serviceInterface) == null)
                continue;
            
            Method method = getMethod(sig, delegateClass);
            
            assert method != null;
            
            CtMethod ctMethod = getCtMethod(sig);
            
            Annotation[] annotations = method.getAnnotations();
            
            for (Annotation annotation : annotations)
            {   
                try
                {
                    addMethodAnnotation(ctMethod, annotation);   
                }
                catch (RuntimeException ex) 
                {
                    //Annotation processing may cause exceptions thrown by Javassist. 
                    //To provide backward compatibility we have to continue even though copying a particular annotation failed.
                    getLogger().error(String.format("Failed to copy annotation '%s' from method '%s' of class '%s'", 
                            annotation.annotationType(), method.getName(), delegateClass.getName()));
                }
            }
            
            try
            {
            	addMethodParameterAnnotation(ctMethod, method.getParameterAnnotations());
            }
            catch (RuntimeException ex) 
            {
                //Annotation processing may cause exceptions thrown by Javassist. 
                //To provide backward compatibility we have to continue even though copying a particular annotation failed.
                getLogger().error(String.format("Failed to copy parameter annotations from method '%s' of class '%s'", 
                		method.getName(), delegateClass.getName()));
            }
        }
    }
    
    private CtMethod getCtMethod(MethodSignature sig)
    {
        try
        {
            return getCtClass().getDeclaredMethod(sig.getName(), toCtClasses(sig.getParameterTypes()));
        }
        catch (NotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private Method getMethod(MethodSignature sig, Class clazz)
    {
        try
        {
            return clazz.getMethod(sig.getName(), sig.getParameterTypes());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void addAnnotation(Annotation annotation)
    {
        
        final ClassFile classFile = getClassFile();
        
        AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        
        if (attribute == null)
        {
            attribute = new AnnotationsAttribute(getConstPool(), AnnotationsAttribute.visibleTag);
        }
        
        final javassist.bytecode.annotation.Annotation copy = toJavassistAnnotation(annotation);
        
        
        attribute.addAnnotation(copy);
        
        classFile.addAttribute(attribute);
        
    }
    
    private void addMethodAnnotation(final CtMethod ctMethod, final Annotation annotation) {

        MethodInfo methodInfo = ctMethod.getMethodInfo();

        AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo
            .getAttribute(AnnotationsAttribute.visibleTag);

        if (attribute == null) {
            attribute = new AnnotationsAttribute(getConstPool(), AnnotationsAttribute.visibleTag);
        }

        final javassist.bytecode.annotation.Annotation copy = toJavassistAnnotation(annotation);

        attribute.addAnnotation(copy);

        methodInfo.addAttribute(attribute);

    }

    private void addMethodParameterAnnotation(final CtMethod ctMethod, final Annotation[][] parameterAnnotations) {

        MethodInfo methodInfo = ctMethod.getMethodInfo();

        ParameterAnnotationsAttribute attribute = (ParameterAnnotationsAttribute) methodInfo
            .getAttribute(ParameterAnnotationsAttribute.visibleTag);

        if (attribute == null) {
            attribute = new ParameterAnnotationsAttribute(getConstPool(), ParameterAnnotationsAttribute.visibleTag);
        }
        
        List<javassist.bytecode.annotation.Annotation[]> result = CollectionFactory.newList();
        
        for (Annotation[] next : parameterAnnotations) 
        {
        	List<javassist.bytecode.annotation.Annotation> list = CollectionFactory.newList();
        	
			for (Annotation annotation : next) 
			{
		        final javassist.bytecode.annotation.Annotation copy = toJavassistAnnotation(annotation);
		        
		        list.add(copy);
			}
			
			result.add(list.toArray(new javassist.bytecode.annotation.Annotation[]{}));
		}
        
        javassist.bytecode.annotation.Annotation[][] annotations = result.toArray(new javassist.bytecode.annotation.Annotation[][]{});
        
        attribute.setAnnotations(annotations);
        
        methodInfo.addAttribute(attribute);
    }
    
    private ClassFile getClassFile()
    {
        return getCtClass().getClassFile();
    }
    
    private ConstPool getConstPool() 
    {   
        return getClassFile().getConstPool();
    }
    
    private javassist.bytecode.annotation.Annotation toJavassistAnnotation(final Annotation source)
    {

        final Class<? extends Annotation> annotationType = source.annotationType();

        final ConstPool constPool = getConstPool();

        final javassist.bytecode.annotation.Annotation copy = new javassist.bytecode.annotation.Annotation(
                annotationType.getName(), constPool);

        final Method[] methods = annotationType.getDeclaredMethods();

        for (final Method method : methods)
        {
            try
            {
                CtClass ctType = toCtClass(method.getReturnType());
                
                final MemberValue memberValue = javassist.bytecode.annotation.Annotation.createMemberValue(constPool, ctType);
                final Object value = method.invoke(source);

                memberValue.accept(new AnnotationMemberValueVisitor(constPool, getSource(), value));

                copy.addMemberValue(method.getName(), memberValue);
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return copy;
    }
}
