// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.*;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

/**
 * Implementation of the {@link org.apache.tapestry5.internal.services.InternalClassTransformation} interface.
 */
@SuppressWarnings("all")
public final class InternalClassTransformationImpl implements InternalClassTransformation
{
    public static final MethodSignature INVOKE_SIGNATURE = new MethodSignature(MethodInvocationResult.class, "invoke",
            new Class[]
            { Object.class, Object[].class }, null);

    public static final MethodSignature FIELD_ACCESS_READ_SIGNATURE = new MethodSignature(Object.class, "read",
            new Class[]
            { Object.class }, null);

    public static final MethodSignature FIELD_ACCESS_WRITE_SIGNATURE = new MethodSignature(void.class, "write",
            new Class[]
            { Object.class, Object.class }, null);

    private static final int INIT_BUFFER_SIZE = 100;

    private boolean frozen;

    private final CtClass ctClass;

    private final Logger logger;

    private final InternalClassTransformation parentTransformation;

    private final ClassPool classPool;

    private final IdAllocator idAllocator;

    private final CtClass providerType;

    class TransformMethodImpl implements TransformMethod
    {
        final CtMethod method;

        private final TransformMethodSignature sig;

        private List<Annotation> annotations;

        private final boolean added;

        private ComponentMethodInvocationBuilder builder;

        private MethodAccess access;

        private String identifier;

        private Boolean override;

        private List<List<Annotation>> parameterAnnotations;

        TransformMethodImpl(CtMethod method, boolean added)
        {
            this.method = method;
            this.sig = toMethodSignature(method);
            this.added = added;

        }

        @Override
        public String toString()
        {
            return String.format("TransformMethod[%s]", getMethodIdentifier());
        }

        public int compareTo(TransformMethod o)
        {
            return sig.compareTo(o.getSignature());
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            if (annotations == null)
                annotations = extractAnnotations(method);

            return findAnnotationInList(annotationClass, annotations);
        }

        public <A extends Annotation> A getParameterAnnotation(int index, Class<A> annotationType)
        {
            if (parameterAnnotations == null)
                extractParameterAnnotations();

            return findAnnotationInList(annotationType, parameterAnnotations.get(index));
        }

        private void extractParameterAnnotations()
        {
            int count = sig.getParameterTypes().length;

            parameterAnnotations = CollectionFactory.newList();

            for (int parameterIndex = 0; parameterIndex < count; parameterIndex++)
            {
                List<Annotation> annotations = extractAnnotationsForParameter(parameterIndex);

                parameterAnnotations.add(annotations);
            }
        }

        private List<Annotation> extractAnnotationsForParameter(int parameterIndex)
        {
            List<Annotation> result = CollectionFactory.newList();

            Object[] parameterAnnotations = method.getAvailableParameterAnnotations()[parameterIndex];

            addAnnotationsToList(result, parameterAnnotations, false);

            return result;
        }

        public TransformMethodSignature getSignature()
        {
            return sig;
        }

        public String getName()
        {
            return sig.getMethodName();
        }

        public void addAdvice(ComponentMethodAdvice advice)
        {
            failIfFrozen();

            assert advice != null;

            if (builder == null)
                builder = createBuilder(sig);

            builder.addAdvice(advice);

            formatter.format("add advice %s : %s\n\n", sig.getMediumDescription(), advice);
        }

        public void addOperationAfter(ComponentInstanceOperation operation)
        {
            addAdvice(toAfterAdvice(operation));
        }

        public void addOperationBefore(ComponentInstanceOperation operation)
        {
            addAdvice(toBeforeAdvice(operation));
        }

        public MethodAccess getAccess()
        {
            failIfFrozen();

            if (access == null)
                access = createMethodAccess();

            return access;
        }

        private MethodAccess createMethodAccess()
        {
            if (isPrivate())
                return createPrivateMethodAccess();

            return createNonPrivateMethodAccess();
        }

        private boolean isPrivate()
        {
            return Modifier.isPrivate(sig.getModifiers());
        }

        private MethodAccess createNonPrivateMethodAccess()
        {
            // For a public method, given the instance, we can just invoke the method directly
            // from the MethodAccess object.

            String accessTarget = "instance." + sig.getMethodName();

            return createMethodAccessForTarget(accessTarget, false);
        }

        private MethodAccess createMethodAccessForTarget(String accessTarget, boolean passInstance)
        {
            boolean isVoid = sig.getReturnType().equals("void");

            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s instance = (%<s) $1;", getClassName());

            builder.addln("try").begin();

            if (!isVoid)
            {
                builder.add("return success(($w) ");
            }

            // Call the target, even if the eventual method is void

            builder.add(accessTarget);
            builder.add("(");

            if (passInstance)
                builder.add("instance");

            int p = 0;

            for (String type : sig.getParameterTypes())
            {
                if (passInstance || p != 0)
                    builder.add(", ");

                String ref = String.format("$2[%d]", p++);
                builder.add(ClassFabUtils.castReference(ref, type));
            }

            // Balance the call to success()
            if (!isVoid)
                builder.add(")");

            builder.addln(");");

            if (isVoid)
                builder.addln("return success(null);");

            builder.end(); // try
            builder.addln("catch (java.lang.RuntimeException ex) { throw ex; }");
            builder.addln("catch (java.lang.Exception ex) { return fail(ex); }");

            builder.end();

            return instantiateMethodAccessFromBody(builder.toString());
        }

