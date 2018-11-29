// Copyright 2011-2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.internal.plastic.asm.ClassReader;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.tree.*;
import org.apache.tapestry5.plastic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Responsible for managing a class loader that allows ASM {@link ClassNode}s
 * to be instantiated as runtime classes.
 */
@SuppressWarnings("rawtypes")
public class PlasticClassPool implements ClassLoaderDelegate, Opcodes, PlasticClassListenerHub
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlasticClassPool.class);

    final PlasticClassLoader loader;

    private final PlasticManagerDelegate delegate;

    private final Set<String> controlledPackages;

    private final Map<String, Boolean> checkedExceptionCache = new HashMap<String, Boolean>();


    // Would use Deque, but that's added in 1.6 and we're still striving for 1.5 code compatibility.

    private final Stack<String> activeInstrumentClassNames = new Stack<String>();

    /**
     * Maps class names to instantiators for that class name.
     * Synchronized on the loader.
     */
    private final Map<String, ClassInstantiator> instantiators = PlasticInternalUtils.newConcurrentMap();

    private final InheritanceData emptyInheritanceData = new InheritanceData(null);

    private final StaticContext emptyStaticContext = new StaticContext();

    private final List<PlasticClassListener> listeners = new CopyOnWriteArrayList<PlasticClassListener>();

    private final Cache<String, TypeCategory> typeName2Category = new Cache<String, TypeCategory>()
    {
        @Override
        protected TypeCategory convert(String typeName)
        {
            ClassNode cn = constructClassNodeFromBytecode(typeName);

            return Modifier.isInterface(cn.access) ? TypeCategory.INTERFACE : TypeCategory.CLASS;
        }
    };

    static class BaseClassDef
    {
        final InheritanceData inheritanceData;

        final StaticContext staticContext;

        public BaseClassDef(InheritanceData inheritanceData, StaticContext staticContext)
        {
            this.inheritanceData = inheritanceData;
            this.staticContext = staticContext;
        }
    }

    /**
     * Map from FQCN to BaseClassDef. Synchronized on the loader.
     */
    private final Map<String, BaseClassDef> baseClassDefs = PlasticInternalUtils.newMap();


    private final Map<String, FieldInstrumentations> instrumentations = PlasticInternalUtils.newMap();

    private final Map<String, String> transformedClassNameToImplementationClassName = PlasticInternalUtils.newMap();


    private final FieldInstrumentations placeholder = new FieldInstrumentations(null);


    private final Set<TransformationOption> options;

    /**
     * Creates the pool with a set of controlled packages; all classes in the controlled packages are loaded by the
     * pool's class loader, and all top-level classes in the controlled packages are transformed via the delegate.
     *
     * @param parentLoader
     *         typically, the Thread's context class loader
     * @param delegate
     *         responsible for end stages of transforming top-level classes
     * @param controlledPackages
     *         set of package names (note: retained, not copied)
     * @param options
     *         used when transforming classes
     */
    public PlasticClassPool(ClassLoader parentLoader, PlasticManagerDelegate delegate, Set<String> controlledPackages,
                            Set<TransformationOption> options)
    {
        loader = new PlasticClassLoader(parentLoader, this);
        this.delegate = delegate;
        this.controlledPackages = controlledPackages;
        this.options = options;
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }

    public Class realizeTransformedClass(ClassNode classNode, InheritanceData inheritanceData,
                                         StaticContext staticContext)
    {
        synchronized (loader)
        {
            Class result = realize(PlasticInternalUtils.toClassName(classNode.name), ClassType.PRIMARY, classNode);
            baseClassDefs.put(result.getName(), new BaseClassDef(inheritanceData, staticContext));

            return result;
        }

    }

    public Class realize(String primaryClassName, ClassType classType, ClassNode classNode)
    {
        synchronized (loader)
        {
            if (!listeners.isEmpty())
            {
                fire(toEvent(primaryClassName, classType, classNode));
            }

            byte[] bytecode = toBytecode(classNode);

            String className = PlasticInternalUtils.toClassName(classNode.name);

            return loader.defineClassWithBytecode(className, bytecode);
        }
    }

    private PlasticClassEvent toEvent(final String primaryClassName, final ClassType classType,
                                      final ClassNode classNode)
    {
        return new PlasticClassEvent()
        {
            @Override
            public ClassType getType()
            {
                return classType;
            }

            @Override
            public String getPrimaryClassName()
            {
                return primaryClassName;
            }

            @Override
            public String getDissasembledBytecode()
            {
                return PlasticInternalUtils.dissasembleBytecode(classNode);
            }

            @Override
            public String getClassName()
            {
                return PlasticInternalUtils.toClassName(classNode.name);
            }
        };
    }

    private void fire(PlasticClassEvent event)
    {
        for (PlasticClassListener listener : listeners)
        {
            listener.classWillLoad(event);
        }
    }

    private byte[] toBytecode(ClassNode classNode)
    {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        classNode.accept(writer);

        return writer.toByteArray();
    }

    public AnnotationAccess createAnnotationAccess(String className)
    {
        try
        {
            final Class<?> searchClass = loader.loadClass(className);

            return new AnnotationAccess()
            {
                @Override
                public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
                {
                    return getAnnotation(annotationType) != null;
                }

                @Override
                public <T extends Annotation> T getAnnotation(Class<T> annotationType)
                {
                    return searchClass.getAnnotation(annotationType);
                }
            };
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public AnnotationAccess createAnnotationAccess(List<AnnotationNode> annotationNodes)
    {
        if (annotationNodes == null)
        {
            return EmptyAnnotationAccess.SINGLETON;
        }

        final Map<String, Object> cache = PlasticInternalUtils.newMap();
        final Map<String, AnnotationNode> nameToNode = PlasticInternalUtils.newMap();

        for (AnnotationNode node : annotationNodes)
        {
            nameToNode.put(PlasticInternalUtils.objectDescriptorToClassName(node.desc), node);
        }

        return new AnnotationAccess()
        {
            @Override
            public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
            {
                return nameToNode.containsKey(annotationType.getName());
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationType)
            {
                String className = annotationType.getName();

                Object result = cache.get(className);

                if (result == null)
                {
                    result = buildAnnotation(className);

                    if (result != null)
                        cache.put(className, result);
                }

                return annotationType.cast(result);
            }

            private Object buildAnnotation(String className)
            {
                AnnotationNode node = nameToNode.get(className);

                if (node == null)
                    return null;

                return createAnnotation(className, node);
            }
        };
    }

    Class loadClass(String className)
    {
        try
        {
            return loader.loadClass(className);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to load class %s: %s", className,
                    PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    protected Object createAnnotation(String className, AnnotationNode node)
    {
        AnnotationBuilder builder = new AnnotationBuilder(loadClass(className), this);

        node.accept(builder);

        return builder.createAnnotation();
    }

    @Override
    public boolean shouldInterceptClassLoading(String className)
    {
        int searchFromIndex = className.length() - 1;

        while (true)
        {
            int dotx = className.lastIndexOf('.', searchFromIndex);

            if (dotx < 0)
                break;

            String packageName = className.substring(0, dotx);

            if (controlledPackages.contains(packageName))
                return true;

            searchFromIndex = dotx - 1;
        }

        return false;
    }

    // Hopefully the synchronized will not cause a deadlock

    @Override
    public synchronized Class<?> loadAndTransformClass(String className) throws ClassNotFoundException
    {
        // Inner classes are not transformed, but they are loaded by the same class loader.

        if (className.contains("$"))
        {
            return loadInnerClass(className);
        }

        // TODO: What about interfaces, enums, annotations, etc. ... they shouldn't be in the package, but
        // we should generate a reasonable error message.

        if (activeInstrumentClassNames.contains(className))
        {
            StringBuilder builder = new StringBuilder("");
            String sep = "";

            for (String name : activeInstrumentClassNames)
            {
                builder.append(sep);
                builder.append(name);

                sep = ", ";
            }

            throw new IllegalStateException(String.format("Unable to transform class %s as it is already in the process of being transformed; there is a cycle among the following classes: %s.",
                    className, builder));
        }

        activeInstrumentClassNames.push(className);

        try
        {

            InternalPlasticClassTransformation transformation = getPlasticClassTransformation(className);

            delegate.transform(transformation.getPlasticClass());

            ClassInstantiator createInstantiator = transformation.createInstantiator();
            ClassInstantiator configuredInstantiator = delegate.configureInstantiator(className, createInstantiator);

            instantiators.put(className, configuredInstantiator);

            return transformation.getTransformedClass();
        } finally
        {
            activeInstrumentClassNames.pop();
        }
    }

    private Class loadInnerClass(String className)
    {
        ClassNode classNode = constructClassNodeFromBytecode(className);

        interceptFieldAccess(classNode);

        return realize(className, ClassType.INNER, classNode);
    }

    private void interceptFieldAccess(ClassNode classNode)
    {
        for (MethodNode method : classNode.methods)
        {
            interceptFieldAccess(classNode.name, method);
        }
    }

    private void interceptFieldAccess(String classInternalName, MethodNode method)
    {
        InsnList insns = method.instructions;

        ListIterator it = insns.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) it.next();

            int opcode = node.getOpcode();

            if (opcode != GETFIELD && opcode != PUTFIELD)
            {
                continue;
            }

            FieldInsnNode fnode = (FieldInsnNode) node;

            String ownerInternalName = fnode.owner;

            if (ownerInternalName.equals(classInternalName))
            {
                continue;
            }

            FieldInstrumentation instrumentation = getFieldInstrumentation(ownerInternalName, fnode.name, opcode == GETFIELD);

            if (instrumentation == null)
            {
                continue;
            }

            // Replace the field access node with the appropriate method invocation.

            insns.insertBefore(fnode, new MethodInsnNode(INVOKEVIRTUAL, ownerInternalName, instrumentation.methodName, instrumentation.methodDescription, false));

            it.remove();
        }
    }


    /**
     * For a fully-qualified class name of an <em>existing</em> class, loads the bytes for the class
     * and returns a PlasticClass instance.
     *
     * @throws ClassNotFoundException
     */
    public InternalPlasticClassTransformation getPlasticClassTransformation(String className)
            throws ClassNotFoundException
    {
        assert PlasticInternalUtils.isNonBlank(className);

        ClassNode classNode = constructClassNodeFromBytecode(className);

        String baseClassName = PlasticInternalUtils.toClassName(classNode.superName);

        instrumentations.put(classNode.name, new FieldInstrumentations(classNode.superName));

        // TODO: check whether second parameter should really be null
        return createTransformation(baseClassName, classNode, null, false);
    }

    /**
     * @param baseClassName
     *         class from which the transformed class extends
     * @param classNode
     *         node for the class
     * @param implementationClassNode
     *         node for the implementation class. May be null.
     * @param proxy
     *         if true, the class is a new empty class; if false an existing class that's being transformed
     * @throws ClassNotFoundException
     */
    private InternalPlasticClassTransformation createTransformation(String baseClassName, ClassNode classNode, ClassNode implementationClassNode, boolean proxy)
            throws ClassNotFoundException
    {
        if (shouldInterceptClassLoading(baseClassName))
        {
            loader.loadClass(baseClassName);

            BaseClassDef def = baseClassDefs.get(baseClassName);

            assert def != null;

            return new PlasticClassImpl(classNode, implementationClassNode, this, def.inheritanceData, def.staticContext, proxy);
        }

        // When the base class is Object, or otherwise not in a transformed package,
        // then start with the empty
        return new PlasticClassImpl(classNode, implementationClassNode, this, emptyInheritanceData, emptyStaticContext, proxy);
    }

    /**
     * Constructs a class node by reading the raw bytecode for a class and instantiating a ClassNode
     * (via {@link ClassReader#accept(org.apache.tapestry5.internal.plastic.asm.ClassVisitor, int)}).
     *
     * @param className
     *         fully qualified class name
     * @return corresponding ClassNode
     */
    public ClassNode constructClassNodeFromBytecode(String className)
    {
        byte[] bytecode = readBytecode(className);

        if (bytecode == null)
            return null;

        return PlasticInternalUtils.convertBytecodeToClassNode(bytecode);
    }

    private byte[] readBytecode(String className)
    {
        ClassLoader parentClassLoader = loader.getParent();

        return PlasticInternalUtils.readBytecodeForClass(parentClassLoader, className, true);
    }

    public PlasticClassTransformation createTransformation(String baseClassName, String newClassName)
    {
        return createTransformation(baseClassName, newClassName, null);
    }

    public PlasticClassTransformation createTransformation(String baseClassName, String newClassName, String implementationClassName)
    {
        try
        {
            ClassNode newClassNode = new ClassNode();

            final String internalNewClassNameinternalName = PlasticInternalUtils.toInternalName(newClassName);
            final String internalBaseClassName = PlasticInternalUtils.toInternalName(baseClassName);
            newClassNode.visit(PlasticConstants.DEFAULT_VERSION_OPCODE, ACC_PUBLIC, internalNewClassNameinternalName, null, internalBaseClassName, null);

            ClassNode implementationClassNode = null;

            if (implementationClassName != null)
            {
                // When decorating or advising a service, implementationClassName is the name
                // of a proxy class already, such as "$ServiceName_[random string]",
                // which doesn't exist as a file in the classpath, just in memory.
                // So we need to keep what's the original implementation class name
                // for each proxy, even a proxy around a proxy.
                if (transformedClassNameToImplementationClassName.containsKey(implementationClassName))
                {
                    implementationClassName =
                            transformedClassNameToImplementationClassName.get(implementationClassName);
                }

                if (!implementationClassName.startsWith("com.sun.proxy"))
                {

                    try
                    {
                        implementationClassNode = readClassNode(implementationClassName);
                    } catch (IOException e)
                    {
                        LOGGER.warn(String.format("Unable to load class %s as the implementation of service %s",
                                implementationClassName, baseClassName));
                        // Go on. Probably a proxy class
                    }

                }

                transformedClassNameToImplementationClassName.put(newClassName, implementationClassName);

            }

            return createTransformation(baseClassName, newClassNode, implementationClassNode, true);
        } catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(String.format("Unable to create class %s as sub-class of %s: %s", newClassName,
                    baseClassName, PlasticInternalUtils.toMessage(ex)), ex);
        }

    }

    private ClassNode readClassNode(String className) throws IOException
    {
        return readClassNode(className, getClassLoader());
    }

    static ClassNode readClassNode(String className, ClassLoader classLoader) throws IOException
    {
        ClassNode classNode = new ClassNode();
        final String location = PlasticInternalUtils.toInternalName(className) + ".class";
        InputStream inputStream = classLoader.getResourceAsStream(location);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ClassReader classReader = new ClassReader(inputStream);
        inputStream.close();
        bis.close();
        classReader.accept(classNode, 0);
        return classNode;

    }

    public ClassInstantiator getClassInstantiator(String className)
    {
        ClassInstantiator result = instantiators.get(className);

        if (result == null)
        {
            try
            {
                loader.loadClass(className);
                result = instantiators.get(className);
            } catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }


        if (result != null)
        {
            return result;
        }

        // TODO: Verify that the problem is incorrect package, and not any other failure.

        StringBuilder b = new StringBuilder();
        b.append("Class '")
                .append(className)
                .append("' is not a transformed class. Transformed classes should be in one of the following packages: ");

        String sep = "";

        List<String> names = new ArrayList<String>(controlledPackages);
        Collections.sort(names);

        for (String name : names)
        {
            b.append(sep);
            b.append(name);

            sep = ", ";
        }

        String message = b.append('.').toString();

        throw new IllegalArgumentException(message);
    }

    TypeCategory getTypeCategory(String typeName)
    {
        synchronized (loader)
        {
            // TODO: Is this the right place to cache this data?

            return typeName2Category.get(typeName);
        }
    }

    @Override
    public void addPlasticClassListener(PlasticClassListener listener)
    {
        assert listener != null;

        listeners.add(listener);
    }

    @Override
    public void removePlasticClassListener(PlasticClassListener listener)
    {
        assert listener != null;

        listeners.remove(listener);
    }

    boolean isEnabled(TransformationOption option)
    {
        return options.contains(option);
    }


    void setFieldReadInstrumentation(String classInternalName, String fieldName, FieldInstrumentation fi)
    {
        instrumentations.get(classInternalName).read.put(fieldName, fi);
    }


    private FieldInstrumentations getFieldInstrumentations(String classInternalName)
    {
        FieldInstrumentations result = instrumentations.get(classInternalName);

        if (result != null)
        {
            return result;
        }

        String className = PlasticInternalUtils.toClassName(classInternalName);

        // If it is a top-level (not inner) class in a controlled package, then we
        // will recursively load the class, to identify any field instrumentations
        // in it.
        if (!className.contains("$") && shouldInterceptClassLoading(className))
        {
            try
            {
                loadAndTransformClass(className);

                // The key is written into the instrumentations map as a side-effect
                // of loading the class.
                return instrumentations.get(classInternalName);
            } catch (Exception ex)
            {
                throw new RuntimeException(PlasticInternalUtils.toMessage(ex), ex);
            }
        }

        // Either a class outside of controlled packages, or an inner class. Use a placeholder
        // that contains empty maps.

        result = placeholder;
        instrumentations.put(classInternalName, result);

        return result;
    }

    FieldInstrumentation getFieldInstrumentation(String ownerClassInternalName, String fieldName, boolean forRead)
    {
        String currentName = ownerClassInternalName;

        while (true)
        {

            if (currentName == null)
            {
                return null;
            }

            FieldInstrumentations instrumentations = getFieldInstrumentations(currentName);

            FieldInstrumentation instrumentation = instrumentations.get(fieldName, forRead);

            if (instrumentation != null)
            {
                return instrumentation;
            }

            currentName = instrumentations.superClassInternalName;
        }
    }


    void setFieldWriteInstrumentation(String classInternalName, String fieldName, FieldInstrumentation fi)
    {
        instrumentations.get(classInternalName).write.put(fieldName, fi);
    }

    boolean isCheckedException(String exceptionName)
    {
        Boolean cached = checkedExceptionCache.get(exceptionName);

        if (cached != null)
        {
            return cached;
        }

        try
        {
            Class asClass = getClassLoader().loadClass(exceptionName);


            boolean checked = !(Error.class.isAssignableFrom(asClass) ||
                    RuntimeException.class.isAssignableFrom(asClass));

            checkedExceptionCache.put(exceptionName, checked);

            return checked;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}


