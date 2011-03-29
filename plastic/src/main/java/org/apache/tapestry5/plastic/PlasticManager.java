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

import java.util.Collections;
import java.util.Set;

import org.apache.tapestry5.internal.plastic.NoopDelegate;
import org.apache.tapestry5.internal.plastic.PlasticClassPool;
import org.apache.tapestry5.internal.plastic.PlasticClassTransformation;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;

/**
 * Manages the internal class loaders and other logics necessary to load and transform existing classes,
 * or to create new classes dynamically at runtime.
 */
public class PlasticManager
{
    private final PlasticClassPool pool;

    /**
     * Creates a PlasticManager using the Thread's contextClassLoader as the parent class loader. No classes will
     * be automatically transformed, but instead {@link #createClass(Class, CreateClassCallback)} can be used to create
     * entirely new classes.
     */
    public PlasticManager()
    {
        this(Thread.currentThread().getContextClassLoader(), new NoopDelegate(), Collections.<String> emptySet());
    }

    /**
     * The standard constructor for PlasticManager, allowing a parent class loader to be specified (this is most often
     * the thread's contextClassLoader), as well as the delegate and the names of all controlled packages.
     * 
     * @param parentClassLoader
     *            main source for (untransformed) classes
     * @param delegate
     *            performs transformations on top-level classes from controlled packages
     * @param controlledPackageName
     *            defines the packages that are to be transformed; top-classes in these packages
     *            (or sub-packages) will be passed to the delegate for transformation
     */
    public PlasticManager(ClassLoader parentClassLoader, PlasticManagerDelegate delegate,
            Set<String> controlledPackageName)
    {
        assert parentClassLoader != null;
        assert delegate != null;

        pool = new PlasticClassPool(parentClassLoader, delegate, controlledPackageName);
    }

    /**
     * Returns the ClassLoader that is used to instantiate transformed classes. The parent class loader
     * of the returned class loader is the class loader provided to
     * {@link #PlasticManager(ClassLoader, PlasticManagerDelegate, Set)}
     * 
     * @return class loader
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
    PlasticClassTransformation getPlasticClass(String className) throws ClassNotFoundException
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
    public ClassInstantiator getClassInstantiator(String className)
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
    public ClassInstantiator createClass(Class baseClass, PlasticClassTransformer callback)
    {
        assert baseClass != null;
        assert callback != null;

        if (baseClass.isInterface())
            throw new IllegalArgumentException(String.format("Class %s defines an interface, not a base class.",
                    baseClass.getName()));

        String name = String.format("$PlasticProxy$%s_%s", baseClass.getSimpleName(), PlasticUtils.nextUID());

        PlasticClassTransformation transformation = pool.createTransformation(baseClass.getName(), name);

        callback.transform(transformation.getPlasticClass());

        return transformation.createInstantiator();
    }
}
