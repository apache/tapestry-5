// Copyright 2011 The Apache Software Foundation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.internal.plastic.asm.ClassReader;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.tree.AnnotationNode;
import org.apache.tapestry5.internal.plastic.asm.tree.ClassNode;
import org.apache.tapestry5.plastic.AnnotationAccess;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticManagerDelegate;

/**
 * Responsible for managing a class loader that allows ASM {@link ClassNode}s
 * to be instantiated as runtime classes.
 */
@SuppressWarnings("rawtypes")
public class PlasticClassPool implements ClassLoaderDelegate, Opcodes
{
    final PlasticClassLoader loader;

    private final PlasticManagerDelegate delegate;

    private final Set<String> controlledPackages;

    private final Map<String, ClassInstantiator> instantiators = PlasticInternalUtils.newMap();

    private final MethodBundle emptyMethodBundle = new MethodBundle();

    private final StaticContext emptyStaticContext = new StaticContext();

    private final Map<String, byte[]> createdClassesBytecode = PlasticInternalUtils.newMap();

    private final Cache<String, TypeCategory> typeName2Category = new Cache<String, TypeCategory>()
    {

        protected TypeCategory convert(String typeName)
        {
            ClassNode cn = constructClassNode(typeName, true);

            return Modifier.isInterface(cn.access) ? TypeCategory.INTERFACE : TypeCategory.CLASS;
        }
    };

    class BaseClassDef
    {
        final MethodBundle methodBundle;

        final StaticContext staticContext;

        public BaseClassDef(MethodBundle methodBundle, StaticContext staticContext)
        {
            this.methodBundle = methodBundle;
            this.staticContext = staticContext;
        }
    }

    /** Map from FQCN to BaseClassDef. */
    private final Map<String, BaseClassDef> baseClassDefs = new HashMap<String, PlasticClassPool.BaseClassDef>();

    /**
     * Creates the pool with a set of controlled packages; all classes in the controlled packages are loaded by the
     * pool's class loader, and all top-level classes in the controlled packages are transformed via the delegate.
     * 
     * @param parentLoader
     *            typically, the Thread's context class loader
     * @param delegate
     *            responsible for end stages of transforming top-level classes
     * @param controlledPackages
     *            set of package names (note: retained, not copied)
     */
    public PlasticClassPool(ClassLoader parentLoader, PlasticManagerDelegate delegate, Set<String> controlledPackages)
    {
        loader = new PlasticClassLoader(parentLoader, this);
        this.delegate = delegate;
        this.controlledPackages = controlledPackages;
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }

    public synchronized Class realizeTransformedClass(ClassNode classNode, MethodBundle methodBundle,
            StaticContext staticContext)
    {
        Class result = realize(classNode);

        baseClassDefs.put(result.getName(), new BaseClassDef(methodBundle, staticContext));

        return result;
    }

    public synchronized Class realize(ClassNode classNode)
    {
        PlasticInternalUtils.debugClass(classNode);

        byte[] bytecode = toBytecode(classNode);

        String className = PlasticInternalUtils.toClassName(classNode.name);

        createdClassesBytecode.put(className, bytecode);

        return loader.defineClassWithBytecode(className, bytecode);
    }

    private byte[] toBytecode(ClassNode classNode)
    {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        classNode.accept(writer);

        return writer.toByteArray();
    }

