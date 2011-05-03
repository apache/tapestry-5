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

package org.apache.tapestry5.plastic;

import java.util.Collection;
import java.util.Set;

import org.apache.tapestry5.internal.plastic.NoopDelegate;
import org.apache.tapestry5.internal.plastic.PlasticClassPool;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;

/**
 * Manages the internal class loaders and other logics necessary to load and transform existing classes,
 * or to create new classes dynamically at runtime. New instances are instantiates using
 * {@link #withClassLoader(ClassLoader)} or {@link #withContextClassLoader()}, then configuring
 * the returned options object before invoking {@link PlasticManagerOptions#create()}.
 */
@SuppressWarnings("unchecked")
public class PlasticManager implements PlasticClassListenerHub
{
    private final PlasticClassPool pool;

    /**
     * A builder object for configuring the PlasticManager before instantiating it. Assumes a no-op
     * {@link PlasticManagerDelegate} and an empty
     * set of controlled packages.
     */
    public static class PlasticManagerOptions
    {
        private final ClassLoader loader;

        private PlasticManagerDelegate delegate = new NoopDelegate();

        private final Set<String> packages = PlasticInternalUtils.newSet();

        private PlasticManagerOptions(ClassLoader loader)
        {
            assert loader != null;

            this.loader = loader;
        }

        /**
         * Sets the {@link PlasticManagerDelegate}, which is ultimately responsible for
         * transforming classes loaded from controlled packages. The default delegate
         * does nothing.
         */
        public PlasticManagerOptions delegate(PlasticManagerDelegate delegate)
        {
            assert delegate != null;

            this.delegate = delegate;

            return this;
        }

        /**
         * Adds additional controlled packages, in which classes are loaded and transformed.
         */
        public PlasticManagerOptions packages(Collection<String> packageNames)
        {
            packages.addAll(packageNames);

            return this;
        }

        /**
         * Creates the PlasticManager with the current set of options.
         * 
         * @return the PlasticManager
         */
        public PlasticManager create()
        {
            return new PlasticManager(loader, delegate, packages);
        }
    }

    /**
     * Creates a new options using the thread's context class loader.
     */
    public static PlasticManagerOptions withContextClassLoader()
    {
        return withClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /** Creates a new options using the specified class loader. */
    public static PlasticManagerOptions withClassLoader(ClassLoader loader)
    {
        return new PlasticManagerOptions(loader);
    }

    /**
     * The standard constructor for PlasticManager, allowing a parent class loader to be specified (this is most often
     * the thread's contextClassLoader), as well as the delegate and the names of all controlled packages.
     * 
     * @param parentClassLoader
     *            main source for (untransformed) classes
     * @param delegate
     *            performs transformations on top-level classes from controlled packages
     * @param controlledPackageNames
     *            defines the packages that are to be transformed; top-classes in these packages
     *            (or sub-packages) will be passed to the delegate for transformation
     */
    private PlasticManager(ClassLoader parentClassLoader, PlasticManagerDelegate delegate,
            Set<String> controlledPackageNames)
    {
        assert parentClassLoader != null;
        assert delegate != null;
        assert controlledPackageNames != null;

        pool = new PlasticClassPool(parentClassLoader, delegate, controlledPackageNames);
    }

    /**
     * Returns the ClassLoader that is used to instantiate transformed classes. The parent class loader
     * of the returned class loader is the context class loader, or the class loader specified by
     * {@link #withClassLoader(ClassLoader)}.
     * 
     * @return class loader used to load classes in controlled packages
     */
    public ClassLoader getClassLoader()
    {
        return pool.getClassLoader();
    }

    /**
     * This method is used only in testing to get the PlasticClass directly, bypassing the normal code paths. This
     * is only invoked by Groovy tests which fudges the fact that the same class implements both PlasticClass and
     * PlasticClassTransformation.
     * TODO: This may make a kind of callback when we get to proxy creation, rather then pure transformation.
     * TODO: Clean up this mess!
     * 
     * @throws ClassNotFoundException
     */
    <T> PlasticClassTransformation<T> getPlasticClass(String className) throws ClassNotFoundException
    {
        assert PlasticInternalUtils.isNonBlank(className);

        return pool.getPlasticClassTransformation(className);
    }

    /**
     * Gets the {@link ClassInstantiator} for the indicated class, which must be in a transformed package.
     * 
     * @param className
     *            fully qualified class name
     * @return instantiator (configured via the
     *         {@linkplain PlasticManagerDelegate#configureInstantiator(String, ClassInstantiator) delegate} for the
     *         class
     * @throws IllegalArgumentException
     *             if the class is not a transformed class
     */
    public <T> ClassInstantiator<T> getClassInstantiator(String className)
    {
        return pool.getClassInstantiator(className);
    }

    /**
     * Creates an entirely new class, extending from the provided base class.
     * 
     * @param baseClass
     *            class to extend from, which must be a class, not an interface
     * @param callback
     *            used to configure the new class
     * @return the instantiator, which allows instances of the new class to be created
     */
    public <T> ClassInstantiator<T> createClass(Class<T> baseClass, PlasticClassTransformer callback)
    {
        assert baseClass != null;
        assert callback != null;

        if (baseClass.isInterface())
            throw new IllegalArgumentException(String.format("Class %s defines an interface, not a base class.",
                    baseClass.getName()));

        String name = String.format("$%s_%s", baseClass.getSimpleName(), PlasticUtils.nextUID());

        PlasticClassTransformation<T> transformation = pool.createTransformation(baseClass.getName(), name);

        callback.transform(transformation.getPlasticClass());

        return transformation.createInstantiator();
    }

    /**
     * Creates an entirely new class, extending from the provided base class.
     * 
     * @param interfaceType
     *            class to extend from, which must be a class, not an interface
     * @param callback
     *            used to configure the new class
     * @return the instantiator, which allows instances of the new class to be created
     * @see #createProxyTransformation(Class)
     */
    public <T> ClassInstantiator<T> createProxy(Class<T> interfaceType, PlasticClassTransformer callback)
    {
        assert callback != null;

        PlasticClassTransformation<T> transformation = createProxyTransformation(interfaceType);

        callback.transform(transformation.getPlasticClass());

        return transformation.createInstantiator();
    }

    /**
     * Creates the underlying {@link PlasticClassTransformation} for an interface proxy. This should only be
     * used in the cases where encapsulating the PlasticClass construction into a {@linkplain PlasticClassTransformer
     * callback} is not feasible (which is the case for some of the older APIs inside Tapestry IoC).
     * 
     * @param interfaceType
     *            class proxy will extend from
     * @return transformation from which an instantiator may be created
     */
    public <T> PlasticClassTransformation<T> createProxyTransformation(Class interfaceType)
    {
        assert interfaceType != null;

        if (!interfaceType.isInterface())
            throw new IllegalArgumentException(String.format(
                    "Class %s is not an interface; proxies may only be created for interfaces.",
                    interfaceType.getName()));

        String name = String.format("$%s_%s", interfaceType.getSimpleName(), PlasticUtils.nextUID());

        PlasticClassTransformation<T> result = pool.createTransformation("java.lang.Object", name);

        result.getPlasticClass().introduceInterface(interfaceType);

        return result;
    }

    public void addPlasticClassListener(PlasticClassListener listener)
    {
        pool.addPlasticClassListener(listener);
    }

    public void removePlasticClassListener(PlasticClassListener listener)
    {
        pool.removePlasticClassListener(listener);
    }
}
