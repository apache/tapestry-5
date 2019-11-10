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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.internal.plastic.asm.AnnotationVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.internal.plastic.asm.tree.*;
import org.apache.tapestry5.plastic.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("all")
public class PlasticClassImpl extends Lockable implements PlasticClass, InternalPlasticClassTransformation, Opcodes
{
    private static final String NOTHING_TO_VOID = "()V";

    static final String CONSTRUCTOR_NAME = "<init>";

    private static final String OBJECT_INT_TO_OBJECT = "(Ljava/lang/Object;I)Ljava/lang/Object;";

    private static final String OBJECT_INT_OBJECT_TO_VOID = "(Ljava/lang/Object;ILjava/lang/Object;)V";

    private static final String OBJECT_INT_OBJECT_ARRAY_TO_METHOD_INVOCATION_RESULT = String.format(
            "(Ljava/lang/Object;I[Ljava/lang/Object;)%s", toDesc(Type.getInternalName(MethodInvocationResult.class)));

    static final String ABSTRACT_METHOD_INVOCATION_INTERNAL_NAME = PlasticInternalUtils
            .toInternalName(AbstractMethodInvocation.class.getName());

    private static final String HANDLE_SHIM_BASE_CLASS_INTERNAL_NAME = Type
            .getInternalName(PlasticClassHandleShim.class);

    static final String STATIC_CONTEXT_INTERNAL_NAME = Type.getInternalName(StaticContext.class);

    private static final String INSTANCE_CONTEXT_INTERNAL_NAME = Type.getInternalName(InstanceContext.class);

    private static final String INSTANCE_CONTEXT_DESC = toDesc(INSTANCE_CONTEXT_INTERNAL_NAME);

    private static final String CONSTRUCTOR_DESC = String.format("(L%s;L%s;)V", STATIC_CONTEXT_INTERNAL_NAME,
            INSTANCE_CONTEXT_INTERNAL_NAME);

    static final Method STATIC_CONTEXT_GET_METHOD = toMethod(StaticContext.class, "get", int.class);

    static final Method COMPUTED_VALUE_GET_METHOD = toMethod(ComputedValue.class, "get", InstanceContext.class);

    private static final Method CONSTRUCTOR_CALLBACK_METHOD = toMethod(ConstructorCallback.class, "onConstruct",
            Object.class, InstanceContext.class);

    private static String toDesc(String internalName)
    {
        return "L" + internalName + ";";
    }

    private static Method toMethod(Class declaringClass, String methodName, Class... parameterTypes)
    {
        return PlasticUtils.getMethod(declaringClass, methodName, parameterTypes);
    }

    static <T> T safeArrayDeref(T[] array, int index)
    {
        if (array == null)
            return null;

        return array[index];
    }

    // Now past the inner classes; these are the instance variables of PlasticClassImpl proper:

    final ClassNode classNode;

    final PlasticClassPool pool;

    private final boolean proxy;

    final String className;

    private final String superClassName;

    private final AnnotationAccess annotationAccess;

    // All the non-introduced (and non-constructor) methods, in sorted order

    private final List<PlasticMethodImpl> methods;

    private final Map<MethodDescription, PlasticMethod> description2method = new HashMap<>();

    final Set<String> methodNames = new HashSet<>();

    private final List<ConstructorCallback> constructorCallbacks = PlasticInternalUtils.newList();

    // All non-introduced instance fields

    private final List<PlasticFieldImpl> fields;

    /**
     * Methods that require special attention inside {@link #createInstantiator()} because they
     * have method advice.
     */
    final Set<PlasticMethodImpl> advisedMethods = PlasticInternalUtils.newSet();

    final NameCache nameCache = new NameCache();

    // This is generated from fields, as necessary
    List<PlasticField> unclaimedFields;

    private final Set<String> fieldNames = PlasticInternalUtils.newSet();

    final StaticContext staticContext;

    final InheritanceData parentInheritanceData, inheritanceData;

    // MethodNodes in which field transformations should occur; this is most existing and
    // introduced methods, outside of special access methods.

    final Set<MethodNode> fieldTransformMethods = PlasticInternalUtils.newSet();

    // Tracks any methods that the Shim class uses to gain access to fields; used to ensure that
    // such methods are not optimized away incorrectly.
    final Set<MethodNode> shimInvokedMethods = PlasticInternalUtils.newSet();


    /**
     * Tracks instrumentations of fields of this class, including private fields which are not published into the
     * {@link PlasticClassPool}.
     */
    private final FieldInstrumentations fieldInstrumentations;

    /**
     * This normal no-arguments constructor, or null. By the end of the transformation
     * this will be converted into an ordinary method.
     */
    private MethodNode originalConstructor;

    private final MethodNode newConstructor;

    final InstructionBuilder constructorBuilder;

    private String instanceContextFieldName;

    private Class<?> transformedClass;

    // Indexes used to identify fields or methods in the shim
    int nextFieldIndex = 0;

    int nextMethodIndex = 0;

    // Set of fields that need to contribute to the shim and gain access to it

    final Set<PlasticFieldImpl> shimFields = PlasticInternalUtils.newSet();

    // Set of methods that need to contribute to the shim and gain access to it

    final Set<PlasticMethodImpl> shimMethods = PlasticInternalUtils.newSet();

    final ClassNode implementationClassNode;

    private ClassNode interfaceClassNode;