        private MethodAccess instantiateMethodAccessFromBody(String body)
        {
            // The access object is created in the same package as the component, so that it can access
            // protected and package private methods.
            String accessClassName = String.format("%s$MethodAccess_%s_%s", getClassName(), sig.getMethodName(),
                    ClassFabUtils.nextUID());

            ClassFab cf = classFactory.newClass(accessClassName, AbstractMethodAccess.class);

            cf.addMethod(Modifier.PUBLIC, INVOKE_SIGNATURE, body);

            cf.addToString(String.format("MethodAccess[method %s of class %s]", sig.getMediumDescription(),
                    getClassName()));

            Class accessClass = cf.createClass();

            try
            {
                Object accessInstance = accessClass.newInstance();

                return (MethodAccess) accessInstance;
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        private MethodAccess createPrivateMethodAccess()
        {
            // As with Java inner classes, we have to create a static bridge method.

            String staticAccessMethodName = createStaticAccessMethodForNonPublicMethod();

            // Have the MethodAccess object call the static method and pass the
            // instance object as the first parameter. The static method will then
            // invoke the non-public method on the passed instance.

            return createMethodAccessForTarget(String.format("%s#%s", getClassName(), staticAccessMethodName), true);
        }

        /**
         * The static method takes the same parameters as the main method, but takes
         * an instance object first. Invoking the static method turns into an invocation
         * of the proper method of the instance object.
         *
         * @return the name of the created static access method
         */
        private String createStaticAccessMethodForNonPublicMethod()
        {
            List<String> parameterTypes = CollectionFactory.newList(getClassName());
            parameterTypes.addAll(Arrays.asList(sig.getParameterTypes()));

            String methodName = newMemberName("access", sig.getMethodName());

            TransformMethodSignature accessMethodSignature = new TransformMethodSignature(Modifier.PUBLIC
                    + Modifier.STATIC, sig.getReturnType(), methodName, parameterTypes.toArray(new String[0]),
                    sig.getExceptionTypes());

            boolean isVoid = sig.getReturnType().equals("void");

            BodyBuilder builder = new BodyBuilder();

            builder.begin();

            if (!isVoid)
                builder.add("return ");

            builder.add("$1.%s(", sig.getMethodName());

            for (int i = 0; i < sig.getParameterTypes().length; i++)
            {
                if (i > 0)
                    builder.add(", ");

                builder.add("$%d", i + 2);
            }

            builder.addln(");");

            builder.end();

            addNewMethod(accessMethodSignature, builder.toString());

            return methodName;
        }

        public String getMethodIdentifier()
        {
            if (identifier == null)
            {
                int lineNumber = method.getMethodInfo2().getLineNumber(0);
                CtClass enclosingClass = method.getDeclaringClass();
                String sourceFile = enclosingClass.getClassFile2().getSourceFile();

                identifier = String.format("%s.%s (at %s:%d)", enclosingClass.getName(), sig.getMediumDescription(),
                        sourceFile, lineNumber);
            }

            return identifier;
        }

        public boolean isOverride()
        {
            if (override == null)
                override = searchForOverride();

            return override;
        }

        private boolean searchForOverride()
        {
            InternalClassTransformation search = parentTransformation;

            while (search != null)
            {
                if (search.isMethod(sig))
                    return true;

                search = search.getParentTransformation();
            }

            // Not found in any super-class.

            return false;
        }

        void doFinish()
        {
            if (builder != null)
            {
                builder.commit();
                builder = null;
            }
        }
    }

    class TransformFieldImpl implements TransformField
    {
        private final CtField field;

        private final CtClass fieldType;

        private final String name, type, signature;

        private boolean added;

        private List<Annotation> annotations;

        private Object claimTag;

        String readValueBody, writeValueBody;

        private DelegateFieldAccess delegateFieldAccess;

        private org.apache.tapestry5.services.FieldAccess access;

        TransformFieldImpl(CtField field, boolean added)
        {
            this.field = field;
            this.name = field.getName();
            this.added = added;

            try
            {
                fieldType = field.getType();

                final FieldInfo fieldInfo = field.getFieldInfo2();
                SignatureAttribute sig = (SignatureAttribute) fieldInfo.getAttribute(SignatureAttribute.tag);
                // This contains the generic type information
                signature = sig != null ? sig.getSignature() : null;

                type = fieldType.getName();
            }
            catch (NotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        void doFinish()
        {
            if (delegateFieldAccess != null)
            {
                access = createAccess();
                delegateFieldAccess.delegate = access;
                delegateFieldAccess = null;
            }
        }

        @Override
        public String toString()
        {
            return String.format("TransformField[%s %s.%s(%s)]", Modifier.toString(field.getModifiers()),
                    getClassName(), name, type);
        }

        public int compareTo(TransformField o)
        {
            return name.compareTo(o.getName());
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }

        public String getSignature()
        {
            return signature;
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            failIfFrozen();

            if (annotations == null)
                annotations = extractAnnotations(field);

            return findAnnotationInList(annotationClass, annotations);
        }

        public void claim(Object tag)
        {
            assert tag != null;

            failIfFrozen();

            if (claimTag != null)
                throw new IllegalStateException(String.format(
                        "Field %s of class %s is already claimed by %s and can not be claimed by %s.", getName(),
                        getClassName(), claimTag, tag));

            claimTag = tag;

            formatter.format("Field %s claimed by %s\n\n", name, tag);
        }

        public boolean isClaimed()
        {
            return claimTag != null;
        }

        public int getModifiers()
        {
            return field.getModifiers();
        }

        void replaceReadAccess(String methodName)
        {
            failIfFrozen();

            if (readValueBody != null)
                throw new IllegalStateException(String.format("Field %s.%s has already had read access replaced.",
                        getClassName(), name));

            // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
            // $0 is valid even inside a static method.

            readValueBody = String.format("$_ = $0.%s();", methodName);

            formatter.format("replace read %s: %s();\n\n", name, methodName);

            fieldAccessReplaced = true;
        }

        void replaceWriteAccess(String methodName)
        {
            failIfFrozen();

            if (writeValueBody != null)
                throw new IllegalStateException(String.format("Field %s.%s has already had write access replaced.",
                        getClassName(), name));

            // Explicitly reference $0 (aka "this") because of TAPESTRY-1511.
            // $0 is valid even inside a static method.

            writeValueBody = String.format("$0.%s($1);", methodName);

            formatter.format("replace write %s: %s();\n\n", name, methodName);

            fieldAccessReplaced = true;
        }

        public org.apache.tapestry5.services.FieldAccess getAccess()
        {
            failIfFrozen();

            if (access != null)
                return access;

            if (delegateFieldAccess == null)
            {
                delegateFieldAccess = new DelegateFieldAccess();
            }

            return delegateFieldAccess;
        }

        private org.apache.tapestry5.services.FieldAccess createAccess()
        {
            TransformMethod reader = createReader();
            TransformMethod writer = createWriter();

            return createFieldAccess(reader, writer);
        }

        private org.apache.tapestry5.services.FieldAccess createFieldAccess(TransformMethod reader,
                TransformMethod writer)
        {
            ClassFab cf = classFactory.newClass(org.apache.tapestry5.services.FieldAccess.class);

            addFieldAccessReadMethod(cf, reader);
            addFieldAccessWriteMethod(cf, writer);

            cf.addToString(String.format("FieldAccess<%s.%s>", getClassName(), name));

            Class accessClass = cf.createClass();

            try
            {
                return (org.apache.tapestry5.services.FieldAccess) accessClass.newInstance();
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        private void addFieldAccessReadMethod(ClassFab cf, TransformMethod readAccess)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s instance = (%<s) $1;", getClassName());
            builder.addln("return ($w) instance.%s();", readAccess.getName());

            builder.end();

            cf.addMethod(Modifier.PUBLIC, FIELD_ACCESS_READ_SIGNATURE, builder.toString());
        }

        private void addFieldAccessWriteMethod(ClassFab cf, TransformMethod writeAccess)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s instance = (%<s) $1;", getClassName());
            builder.addln("%s value = %s;", type, ClassFabUtils.castReference("$2", type));
            builder.addln("instance.%s(value);", writeAccess.getName());

            builder.end();

            cf.addMethod(Modifier.PUBLIC, FIELD_ACCESS_WRITE_SIGNATURE, builder.toString());
        }

        private TransformMethod createReader()
        {
            String methodName = newMemberName("readaccess", name);

            TransformMethodSignature signature = new TransformMethodSignature(Modifier.PUBLIC, type, methodName, null,
                    null);

            // Add the method as existing, so that the field access may be replaced with a FieldValueConduit
            // if necessary.
            return addOrReplaceMethod(signature, String.format("return %s;", name), false);
        }

        private TransformMethod createWriter()
        {
            String methodName = newMemberName("writeaccess", name);

            TransformMethodSignature signature = new TransformMethodSignature(Modifier.PUBLIC, "void", methodName,
                    new String[]
                    { type }, null);

            // Add the method as existing, so that the field access may be replaced with a FieldValueConduit
            // if necessary.

            return addOrReplaceMethod(signature, String.format("%s = $1;", name), false);
        }

        public void replaceAccess(ComponentValueProvider<FieldValueConduit> conduitProvider)
        {
            replaceAccess(addIndirectInjectedField(FieldValueConduit.class, name + "$conduit", conduitProvider));
        }

        public void replaceAccess(FieldValueConduit conduit)
        {
            String fieldName = addInjectedFieldUncached(FieldValueConduit.class, name + "$conduit", conduit);

            // TODO: If access != null?

            access = toFieldAccess(conduit);

            if (delegateFieldAccess != null)
            {
                delegateFieldAccess.delegate = access;
                delegateFieldAccess = null;
            }

            replaceAccess(getTransformFieldImpl(fieldName));
        }

        public void replaceAccess(TransformField conduitField)
        {
            failIfFrozen();

            String conduitFieldName = conduitField.getName();

            String readMethodName = newMemberName("get", name);

            TransformMethodSignature readSig = new TransformMethodSignature(Modifier.PRIVATE, type, readMethodName,
                    null, null);

            String cast = TransformUtils.getWrapperTypeName(type);

            // The ($r) cast will convert the result to the method return type; generally
            // this does nothing. but for primitive types, it will unwrap
            // the wrapper type back to a primitive.

            BodyBuilder builder = new BodyBuilder();

            builder.begin();

            builder.addln("%s result = ($r) ((%s) %s.get());", type, cast, conduitFieldName);

            if (developmentMode)
            {
                builder.addln("%s = result;", name);
            }

            builder.addln("return result;");

            builder.end();

            addNewMethod(readSig, builder.toString());

            replaceReadAccess(readMethodName);

            String writeMethodName = newMemberName("set", name);

            TransformMethodSignature writeSig = new TransformMethodSignature(Modifier.PRIVATE, "void", writeMethodName,
                    new String[]
                    { type }, null);

            builder.clear().begin();

            if (developmentMode)
            {
                builder.addln("%s = $1;", name);
            }

            builder.addln("%s.set(($w) $1);", conduitFieldName);

            builder.end();

            addNewMethod(writeSig, builder.toString());

            replaceWriteAccess(writeMethodName);
        }

        public void inject(Object value)
        {
            failIfFrozen();

            addInjectToConstructor(name, fieldType, value);

            makeReadOnly(name);
        }

        public <T> void injectIndirect(ComponentValueProvider<T> provider)
        {
            assert provider != null;

            failIfFrozen();

            String argReference = addConstructorArg(providerType, provider);

            addToConstructor(String.format("  %s = (%s) (%s).get(%s);", name, type, argReference, resourcesFieldName));

            makeReadOnly(name);
        }

    }

    private final Map<TransformMethodSignature, TransformMethodImpl> methods = CollectionFactory.newMap();

    private Map<String, TransformFieldImpl> fields = CollectionFactory.newMap();

    /**
     * Map, keyed on InjectKey, of field name. Injections are always added as protected (not
     * private) fields to support
     * sharing of injections between a base class and a sub class.
     */
    private final Map<InjectionKey, String> injectionCache = CollectionFactory.newMap();

    // Cache of class annotation

    private List<Annotation> classAnnotations;

    /**
     * Contains the assembled Javassist code for the class' default constructor.
     */
    private StringBuilder constructor = new StringBuilder(INIT_BUFFER_SIZE);

    private final List<Object> constructorArgs;

    private final MutableComponentModel componentModel;

    private final String resourcesFieldName;

    private StringBuilder description = new StringBuilder(INIT_BUFFER_SIZE);

    private Formatter formatter = new Formatter(description);

    private final ClassFactory classFactory;

    private final ComponentClassCache componentClassCache;

    private final CtClassSource classSource;

    // In development mode, extra logic is inserted to "shadow" fields (that have been replaced with a FieldConduit).
    // The "live" value is stored into the field any time it is read or updated, to assist with debugging.
    // https://issues.apache.org/jira/browse/TAP5-1208
    private final boolean developmentMode;

    // If true, then during finish, it is necessary to search for field replacements
    // (field reads or writes replaces with method calls).
    private boolean fieldAccessReplaced;

    /**
     * Signature for newInstance() method of Instantiator.
     */
    private static final MethodSignature NEW_INSTANCE_SIGNATURE = new MethodSignature(Component.class, "newInstance",
            new Class[]
            { InternalComponentResources.class }, null);

    private static final TransformMethodSignature GET_COMPONENT_RESOURCES_SIGNATURE = new TransformMethodSignature(
            Modifier.PUBLIC | Modifier.FINAL, ComponentResources.class.getName(), "getComponentResources", null, null);

    /**
     * This is a constructor for a base class.
     */
    public InternalClassTransformationImpl(ClassFactory classFactory, CtClass ctClass,
            ComponentClassCache componentClassCache, MutableComponentModel componentModel, CtClassSource classSource,
            boolean developmentMode)
    {
        this.ctClass = ctClass;
        this.componentClassCache = componentClassCache;
        this.classSource = classSource;
        this.developmentMode = developmentMode;
        classPool = this.ctClass.getClassPool();
        this.classFactory = classFactory;
        parentTransformation = null;
        this.componentModel = componentModel;

        providerType = toCtClass(ComponentValueProvider.class);

        idAllocator = new IdAllocator();

        logger = componentModel.getLogger();

        preloadMembers();

        constructorArgs = CollectionFactory.newList();
        constructor.append("{\n");

        addImplementedInterface(Component.class);

        resourcesFieldName = addField(Modifier.PROTECTED | Modifier.FINAL, InternalComponentResources.class.getName(),
                "resources");

        addToConstructor(String.format("  %s = $1;", resourcesFieldName));

        addNewMethod(GET_COMPONENT_RESOURCES_SIGNATURE, "return " + resourcesFieldName + ";");

        // The "}" will be added later, inside finish().
    }

    /**
     * Constructor for a component sub-class.
     * 
     * @param developmentMode
     *            TODO
     */
    private InternalClassTransformationImpl(CtClass ctClass, InternalClassTransformation parentTransformation,
            ClassFactory classFactory, CtClassSource classSource, ComponentClassCache componentClassCache,
            MutableComponentModel componentModel, boolean developmentMode)
    {
        this.ctClass = ctClass;
        this.componentClassCache = componentClassCache;
        this.classSource = classSource;
        classPool = this.ctClass.getClassPool();
        this.classFactory = classFactory;
        logger = componentModel.getLogger();
        this.parentTransformation = parentTransformation;
        this.componentModel = componentModel;
        this.developmentMode = developmentMode;

        providerType = toCtClass(ComponentValueProvider.class);

        resourcesFieldName = parentTransformation.getResourcesFieldName();

        idAllocator = parentTransformation.getIdAllocator();

        preloadMembers();

        constructorArgs = parentTransformation.getConstructorArgs();

        // Re-invoke the constructor, passing the resources and array of values to the super class

        addToConstructor("{\n  super($$);");

        // The "}" will be added later, inside finish().
    }

    public InternalClassTransformation createChildTransformation(CtClass childClass, MutableComponentModel childModel)
    {
        return new InternalClassTransformationImpl(childClass, this, classFactory, classSource, componentClassCache,
                childModel, developmentMode);
    }

    private void freeze()
    {
        frozen = true;

        // Free up stuff we don't need after freezing.
        // Everything else should be final.

        fields = null;

        classAnnotations = null;
        constructor = null;
        formatter = null;
        description = null;
    }

    public String getResourcesFieldName()
    {
        return resourcesFieldName;
    }

    /**
     * Loads all existing fields and methods defined by the class.
     */
    private void preloadMembers()
    {
        preloadFields();
        preloadMethods();
    }

    private void preloadMethods()
    {
        for (CtMethod method : ctClass.getDeclaredMethods())
        {
            recordMethod(method, false);

            idAllocator.allocateId(method.getName());
        }
    }

    /**
     * Converts and stores {@link CtField} to {@link TransformField}, and checks that each field is
     * one of:
     * <ul>
     * <li>private</li>
     * <li>static</li>
     * <li>groovy.lang.MetaClass (for Groovy compatibility)</li> </li>
     */
    private void preloadFields()
    {
        List<String> names = CollectionFactory.newList();

        for (CtField field : ctClass.getDeclaredFields())
        {
            String name = field.getName();

            idAllocator.allocateId(name);

            TransformFieldImpl tfi = fields.put(name, new TransformFieldImpl(field, false));

            int modifiers = field.getModifiers();

            // Fields must be either static or private.

            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers))
                continue;

            // Groovy injects a public field named metaClass. We ignore it, and add it as a claimed
            // field to prevent any of the workers from seeing it.

            if (name.equals("metaClass") && getFieldType(name).equals("groovy.lang.MetaClass"))
            {
                tfi.claim("Ignored");
                continue;
            }

            names.add(name);
        }

        if (!names.isEmpty())
            throw new RuntimeException(ServicesMessages.nonPrivateFields(getClassName(), names));
    }

    public <T extends Annotation> T getFieldAnnotation(String fieldName, Class<T> annotationClass)
    {
        return getField(fieldName).getAnnotation(annotationClass);
    }

    public <T extends Annotation> T getMethodAnnotation(TransformMethodSignature signature, Class<T> annotationClass)
    {
        return getOrCreateMethod(signature).getAnnotation(annotationClass);
    }

    /**
     * Searches an array of objects (that are really annotations instances) to find one that is of
     * the correct type,
     * which is returned.
     *
     * @param <T>
     * @param annotationClass
     *            the annotation to search for
     * @param annotations
     *            the available annotations
     * @return the matching annotation instance, or null if not found
     */
    private <T extends Annotation> T findAnnotationInList(Class<T> annotationClass, List<Annotation> annotations)
    {
        for (Object annotation : annotations)
        {
            if (annotationClass.isInstance(annotation))
                return annotationClass.cast(annotation);
        }

        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return findAnnotationInList(annotationClass, getClassAnnotations());
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

                if (inherited == null)
                    continue;
            }

            list.add(a);
        }
    }

