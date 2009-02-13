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

import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.services.InvalidationEventHub;

/**
 * Creates {@link org.apache.tapestry5.internal.services.Instantiator}s for components, based on component class name.
 * This will involve transforming the component's class before it is loaded.
 * <p/>
 * In addition, a source acts as an event hub for {@link org.apache.tapestry5.services.InvalidationListener}s, so that
 * any information derived from loaded classes can be discarded and rebuilt when classes change.
 * <p/>
 * The strategy used is that when <em>any</em> class (in a controlled package) changes, the entire class loader is
 * discarded, along with any instances derived from those classes. A new class loader is created, and then invalidation
 * events are fired to listeners.
 */
public interface ComponentInstantiatorSource
{

    /**
     * Given the name of a component class, provides an instantiator for that component. Instantiators are cached, so
     * repeated calls to this method with the same class name will return the same instance; however, callers should
     * also be aware that the instantiators may lose validity after an invalidation (caused by changes to external Java
     * class files).
     *
     * @param classname FQCN to find (and perhaps transform and load)
     * @return an object which can instantiate an instance of the component
     */
    Instantiator getInstantiator(String classname);

    /**
     * Adds a controlled package. Only classes within controlled packages are subject to transformation.
     *
     * @param packageName the package name to add (must not be blank)
     */
    void addPackage(String packageName);

    /**
     * Checks to see if a fully qualfied class name exists. This method appears to exist only for testing.
     *
     * @param className name of class to check
     * @return true if the class exists (there's a ".class" file), false otherwise
     */
    boolean exists(String className);

    /**
     * Returns a class factory that can be used to generate additional classes around enhanced classes, or create
     * subclasses of enhanced classes.
     */
    ClassFactory getClassFactory();

    /**
     * Returns a class source used when creating new classes dynamically.
     */
    CtClassSource getClassSource();

    /**
     * Invalidation event hub used to notify listeners that component classes have changed.
     *
     * @see org.apache.tapestry5.services.ComponentClasses
     * @since 5.1.0.0
     */
    InvalidationEventHub getInvalidationEventHub();
}
