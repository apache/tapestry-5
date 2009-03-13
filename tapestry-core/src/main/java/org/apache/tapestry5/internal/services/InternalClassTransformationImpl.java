// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Implementation of the {@link org.apache.tapestry5.internal.services.InternalClassTransformation} interface.
 */
public final class InternalClassTransformationImpl implements InternalClassTransformation
{
    private static final int INIT_BUFFER_SIZE = 100;

    private boolean frozen;

    private final CtClass ctClass;

    private final Logger logger;

    private final InternalClassTransformation parentTransformation;

    private final ClassPool classPool;

    private final IdAllocator idAllocator;

    /**
     * Map, keyed on InjectKey, of field name.  Injections are always added as protected (not private) fields to support
     * sharing of injections between a base class and a sub class.
     */
    private final Map<InjectionKey, String> injectionCache = CollectionFactory.newMap();

    /**
     * Map from a field to the annotation objects for that field.
     */
    private Map<String, List<Annotation>> fieldAnnotations = CollectionFactory.newMap();

    /**
     * Used to identify fields that have been "claimed" by other annotations.
     */
    private Map<String, Object> claimedFields = CollectionFactory.newMap();

    private Set<String> addedFieldNames = CollectionFactory.newSet();

    private Set<CtBehavior> addedMethods = CollectionFactory.newSet();

    // Cache of class annotation

    private List<Annotation> classAnnotations;

    // Cache of method annotation

    private Map<CtMethod, List<Annotation>> methodAnnotations = CollectionFactory.newMap();

    private Map<CtMethod, TransformMethodSignature> methodSignatures = CollectionFactory.newMap();

    private Map<TransformMethodSignature, ComponentMethodInvocationBuilder> methodToInvocationBuilder = CollectionFactory.newMap();

    // Key is field name, value is expression used to replace read access

    private Map<String, String> fieldReadTransforms;

    // Key is field name, value is expression used to replace read access
    private Map<String, String> fieldWriteTransforms;

    private Set<String> removedFieldNames;

    /**
     * Contains the assembled Javassist code for the class' default constructor.
     */
    private StringBuilder constructor = new StringBuilder(INIT_BUFFER_SIZE);

    private final List<ConstructorArg> constructorArgs;

    private final ComponentModel componentModel;

    private final String resourcesFieldName;

    private final StringBuilder description = new StringBuilder(INIT_BUFFER_SIZE);

    private Formatter formatter = new Formatter(description);

    private final ClassFactory classFactory;

    private final ComponentClassCache componentClassCache;

    private final CtClassSource classSource;

    /**
     * Signature for newInstance() method of Instantiator.
     */
    private static final MethodSignature NEW_INSTANCE_SIGNATURE = new MethodSignature(Component.class, "newInstance",
                                                                                      new Class[] {
                                                                                              InternalComponentResources.class },
                                                                                      null);

    /**
     * This is a constructor for a base class.
     */
    public InternalClassTransformationImpl(ClassFactory classFactory, CtClass ctClass,
                                           ComponentClassCache componentClassCache,
                                           ComponentModel componentModel, CtClassSource classSource)
    {
        this.ctClass = ctClass;
        this.componentClassCache = componentClassCache;
        this.classSource = classSource;
        classPool = this.ctClass.getClassPool();
        this.classFactory = classFactory;
        parentTransformation = null;
        this.componentModel = componentModel;

        idAllocator = new IdAllocator();

        logger = componentModel.getLogger();

        preloadMemberNames();

        constructorArgs = CollectionFactory.newList();
        constructor.append("{\n");

        addImplementedInterface(Component.class);

        resourcesFieldName = addInjectedFieldUncached(InternalComponentResources.class, "resources", null);

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC | Modifier.FINAL,
                                                                    ComponentResources.class.getName(),
                                                                    "getComponentResources", null, null);

        addMethod(sig, "return " + resourcesFieldName + ";");