    public TransformField getField(String fieldName)
    {
        return getTransformFieldImpl(fieldName);
    }

    private TransformFieldImpl getTransformFieldImpl(String fieldName)
    {
        failIfFrozen();

        TransformFieldImpl result = fields.get(fieldName);

        if (result != null)
            return result;

        throw new RuntimeException(String.format("Class %s does not contain a field named '%s'.", getClassName(),
                fieldName));
    }

    public String newMemberName(String suggested)
    {
        failIfFrozen();

        assert InternalUtils.isNonBlank(suggested);

        String memberName = InternalUtils.createMemberName(suggested);

        return idAllocator.allocateId(memberName);
    }

    public String newMemberName(String prefix, String baseName)
    {
        return newMemberName(prefix + "_" + InternalUtils.stripMemberName(baseName));
    }

    public void addImplementedInterface(Class interfaceClass)
    {
        failIfFrozen();

        try
        {
            CtClass ctInterface = toCtClass(interfaceClass);

            if (classImplementsInterface(ctInterface))
                return;

            implementDefaultMethodsForInterface(ctInterface);

            ctClass.addInterface(ctInterface);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Adds default implementations for the methods defined by the interface (and all of its
     * super-interfaces). The
     * implementations return null (or 0, or false, as appropriate to to the method type). There are
     * a number of
     * degenerate cases that are not covered properly: these are related to base interfaces that may
     * be implemented by
     * base classes.
     *
     * @param ctInterface
     * @throws NotFoundException
     */
    private void implementDefaultMethodsForInterface(CtClass ctInterface) throws NotFoundException
    {
        // java.lang.Object is the parent interface of interfaces

        if (ctInterface.getName().equals(Object.class.getName()))
            return;

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

        if (!Modifier.isAbstract(method.getModifiers()))
            return;

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

            TransformMethod tm = recordMethod(newMethod, true);

            addMethodToDescription("add default", tm.getSignature(), "<default>");
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ServicesMessages.errorAddingMethod(ctClass, method.getName(), ex), ex);
        }
    }

