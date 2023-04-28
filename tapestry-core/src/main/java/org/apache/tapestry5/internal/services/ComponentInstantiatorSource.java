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

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.services.transform.ControlledPackageType;

/**
 * Creates {@link org.apache.tapestry5.internal.services.Instantiator}s for components, based on component class name.
 * This will involve transforming the component's class before it is loaded.
 *
 * In addition, a source acts as an event hub for {@link org.apache.tapestry5.commons.services.InvalidationListener}s, so that
 * any information derived from loaded classes can be discarded and rebuilt when classes change.
 *
 * The strategy used is that when <em>any</em> class (in a controlled package) changes, the entire class loader is
 * discarded, along with any instances derived from those classes. A new class loader is created, and then invalidation
 * events are fired to listeners.
 *
 * Starting in Tapestry 5.3, the packages that are loaded are controlled by a configuration that maps package names to
 * {@link ControlledPackageType}s.
 */
@UsesMappedConfiguration(key = String.class, value = ControlledPackageType.class)
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
     * Checks to see if a fully qualified class name exists. This method appears to exist only for testing.
     *
     * @param className name of class to check
     * @return true if the class exists (there's a ".class" file), false otherwise
     */
    boolean exists(String className);

    /**
     * Returns a proxy factory that can be used to generate additional classes around enhanced classes, or create
     * subclasses of enhanced classes.
     *
     * @since 5.3
     */
    PlasticProxyFactory getProxyFactory();

    /**
     * Forces invalidation logic, as if a component class on the disk had changed, forcing a reload
     * of all pages and components.
     *
     * @since 5.3
     */
    void forceComponentInvalidation();
}
