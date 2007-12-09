// Copyright 2006, 2007 The Apache Software Foundation
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
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.util.MultiKey;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.*;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.FieldFilter;
import org.apache.tapestry.services.MethodFilter;
import org.apache.tapestry.services.TransformMethodSignature;
import org.apache.tapestry.services.TransformUtils;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Implementation of the {@link org.apache.tapestry.internal.services.InternalClassTransformation}
 * interface.
 */
public final class InternalClassTransformationImpl implements InternalClassTransformation
{
    private boolean _frozen;

    private final CtClass _ctClass;

    private final Logger _logger;

    private final InternalClassTransformation _parentTransformation;

    private ClassPool _classPool;

    private final IdAllocator _idAllocator;

    /**
     * Map, keyed on InjectKey, of field name.
     */
    private final Map<MultiKey, String> _injectionCache = newMap();

    /**
     * Map from a field to the annotation objects for that field.
     */
    private Map<String, List<Annotation>> _fieldAnnotations = newMap();

    /**
     * Used to identify fields that have been "claimed" by other annotations.
     */
    private Map<String, Object> _claimedFields = newMap();

    private Set<String> _addedFieldNames = newSet();

    private Set<CtBehavior> _addedMethods = newSet();

    // Cache of class annotations

    private List<Annotation> _classAnnotations;

    // Cache of method annotations

    private Map<CtMethod, List<Annotation>> _methodAnnotations = newMap();

    private Map<CtMethod, TransformMethodSignature> _methodSignatures = newMap();

    // Key is field name, value is expression used to replace read access

    private Map<String, String> _fieldReadTransforms;

    // Key is field name, value is expression used to replace read access
    private Map<String, String> _fieldWriteTransforms;

    private Set<String> _removedFieldNames;

    /**
     * Contains the assembled Javassist code for the class' default constructor.
     */
    private StringBuilder _constructor = new StringBuilder();

    private final List<ConstructorArg> _constructorArgs;

    private final ComponentModel _componentModel;

    private final String _resourcesFieldName;

    private final StringBuilder _description = new StringBuilder();

    private Formatter _formatter = new Formatter(_description);

    private ClassLoader _loader;

    /**
     * This is a constructor for a base class.
     */
    public InternalClassTransformationImpl(CtClass ctClass, ClassLoader loader, Logger logger,
                                           ComponentModel componentModel)
    {
        _ctClass = ctClass;
        _classPool = _ctClass.getClassPool();
        _loader = loader;
        _parentTransformation = null;
        _componentModel = componentModel;

        _idAllocator = new IdAllocator();

        _logger = logger;

        preloadMemberNames();

        _constructorArgs = newList();
        _constructor.append("{\n");

        addImplementedInterface(Component.class);

        _resourcesFieldName = addInjectedFieldUncached(InternalComponentResources.class, "resources", null);

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC | Modifier.FINAL,
                                                                    ComponentResources.class.getName(),
                                                                    "getComponentResources", null, null);

        addMethod(sig, "return " + _resourcesFieldName + ";");