    private TransformMethodImpl recordMethod(CtMethod method, boolean asNew)
    {
        TransformMethodImpl tmi = new TransformMethodImpl(method, asNew);

        methods.put(tmi.getSignature(), tmi);

        return tmi;
    }

    /**
     * Check to see if the target class (or any of its super classes) implements the provided
     * interface. This is geared
     * for simple interfaces (that don't extend other interfaces), thus if the class (or a base
     * class) implement
     * interface Y that extends interface X, we may not return true for interface X.
     */

    private boolean classImplementsInterface(CtClass ctInterface) throws NotFoundException
    {

        for (CtClass current = ctClass; current != null; current = current.getSuperclass())
        {
            for (CtClass anInterface : current.getInterfaces())
            {
                if (anInterface == ctInterface)
                    return true;
            }
        }

        return false;
    }

    public void claimField(String fieldName, Object tag)
    {
        getField(fieldName).claim(tag);
    }

    public void addMethod(TransformMethodSignature signature, String methodBody)
    {
        removed("addMethod(TransformMethodSignature,String)");
    }

    public TransformMethod addNewMethod(TransformMethodSignature signature, String methodBody)
    {
        return addOrReplaceMethod(signature, methodBody, true);
    }

    public TransformMethod addNewTransformedMethod(TransformMethodSignature signature, String methodBody)
    {
        return addOrReplaceMethod(signature, methodBody, false);
    }