    public AnnotationAccess createAnnotationAccess(List<AnnotationNode> annotationNodes)
    {
        if (annotationNodes == null)
            return EmptyAnnotationAccess.SINGLETON;

        final Map<String, Object> cache = PlasticInternalUtils.newMap();
        final Map<String, AnnotationNode> nameToNode = PlasticInternalUtils.newMap();

        for (AnnotationNode node : annotationNodes)
        {
            nameToNode.put(PlasticInternalUtils.objectDescriptorToClassName(node.desc), node);
        }

        return new AnnotationAccess()
        {

            public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
            {
                return nameToNode.containsKey(annotationType.getName());
            }

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
        }
        catch (Exception ex)
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

    public Class<?> loadAndTransformClass(String className) throws ClassNotFoundException
    {
        // Inner classes are not transformed, but they are loaded by the same class loader.

        if (className.contains("$"))
            return loadInnerClass(className);

        // TODO: What about interfaces, enums, annotations, etc. ... they shouldn't be in the package, but
        // we should generate a reasonable error message.

        InternalPlasticClassTransformation transformation = getPlasticClassTransformation(className);

        delegate.transform(transformation.getPlasticClass());

        ClassInstantiator createInstantiator = transformation.createInstantiator();
        ClassInstantiator configuredInstantiator = delegate.configureInstantiator(className, createInstantiator);

        instantiators.put(className, configuredInstantiator);

        return transformation.getTransformedClass();
    }

    private Class loadInnerClass(String className)
    {
        byte[] bytecode = readBytecode(className, true);

        return loader.defineClassWithBytecode(className, bytecode);
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

        ClassNode classNode = constructClassNode(className, true);

        String baseClassName = PlasticInternalUtils.toClassName(classNode.superName);

        return createTransformation(baseClassName, classNode);
    }

    private InternalPlasticClassTransformation createTransformation(String baseClassName, ClassNode classNode)
            throws ClassNotFoundException
    {
        if (shouldInterceptClassLoading(baseClassName))
        {
            loader.loadClass(baseClassName);

            BaseClassDef def = baseClassDefs.get(baseClassName);

            assert def != null;

            return new PlasticClassImpl(classNode, this, def.methodBundle, def.staticContext);
        }

        return new PlasticClassImpl(classNode, this, emptyMethodBundle, emptyStaticContext);
    }

    /**
     * Constructs a class node by reading the raw bytecode for a class and instantiating a ClassNode
     * (via {@link ClassReader#accept(org.apache.tapestry5.internal.plastic.asm.ClassVisitor, int)}).
     * 
     * @param className
     *            fully qualified class name
     * @param mustExist
     *            TODO
     * @return corresponding ClassNode
     */
    public ClassNode constructClassNode(String className, boolean mustExist)
    {
        byte[] bytecode = readBytecode(className, mustExist);

        if (bytecode == null)
            return null;

        return convertBytecodeToClassNode(bytecode);
    }

    private ClassNode convertBytecodeToClassNode(byte[] bytecode)
    {
        ClassReader cr = new ClassReader(bytecode);

        ClassNode result = new ClassNode();

        cr.accept(result, 0);

        return result;
    }

    private byte[] readBytecode(String className, boolean mustExist)
    {
        byte[] createdBytecode = createdClassesBytecode.get(className);

        if (createdBytecode != null)
            return createdBytecode;

        ClassLoader parentClassLoader = loader.getParent();

        String path = PlasticInternalUtils.toClassPath(className);

        InputStream stream = parentClassLoader.getResourceAsStream(path);

        if (stream == null)
        {
            if (mustExist)
                throw new RuntimeException(String.format("Unable to locate class file for '%s' in class loader %s.",
                        className, parentClassLoader));

            return null;
        }

        try
        {
            return readBytestream(stream);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(String.format("Failure reading bytecode for class %s: %s", className,
                    PlasticInternalUtils.toMessage(ex)), ex);
        }
        finally
        {
            PlasticInternalUtils.close(stream);
        }
    }

    private byte[] readBytestream(InputStream stream) throws IOException
    {
        byte[] buffer = new byte[5000];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true)
        {
            int length = stream.read(buffer);

            if (length < 0)
                break;

            bos.write(buffer, 0, length);
        }

        bos.close();

        return bos.toByteArray();
    }

    public PlasticClassTransformation createTransformation(String baseClassName, String newClassName)
    {
        try
        {
            ClassNode newClassNode = new ClassNode();

            newClassNode.visit(V1_5, ACC_PUBLIC, PlasticInternalUtils.toInternalName(newClassName), null,
                    PlasticInternalUtils.toInternalName(baseClassName), null);

            return createTransformation(baseClassName, newClassNode);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(String.format("Unable to create class %s as sub-class of %s: %s", newClassName,
                    baseClassName, PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    public synchronized ClassInstantiator getClassInstantiator(String className)
    {
        if (!instantiators.containsKey(className))
        {
            try
            {
                loader.loadClass(className);
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        ClassInstantiator result = instantiators.get(className);

        if (result == null)
        {
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

            String message = b.append(".").toString();

            throw new IllegalArgumentException(message);
        }

        return result;
    }

    TypeCategory getTypeCategory(String typeName)
    {
        // TODO: Is this the right place to cache this data?

        return typeName2Category.get(typeName);
    }
}