    /**
     * @param classNode
     * @param implementationClassNode
     * @param pool
     * @param parentInheritanceData
     * @param parentStaticContext
     * @param proxy
     */
    public PlasticClassImpl(ClassNode classNode, ClassNode implementationClassNode, PlasticClassPool pool, InheritanceData parentInheritanceData,
                            StaticContext parentStaticContext, boolean proxy)
    {
        this.classNode = classNode;
        this.pool = pool;
        this.proxy = proxy;
        this.implementationClassNode = implementationClassNode;

        staticContext = parentStaticContext.dupe();

        className = PlasticInternalUtils.toClassName(classNode.name);
        superClassName = PlasticInternalUtils.toClassName(classNode.superName);
        int lastIndexOfDot = className.lastIndexOf('.');

        String packageName = lastIndexOfDot > -1 ? className.substring(0, lastIndexOfDot) : "";

        fieldInstrumentations = new FieldInstrumentations(classNode.superName);

        annotationAccess = new DelegatingAnnotationAccess(pool.createAnnotationAccess(classNode.visibleAnnotations),
                pool.createAnnotationAccess(superClassName));

        this.parentInheritanceData = parentInheritanceData;

        inheritanceData = parentInheritanceData.createChild(packageName);

        for (String interfaceName : classNode.interfaces)
        {
            inheritanceData.addInterface(interfaceName);
        }

        methods = new ArrayList<>(classNode.methods.size());

        String invalidConstructorMessage = invalidConstructorMessage();

        for (MethodNode node : classNode.methods)
        {
            if (node.name.equals(CONSTRUCTOR_NAME))
            {
                if (node.desc.equals(NOTHING_TO_VOID))
                {
                    originalConstructor = node;
                    fieldTransformMethods.add(node);
                } else
                {
                    node.instructions.clear();

                    newBuilder(node).throwException(IllegalStateException.class, invalidConstructorMessage);
                }

                continue;
            }

            /*
             * Static methods are not visible to the main API methods, but they must still be transformed,
             * in case they directly access fields. In addition, track their names to avoid collisions.
             */
            if (Modifier.isStatic(node.access))
            {
                if (isInheritableMethod(node))
                {
                    inheritanceData.addMethod(node.name, node.desc, node.access == 0);
                }

                methodNames.add(node.name);

                fieldTransformMethods.add(node);

                continue;
            }

            if (!Modifier.isAbstract(node.access))
            {
                fieldTransformMethods.add(node);
            }

            PlasticMethodImpl pmi = new PlasticMethodImpl(this, node);

            methods.add(pmi);
            description2method.put(pmi.getDescription(), pmi);

            if (isInheritableMethod(node))
            {
                inheritanceData.addMethod(node.name, node.desc, node.access == 0);
            }

            methodNames.add(node.name);
        }

        methodNames.addAll(parentInheritanceData.methodNames());

        Collections.sort(methods);

        fields = new ArrayList<>(classNode.fields.size());

        for (FieldNode node : classNode.fields)
        {
            fieldNames.add(node.name);

            // Ignore static fields.

            if (Modifier.isStatic(node.access))
                continue;

            // When we instrument the field such that it must be private, we'll get an exception.

            fields.add(new PlasticFieldImpl(this, node));
        }

        Collections.sort(fields);

        // TODO: Make the output class's constructor protected, and create a shim class to instantiate it
        // efficiently (without reflection).
        newConstructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, null, null);
        constructorBuilder = newBuilder(newConstructor);

        // Start by calling the super-class no args constructor

        if (parentInheritanceData.isTransformed())
        {
            // If the parent is transformed, our first step is always to invoke its constructor.

            constructorBuilder.loadThis().loadArgument(0).loadArgument(1);
            constructorBuilder.invokeConstructor(superClassName, StaticContext.class.getName(),
                    InstanceContext.class.getName());
        } else
        {
            // Assumes the base class includes a visible constructor that takes no arguments.
            // TODO: Do a proper check for this case and throw a meaningful exception
            // if not present.

            constructorBuilder.loadThis().invokeConstructor(superClassName);
        }

        // During the transformation, we'll be adding code to the constructor to pull values
        // out of the static or instance context and assign them to fields.