        // The "}" will be added later, inside  finish().
    }

    /**
     * Constructor for a component sub-class.
     */
    private InternalClassTransformationImpl(CtClass ctClass, InternalClassTransformation parentTransformation,
                                            ClassFactory classFactory, CtClassSource classSource,
                                            ComponentClassCache componentClassCache,
                                            ComponentModel componentModel)
    {
        this.ctClass = ctClass;
        this.componentClassCache = componentClassCache;
        this.classSource = classSource;
        classPool = this.ctClass.getClassPool();
        this.classFactory = classFactory;
        logger = componentModel.getLogger();
        this.parentTransformation = parentTransformation;
        this.componentModel = componentModel;

        resourcesFieldName = parentTransformation.getResourcesFieldName();

        idAllocator = parentTransformation.getIdAllocator();

        preloadMemberNames();

        constructorArgs = parentTransformation.getConstructorArgs();

        int count = constructorArgs.size();

        // Build the call to the super-constructor.

        constructor.append("{ super(");

        for (int i = 1; i <= count; i++)
        {
            if (i > 1) constructor.append(", ");

            // $0 is implicitly self, so the 0-index ConstructorArg will be Javassisst
            // pseudeo-variable $1, and so forth.

            constructor.append("$");
            constructor.append(i);
        }

        constructor.append(");\n");

        // The "}" will be added later, inside  finish().
    }

    public InternalClassTransformation createChildTransformation(CtClass childClass, MutableComponentModel childModel)
    {
        return new InternalClassTransformationImpl(childClass, this, classFactory, classSource, componentClassCache,
                                                   childModel);
    }

    private void freeze()
    {
        frozen = true;

        // Free up stuff we don't need after freezing.
        // Everything else should be final.

        fieldAnnotations = null;
        claimedFields = null;
        addedFieldNames = null;
        addedMethods = null;
        classAnnotations = null;
        methodAnnotations = null;
        methodSignatures = null;
        fieldReadTransforms = null;
        fieldWriteTransforms = null;
        removedFieldNames = null;
        constructor = null;
        formatter = null;
        methodToInvocationBuilder = null;
    }

    public String getResourcesFieldName()
    {
        return resourcesFieldName;
    }

    /**
     * Loads the names of all declared fields and methods into the idAllocator.
     */

    private void preloadMemberNames()
    {
        verifyFields();

        addMemberNames(ctClass.getDeclaredFields());
        addMemberNames(ctClass.getDeclaredMethods());
    }

    /**
     * Invoked during instance construction to check that all fields are either: <ul> <li>private</li> <li>static</li>
     * <li>groovy.lang.MetaClass (for Groovy compatiblility)</li> </li>
     */
    void verifyFields()
    {
        List<String> names = CollectionFactory.newList();

        for (CtField field : ctClass.getDeclaredFields())
        {
            String name = field.getName();

            int modifiers = field.getModifiers();

            // Fields must be either static or private.

            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) continue;

            // Groovy injects a public field named metaClass.  We ignore it, and add it as a claimed
            // field to prevent any of the workers from seeing it.

            if (name.equals("metaClass") && getFieldType(name).equals("groovy.lang.MetaClass"))
            {
                claimField(name, "Ignored");

                continue;
            }

            names.add(name);
        }

        if (!names.isEmpty())
            throw new RuntimeException(ServicesMessages.nonPrivateFields(getClassName(), names));
    }

    private void addMemberNames(CtMember[] members)
    {
        for (CtMember member : members)
        {
            idAllocator.allocateId(member.getName());
        }
    }

    public <T extends Annotation> T getFieldAnnotation(String fieldName, Class<T> annotationClass)
    {
        failIfFrozen();

        List<Annotation> annotations = findFieldAnnotations(fieldName);

        return findAnnotationInList(annotationClass, annotations);
    }

    public <T extends Annotation> T getMethodAnnotation(TransformMethodSignature signature, Class<T> annotationClass)
    {
        failIfFrozen();

        CtMethod method = findMethod(signature);

        if (method == null) throw new IllegalArgumentException(ServicesMessages.noDeclaredMethod(ctClass, signature));

        List<Annotation> annotations = findMethodAnnotations(method);

        return findAnnotationInList(annotationClass, annotations);
    }

    /**
     * Searches an array of objects (that are really annotations instances) to find one that is of the correct type,
     * which is returned.
     *
     * @param <T>
     * @param annotationClass the annotation to search for
     * @param annotations     the available annotations
     * @return the matching annotation instance, or null if not found
     */
    private <T extends Annotation> T findAnnotationInList(Class<T> annotationClass, List<Annotation> annotations)
    {
        for (Object annotation : annotations)
        {
            if (annotationClass.isInstance(annotation)) return annotationClass.cast(annotation);
        }

        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return findAnnotationInList(annotationClass, getClassAnnotations());
    }

    private List<Annotation> findFieldAnnotations(String fieldName)
    {
        List<Annotation> annotations = fieldAnnotations.get(fieldName);

        if (annotations == null)
        {
            annotations = findAnnotationsForField(fieldName);
            fieldAnnotations.put(fieldName, annotations);
        }

        return annotations;
    }

    private List<Annotation> findMethodAnnotations(CtMethod method)
    {
        List<Annotation> annotations = methodAnnotations.get(method);

        if (annotations == null)
        {
            annotations = extractAnnotations(method);

            methodAnnotations.put(method, annotations);
        }

        return annotations;
    }

    private List<Annotation> findAnnotationsForField(String fieldName)
    {
        CtField field = findDeclaredCtField(fieldName);

        return extractAnnotations(field);
    }

    private List<Annotation> extractAnnotations(CtMember member)
    {
        try
        {
            List<Annotation> result = CollectionFactory.newList();

            addAnnotationsToList(result, member.getAnnotations(), false);

            return result;
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void addAnnotationsToList(List<Annotation> list, Object[] annotations, boolean filterNonInherited)
    {
        for (Object o : annotations)
        {
            Annotation a = (Annotation) o;

            // When assembling class annotations from a base class, you want to ignore any
            // that are not @Inherited.

            if (filterNonInherited)
            {
                Class<? extends Annotation> annotationType = a.annotationType();

                Inherited inherited = annotationType.getAnnotation(Inherited.class);

                if (inherited == null) continue;
            }

            list.add(a);
        }
    }

    private CtField findDeclaredCtField(String fieldName)
    {
        try
        {
            return ctClass.getDeclaredField(fieldName);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ServicesMessages.missingDeclaredField(ctClass, fieldName), ex);
        }
    }

    public String newMemberName(String suggested)
    {
        failIfFrozen();

        String memberName = InternalUtils.createMemberName(Defense.notBlank(suggested, "suggested"));

        return idAllocator.allocateId(memberName);
    }

    public String newMemberName(String prefix, String baseName)
    {
        return newMemberName(prefix + "_" + InternalUtils.stripMemberName(baseName));
    }

    public void addImplementedInterface(Class interfaceClass)
    {
        failIfFrozen();

        String interfaceName = interfaceClass.getName();

        try
        {
            CtClass ctInterface = classPool.get(interfaceName);

            if (classImplementsInterface(ctInterface)) return;

            implementDefaultMethodsForInterface(ctInterface);

            ctClass.addInterface(ctInterface);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Adds default implementations for the methods defined by the interface (and all of its super-interfaces). The
     * implementations return null (or 0, or false, as appropriate to to the method type). There are a number of
     * degenerate cases that are not covered properly: these are related to base interfaces that may be implemented by
     * base classes.
     *
     * @param ctInterface
     * @throws NotFoundException
     */
    private void implementDefaultMethodsForInterface(CtClass ctInterface) throws NotFoundException
    {
        // java.lang.Object is the parent interface of interfaces

        if (ctInterface.getName().equals(Object.class.getName())) return;

        for (CtMethod method : ctInterface.getDeclaredMethods())
        {
            addDefaultImplementation(method);
        }

        for (CtClass parent : ctInterface.getInterfaces())
        {
            implementDefaultMethodsForInterface(parent);
        }
    }

    private void addDefaultImplementation(CtMethod method)
    {
        // Javassist has an oddity for interfaces: methods "inherited" from java.lang.Object show
        // up as methods of the interface. We skip those and only consider the methods
        // that are abstract.

        if (!Modifier.isAbstract(method.getModifiers())) return;

        try
        {
            CtMethod newMethod = CtNewMethod.copy(method, ctClass, null);

            // Methods from interfaces are always public. We definitely
            // need to change the modifiers of the method so that
            // it is not abstract.

            newMethod.setModifiers(Modifier.PUBLIC);

            // Javassist will provide a minimal implementation for us (return null, false, 0,
            // whatever).

            newMethod.setBody(null);

            ctClass.addMethod(newMethod);

            TransformMethodSignature sig = getMethodSignature(newMethod);

            addMethodToDescription("add default", sig, "<default>");
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ServicesMessages.errorAddingMethod(ctClass, method
                    .getName(), ex), ex);
        }
    }

    /**
     * Check to see if the target class (or any of its super classes) implements the provided interface. This is geared
     * for simple interfaces (that don't extend other interfaces), thus if the class (or a base class) implement
     * interface Y that extends interface X, we may not return true for interface X.
     */

    private boolean classImplementsInterface(CtClass ctInterface) throws NotFoundException
    {

        for (CtClass current = ctClass; current != null; current = current.getSuperclass())
        {
            for (CtClass anInterface : current.getInterfaces())
            {
                if (anInterface == ctInterface) return true;
            }
        }

        return false;
    }

    public void claimField(String fieldName, Object tag)
    {
        Defense.notBlank(fieldName, "fieldName");
        Defense.notNull(tag, "tag");

        failIfFrozen();

        Object existing = claimedFields.get(fieldName);

        if (existing != null)
        {
            String message = ServicesMessages.fieldAlreadyClaimed(fieldName, ctClass, existing, tag);

            throw new RuntimeException(message);
        }

        // TODO: Ensure that fieldName is a known field?

        claimedFields.put(fieldName, tag);
    }

    public void addMethod(TransformMethodSignature signature, String methodBody)
    {
        addOrReplaceMethod(signature, methodBody, true);
    }

    private void addOrReplaceMethod(TransformMethodSignature signature, String methodBody, boolean addAsNew)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(signature.getReturnType());
        CtClass[] parameters = buildCtClassList(signature.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(signature.getExceptionTypes());

        String suffix = addAsNew ? "" : " transformed";

        String action = "add" + suffix;

        try
        {
            CtMethod existing = ctClass.getDeclaredMethod(signature.getMethodName(), parameters);

            if (existing != null)
            {
                action = "replace" + suffix;

                ctClass.removeMethod(existing);
            }
        }
        catch (NotFoundException ex)
        {
            // That's ok. Kind of sloppy to rely on a thrown exception; wish getDeclaredMethod()
            // would return null for
            // that case. Alternately, we could maintain a set of the method signatures of declared
            // or added methods.
        }

        try
        {

            CtMethod method = new CtMethod(returnType, signature.getMethodName(), parameters, ctClass);

            // TODO: Check for duplicate method add

            method.setModifiers(signature.getModifiers());

            method.setBody(methodBody);
            method.setExceptionTypes(exceptions);

            ctClass.addMethod(method);

            if (addAsNew) addedMethods.add(method);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(signature, methodBody, ex), methodBody,
                                             ex);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        addMethodToDescription(action, signature, methodBody);
    }

    public void addTransformedMethod(TransformMethodSignature signature, String methodBody)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(signature.getReturnType());
        CtClass[] parameters = buildCtClassList(signature.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(signature.getExceptionTypes());


        try
        {
            CtMethod existing = ctClass.getDeclaredMethod(signature.getMethodName(), parameters);

            if (existing != null)
                throw new RuntimeException(ServicesMessages.addNewMethodConflict(signature));
        }
        catch (NotFoundException ex)
        {
            // That's ok. Kind of sloppy to rely on a thrown exception; wish getDeclaredMethod()
            // would return null for
            // that case. Alternately, we could maintain a set of the method signatures of declared
            // or added methods.
        }

        try
        {
            CtMethod method = new CtMethod(returnType, signature.getMethodName(), parameters, ctClass);

            // TODO: Check for duplicate method add

            method.setModifiers(signature.getModifiers());

            method.setBody(methodBody);
            method.setExceptionTypes(exceptions);

            ctClass.addMethod(method);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(signature, methodBody, ex), methodBody,
                                             ex);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        addMethodToDescription("add transformed", signature, methodBody);
    }

    private CtClass[] buildCtClassList(String[] typeNames)
    {
        CtClass[] result = new CtClass[typeNames.length];

        for (int i = 0; i < typeNames.length; i++)
            result[i] = findCtClass(typeNames[i]);

        return result;
    }

    private CtClass findCtClass(String type)
    {
        try
        {
            return classPool.get(type);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void extendMethod(TransformMethodSignature methodSignature, String methodBody)
    {
        failIfFrozen();

        CtMethod method = findMethod(methodSignature);

        try
        {
            method.insertAfter(methodBody);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(methodSignature, methodBody, ex),
                                             methodBody, ex);
        }

        addMethodToDescription("extend", methodSignature, methodBody);

        addedMethods.add(method);
    }

    public void extendExistingMethod(TransformMethodSignature methodSignature, String methodBody)
    {
        failIfFrozen();

        CtMethod method = findMethod(methodSignature);

        try
        {
            method.insertAfter(methodBody);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(methodSignature, methodBody, ex),
                                             methodBody, ex);
        }

        addMethodToDescription("extend existing", methodSignature, methodBody);
    }

    public void copyMethod(TransformMethodSignature sourceMethod, int modifiers, String newMethodName)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(sourceMethod.getReturnType());
        CtClass[] parameters = buildCtClassList(sourceMethod.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(sourceMethod.getExceptionTypes());
        CtMethod source = findMethod(sourceMethod);

        try
        {
            CtMethod method = new CtMethod(returnType, newMethodName, parameters, ctClass);

            method.setModifiers(modifiers);

            method.setExceptionTypes(exceptions);

            method.setBody(source, null);

            ctClass.addMethod(method);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(String.format("Error copying method %s to new method %s().",
                                                     sourceMethod,
                                                     newMethodName), ex);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        // The new method is *not* considered an added method, so field references inside the method
        // will be transformed.

        formatter.format("\n%s renamed to %s\n\n", sourceMethod, newMethodName);
    }

    public void addCatch(TransformMethodSignature methodSignature, String exceptionType, String body)
    {
        failIfFrozen();

        CtMethod method = findMethod(methodSignature);
        CtClass exceptionCtType = findCtClass(exceptionType);

        try
        {
            method.addCatch(body, exceptionCtType);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(methodSignature, body, ex),
                                             body, ex);
        }

        addMethodToDescription(String.format("catch(%s) in", exceptionType), methodSignature, body);
    }

    public void prefixMethod(TransformMethodSignature methodSignature, String methodBody)
    {
        failIfFrozen();

        CtMethod method = findMethod(methodSignature);

        try
        {
            method.insertBefore(methodBody);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(methodSignature, methodBody, ex),
                                             methodBody, ex);
        }

        addMethodToDescription("prefix", methodSignature, methodBody);
    }

    private void addMethodToDescription(String operation, TransformMethodSignature methodSignature, String methodBody)
    {
        formatter.format("%s method: %s %s %s(", operation, Modifier.toString(methodSignature
                .getModifiers()), methodSignature.getReturnType(), methodSignature.getMethodName());

        String[] parameterTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (i > 0) description.append(", ");

            formatter.format("%s $%d", parameterTypes[i], i + 1);
        }

        description.append(")");

        String[] exceptionTypes = methodSignature.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++)
        {
            if (i == 0) description.append("\n  throws ");
            else description.append(", ");

            description.append(exceptionTypes[i]);
        }

        formatter.format("\n%s\n\n", methodBody);
    }

    private CtMethod findMethod(TransformMethodSignature methodSignature)
    {
        CtMethod method = findDeclaredMethod(methodSignature);

        if (method != null) return method;

        CtMethod result = addOverrideOfSuperclassMethod(methodSignature);

        if (result != null) return result;

        throw new IllegalArgumentException(ServicesMessages.noDeclaredMethod(ctClass, methodSignature));
    }

    private CtMethod findDeclaredMethod(TransformMethodSignature methodSignature)
    {
        for (CtMethod method : ctClass.getDeclaredMethods())
        {
            if (match(method, methodSignature)) return method;
        }

        return null;
    }

    private CtMethod addOverrideOfSuperclassMethod(TransformMethodSignature methodSignature)
    {
        try
        {
            for (CtClass current = ctClass; current != null; current = current.getSuperclass())
            {
                for (CtMethod method : current.getDeclaredMethods())
                {
                    if (match(method, methodSignature))
                    {
                        // TODO: If the moethod is not overridable (i.e. private, or final)?
                        // Perhaps we should limit it to just public methods.

                        CtMethod newMethod = CtNewMethod.delegator(method, ctClass);
                        ctClass.addMethod(newMethod);

                        return newMethod;
                    }
                }
            }
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        // Not found in a super-class.

        return null;
    }

    private boolean match(CtMethod method, TransformMethodSignature sig)
    {
        if (!sig.getMethodName().equals(method.getName())) return false;

        CtClass[] paramTypes;

        try
        {
            paramTypes = method.getParameterTypes();
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        String[] sigTypes = sig.getParameterTypes();

        int count = sigTypes.length;

        if (paramTypes.length != count) return false;

        for (int i = 0; i < count; i++)
        {
            String paramType = paramTypes[i].getName();

            if (!paramType.equals(sigTypes[i])) return false;
        }

        // Ignore exceptions thrown and modifiers.
        // TODO: Validate a match on return type?

        return true;
    }

    public List<String> findFieldsWithAnnotation(final Class<? extends Annotation> annotationClass)
    {
        FieldFilter filter = new FieldFilter()
        {
            public boolean accept(String fieldName, String fieldType)
            {
                return getFieldAnnotation(fieldName, annotationClass) != null;
            }
        };

        return findFields(filter);
    }


    public List<String> findFields(FieldFilter filter)
    {
        failIfFrozen();

        List<String> result = CollectionFactory.newList();

        try
        {
            for (CtField field : ctClass.getDeclaredFields())
            {
                if (!isInstanceField(field)) continue;

                String fieldName = field.getName();

                if (filter.accept(fieldName, field.getType().getName())) result.add(fieldName);
            }
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        Collections.sort(result);

        return result;
    }

    public List<TransformMethodSignature> findMethodsWithAnnotation(final Class<? extends Annotation> annotationClass)
    {
        failIfFrozen();

        List<TransformMethodSignature> result = CollectionFactory.newList();

        for (CtMethod method : ctClass.getDeclaredMethods())
        {
            List<Annotation> annotations = findMethodAnnotations(method);

            if (findAnnotationInList(annotationClass, annotations) != null)
            {
                TransformMethodSignature sig = getMethodSignature(method);
                result.add(sig);
            }
        }

        Collections.sort(result);

        return result;
    }

    public List<TransformMethodSignature> findMethods(MethodFilter filter)
    {
        Defense.notNull(filter, "filter");

        List<TransformMethodSignature> result = CollectionFactory.newList();

        for (CtMethod method : ctClass.getDeclaredMethods())
        {
            TransformMethodSignature sig = getMethodSignature(method);

            if (filter.accept(sig)) result.add(sig);
        }

        Collections.sort(result);

        return result;
    }

    private TransformMethodSignature getMethodSignature(CtMethod method)
    {
        TransformMethodSignature result = methodSignatures.get(method);
        if (result == null)
        {
            try
            {
                String type = method.getReturnType().getName();
                String[] parameters = toTypeNames(method.getParameterTypes());
                String[] exceptions = toTypeNames(method.getExceptionTypes());

                result = new TransformMethodSignature(method.getModifiers(), type, method.getName(), parameters,
                                                      exceptions);

                methodSignatures.put(method, result);
            }
            catch (NotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        return result;
    }

    private String[] toTypeNames(CtClass[] types)
    {
        String[] result = new String[types.length];

        for (int i = 0; i < types.length; i++)
            result[i] = types[i].getName();

        return result;
    }

    public List<String> findUnclaimedFields()
    {
        failIfFrozen();

        List<String> names = CollectionFactory.newList();

        Set<String> skipped = CollectionFactory.newSet();

        skipped.addAll(claimedFields.keySet());
        skipped.addAll(addedFieldNames);

        if (removedFieldNames != null) skipped.addAll(removedFieldNames);

        for (CtField field : ctClass.getDeclaredFields())
        {
            if (!isInstanceField(field)) continue;

            String name = field.getName();

            if (skipped.contains(name)) continue;

            names.add(name);
        }

        Collections.sort(names);

        return names;
    }

    private boolean isInstanceField(CtField field)
    {
        int modifiers = field.getModifiers();

        return Modifier.isPrivate(modifiers) && !Modifier.isStatic(modifiers);
    }

    public String getFieldType(String fieldName)
    {
        failIfFrozen();

        CtClass type = getFieldCtType(fieldName);

        return type.getName();
    }

    public boolean isField(String fieldName)
    {
        failIfFrozen();

        try
        {
            CtField field = ctClass.getDeclaredField(fieldName);

            return isInstanceField(field);
        }
        catch (NotFoundException ex)
        {
            return false;
        }
    }

    public int getFieldModifiers(String fieldName)
    {
        failIfFrozen();

        try
        {
            return ctClass.getDeclaredField(fieldName).getModifiers();
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private CtClass getFieldCtType(String fieldName)
    {
        try
        {
            CtField field = ctClass.getDeclaredField(fieldName);

            return field.getType();
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String addField(int modifiers, String type, String suggestedName)
    {
        failIfFrozen();

        String fieldName = newMemberName(suggestedName);

        try
        {
            CtClass ctType = convertNameToCtType(type);

            CtField field = new CtField(ctType, fieldName, ctClass);
            field.setModifiers(modifiers);

            ctClass.addField(field);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        formatter
                .format("add field: %s %s %s;\n\n", Modifier.toString(modifiers), type, fieldName);

        addedFieldNames.add(fieldName);

        return fieldName;
    }

    public String addInjectedField(Class type, String suggestedName, Object value)
    {
        Defense.notNull(type, "type");

        failIfFrozen();

        InjectionKey key = new InjectionKey(type, value);

        String fieldName = searchForPreviousInjection(key);

        if (fieldName != null) return fieldName;

        // TODO: Probably doesn't handle arrays and primitives.

        fieldName = addInjectedFieldUncached(type, suggestedName, value);

        // Remember the injection in-case this class, or a subclass, injects the value again.

        injectionCache.put(key, fieldName);

        return fieldName;
    }

    /**
     * This is split out from {@link #addInjectedField(Class, String, Object)} to handle a special case for the
     * InternalComponentResources, which is null when "injected" (during the class transformation) and is only
     * determined when a component is actually instantiated.
     */
    private String addInjectedFieldUncached(Class type, String suggestedName, Object value)
    {
        CtClass ctType;

        try
        {
            ctType = classPool.get(type.getName());
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        String fieldName = addField(Modifier.PROTECTED | Modifier.FINAL, type.getName(), suggestedName);

        addInjectToConstructor(fieldName, ctType, value);

        addedFieldNames.add(fieldName);

        return fieldName;
    }

    public String searchForPreviousInjection(InjectionKey key)
    {
        String result = injectionCache.get(key);

        if (result != null) return result;

        if (parentTransformation != null) return parentTransformation.searchForPreviousInjection(key);

        return null;
    }

    public void advise(TransformMethodSignature methodSignature, ComponentMethodAdvice advice)
    {
        Defense.notNull(methodSignature, "methodSignature");
        Defense.notNull(advice, "advice");

        ComponentMethodInvocationBuilder builder = methodToInvocationBuilder.get(methodSignature);

        if (builder == null)
        {
            builder = new ComponentMethodInvocationBuilder(this, componentClassCache, methodSignature, classSource);
            methodToInvocationBuilder.put(methodSignature, builder);
        }

        builder.addAdvice(advice);
    }

    public boolean isMethodOverride(TransformMethodSignature methodSignature)
    {
        Defense.notNull(methodSignature, "methodSignature");

        if (!isMethod(methodSignature))
            throw new IllegalArgumentException(String.format("Method %s is not implemented by transformed class %s.",
                                                             methodSignature, getClassName()));

        InternalClassTransformation search = parentTransformation;
        while (search != null)
        {
            if (search.isMethod(methodSignature)) return true;

            search = search.getParentTransformation();
        }

        // Not found in any super-class.

        return false;
    }

    public InternalClassTransformation getParentTransformation()
    {
        return parentTransformation;
    }

    public boolean isMethod(TransformMethodSignature signature)
    {
        Defense.notNull(signature, "signature");

        return findDeclaredMethod(signature) != null;
    }

    /**
     * Adds a parameter to the constructor for the class; the parameter is used to initialize the value for a field.
     *
     * @param fieldName name of field to inject
     * @param fieldType Javassist type of the field (and corresponding parameter)
     * @param value     the value to be injected (which will in unusual cases be null)
     */
    private void addInjectToConstructor(String fieldName, CtClass fieldType, Object value)
    {
        constructorArgs.add(new ConstructorArg(fieldType, value));

        extendConstructor(String.format("  %s = $%d;", fieldName, constructorArgs.size()));
    }

    public void injectField(String fieldName, Object value)
    {
        Defense.notNull(fieldName, "fieldName");

        failIfFrozen();

        CtClass type = getFieldCtType(fieldName);

        addInjectToConstructor(fieldName, type, value);

        makeReadOnly(fieldName);
    }

    private CtClass convertNameToCtType(String type) throws NotFoundException
    {
        return classPool.get(type);
    }

    public void finish()
    {
        failIfFrozen();

        for (ComponentMethodInvocationBuilder builder : methodToInvocationBuilder.values())
        {
            builder.commit();
        }

        String initializer = convertConstructorToMethod();

        performFieldTransformations();

        addConstructor(initializer);

        freeze();
    }

    private void addConstructor(String initializer)
    {
        int count = constructorArgs.size();

        CtClass[] types = new CtClass[count];

        for (int i = 0; i < count; i++)
        {
            ConstructorArg arg = constructorArgs.get(i);

            types[i] = arg.getType();
        }

        // Add a call to the initializer; the method converted fromt the classes default
        // constructor.

        constructor.append("  ");
        constructor.append(initializer);

        // This finally matches the "{" added inside the constructor

        constructor.append("();\n\n}");

        String constructorBody = constructor.toString();

        try
        {
            CtConstructor cons = CtNewConstructor.make(types, null, constructorBody, ctClass);

            ctClass.addConstructor(cons);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        formatter.format("add constructor: %s(", ctClass.getName());

        for (int i = 0; i < count; i++)
        {
            if (i > 0) description.append(", ");

            formatter.format("%s $%d", types[i].getName(), i + 1);
        }

        formatter.format(")\n%s\n\n", constructorBody);
    }

    private String convertConstructorToMethod()
    {
        String initializer = idAllocator.allocateId("initializer");

        try
        {
            CtConstructor defaultConstructor = ctClass.getConstructor("()V");

            CtMethod initializerMethod = defaultConstructor.toMethod(initializer, ctClass);

            ctClass.addMethod(initializerMethod);

            // Replace the constructor body with one that fails.  This leaves, as an open question,
            // what to do about any other constructors.

            String body = String.format("throw new RuntimeException(\"%s\");",
                                        ServicesMessages.forbidInstantiateComponentClass(getClassName()));

            defaultConstructor.setBody(body);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        formatter.format("convert default constructor: %s();\n\n", initializer);

        return initializer;
    }

    public Instantiator createInstantiator()
    {
        if (Modifier.isAbstract(ctClass.getModifiers()))
        {
            return new Instantiator()
            {
                public Component newInstance(InternalComponentResources resources)
                {
                    throw new RuntimeException(
                            String.format("Component class %s is abstract and can not be instantiated.",
                                          ctClass.getName()));
                }

                public ComponentModel getModel()
                {
                    return componentModel;
                }
            };
        }

        String componentClassName = ctClass.getName();

        String name = ClassFabUtils.generateClassName("Instantiator");

        ClassFab cf = classFactory.newClass(name, AbstractInstantiator.class);

        BodyBuilder constructor = new BodyBuilder();

        // This is realy -1 + 2: The first value in constructorArgs is the InternalComponentResources, which doesn't
        // count toward's the Instantiator's constructor ... then we add in the Model and String description.
        // It's tricky because there's the constructor parameters for the Instantiator, most of which are stored
        // in fields and then used as the constructor parameters for the Component.

        Class[] constructorParameterTypes = new Class[constructorArgs.size() + 1];
        Object[] constructorParameterValues = new Object[constructorArgs.size() + 1];

        constructorParameterTypes[0] = ComponentModel.class;
        constructorParameterValues[0] = componentModel;

        constructorParameterTypes[1] = String.class;
        constructorParameterValues[1] = String.format("Instantiator[%s]", componentClassName);

        BodyBuilder newInstance = new BodyBuilder();

        newInstance.add("return new %s($1", componentClassName);

        constructor.begin();

        // Pass the model and description to AbstractInstantiator

        constructor.addln("super($1, $2);");

        // Again, skip the (implicit) InternalComponentResources field, that's
        // supplied to the Instantiator's newInstance() method.

        for (int i = 1; i < constructorArgs.size(); i++)
        {
            ConstructorArg arg = constructorArgs.get(i);

            CtClass argCtType = arg.getType();
            Class argType = toClass(argCtType.getName());

            boolean primitive = argCtType.isPrimitive();

            Class fieldType = primitive ? ClassFabUtils.getPrimitiveType(argType) : argType;

            String fieldName = "_param_" + i;

            constructorParameterTypes[i + 1] = argType;
            constructorParameterValues[i + 1] = arg.getValue();

            cf.addField(fieldName, fieldType);

            // $1 is model, $2 is description, to $3 is first dynamic parameter.

            // The arguments may be wrapper types, so we cast down to
            // the primitive type.

            String parameterReference = "$" + (i + 2);

            constructor.addln("%s = %s;",
                              fieldName,
                              ClassFabUtils.castReference(parameterReference, fieldType.getName()));

            newInstance.add(", %s", fieldName);
        }

        constructor.end();
        newInstance.addln(");");

        cf.addConstructor(constructorParameterTypes, null, constructor.toString());

        cf.addMethod(Modifier.PUBLIC, NEW_INSTANCE_SIGNATURE, newInstance.toString());

        Class instantiatorClass = cf.createClass();

        try
        {
            Object instance = instantiatorClass.getConstructors()[0].newInstance(constructorParameterValues);

            return (Instantiator) instance;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void failIfFrozen()
    {
        if (frozen) throw new IllegalStateException(
                "The ClassTransformation instance (for " + ctClass.getName() + ") has completed all transformations and may not be further modified.");
    }

    private void failIfNotFrozen()
    {
        if (!frozen) throw new IllegalStateException(
                "The ClassTransformation instance (for " + ctClass.getName() + ") has not yet completed all transformations.");
    }

    public IdAllocator getIdAllocator()
    {
        failIfNotFrozen();

        return idAllocator;
    }

    public List<ConstructorArg> getConstructorArgs()
    {
        failIfNotFrozen();

        return CollectionFactory.newList(constructorArgs);
    }

    public List<Annotation> getClassAnnotations()
    {
        failIfFrozen();

        if (classAnnotations == null) assembleClassAnnotations();

        return classAnnotations;
    }

    private void assembleClassAnnotations()
    {
        classAnnotations = CollectionFactory.newList();

        boolean filter = false;

        try
        {
            for (CtClass current = ctClass; current != null; current = current.getSuperclass())
            {
                addAnnotationsToList(classAnnotations, current.getAnnotations(), filter);

                // Super-class annotations are filtered

                filter = true;
            }
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("InternalClassTransformation[\n");

        try
        {
            Formatter formatter = new Formatter(builder);

            formatter.format("%s %s extends %s", Modifier.toString(ctClass.getModifiers()), ctClass.getName(),
                             ctClass.getSuperclass().getName());

            CtClass[] interfaces = ctClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++)
            {
                if (i == 0) builder.append("\n  implements ");
                else builder.append(", ");

                builder.append(interfaces[i].getName());
            }

            formatter.format("\n\n%s", description.toString());
        }
        catch (NotFoundException ex)
        {
            builder.append(ex);
        }

        builder.append("]");

        return builder.toString();
    }

    public void makeReadOnly(String fieldName)
    {
        String methodName = newMemberName("write", fieldName);

        String fieldType = getFieldType(fieldName);

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, "void", methodName,
                                                                    new String[] { fieldType }, null);

        String message = ServicesMessages.readOnlyField(ctClass.getName(), fieldName);

        String body = String.format("throw new java.lang.RuntimeException(\"%s\");", message);

        addMethod(sig, body);

        replaceWriteAccess(fieldName, methodName);
    }

    public void removeField(String fieldName)
    {
        formatter.format("remove field %s;\n\n", fieldName);

        // TODO: We could check that there's an existing field read and field write transform ...

        if (removedFieldNames == null) removedFieldNames = CollectionFactory.newSet();

        removedFieldNames.add(fieldName);
    }

    public void replaceReadAccess(String fieldName, String methodName)
    {
        // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
        // $0 is valid even inside a static method.

        String body = String.format("$_ = $0.%s();", methodName);

        if (fieldReadTransforms == null) fieldReadTransforms = CollectionFactory.newMap();

        // TODO: Collisions?

        fieldReadTransforms.put(fieldName, body);

        formatter.format("replace read %s: %s();\n\n", fieldName, methodName);
    }

    public void replaceWriteAccess(String fieldName, String methodName)
    {
        // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
        // $0 is valid even inside a static method.

        String body = String.format("$0.%s($1);", methodName);

        if (fieldWriteTransforms == null) fieldWriteTransforms = CollectionFactory.newMap();

        // TODO: Collisions?

        fieldWriteTransforms.put(fieldName, body);

        formatter.format("replace write %s: %s();\n\n", fieldName, methodName);
    }

    private void performFieldTransformations()
    {
        // If no field transformations have been requested, then we can save ourselves some
        // trouble!

        if (fieldReadTransforms != null || fieldWriteTransforms != null) replaceFieldAccess();

        if (removedFieldNames != null)
        {
            for (String fieldName : removedFieldNames)
            {
                try
                {
                    CtField field = ctClass.getDeclaredField(fieldName);
                    ctClass.removeField(field);
                }
                catch (NotFoundException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    static final int SYNTHETIC = 0x00001000;

    private void replaceFieldAccess()
    {
        // Provide empty maps here, to make the code in the inner class a tad
        // easier.

        if (fieldReadTransforms == null) fieldReadTransforms = CollectionFactory.newMap();

        if (fieldWriteTransforms == null) fieldWriteTransforms = CollectionFactory.newMap();

        ExprEditor editor = new ExprEditor()
        {
            @Override
            public void edit(FieldAccess access) throws CannotCompileException
            {
                CtBehavior where = access.where();

                if (where instanceof CtConstructor) return;

                boolean isRead = access.isReader();
                String fieldName = access.getFieldName();
                CtMethod method = (CtMethod) where;

                formatter.format("Checking field %s %s in method %s(): ", isRead ? "read" : "write", fieldName,
                                 method.getName());

                // Ignore any methods to were added as part of the transformation.
                // If we reference the field there, we really mean the field.

                if (addedMethods.contains(where))
                {
                    formatter.format("added method\n");
                    return;
                }

                Map<String, String> transformMap = isRead ? fieldReadTransforms : fieldWriteTransforms;

                String body = transformMap.get(fieldName);
                if (body == null)
                {
                    formatter.format("field not transformed\n");
                    return;
                }

                formatter.format("replacing with %s\n", body);

                access.replace(body);
            }
        };

        try
        {
            ctClass.instrument(editor);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        formatter.format("\n");
    }

    public Class toClass(String type)
    {
        String finalType = TransformUtils.getWrapperTypeName(type);

        try
        {
            return Class.forName(finalType, true, classFactory.getClassLoader());
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getClassName()
    {
        return ctClass.getName();
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void extendConstructor(String statement)
    {
        Defense.notNull(statement, "statement");

        failIfFrozen();

        constructor.append(statement);
        constructor.append("\n");
    }

    public String getMethodIdentifier(TransformMethodSignature signature)
    {
        Defense.notNull(signature, "signature");

        CtMethod method = findMethod(signature);

        int lineNumber = method.getMethodInfo2().getLineNumber(0);
        CtClass enclosingClass = method.getDeclaringClass();
        String sourceFile = enclosingClass.getClassFile2().getSourceFile();

        return String.format("%s.%s (at %s:%d)", enclosingClass.getName(), signature
                .getMediumDescription(), sourceFile, lineNumber);
    }

    public boolean isRootTransformation()
    {
        return parentTransformation == null;
    }
}