    /**
     * @param signature
     *            of method to add
     * @param methodBody
     *            or null for default
     * @param addAsNew
     *            if true, then fields in the method will not be transformed
     */
    private TransformMethodImpl addOrReplaceMethod(TransformMethodSignature signature, String methodBody,
            boolean addAsNew)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(signature.getReturnType());
        CtClass[] parameters = buildCtClassList(signature.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(signature.getExceptionTypes());

        String suffix = addAsNew ? "" : " transformed";

        String action = "add" + suffix;

        TransformMethodImpl result = null;

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
            String sig = signature.getSignature();
            if (sig != null)
            {
                final MethodInfo methodInfo = method.getMethodInfo();
                methodInfo.addAttribute(new SignatureAttribute(methodInfo.getConstPool(), sig));
            }
            ctClass.addMethod(method);

            result = recordMethod(method, addAsNew);
        }
        catch (CannotCompileException ex)
        {
            throw new MethodCompileException(ServicesMessages.methodCompileError(signature, methodBody, ex),
                    methodBody, ex);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        addMethodToDescription(action, signature, methodBody);

        return result;
    }

    public void addTransformedMethod(TransformMethodSignature signature, String methodBody)
    {
        removed("addTransformedMethod(TransformMethodSignature,String)");
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
        removed("extendMethod(TransformMethodSignature, String)");
    }

    public void extendExistingMethod(TransformMethodSignature methodSignature, String methodBody)
    {
        removed("extendExistingMethod(TransformMethodSignature, String)");
    }

    public void copyMethod(TransformMethodSignature sourceMethod, int modifiers, String newMethodName)
    {
        failIfFrozen();

        CtClass returnType = findCtClass(sourceMethod.getReturnType());
        CtClass[] parameters = buildCtClassList(sourceMethod.getParameterTypes());
        CtClass[] exceptions = buildCtClassList(sourceMethod.getExceptionTypes());

        TransformMethodImpl tmi = locateExistingOrCreateOverrideMethod(sourceMethod);

        CtMethod source = tmi.method;

        try
        {
            CtMethod method = new CtMethod(returnType, newMethodName, parameters, ctClass);

            method.setModifiers(modifiers);

            method.setExceptionTypes(exceptions);

            method.setBody(source, null);

            ctClass.addMethod(method);

            recordMethod(method, false);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(String.format("Error copying method %s to new method %s().", sourceMethod,
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
        removed("addCatch(TransformMethodSignature, String, String)");
    }

    public void prefixMethod(TransformMethodSignature methodSignature, String methodBody)
    {
        removed("prefixMethod(TransformMethodSignature, String)");
    }

    private void addMethodToDescription(String operation, TransformMethodSignature methodSignature, String methodBody)
    {
        formatter.format("%s method: %s %s %s(", operation, Modifier.toString(methodSignature.getModifiers()),
                methodSignature.getReturnType(), methodSignature.getMethodName());

        String[] parameterTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (i > 0)
                description.append(", ");

            formatter.format("%s $%d", parameterTypes[i], i + 1);
        }

        description.append(")");

        String[] exceptionTypes = methodSignature.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++)
        {
            if (i == 0)
                description.append("\n  throws ");
            else
                description.append(", ");

            description.append(exceptionTypes[i]);
        }

        if (methodBody != null)
            formatter.format("\n%s", methodBody);

        description.append("\n\n");
    }

    public TransformMethod getOrCreateMethod(TransformMethodSignature signature)
    {
        failIfFrozen();

        return findOverrideOrCreateMethod(signature);
    }

    private TransformMethodImpl findOverrideOrCreateMethod(TransformMethodSignature signature)
    {
        TransformMethodImpl result = findOrOverrideMethod(signature);

        if (result != null)
            return result;

        return addOrReplaceMethod(signature, null, true);
    }

    private TransformMethodImpl findOrOverrideMethod(TransformMethodSignature signature)
    {
        TransformMethodImpl result = methods.get(signature);

        if (result != null)
            return result;

        return addOverrideOfSuperclassMethod(signature);
    }

    private TransformMethodImpl locateExistingOrCreateOverrideMethod(TransformMethodSignature signature)
    {
        TransformMethodImpl result = findOrOverrideMethod(signature);

        if (result != null)
            return result;

        throw new IllegalArgumentException(String.format("Class %s does not declare method '%s'.", getClassName(),
                signature));

    }

    // TODO: Rework this method for efficiency, i.e., so that we can leverage the methods
    // map in parent InternalClassTransformImpls, rather than the exhaustive
    // search.
    private TransformMethodImpl addOverrideOfSuperclassMethod(TransformMethodSignature methodSignature)
    {
        try
        {
            for (CtClass current = ctClass; current != null; current = current.getSuperclass())
            {
                for (CtMethod method : current.getDeclaredMethods())
                {
                    if (match(method, methodSignature))
                    {
                        // TODO: What if the method is not overridable (i.e. private, or final)?
                        // Perhaps we should limit it to just public methods.

                        CtMethod newMethod = CtNewMethod.delegator(method, ctClass);
                        ctClass.addMethod(newMethod);

                        // Record it as a new method.
                        return recordMethod(newMethod, true);
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
        if (!sig.getMethodName().equals(method.getName()))
            return false;

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

        if (paramTypes.length != count)
            return false;

        for (int i = 0; i < count; i++)
        {
            String paramType = paramTypes[i].getName();

            if (!paramType.equals(sigTypes[i]))
                return false;
        }

        // Ignore exceptions thrown and modifiers.
        // TODO: Validate a match on return type?

        return true;
    }

    public List<String> findFieldsWithAnnotation(final Class<? extends Annotation> annotationClass)
    {
        return toFieldNames(matchFieldsWithAnnotation(annotationClass));
    }

    public List<String> findFields(final FieldFilter filter)
    {
        assert filter != null;

        failIfFrozen();

        List<TransformField> fields = matchFields(new Predicate<TransformField>()
        {
            public boolean accept(TransformField object)
            {
                return filter.accept(object.getName(), object.getType());
            }
        });

        return toFieldNames(fields);
    }

    public List<TransformField> matchFields(Predicate<TransformField> predicate)
    {
        failIfFrozen();

        return InternalUtils.matchAndSort(fields.values(), predicate);
    }

    public List<TransformField> matchFieldsWithAnnotation(final Class<? extends Annotation> annotationClass)
    {
        return matchFields(new Predicate<TransformField>()
        {
            public boolean accept(TransformField field)
            {
                return field.getAnnotation(annotationClass) != null;
            }
        });
    }

    public List<TransformMethodSignature> findMethodsWithAnnotation(final Class<? extends Annotation> annotationClass)
    {
        List<TransformMethod> methods = matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return method.getAnnotation(annotationClass) != null;
            };
        });

        return toMethodSignatures(methods);
    }

    public List<TransformMethodSignature> findMethods(final MethodFilter filter)
    {
        assert filter != null;

        List<TransformMethod> methods = matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod object)
            {
                return filter.accept(object.getSignature());
            };
        });