        // Later on, we'll add the RETURN opcode
    }

    private String invalidConstructorMessage()
    {
        return String.format("Class %s has been transformed and may not be directly instantiated.", className);
    }

    @Override
    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
    {
        check();

        return annotationAccess.hasAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        check();

        return annotationAccess.getAnnotation(annotationType);
    }

    private static void addMethodAndParameterAnnotationsFromExistingClass(MethodNode methodNode, MethodNode implementationMethodNode)
    {
        // visits the method attributes
        int i, j, n;
        if (implementationMethodNode.annotationDefault != null)
        {
            AnnotationVisitor av = methodNode.visitAnnotationDefault();
            AnnotationNode.accept(av, null, implementationMethodNode.annotationDefault);
            if (av != null)
            {
                av.visitEnd();
            }
        }
        n = implementationMethodNode.visibleAnnotations == null ? 0 : implementationMethodNode.visibleAnnotations.size();
        for (i = 0; i < n; ++i)
        {
            AnnotationNode an = implementationMethodNode.visibleAnnotations.get(i);
            an.accept(methodNode.visitAnnotation(an.desc, true));
        }
        n = implementationMethodNode.invisibleAnnotations == null ? 0 : implementationMethodNode.invisibleAnnotations.size();
        for (i = 0; i < n; ++i)
        {
            AnnotationNode an = implementationMethodNode.invisibleAnnotations.get(i);
            an.accept(methodNode.visitAnnotation(an.desc, false));
        }
        n = implementationMethodNode.visibleParameterAnnotations == null
                ? 0
                : implementationMethodNode.visibleParameterAnnotations.length;
        for (i = 0; i < n; ++i)
        {
            List<?> l = implementationMethodNode.visibleParameterAnnotations[i];
            if (l == null)
            {
                continue;
            }
            for (j = 0; j < l.size(); ++j)
            {
                AnnotationNode an = (AnnotationNode) l.get(j);
                an.accept(methodNode.visitParameterAnnotation(i, an.desc, true));
            }
        }
        n = implementationMethodNode.invisibleParameterAnnotations == null
                ? 0
                : implementationMethodNode.invisibleParameterAnnotations.length;
        for (i = 0; i < n; ++i)
        {
            List<?> l = implementationMethodNode.invisibleParameterAnnotations[i];
            if (l == null)
            {
                continue;
            }
            for (j = 0; j < l.size(); ++j)
            {
                AnnotationNode an = (AnnotationNode) l.get(j);
                an.accept(methodNode.visitParameterAnnotation(i, an.desc, false));
            }
        }

        methodNode.visitEnd();

    }

    private static void removeDuplicatedAnnotations(MethodNode node)
    {

        removeDuplicatedAnnotations(node.visibleAnnotations);
        removeDuplicatedAnnotations(node.invisibleAnnotations);

        if (node.visibleParameterAnnotations != null)
        {
            for (List<AnnotationNode> list : node.visibleParameterAnnotations)
            {
                removeDuplicatedAnnotations(list);
            }
        }

        if (node.invisibleParameterAnnotations != null)
        {
            for (List<AnnotationNode> list : node.invisibleParameterAnnotations)
            {
                removeDuplicatedAnnotations(list);
            }
        }

    }

    private static void removeDuplicatedAnnotations(ClassNode node)
    {
        removeDuplicatedAnnotations(node.visibleAnnotations, true);
        removeDuplicatedAnnotations(node.invisibleAnnotations, true);
    }

    private static void removeDuplicatedAnnotations(List<AnnotationNode> list) {
        removeDuplicatedAnnotations(list, false);
    }

    private static void removeDuplicatedAnnotations(List<AnnotationNode> list, boolean reverse) {

        if (list != null)
        {

            final Set<String> annotations = new HashSet<>();
            final List<AnnotationNode> toBeRemoved = new ArrayList<>();
            final List<AnnotationNode> toBeIterated;

            if (reverse)
            {
                toBeIterated = new ArrayList<>(list);
                Collections.reverse(toBeIterated);
            }
            else {
                toBeIterated = list;
            }

            for (AnnotationNode annotationNode : toBeIterated)
            {
                if (annotations.contains(annotationNode.desc))
                {
                    toBeRemoved.add(annotationNode);
                }
                else
                {
                    annotations.add(annotationNode.desc);
                }
            }

            for (AnnotationNode annotationNode : toBeRemoved)
            {
                list.remove(annotationNode);
            }

        }

    }

    private static String getParametersDesc(MethodNode methodNode) {
        return methodNode.desc.substring(methodNode.desc.indexOf('(') + 1, methodNode.desc.lastIndexOf(')'));
    }

    private static MethodNode findExactMatchMethod(MethodNode methodNode, ClassNode source) {

        MethodNode found = null;

        final String methodDescription = getParametersDesc(methodNode);

        for (MethodNode implementationMethodNode : source.methods)
        {

            final String implementationMethodDescription = getParametersDesc(implementationMethodNode);
            if (methodNode.name.equals(implementationMethodNode.name) &&
                    // We don't want synthetic methods.
                    ((implementationMethodNode.access & Opcodes.ACC_SYNTHETIC) == 0)
                    && (methodDescription.equals(implementationMethodDescription)))
            {
                found = implementationMethodNode;
                break;
            }
        }

        return found;

    }

    private static List<Class> getJavaParameterTypes(MethodNode methodNode) {
        final ClassLoader classLoader = PlasticInternalUtils.class.getClassLoader();
        Type[] parameterTypes = Type.getArgumentTypes(methodNode.desc);
        List<Class> list = new ArrayList<>();
        for (Type type : parameterTypes)
        {
            try
            {
                list.add(PlasticInternalUtils.toClass(classLoader, type.getClassName()));
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e); // shouldn't happen anyway
            }
        }
        return list;
    }

    /**
     * Returns the first method which matches the given methodNode.
     * FIXME: this may not find the correct method if the correct one is declared after
     * another in which all parameters are supertypes of the parameters of methodNode.
     * To solve this, we would need to dig way deeper than we have time for this.
     * @param methodNode
     * @param classNode
     * @return
     */
    private static MethodNode findGenericMethod(MethodNode methodNode, ClassNode classNode)
    {

        MethodNode found = null;

        List<Class> parameterTypes = getJavaParameterTypes(methodNode);

        for (MethodNode implementationMethodNode : classNode.methods)
        {

            if (methodNode.name.equals(implementationMethodNode.name))
            {

                final List<Class> implementationParameterTypes = getJavaParameterTypes(implementationMethodNode);

                if (parameterTypes.size() == implementationParameterTypes.size())
                {

                    boolean matches = true;
                    for (int i = 0; i < parameterTypes.size(); i++)
                    {
                        final Class implementationParameterType = implementationParameterTypes.get(i);
                        final Class parameterType = parameterTypes.get(i);
                        if (!parameterType.isAssignableFrom(implementationParameterType)) {
                            matches = false;
                            break;
                        }

                    }

                    if (matches && !isBridge(implementationMethodNode))
                    {
                        found = implementationMethodNode;
                        break;
                    }

                }

            }

        }

        return found;

    }

    private static void addMethodAndParameterAnnotationsFromExistingClass(MethodNode methodNode, ClassNode source)
    {
        if (source != null)
        {

            MethodNode candidate = findExactMatchMethod(methodNode, source);

            final String parametersDesc = getParametersDesc(methodNode);

            // candidate will be null when the method has generic parameters
            if (candidate == null && parametersDesc.trim().length() > 0)
            {
                candidate = findGenericMethod(methodNode, source);
            }

            if (candidate != null)
            {
                addMethodAndParameterAnnotationsFromExistingClass(methodNode, candidate);
            }

        }

    }

    /**
     * Tells whether a given method is a bridge one or not.
     * Notice the flag for bridge method is the same as volatile field. Java 6 doesn't have
     * Modifiers.isBridge(), so we use a workaround.
     */
    private static boolean isBridge(MethodNode methodNode)
    {
        return Modifier.isVolatile(methodNode.access);
    }

    @Override
    public PlasticClass proxyInterface(Class interfaceType, PlasticField field)
    {
        check();

        assert field != null;

        introduceInterface(interfaceType);

        for (Method m : getUniqueMethods(interfaceType))
        {
            final MethodDescription description = new MethodDescription(m);
            if(Modifier.isStatic(description.modifiers))
            {
                continue;
            }
            introduceMethod(description).delegateTo(field);
        }

        return this;
    }

    @Override
    public ClassInstantiator createInstantiator()
    {
        lock();

        addClassAnnotations(implementationClassNode);
        removeDuplicatedAnnotations(classNode);

        createShimIfNeeded();

        interceptFieldAccess();

        rewriteAdvisedMethods();

        completeConstructor();

        transformedClass = pool.realizeTransformedClass(classNode, inheritanceData, staticContext);

        return createInstantiatorFromClass(transformedClass);
    }

    private void addClassAnnotations(ClassNode otherClassNode)
    {
        // Copy annotations from implementation if available.
        // Code adapted from ClassNode.accept(), as we just want to copy
        // the annotations and nothing more.
        if (otherClassNode != null)
        {

            int i, n;
            n = otherClassNode.visibleAnnotations == null ? 0 : otherClassNode.visibleAnnotations.size();
            for (i = 0; i < n; ++i)
            {
                AnnotationNode an = otherClassNode.visibleAnnotations.get(i);
                an.accept(classNode.visitAnnotation(an.desc, true));
            }
            n = otherClassNode.invisibleAnnotations == null ? 0 : otherClassNode.invisibleAnnotations.size();
            for (i = 0; i < n; ++i)
            {
                AnnotationNode an = otherClassNode.invisibleAnnotations.get(i);
                an.accept(classNode.visitAnnotation(an.desc, false));
            }

        }
    }

    private ClassInstantiator createInstantiatorFromClass(Class clazz)
    {
        try
        {
            Constructor ctor = clazz.getConstructor(StaticContext.class, InstanceContext.class);

            return new ClassInstantiatorImpl(clazz, ctor, staticContext);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to create ClassInstantiator for class %s: %s",
                    clazz.getName(), PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    private void completeConstructor()
    {
        if (originalConstructor != null)
        {
            convertOriginalConstructorToMethod();
        }

        invokeCallbacks();

        constructorBuilder.returnResult();

        classNode.methods.add(newConstructor);
    }

    private void invokeCallbacks()
    {
        for (ConstructorCallback callback : constructorCallbacks)
        {
            invokeCallback(callback);
        }
    }

    private void invokeCallback(ConstructorCallback callback)
    {
        int index = staticContext.store(callback);

        // First, load the callback

        constructorBuilder.loadArgument(0).loadConstant(index).invoke(STATIC_CONTEXT_GET_METHOD).castOrUnbox(ConstructorCallback.class.getName());

        // Load this and the InstanceContext
        constructorBuilder.loadThis().loadArgument(1);

        constructorBuilder.invoke(CONSTRUCTOR_CALLBACK_METHOD);
    }


    /**
     * Convert the original constructor into a private method invoked from the
     * generated constructor.
     */
    private void convertOriginalConstructorToMethod()
    {
        String initializerName = makeUnique(methodNames, "initializeInstance");

        int originalAccess = originalConstructor.access;

        originalConstructor.access = ACC_PRIVATE;
        originalConstructor.name = initializerName;

        stripOutSuperConstructorCall(originalConstructor);

        constructorBuilder.loadThis().invokeVirtual(className, "void", initializerName);

        // And replace it with a constructor that throws an exception

        MethodNode replacementConstructor = new MethodNode(originalAccess, CONSTRUCTOR_NAME, NOTHING_TO_VOID, null,
                null);

        newBuilder(replacementConstructor).throwException(IllegalStateException.class, invalidConstructorMessage());

        classNode.methods.add(replacementConstructor);
    }

    private void stripOutSuperConstructorCall(MethodNode cons)
    {
        InsnList ins = cons.instructions;

        ListIterator li = ins.iterator();

        // Look for the ALOAD 0 (i.e., push this on the stack)
        while (li.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) li.next();

            if (node.getOpcode() == ALOAD)
            {
                VarInsnNode varNode = (VarInsnNode) node;

                assert varNode.var == 0;

                // Remove the ALOAD
                li.remove();
                break;
            }
        }

        // Look for the call to the super-class, an INVOKESPECIAL
        while (li.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) li.next();

            if (node.getOpcode() == INVOKESPECIAL)
            {
                MethodInsnNode mnode = (MethodInsnNode) node;

                assert mnode.owner.equals(classNode.superName);
                assert mnode.name.equals(CONSTRUCTOR_NAME);
                assert mnode.desc.equals(cons.desc);

                li.remove();
                return;
            }
        }

        throw new AssertionError("Could not convert constructor to simple method.");
    }

    @Override
    public <T extends Annotation> List<PlasticField> getFieldsWithAnnotation(Class<T> annotationType)
    {
        check();

        List<PlasticField> result = getAllFields();

        Iterator<PlasticField> iterator = result.iterator();

        while (iterator.hasNext())
        {
            PlasticField plasticField = iterator.next();

            if (!plasticField.hasAnnotation(annotationType))
                iterator.remove();
        }

        return result;
    }

    @Override
    public List<PlasticField> getAllFields()
    {
        check();

        return new ArrayList<>(fields);
    }

    @Override
    public List<PlasticField> getUnclaimedFields()
    {
        check();

        // Initially null, and set back to null by PlasticField.claim().

        if (unclaimedFields == null)
        {
            unclaimedFields = new ArrayList<>(fields.size());

            for (PlasticField f : fields)
            {
                if (!f.isClaimed())
                    unclaimedFields.add(f);
            }
        }

        return unclaimedFields;
    }

    @Override
    public PlasticMethod introducePrivateMethod(String typeName, String suggestedName, String[] argumentTypes,
                                                String[] exceptionTypes)
    {
        check();

        assert PlasticInternalUtils.isNonBlank(typeName);
        assert PlasticInternalUtils.isNonBlank(suggestedName);

        String name = makeUnique(methodNames, suggestedName);

        MethodDescription description = new MethodDescription(Modifier.PRIVATE, typeName, name, argumentTypes, null,
                exceptionTypes);

        return introduceMethod(description);
    }

    @Override
    public PlasticField introduceField(String className, String suggestedName)
    {
        check();

        assert PlasticInternalUtils.isNonBlank(className);
        assert PlasticInternalUtils.isNonBlank(suggestedName);

        String name = makeUnique(fieldNames, suggestedName);

        // No signature and no initial value

        FieldNode fieldNode = new FieldNode(ACC_PRIVATE, name, PlasticInternalUtils.toDescriptor(className), null, null);

        classNode.fields.add(fieldNode);

        fieldNames.add(name);

        PlasticFieldImpl newField = new PlasticFieldImpl(this, fieldNode);

        return newField;
    }

    @Override
    public PlasticField introduceField(Class fieldType, String suggestedName)
    {
        assert fieldType != null;

        return introduceField(nameCache.toTypeName(fieldType), suggestedName);
    }

    String makeUnique(Set<String> values, String input)
    {
        return values.contains(input) ? input + "$" + PlasticUtils.nextUID() : input;
    }

    @Override
    public <T extends Annotation> List<PlasticMethod> getMethodsWithAnnotation(Class<T> annotationType)
    {
        check();

        List<PlasticMethod> result = getMethods();

        result.removeIf(method -> !method.hasAnnotation(annotationType));

        return result;
    }

    @Override
    public List<PlasticMethod> getMethods()
    {
        check();

        return new ArrayList<>(methods);
    }

    @Override
    public PlasticMethod introduceMethod(MethodDescription description)
    {
        check();

        if (Modifier.isAbstract(description.modifiers))
        {
            description = description.withModifiers(description.modifiers & ~ACC_ABSTRACT);
        }

        PlasticMethod result = description2method.get(description);

        if (result == null)
        {
            result = createNewMethod(description);

            description2method.put(description, result);
        }

        methodNames.add(description.methodName);

        // Note that is it not necessary to add the new MethodNode to
        // fieldTransformMethods (the default implementations provided by introduceMethod() do not
        // ever access instance fields) ... unless the caller invokes changeImplementation().

        return result;
    }

    @Override
    public PlasticMethod introduceMethod(MethodDescription description, InstructionBuilderCallback callback)
    {
        check();

        // TODO: optimize this so that a default implementation is not created.

        return introduceMethod(description).changeImplementation(callback);
    }

    @Override
    public PlasticMethod introduceMethod(Method method)
    {
        check();
        return introduceMethod(new MethodDescription(method));
    }

    void addMethod(MethodNode methodNode)
    {
        classNode.methods.add(methodNode);

        methodNames.add(methodNode.name);

        if (isInheritableMethod(methodNode))
        {
            inheritanceData.addMethod(methodNode.name, methodNode.desc, methodNode.access == 0);
        }
    }

    private PlasticMethod createNewMethod(MethodDescription description)
    {
        if (Modifier.isStatic(description.modifiers))
            throw new IllegalArgumentException(String.format(
                    "Unable to introduce method '%s' into class %s: introduced methods may not be static.",
                    description, className));

        String desc = nameCache.toDesc(description);

        String[] exceptions = new String[description.checkedExceptionTypes.length];
        for (int i = 0; i < exceptions.length; i++)
        {
            exceptions[i] = PlasticInternalUtils.toInternalName(description.checkedExceptionTypes[i]);
        }

        MethodNode methodNode = new MethodNode(description.modifiers, description.methodName, desc,
                description.genericSignature, exceptions);
        boolean isOverride = inheritanceData.isImplemented(methodNode.name, desc);

        if (!isOverride)
        {
            addMethodAndParameterAnnotationsFromExistingClass(methodNode, implementationClassNode);
            addMethodAndParameterAnnotationsFromExistingClass(methodNode, interfaceClassNode);
            removeDuplicatedAnnotations(methodNode);
        }

        if (isOverride)
            createOverrideOfBaseClassImpl(description, methodNode);
        else
            createNewMethodImpl(description, methodNode);

        addMethod(methodNode);

        return new PlasticMethodImpl(this, methodNode);
    }

    private void createNewMethodImpl(MethodDescription methodDescription, MethodNode methodNode)
    {
        newBuilder(methodDescription, methodNode).returnDefaultValue();
    }

    private void createOverrideOfBaseClassImpl(MethodDescription methodDescription, MethodNode methodNode)
    {
        InstructionBuilder builder = newBuilder(methodDescription, methodNode);

        builder.loadThis();
        builder.loadArguments();
        builder.invokeSpecial(superClassName, methodDescription);
        builder.returnResult();
    }

    /**
     * Iterates over all non-introduced methods, including the original constructor. For each
     * method, the bytecode is scanned for field reads and writes. When a match is found against an intercepted field,
     * the operation is replaced with a method invocation. This is invoked only after the {@link PlasticClassHandleShim}
     * for the class has been created, as the shim may create methods that contain references to fields that may be
     * subject to field access interception.
     */
    private void interceptFieldAccess()
    {
        for (MethodNode node : fieldTransformMethods)
        {
            // Intercept field access inside the method, tracking which access methods
            // are actually used by removing them from accessMethods

            interceptFieldAccess(node);
        }
    }

    /**
     * Determines if any fields or methods have provided FieldHandles or MethodHandles; if so
     * a shim class must be created to facilitate read/write access to fields, or invocation of methods.
     */
    private void createShimIfNeeded()
    {
        if (shimFields.isEmpty() && shimMethods.isEmpty())
            return;

        PlasticClassHandleShim shim = createShimInstance();

        installShim(shim);
    }

    public void installShim(PlasticClassHandleShim shim)
    {
        for (PlasticFieldImpl f : shimFields)
        {
            f.installShim(shim);
        }

        for (PlasticMethodImpl m : shimMethods)
        {
            m.installShim(shim);
        }
    }

    public PlasticClassHandleShim createShimInstance()
    {
        String shimClassName = String.format("%s$Shim_%s", classNode.name, PlasticUtils.nextUID());

        ClassNode shimClassNode = new ClassNode();

        shimClassNode.visit(PlasticConstants.DEFAULT_VERSION_OPCODE, ACC_PUBLIC | ACC_FINAL, shimClassName, null, HANDLE_SHIM_BASE_CLASS_INTERNAL_NAME,
                null);

        implementConstructor(shimClassNode);

        if (!shimFields.isEmpty())
        {
            implementShimGet(shimClassNode);
            implementShimSet(shimClassNode);
        }

        if (!shimMethods.isEmpty())
        {
            implementShimInvoke(shimClassNode);
        }

        return instantiateShim(shimClassNode);
    }

    private void implementConstructor(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, CONSTRUCTOR_NAME, NOTHING_TO_VOID, null, null);

        InstructionBuilder builder = newBuilder(mn);

        builder.loadThis().invokeConstructor(PlasticClassHandleShim.class).returnResult();

        shimClassNode.methods.add(mn);

    }

    private PlasticClassHandleShim instantiateShim(ClassNode shimClassNode)
    {
        try
        {
            Class shimClass = pool.realize(className, ClassType.SUPPORT, shimClassNode);

            return (PlasticClassHandleShim) shimClass.newInstance();
        } catch (Exception ex)
        {
            throw new RuntimeException(
                    String.format("Unable to instantiate shim class %s for plastic class %s: %s",
                            PlasticInternalUtils.toClassName(shimClassNode.name), className,
                            PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    private void implementShimGet(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "get", OBJECT_INT_TO_OBJECT, null, null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index

        builder.loadArgument(0).checkcast(className);
        builder.loadArgument(1);

        builder.startSwitch(0, nextFieldIndex - 1, new SwitchCallback()
        {
            @Override
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticFieldImpl f : shimFields)
                {
                    f.extendShimGet(block);
                }
            }
        });

        shimClassNode.methods.add(mn);
    }

    private void implementShimSet(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "set", OBJECT_INT_OBJECT_TO_VOID, null, null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index
        // Arg 2 is the new value

        builder.loadArgument(0).checkcast(className);
        builder.loadArgument(2);

        builder.loadArgument(1);

        builder.startSwitch(0, nextFieldIndex - 1, new SwitchCallback()
        {
            @Override
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticFieldImpl f : shimFields)
                {
                    f.extendShimSet(block);
                }
            }
        });

        builder.returnResult();

        shimClassNode.methods.add(mn);
    }

    private void implementShimInvoke(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "invoke", OBJECT_INT_OBJECT_ARRAY_TO_METHOD_INVOCATION_RESULT, null,
                null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index
        // Arg 2 is the object array of parameters

        builder.loadArgument(0).checkcast(className);

        builder.loadArgument(1);

        builder.startSwitch(0, nextMethodIndex - 1, new SwitchCallback()
        {
            @Override
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticMethodImpl m : shimMethods)
                {
                    m.extendShimInvoke(block);
                }
            }
        });

        shimClassNode.methods.add(mn);
    }

    private void rewriteAdvisedMethods()
    {
        for (PlasticMethodImpl method : advisedMethods)
        {
            method.rewriteMethodForAdvice();
        }
    }

    private void interceptFieldAccess(MethodNode methodNode)
    {
        InsnList insns = methodNode.instructions;

        ListIterator<AbstractInsnNode> it = insns.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = it.next();

            int opcode = node.getOpcode();

            if (opcode != GETFIELD && opcode != PUTFIELD)
            {
                continue;
            }

            FieldInsnNode fnode = (FieldInsnNode) node;

            FieldInstrumentation instrumentation = findFieldNodeInstrumentation(fnode, opcode == GETFIELD);

            if (instrumentation == null)
            {
                continue;
            }

            // Replace the field access node with the appropriate method invocation.

            insns.insertBefore(fnode, new MethodInsnNode(INVOKEVIRTUAL, fnode.owner, instrumentation.methodName, instrumentation.methodDescription, false));

            it.remove();
        }
    }

    private FieldInstrumentation findFieldNodeInstrumentation(FieldInsnNode node, boolean forRead)
    {
        // First look in the local fieldInstrumentations, which contains private field instrumentations
        // (as well as non-private ones).

        String searchStart = node.owner;

        if (searchStart.equals(classNode.name))
        {
            FieldInstrumentation result = fieldInstrumentations.get(node.name, forRead);

            if (result != null)
            {
                return result;
            }

            // Slight optimization: start the search in the super-classes' fields, since we've already
            // checked this classes fields.

            searchStart = classNode.superName;
        }

        return pool.getFieldInstrumentation(searchStart, node.name, forRead);
    }

    String getInstanceContextFieldName()
    {
        if (instanceContextFieldName == null)
        {
            instanceContextFieldName = makeUnique(fieldNames, "instanceContext");

            // TODO: We could use a protected field and only initialize
            // it once, in the first base class where it is needed, though that raises the possibilities
            // of name conflicts (a subclass might introduce a field with a conflicting name).

            FieldNode node = new FieldNode(ACC_PRIVATE | ACC_FINAL, instanceContextFieldName, INSTANCE_CONTEXT_DESC,
                    null, null);

            classNode.fields.add(node);

            // Extend the constructor to store the context in a field.

            constructorBuilder.loadThis().loadArgument(1)
                    .putField(className, instanceContextFieldName, InstanceContext.class);
        }

        return instanceContextFieldName;
    }

    /**
     * Creates a new private final field and initializes its value (using the StaticContext).
     */
    String createAndInitializeFieldFromStaticContext(String suggestedFieldName, String fieldType,
                                                     Object injectedFieldValue)
    {
        String name = makeUnique(fieldNames, suggestedFieldName);

        FieldNode field = new FieldNode(ACC_PRIVATE | ACC_FINAL, name, nameCache.toDesc(fieldType), null, null);

        classNode.fields.add(field);

        initializeFieldFromStaticContext(name, fieldType, injectedFieldValue);

        return name;
    }

    /**
     * Initializes a field from the static context. The injected value is added to the static
     * context and the class constructor updated to assign the value from the context (which includes casting and
     * possibly unboxing).
     */
    void initializeFieldFromStaticContext(String fieldName, String fieldType, Object injectedFieldValue)
    {
        int index = staticContext.store(injectedFieldValue);

        // Although it feels nicer to do the loadThis() later and then swap(), that breaks
        // on primitive longs and doubles, so its just easier to do the loadThis() first
        // so its at the right place on the stack for the putField().

        constructorBuilder.loadThis();

        constructorBuilder.loadArgument(0).loadConstant(index);
        constructorBuilder.invoke(STATIC_CONTEXT_GET_METHOD);
        constructorBuilder.castOrUnbox(fieldType);

        constructorBuilder.putField(className, fieldName, fieldType);
    }

    void pushInstanceContextFieldOntoStack(InstructionBuilder builder)
    {
        builder.loadThis().getField(className, getInstanceContextFieldName(), InstanceContext.class);
    }

    @Override
    public PlasticClass getPlasticClass()
    {
        return this;
    }

    @Override
    public Class<?> getTransformedClass()
    {
        if (transformedClass == null)
            throw new IllegalStateException(String.format(
                    "Transformed class %s is not yet available because the transformation is not yet complete.",
                    className));

        return transformedClass;
    }

    private boolean isInheritableMethod(MethodNode node)
    {
        return !Modifier.isPrivate(node.access);
    }

    @Override
    public String getClassName()
    {
        return className;
    }

    InstructionBuilderImpl newBuilder(MethodNode mn)
    {
        return newBuilder(PlasticInternalUtils.toMethodDescription(mn), mn);
    }

    InstructionBuilderImpl newBuilder(MethodDescription description, MethodNode mn)
    {
        return new InstructionBuilderImpl(description, mn, nameCache);
    }

    public Set<PlasticMethod> introduceInterface(Class interfaceType)
    {
        return introduceInterface(interfaceType, null);
    }
    
    private Set<PlasticMethod> introduceInterface(Class interfaceType, PlasticMethod method)
    {
        check();

        assert interfaceType != null;

        if (!interfaceType.isInterface())
            throw new IllegalArgumentException(String.format(
                    "Class %s is not an interface; only interfaces may be introduced.", interfaceType.getName()));

        String interfaceName = nameCache.toInternalName(interfaceType);

        try
        {
            interfaceClassNode = PlasticClassPool.readClassNode(interfaceType.getName(), getClass().getClassLoader());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (!inheritanceData.isInterfaceImplemented(interfaceName))
        {
            classNode.interfaces.add(interfaceName);
            inheritanceData.addInterface(interfaceName);
        }

        addClassAnnotations(interfaceClassNode);

        Set<PlasticMethod> introducedMethods = new HashSet<PlasticMethod>();
        Set<Method> alreadyIntroducedMethods = new HashSet<>();

        Method[] sortedMethods = interfaceType.getMethods();
        Arrays.sort(sortedMethods, METHOD_COMPARATOR);
        for (Method m : sortedMethods)
        {
            MethodDescription description = new MethodDescription(m);

            if (!isMethodImplemented(description) && 
                    !(m.isDefault() && m.isBridge()) && 
                    !Modifier.isStatic(description.modifiers) && 
                    !contains(alreadyIntroducedMethods, m))
            {
                PlasticMethod introducedMethod = introduceMethod(m);
                introducedMethods.add(introducedMethod);
                if (method != null) {
                    introducedMethod.delegateTo(method);
                }
                alreadyIntroducedMethods.add(m);
            }
        }

        interfaceClassNode = null;

        return introducedMethods;
    }
    
    @Override
    public PlasticClass proxyInterface(Class interfaceType, PlasticMethod method)
    {
        check();
        assert method != null;

        introduceInterface(interfaceType, method);
        
        return this;
    }

    private boolean contains(Set<Method> alreadyIntroducedMethods, Method m) {
        boolean contains = false;
        for (Method method : alreadyIntroducedMethods) 
        {
            if (METHOD_COMPARATOR.compare(method, m) == 0)
            {
                contains = true;
                break;
            }
        }
        return false;
    }

    private Map<MethodSignature, MethodDescription> createMethodSignatureMap(Class interfaceType) {
        // TAP-2582: preprocessing the method list so we don't add duplicated
        // methods, something that happens when an interface has superinterfaces
        // and they define the same method signature.
        // In addition, we collect all the thrown checked exceptions, just in case.
        Map<MethodSignature, MethodDescription> map = new HashMap<>();
        for (Method m : interfaceType.getMethods())
        {
            final MethodSignature methodSignature = new MethodSignature(m);
            final MethodDescription newMethodDescription = new MethodDescription(m);
            if (!map.containsKey(methodSignature))
            {
                map.put(methodSignature, newMethodDescription);
            }
            else
            {
                if (newMethodDescription.checkedExceptionTypes != null && newMethodDescription.checkedExceptionTypes.length > 0)
                {
                    final MethodDescription methodDescription = map.get(methodSignature);
                        final Set<String> checkedExceptionTypes = new HashSet<>();
                        checkedExceptionTypes.addAll(Arrays.asList(methodDescription.checkedExceptionTypes));
                        checkedExceptionTypes.addAll(Arrays.asList(newMethodDescription.checkedExceptionTypes));
                        map.put(methodSignature, new MethodDescription(
                            methodDescription,
                            checkedExceptionTypes.toArray(new String[checkedExceptionTypes.size()])));
                }
            }
        }
        return map;
    }

    final private static class MethodSignature implements Comparable<MethodSignature> {
        final private Method method;
        final private String name;
        final private Class<?>[] parameterTypes;

        public MethodSignature(Method method) {
            this.method = method;
            this.name = method.getName();
            this.parameterTypes = method.getParameterTypes();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(parameterTypes);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            MethodSignature other = (MethodSignature) obj;
            if (!Arrays.equals(parameterTypes, other.parameterTypes)) return false;

            return name == null ? other.name == null : name.equals(other.name);
        }

        @Override
        public int compareTo(MethodSignature o) {
            return method.getName().compareTo(o.method.getName());
        }
    }

    @Override
    public PlasticClass addToString(final String toStringValue)
    {
        check();

        if (!isMethodImplemented(PlasticUtils.TO_STRING_DESCRIPTION))
        {
            introduceMethod(PlasticUtils.TO_STRING_DESCRIPTION, new InstructionBuilderCallback()
            {
                @Override
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadConstant(toStringValue).returnResult();
                }
            });
        }

        return this;
    }

    @Override
    public boolean isMethodImplemented(MethodDescription description)
    {
        return inheritanceData.isImplemented(description.methodName, nameCache.toDesc(description));
    }

    @Override
    public boolean isInterfaceImplemented(Class interfaceType)
    {
        assert interfaceType != null;
        assert interfaceType.isInterface();

        String interfaceName = nameCache.toInternalName(interfaceType);

        return inheritanceData.isInterfaceImplemented(interfaceName);
    }

    @Override
    public String getSuperClassName()
    {
        return superClassName;
    }

    @Override
    public PlasticClass onConstruct(ConstructorCallback callback)
    {
        check();

        assert callback != null;

        constructorCallbacks.add(callback);

        return this;
    }

    void redirectFieldWrite(String fieldName, boolean privateField, MethodNode method)
    {
        FieldInstrumentation fi = new FieldInstrumentation(method.name, method.desc);

        fieldInstrumentations.write.put(fieldName, fi);

        if (!proxy)
        {
            pool.setFieldWriteInstrumentation(classNode.name, fieldName, fi);
        }
    }

    void redirectFieldRead(String fieldName, boolean privateField, MethodNode method)
    {
        FieldInstrumentation fi = new FieldInstrumentation(method.name, method.desc);

        fieldInstrumentations.read.put(fieldName, fi);

        if (!proxy)
        {
            pool.setFieldReadInstrumentation(classNode.name, fieldName, fi);
        }
    }
    
    final private MethodComparator METHOD_COMPARATOR = new MethodComparator();
    
    final private class MethodComparator implements Comparator<Method> 
    {

        @Override
        public int compare(Method o1, Method o2) 
        {
            
            int comparison = comparison = o1.getName().compareTo(o2.getName());
            
            if (comparison == 0) 
            {
                comparison = o1.getParameterTypes().length - o2.getParameterTypes().length;
            }
            
            if (comparison == 0) 
            {
                final int count = o1.getParameterTypes().length;
                for (int i = 0; i < count; i++) 
                {
                    Class p1 = o1.getParameterTypes()[i];
                    Class p2 = o1.getParameterTypes()[i];
                    if (!p1.equals(p2)) 
                    {
                        comparison = p1.getName().compareTo(p2.getName());
                        break;
                    }
                }
            }
            
            if (comparison == 0)
            {
                // Trying to get methods lower in the interface hierarchy sorted before the same methods
                // higher in the hierarchy
                Class<?> declaringClass1 = o1.getDeclaringClass();
                Class<?> declaringClass2 = o2.getDeclaringClass();
                if (declaringClass1.isInterface() && declaringClass2.isInterface() && 
                        !declaringClass1.equals(declaringClass2)) {
                    if (declaringClass1.isAssignableFrom(declaringClass2))
                    {
                        comparison = -1;
                    }
                    else if (declaringClass1.isAssignableFrom(declaringClass2))
                    {
                        comparison = 1;
                    }
                }
            }
                    
            return comparison;
        }
    }
    
    private List<Method> getUniqueMethods(Class interfaceType) 
    {
        final List<Method> unique = new ArrayList<>(Arrays.asList(interfaceType.getMethods()));
        Collections.sort(unique, METHOD_COMPARATOR);
        Method last = null;
        Iterator<Method> iterator = unique.iterator();
        while (iterator.hasNext()) 
        {
            Method m = iterator.next();
            if (last != null && METHOD_COMPARATOR.compare(m, last) == 0)
            {
                last = m;
                iterator.remove();
            }
        }
        return unique;
    }

    @Override
    public String toString()
    {
        return String.format("PlasticClassImpl[%s]", className);
    }
}