        // The "}" will be added later, inside  finish().
    }

    /**
     * Constructor for a component sub-class.
     */
    public InternalClassTransformationImpl(CtClass ctClass, InternalClassTransformation parentTransformation,
                                           ClassLoader loader, Logger logger, ComponentModel componentModel)
    {
        _ctClass = ctClass;
        _classPool = _ctClass.getClassPool();
        _loader = loader;
        _logger = logger;
        _parentTransformation = parentTransformation;
        _componentModel = componentModel;

        _resourcesFieldName = parentTransformation.getResourcesFieldName();

        _idAllocator = parentTransformation.getIdAllocator();

        preloadMemberNames();

        verifyFields();

        _constructorArgs = parentTransformation.getConstructorArgs();

        int count = _constructorArgs.size();

        // Build the call to the super-constructor.

        _constructor.append("{ super(");

        for (int i = 1; i <= count; i++)
        {
            if (i > 1) _constructor.append(", ");

            // $0 is implicitly self, so the 0-index ConstructorArg will be Javassisst
            // pseudeo-variable $1, and so forth.

            _constructor.append("$");
            _constructor.append(i);
        }

        _constructor.append(");\n");

        // The "}" will be added later, inside  finish().
    }

    private void freeze()
    {
        _frozen = true;

        // Free up stuff we don't need after freezing.
        // Everything else should be final.

        _fieldAnnotations = null;
        _claimedFields = null;
        _addedFieldNames = null;
        _addedMethods = null;
        _classAnnotations = null;
        _methodAnnotations = null;
        _methodSignatures = null;
        _fieldReadTransforms = null;
        _fieldWriteTransforms = null;
        _removedFieldNames = null;
        _constructor = null;
        _formatter = null;
        _loader = null;
        // _ctClass = null; -- needed by toString()
        _classPool = null;
    }

    public String getResourcesFieldName()
    {
        return _resourcesFieldName;
    }

    /**
     * Loads the names of all declared fields and methods into the idAllocator.
     */

    private void preloadMemberNames()
    {
        addMemberNames(_ctClass.getDeclaredFields());
        addMemberNames(_ctClass.getDeclaredMethods());
    }

    void verifyFields()
    {
        List<String> names = newList();

        for (CtField field : _ctClass.getDeclaredFields())
        {
            String name = field.getName();

            if (_addedFieldNames.contains(name)) continue;

            int modifiers = field.getModifiers();

            // Fields must be either static or private.

            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) continue;

            names.add(name);
        }

        if (!names.isEmpty()) throw new RuntimeException(ServicesMessages.nonPrivateFields(getClassName(), names));
    }

    private void addMemberNames(CtMember[] members)
    {
        for (CtMember member : members)
        {
            _idAllocator.allocateId(member.getName());
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

        if (method == null) throw new IllegalArgumentException(ServicesMessages.noDeclaredMethod(_ctClass, signature));

        List<Annotation> annotations = findMethodAnnotations(method);

        return findAnnotationInList(annotationClass, annotations);
    }

    /**
     * Searches an array of objects (that are really annotations instances) to find one that is of
     * the correct type, which is returned.
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
        List<Annotation> annotations = _fieldAnnotations.get(fieldName);

        if (annotations == null)
        {
            annotations = findAnnotationsForField(fieldName);
            _fieldAnnotations.put(fieldName, annotations);
        }

        return annotations;
    }

    private List<Annotation> findMethodAnnotations(CtMethod method)
    {
        List<Annotation> annotations = _methodAnnotations.get(method);

        if (annotations == null)
        {
            annotations = extractAnnotations(method);

            _methodAnnotations.put(method, annotations);
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
            List<Annotation> result = newList();

            addAnnotationsToList(result, member.getAnnotations());

            return result;
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void addAnnotationsToList(List<Annotation> list, Object[] annotations)
    {
        for (Object o : annotations)
        {
            Annotation a = (Annotation) o;
            list.add(a);
        }
    }

    private CtField findDeclaredCtField(String fieldName)
    {
        try
        {
            return _ctClass.getDeclaredField(fieldName);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ServicesMessages.missingDeclaredField(_ctClass, fieldName), ex);
        }
    }

    public String newMemberName(String suggested)
    {
        failIfFrozen();

        String memberName = InternalUtils.createMemberName(notBlank(suggested, "suggested"));

        return _idAllocator.allocateId(memberName);
    }

    public String newMemberName(String prefix, String baseName)
    {
        return newMemberName(prefix + "_" + InternalUtils.stripMemberPrefix(baseName));
    }

    public void addImplementedInterface(Class interfaceClass)
    {
        failIfFrozen();

        String interfaceName = interfaceClass.getName();

        try
        {
            CtClass ctInterface = _classPool.get(interfaceName);

            if (classImplementsInterface(ctInterface)) return;

            implementDefaultMethodsForInterface(ctInterface);

            _ctClass.addInterface(ctInterface);

        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Adds default implementations for the methods defined by the interface (and all of its
     * super-interfaces). The implementations return null (or 0, or false, as appropriate to to the
     * method type). There are a number of degenerate cases that are not covered properly: these are
     * related to base interfaces that may be implemented by base classes.
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
            CtMethod newMethod = CtNewMethod.copy(method, _ctClass, null);

            // Methods from interfaces are always public. We definitely
            // need to change the modifiers of the method so that
            // it is not abstract.

            newMethod.setModifiers(Modifier.PUBLIC);

            // Javassist will provide a minimal implementation for us (return null, false, 0,
            // whatever).

            newMethod.setBody(null);

            _ctClass.addMethod(newMethod);

            TransformMethodSignature sig = getMethodSignature(newMethod);

            addMethodToDescription("add default", sig, "<default>");
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ServicesMessages.errorAddingMethod(_ctClass, method
                    .getName(), ex), ex);
        }

    }

    /**
     * Check to see if the target class (or any of its super classes) implements the provided
     * interface. This is geared for simple interfaces (that don't extend other interfaces), thus if
     * the class (or a base class) implement interface Y that extends interface X, we may not return
     * true for interface X.
     */

    private boolean classImplementsInterface(CtClass ctInterface) throws NotFoundException
    {

        for (CtClass current = _ctClass; current != null; current = current.getSuperclass())
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
        notBlank(fieldName, "fieldName");
        notNull(tag, "tag");

        failIfFrozen();

        Object existing = _claimedFields.get(fieldName);

        if (existing != null)
        {
            String message = ServicesMessages.fieldAlreadyClaimed(fieldName, _ctClass, existing, tag);

            throw new RuntimeException(message);
        }

        // TODO: Ensure that fieldName is a known field?

        _claimedFields.put(fieldName, tag);
    }

    public void addMethod(TransformMethodSignature signature, String methodBody)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(signature.getReturnType());
        CtClass[] parameters = buildCtClassList(signature.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(signature.getExceptionTypes());

        String action = "add";

        try
        {
            CtMethod existing = _ctClass.getDeclaredMethod(signature.getMethodName(), parameters);

            if (existing != null)
            {
                action = "replace";

                _ctClass.removeMethod(existing);
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

            CtMethod method = new CtMethod(returnType, signature.getMethodName(), parameters, _ctClass);

            // TODO: Check for duplicate method add

            method.setModifiers(signature.getModifiers());

            method.setBody(methodBody);
            method.setExceptionTypes(exceptions);

            _ctClass.addMethod(method);

            _addedMethods.add(method);
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
            return _classPool.get(type);
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

        _addedMethods.add(method);
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
        _formatter.format("%s method: %s %s %s(", operation, Modifier.toString(methodSignature
                .getModifiers()), methodSignature.getReturnType(), methodSignature.getMethodName());

        String[] parameterTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (i > 0) _description.append(", ");

            _formatter.format("%s $%d", parameterTypes[i], i + 1);
        }

        _description.append(")");

        String[] exceptionTypes = methodSignature.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++)
        {
            if (i == 0) _description.append("\n  throws ");
            else _description.append(", ");

            _description.append(exceptionTypes[i]);
        }

        _formatter.format("\n%s\n\n", methodBody);
    }

    private CtMethod findMethod(TransformMethodSignature methodSignature)
    {
        CtMethod method = findDeclaredMethod(methodSignature);

        if (method != null) return method;

        CtMethod result = addOverrideOfSuperclassMethod(methodSignature);

        if (result != null) return result;

        throw new IllegalArgumentException(ServicesMessages.noDeclaredMethod(_ctClass, methodSignature));
    }

    private CtMethod findDeclaredMethod(TransformMethodSignature methodSignature)
    {
        for (CtMethod method : _ctClass.getDeclaredMethods())
        {
            if (match(method, methodSignature)) return method;
        }

        return null;
    }

    private CtMethod addOverrideOfSuperclassMethod(TransformMethodSignature methodSignature)
    {
        try
        {
            for (CtClass current = _ctClass; current != null; current = current.getSuperclass())
            {
                for (CtMethod method : current.getDeclaredMethods())
                {
                    if (match(method, methodSignature))
                    {
                        // TODO: If the moethod is not overridable (i.e. private, or final)?
                        // Perhaps we should limit it to just public methods.

                        CtMethod newMethod = CtNewMethod.delegator(method, _ctClass);
                        _ctClass.addMethod(newMethod);

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

        List<String> result = newList();

        try
        {
            for (CtField field : _ctClass.getDeclaredFields())
            {
                if (!isInstanceField(field)) continue;

                String fieldName = field.getName();

                if (_claimedFields.containsKey(fieldName)) continue;

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

    public List<TransformMethodSignature> findMethodsWithAnnotation(Class<? extends Annotation> annotationClass)
    {
        failIfFrozen();

        List<TransformMethodSignature> result = newList();

        for (CtMethod method : _ctClass.getDeclaredMethods())
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
        notNull(filter, "filter");

        List<TransformMethodSignature> result = newList();

        for (CtMethod method : _ctClass.getDeclaredMethods())
        {
            TransformMethodSignature sig = getMethodSignature(method);

            if (filter.accept(sig)) result.add(sig);
        }

        Collections.sort(result);

        return result;
    }

    private TransformMethodSignature getMethodSignature(CtMethod method)
    {
        TransformMethodSignature result = _methodSignatures.get(method);
        if (result == null)
        {
            try
            {
                String type = method.getReturnType().getName();
                String[] parameters = toTypeNames(method.getParameterTypes());
                String[] exceptions = toTypeNames(method.getExceptionTypes());

                result = new TransformMethodSignature(method.getModifiers(), type, method.getName(), parameters,
                                                      exceptions);

                _methodSignatures.put(method, result);
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

        List<String> names = newList();

        Set<String> skipped = newSet();

        skipped.addAll(_claimedFields.keySet());
        skipped.addAll(_addedFieldNames);

        if (_removedFieldNames != null) skipped.addAll(_removedFieldNames);

        for (CtField field : _ctClass.getDeclaredFields())
        {
            if (!isInstanceField(field)) continue;

            String name = field.getName();

            if (skipped.contains(name)) continue;

            // May need to add a filter to edit out explicitly added fields.

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
            CtField field = _ctClass.getDeclaredField(fieldName);

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
            return _ctClass.getDeclaredField(fieldName).getModifiers();
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
            CtField field = _ctClass.getDeclaredField(fieldName);

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

            CtField field = new CtField(ctType, fieldName, _ctClass);
            field.setModifiers(modifiers);

            _ctClass.addField(field);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        _formatter
                .format("add field: %s %s %s;\n\n", Modifier.toString(modifiers), type, fieldName);

        _addedFieldNames.add(fieldName);

        return fieldName;
    }

    public String addInjectedField(Class type, String suggestedName, Object value)
    {
        notNull(type, "type");

        failIfFrozen();

        MultiKey key = new MultiKey(type, value);

        String fieldName = searchForPreviousInjection(key);

        if (fieldName != null) return fieldName;

        // TODO: Probably doesn't handle arrays and primitives.

        fieldName = addInjectedFieldUncached(type, suggestedName, value);

        // Remember the injection in-case this class, or a subclass, injects the value again.

        _injectionCache.put(key, fieldName);

        return fieldName;
    }

    /**
     * This is split out from {@link #addInjectedField(Class, String, Object)} to handle a special
     * case for the InternalComponentResources, which is null when "injected" (during the class
     * transformation) and is only determined when a component is actually instantiated.
     */
    private String addInjectedFieldUncached(Class type, String suggestedName, Object value)
    {
        CtClass ctType;

        try
        {
            ctType = _classPool.get(type.getName());
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        String fieldName = addField(Modifier.PROTECTED | Modifier.FINAL, type.getName(), suggestedName);

        addInjectToConstructor(fieldName, ctType, value);

        return fieldName;
    }

    public String searchForPreviousInjection(MultiKey key)
    {
        String result = _injectionCache.get(key);

        if (result != null) return result;

        if (_parentTransformation != null) return _parentTransformation.searchForPreviousInjection(key);

        return null;
    }

    /**
     * Adds a parameter to the constructor for the class; the parameter is used to initialize the
     * value for a field.
     *
     * @param fieldName name of field to inject
     * @param fieldType Javassist type of the field (and corresponding parameter)
     * @param value     the value to be injected (which will in unusual cases be null)
     */
    private void addInjectToConstructor(String fieldName, CtClass fieldType, Object value)
    {
        _constructorArgs.add(new ConstructorArg(fieldType, value));

        extendConstructor(format("  %s = $%d;", fieldName, _constructorArgs.size()));
    }

    public void injectField(String fieldName, Object value)
    {
        notNull(fieldName, "fieldName");

        failIfFrozen();

        CtClass type = getFieldCtType(fieldName);

        addInjectToConstructor(fieldName, type, value);

        makeReadOnly(fieldName);
    }

    private CtClass convertNameToCtType(String type) throws NotFoundException
    {
        return _classPool.get(type);
    }

    public void finish()
    {
        failIfFrozen();

        performFieldTransformations();

        addConstructor();

        verifyFields();

        freeze();
    }

    private void addConstructor()
    {
        String initializer = _idAllocator.allocateId("initializer");

        try
        {
            CtConstructor defaultConstructor = _ctClass.getConstructor("()V");

            CtMethod initializerMethod = defaultConstructor.toMethod(initializer, _ctClass);

            _ctClass.addMethod(initializerMethod);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        _formatter.format("convert default constructor: %s();\n\n", initializer);

        int count = _constructorArgs.size();

        CtClass[] types = new CtClass[count];

        for (int i = 0; i < count; i++)
        {
            ConstructorArg arg = _constructorArgs.get(i);

            types[i] = arg.getType();
        }

        // Add a call to the initializer; the method converted fromt the classes default
        // constructor.

        _constructor.append("  ");
        _constructor.append(initializer);

        // This finally matches the "{" added inside the constructor

        _constructor.append("();\n\n}");

        String constructorBody = _constructor.toString();

        try
        {
            CtConstructor cons = CtNewConstructor.make(types, null, constructorBody, _ctClass);
            _ctClass.addConstructor(cons);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        _formatter.format("add constructor: %s(", _ctClass.getName());

        for (int i = 0; i < count; i++)
        {
            if (i > 0) _description.append(", ");

            _formatter.format("%s $%d", types[i].getName(), i + 1);
        }

        _formatter.format(")\n%s\n\n", constructorBody);
    }

    public Instantiator createInstantiator(Class componentClass)
    {
        String className = _ctClass.getName();

        if (!className.equals(componentClass.getName())) throw new IllegalArgumentException(
                ServicesMessages.incorrectClassForInstantiator(className, componentClass));

        Object[] parameters = new Object[_constructorArgs.size()];

        // Skip the first constructor argument, it's always a placeholder
        // for the InternalComponentResources instance that's provided
        // later.

        for (int i = 1; i < _constructorArgs.size(); i++)
        {
            parameters[i] = _constructorArgs.get(i).getValue();
        }

        return new ReflectiveInstantiator(_componentModel, componentClass, parameters);
    }

    private void failIfFrozen()
    {
        if (_frozen) throw new IllegalStateException(
                "The ClassTransformation instance (for " + _ctClass.getName() + ") has completed all transformations and may not be further modified.");
    }

    private void failIfNotFrozen()
    {
        if (!_frozen) throw new IllegalStateException(
                "The ClassTransformation instance (for " + _ctClass.getName() + ") has not yet completed all transformations.");
    }

    public IdAllocator getIdAllocator()
    {
        failIfNotFrozen();

        return _idAllocator;
    }

    public List<ConstructorArg> getConstructorArgs()
    {
        failIfNotFrozen();

        return CollectionFactory.newList(_constructorArgs);
    }

    public List<Annotation> getClassAnnotations()
    {
        failIfFrozen();

        if (_classAnnotations == null) assembleClassAnnotations();

        return _classAnnotations;
    }

    private void assembleClassAnnotations()
    {
        _classAnnotations = newList();

        try
        {
            for (CtClass current = _ctClass; current != null; current = current.getSuperclass())
            {
                addAnnotationsToList(_classAnnotations, current.getAnnotations());
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

            formatter.format("%s %s extends %s", Modifier.toString(_ctClass.getModifiers()), _ctClass.getName(),
                             _ctClass.getSuperclass().getName());

            CtClass[] interfaces = _ctClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++)
            {
                if (i == 0) builder.append("\n  implements ");
                else builder.append(", ");

                builder.append(interfaces[i].getName());
            }

            formatter.format("\n\n%s", _description.toString());
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
                                                                    new String[]{fieldType}, null);

        String message = ServicesMessages.readOnlyField(_ctClass.getName(), fieldName);

        String body = format("throw new java.lang.RuntimeException(\"%s\");", message);

        addMethod(sig, body);

        replaceWriteAccess(fieldName, methodName);
    }

    public void removeField(String fieldName)
    {
        _formatter.format("remove field %s;\n\n", fieldName);

        // TODO: We could check that there's an existing field read and field write transform ...

        if (_removedFieldNames == null) _removedFieldNames = newSet();

        _removedFieldNames.add(fieldName);

    }

    public void replaceReadAccess(String fieldName, String methodName)
    {
        // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
        // $0 is valid even inside a static method.

        String body = String.format("$_ = $0.%s();", methodName);

        if (_fieldReadTransforms == null) _fieldReadTransforms = newMap();

        // TODO: Collisions?

        _fieldReadTransforms.put(fieldName, body);

        _formatter.format("replace read %s: %s();\n\n", fieldName, methodName);
    }

    public void replaceWriteAccess(String fieldName, String methodName)
    {
        // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
        // $0 is valid even inside a static method.

        String body = String.format("$0.%s($1);", methodName);

        if (_fieldWriteTransforms == null) _fieldWriteTransforms = newMap();

        // TODO: Collisions?

        _fieldWriteTransforms.put(fieldName, body);

        _formatter.format("replace write %s: %s();\n\n", fieldName, methodName);
    }

    private void performFieldTransformations()
    {
        // If no field transformations have been requested, then we can save ourselves some
        // trouble!

        if (_fieldReadTransforms != null || _fieldWriteTransforms != null) replaceFieldAccess();

        if (_removedFieldNames != null)
        {
            for (String fieldName : _removedFieldNames)
            {
                try
                {
                    CtField field = _ctClass.getDeclaredField(fieldName);
                    _ctClass.removeField(field);
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

        if (_fieldReadTransforms == null) _fieldReadTransforms = newMap();

        if (_fieldWriteTransforms == null) _fieldWriteTransforms = newMap();

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

                _formatter.format("Checking field %s %s in method %s(): ", isRead ? "read" : "write", fieldName,
                                  method.getName());

                // Ignore any methods to were added as part of the transformation.
                // If we reference the field there, we really mean the field.

                if (_addedMethods.contains(where))
                {
                    _formatter.format("added method\n");
                    return;
                }

                Map<String, String> transformMap = isRead ? _fieldReadTransforms : _fieldWriteTransforms;

                String body = transformMap.get(fieldName);
                if (body == null)
                {
                    _formatter.format("field not transformed\n");
                    return;
                }

                _formatter.format("replacing with %s\n", body);

                access.replace(body);
            }
        };

        try
        {
            _ctClass.instrument(editor);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        _formatter.format("\n");
    }

    public Class toClass(String type)
    {
        failIfFrozen();

        // No reason why this can't be allowed to work after freezing.

        String finalType = TransformUtils.getWrapperTypeName(type);

        try
        {
            return Class.forName(finalType, true, _loader);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String getClassName()
    {
        return _ctClass.getName();
    }

    public Logger getLogger()
    {
        return _logger;
    }

    public void extendConstructor(String statement)
    {
        notNull(statement, "statement");

        failIfFrozen();

        _constructor.append(statement);
        _constructor.append("\n");
    }

    public String getMethodIdentifier(TransformMethodSignature signature)
    {
        notNull(signature, "signature");

        CtMethod method = findMethod(signature);

        int lineNumber = method.getMethodInfo2().getLineNumber(0);
        CtClass enclosingClass = method.getDeclaringClass();
        String sourceFile = enclosingClass.getClassFile2().getSourceFile();

        return format("%s.%s (at %s:%d)", enclosingClass.getName(), signature
                .getMediumDescription(), sourceFile, lineNumber);
    }

}