        return toMethodSignatures(methods);
    }

    public List<TransformMethod> matchMethods(Predicate<TransformMethod> predicate)
    {
        failIfFrozen();

        return InternalUtils.matchAndSort(methods.values(), predicate);
    }

    public List<TransformMethod> matchMethodsWithAnnotation(final Class<? extends Annotation> annotationType)
    {
        return matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return method.getAnnotation(annotationType) != null;
            }
        });
    }

    private TransformMethodSignature toMethodSignature(CtMethod method)
    {
        try
        {
            String type = method.getReturnType().getName();
            String[] parameters = toTypeNames(method.getParameterTypes());
            String[] exceptions = toTypeNames(method.getExceptionTypes());

            final SignatureAttribute attribute = (SignatureAttribute)method.getMethodInfo().getAttribute(SignatureAttribute.tag);
            String sig = attribute != null ? attribute.getSignature() : null;

            return new TransformMethodSignature(method.getModifiers(), type, sig, method.getName(), parameters, exceptions);
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
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
        return toFieldNames(matchUnclaimedFields());
    }

    public List<TransformField> matchUnclaimedFields()
    {
        return matchFields(new Predicate<TransformField>()
        {
            public boolean accept(TransformField object)
            {
                TransformFieldImpl tmi = (TransformFieldImpl) object;

                return !(tmi.added || tmi.isClaimed());
            }
        });
    }

    public String getFieldType(String fieldName)
    {
        return getField(fieldName).getType();
    }

    public boolean isField(String fieldName)
    {
        failIfFrozen();

        // Only declared instance fields end up in this map, and all
        // fields are either static or private.

        return fields.containsKey(fieldName);
    }

    public int getFieldModifiers(String fieldName)
    {
        return getField(fieldName).getModifiers();
    }

    public String addField(int modifiers, String type, String suggestedName)
    {
        return createField(modifiers, type, suggestedName).getName();
    }

    public TransformField createField(int modifiers, String type, String suggestedName)
    {
        failIfFrozen();

        String fieldName = newMemberName(suggestedName);

        TransformFieldImpl result = null;

        try
        {
            CtClass ctType = convertNameToCtType(type);

            CtField field = new CtField(ctType, fieldName, ctClass);
            field.setModifiers(modifiers);

            ctClass.addField(field);

            result = new TransformFieldImpl(field, true);

            fields.put(fieldName, result);

        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        formatter.format("add field: %s %s %s;\n\n", Modifier.toString(modifiers), type, fieldName);

        return result;
    }

    // Returns String for backwards compatibility reasons
    public String addInjectedField(Class type, String suggestedName, Object value)
    {
        assert type != null;

        failIfFrozen();

        InjectionKey key = new InjectionKey(type, value);

        String fieldName = searchForPreviousInjection(key);

        if (fieldName != null)
            return fieldName;

        // TODO: Probably doesn't handle arrays and primitives.

        fieldName = addInjectedFieldUncached(type, suggestedName, value);

        // Remember the injection in-case this class, or a subclass, injects the value again.

        injectionCache.put(key, fieldName);

        return fieldName;
    }

    public <T> TransformField addIndirectInjectedField(Class<T> type, String suggestedName,
            ComponentValueProvider<T> provider)
    {
        assert type != null;
        assert provider != null;

        TransformField field = createField(Modifier.PROTECTED | Modifier.FINAL, type.getName(), suggestedName);

        String argReference = addConstructorArg(providerType, provider);

        // Inside the constructor,
        // pass the resources to the provider's get() method, cast to the
        // field type and assign. This will likely not work with
        // primitives and arrays, but that's ok for now.

        addToConstructor(String.format("  %s = (%s) (%s).get(%s);", field.getName(), type.getName(), argReference,
                resourcesFieldName));

        return field;
    }

    private CtClass toCtClass(Class type)
    {
        try
        {
            return classPool.get(type.getName());
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This is split out from {@link #addInjectedField(Class, String, Object)} to handle a special
     * case for the
     * InternalComponentResources, which is null when "injected" (during the class transformation)
     * and is only
     * determined when a component is actually instantiated.
     */
    private String addInjectedFieldUncached(Class type, String suggestedName, Object value)
    {
        CtClass ctType = toCtClass(type);

        String fieldName = addField(Modifier.PROTECTED | Modifier.FINAL, type.getName(), suggestedName);

        addInjectToConstructor(fieldName, ctType, value);

        return fieldName;
    }

    public String searchForPreviousInjection(InjectionKey key)
    {
        String result = injectionCache.get(key);

        if (result != null)
            return result;

        if (parentTransformation != null)
            return parentTransformation.searchForPreviousInjection(key);

        return null;
    }

    public void advise(TransformMethodSignature methodSignature, ComponentMethodAdvice advice)
    {
        getOrCreateMethod(methodSignature).addAdvice(advice);
    }

    public boolean isMethodOverride(TransformMethodSignature methodSignature)
    {
        if (!isMethod(methodSignature))
            throw new IllegalArgumentException(String.format("Method %s is not implemented by transformed class %s.",
                    methodSignature, getClassName()));

        return getOrCreateMethod(methodSignature).isOverride();
    }

    public InternalClassTransformation getParentTransformation()
    {
        return parentTransformation;
    }

    public boolean isMethod(TransformMethodSignature signature)
    {
        assert signature != null;

        return methods.containsKey(signature);
    }

    /**
     * Adds a parameter to the constructor for the class; the parameter is used to initialize the
     * value for a field.
     *
     * @param fieldName
     *            name of field to inject
     * @param fieldType
     *            Javassist type of the field (and corresponding parameter)
     * @param value
     *            the value to be injected (which will in unusual cases be null)
     */
    private void addInjectToConstructor(String fieldName, CtClass fieldType, Object value)
    {
        addToConstructor(String.format("  %s = %s;", fieldName, addConstructorArg(fieldType, value)));
    }

    public void injectField(String fieldName, Object value)
    {
        getField(fieldName).inject(value);
    }

    private CtClass convertNameToCtType(String type) throws NotFoundException
    {
        return classPool.get(type);
    }

    public String finish()
    {
        failIfFrozen();

        // Finishing fields may sometimes create new methods, so finish the fields
        // first.

        for (TransformFieldImpl tfi : fields.values())
        {
            tfi.doFinish();
        }

        // doFinish() will sometimes create new methods on the ClassTransformation, yielding
        // a concurrent modification exception, so do a defensive copy.

        List<TransformMethodImpl> tmis = CollectionFactory.newList(methods.values());

        for (TransformMethodImpl tmi : tmis)
        {
            tmi.doFinish();
        }

        String initializer = convertConstructorToMethod();

        performFieldTransformations();

        addConstructor(initializer);

        String description = toString();

        freeze();

        return description;
    }

    private void addConstructor(String initializer)
    {
        // Add a call to the initializer; the method converted from the class' default
        // constructor.

        constructor.append("  ");
        constructor.append(initializer);

        // This finally matches the "{" added inside the constructor

        constructor.append("();\n\n}");

        String constructorBody = constructor.toString();

        try
        {
            CtClass[] types = new CtClass[]
            { toCtClass(InternalComponentResources.class), toCtClass(Object[].class) };

            CtConstructor cons = CtNewConstructor.make(types, null, constructorBody, ctClass);

            ctClass.addConstructor(cons);
        }
        catch (CannotCompileException ex)
        {
            throw new RuntimeException(ex);
        }

        formatter.format("add constructor: %s(ComponentResources, Object[])\n%s\n\n", getClassName(), constructorBody);
    }

    private String convertConstructorToMethod()
    {
        String initializer = idAllocator.allocateId("initializer");

        try
        {
            CtConstructor defaultConstructor = ctClass.getConstructor("()V");

            CtMethod initializerMethod = defaultConstructor.toMethod(initializer, ctClass);

            ctClass.addMethod(initializerMethod);

            recordMethod(initializerMethod, false);

            // Replace the constructor body with one that fails. This leaves, as an open question,
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
            return createAbstractClassInstantiator();

        String componentClassName = getClassName();

        String name = ClassFabUtils.generateClassName("Instantiator");

        ClassFab cf = classFactory.newClass(name, AbstractInstantiator.class);

        Object[] componentConstructorArgs = constructorArgs.toArray(new Object[constructorArgs.size()]);

        cf.addConstructor(new Class[]
        { ComponentModel.class, String.class, Object[].class }, null, "super($1, $2, $3);");

        // Pass $1 (the InternalComponentResources object) and the constructorArgs (from the AbstractIntantiator
        // base class) into the new component instance's constructor

        cf.addMethod(Modifier.PUBLIC, NEW_INSTANCE_SIGNATURE,
                String.format("return new %s($1, constructorArgs);", componentClassName));

        Class instantiatorClass = cf.createClass();

        try
        {
            Object instance = instantiatorClass.getConstructors()[0].newInstance(componentModel,
                    String.format("Instantiator[%s]", componentClassName), componentConstructorArgs);

            return (Instantiator) instance;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Instantiator createAbstractClassInstantiator()
    {
        return new Instantiator()
        {
            public Component newInstance(InternalComponentResources resources)
            {
                throw new RuntimeException(String.format("Component class %s is abstract and can not be instantiated.",
                        ctClass.getName()));
            }

            public ComponentModel getModel()
            {
                return componentModel;
            }
        };
    }

    private void failIfFrozen()
    {
        if (frozen)
            throw new IllegalStateException("The ClassTransformation instance (for " + getClassName()
                    + ") has completed all transformations and may not be further modified.");
    }

    private void failIfNotFrozen()
    {
        if (!frozen)
            throw new IllegalStateException("The ClassTransformation instance (for " + getClassName()
                    + ") has not yet completed all transformations.");
    }

    public IdAllocator getIdAllocator()
    {
        failIfNotFrozen();

        return idAllocator;
    }

    public List<Object> getConstructorArgs()
    {
        failIfNotFrozen();

        return CollectionFactory.newList(constructorArgs);
    }

    public List<Annotation> getClassAnnotations()
    {
        failIfFrozen();

        if (classAnnotations == null)
            assembleClassAnnotations();

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

    public String toString()
    {
        StringBuilder builder = new StringBuilder("InternalClassTransformation[\n");

        try
        {
            Formatter formatter = new Formatter(builder);

            formatter.format("%s %s extends %s", Modifier.toString(ctClass.getModifiers()), ctClass.getName(), ctClass
                    .getSuperclass().getName());

            CtClass[] interfaces = ctClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++)
            {
                if (i == 0)
                    builder.append("\n  implements ");
                else
                    builder.append(", ");

                builder.append(interfaces[i].getName());
            }

            if (description != null)
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

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, "void", methodName, new String[]
        { fieldType }, null);

        String message = ServicesMessages.readOnlyField(getClassName(), fieldName);

        String body = String.format("throw new java.lang.RuntimeException(\"%s\");", message);

        addNewMethod(sig, body);

        replaceWriteAccess(fieldName, methodName);
    }

    public void removeField(String fieldName)
    {
        removed("removeField(String)");
    }

    public void replaceReadAccess(String fieldName, String methodName)
    {
        getTransformFieldImpl(fieldName).replaceReadAccess(methodName);
    }

    public void replaceWriteAccess(String fieldName, String methodName)
    {
        getTransformFieldImpl(fieldName).replaceWriteAccess(methodName);
    }

    private void performFieldTransformations()
    {
        // If no field transformations have been requested, then we can save ourselves some
        // trouble!

        if (fieldAccessReplaced)
            replaceFieldAccess();
    }

    static final int SYNTHETIC = 0x00001000;

    private void replaceFieldAccess()
    {
        final Map<String, String> fieldReadTransforms = CollectionFactory.newMap();
        final Map<String, String> fieldWriteTransforms = CollectionFactory.newMap();

        for (TransformFieldImpl tfi : fields.values())
        {
            putIfNotNull(fieldReadTransforms, tfi.name, tfi.readValueBody);
            putIfNotNull(fieldWriteTransforms, tfi.name, tfi.writeValueBody);
        }

        ExprEditor editor = new ExprEditor()
        {
            private final Set<CtBehavior> addedMethods = CollectionFactory.newSet();

            {
                for (TransformMethodImpl tmi : methods.values())
                {
                    if (tmi.added)
                        addedMethods.add(tmi.method);
                }
            }

            public void edit(FieldAccess access) throws CannotCompileException
            {
                CtBehavior where = access.where();

                if (where instanceof CtConstructor)
                    return;

                // It may be access to a public field of a data object somewhere and
                // that's ignored TAP5-1222

                if (!access.getClassName().equals(getClassName()))
                    return;

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

    private static <K, V> void putIfNotNull(Map<K, V> map, K key, V value)
    {
        if (value != null)
            map.put(key, value);
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
        removed("extendConstructor(String)");
    }

    void addToConstructor(String statement)
    {
        constructor.append(statement);
        constructor.append("\n");
    }

    public String getMethodIdentifier(TransformMethodSignature signature)
    {
        return getOrCreateMethod(signature).getMethodIdentifier();
    }

    public boolean isRootTransformation()
    {
        return parentTransformation == null;
    }

    /**
     * Adds a new constructor argument to the transformed constructor.
     *
     * @param parameterType
     *            type of parameter
     * @param value
     *            value of parameter
     * @return de-referenced argument value
     */
    private String addConstructorArg(CtClass parameterType, Object value)
    {

        int index = constructorArgs.size();

        constructorArgs.add(value);

        return ClassFabUtils.castReference(String.format("$2[%d]", index), parameterType.getName());
    }

    private static List<TransformMethodSignature> toMethodSignatures(List<TransformMethod> input)
    {
        List<TransformMethodSignature> result = CollectionFactory.newList();

        for (TransformMethod m : input)
        {
            result.add(m.getSignature());
        }

        return result;
    }

    private static List<String> toFieldNames(List<TransformField> fields)
    {
        List<String> result = CollectionFactory.newList();

        for (TransformField f : fields)
        {
            result.add(f.getName());
        }

        return result;
    }

    private ComponentMethodInvocationBuilder createBuilder(TransformMethodSignature signature)
    {
        return new ComponentMethodInvocationBuilder(this, componentClassCache, signature, classSource);
    }

    public boolean isDeclaredMethod(TransformMethodSignature signature)
    {
        failIfFrozen();

        assert signature != null;

        return methods.containsKey(signature);
    }

    private void removed(String methodName)
    {
        throw new RuntimeException(String.format(
                "Method ClassTransformation.%s has been deprecated and is no longer functional. "
                        + "Please consult the JavaDoc for a suitable replacement.", methodName));
    }

    private static ComponentMethodAdvice toBeforeAdvice(final ComponentInstanceOperation operation)
    {
        return new ComponentMethodAdvice()
        {

            public void advise(ComponentMethodInvocation invocation)
            {
                operation.invoke(invocation.getInstance());

                invocation.proceed();
            }
        };
    }

    private static ComponentMethodAdvice toAfterAdvice(final ComponentInstanceOperation operation)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                operation.invoke(invocation.getInstance());
            }
        };
    }

    public void addComponentEventHandler(String eventType, int minContextValues, String methodDescription,
            ComponentEventHandler handler)
    {
        assert InternalUtils.isNonBlank(eventType);
        assert InternalUtils.isNonBlank(methodDescription);
        assert handler != null;

        componentModel.addEventHandler(eventType);

        getOrCreateMethod(TransformConstants.DISPATCH_COMPONENT_EVENT).addAdvice(
                createEventHandlerAdvice(eventType, minContextValues, methodDescription, handler));

    }

    private static ComponentMethodAdvice createEventHandlerAdvice(final String eventType, final int minContextValues,
            final String methodDescription, final ComponentEventHandler handler)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                // Invoke the super-class implementation first.

                invocation.proceed();

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (!event.isAborted() && event.matches(eventType, "", minContextValues))
                {
                    event.setMethodDescription(methodDescription);

                    handler.handleEvent(invocation.getInstance(), event);

                    // Ensure that the caller knows that some event handler method
                    // was invoked.
                    invocation.overrideResult(true);
                }
            }
        };
    }

    private static org.apache.tapestry5.services.FieldAccess toFieldAccess(final FieldValueConduit conduit)
    {
        return new org.apache.tapestry5.services.FieldAccess()
        {

            public void write(Object instance, Object value)
            {
                conduit.set(value);
            }

            public Object read(Object instance)
            {
                return conduit.get();
            }
        };
    }
}
