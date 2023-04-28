// Copyright 2023 The Apache Software Foundation
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
package org.apache.tapestry5.services.pageload;

import java.util.Set;
import java.util.function.Function;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Service that creates {@linkplain PageClassloaderContext} instances (except the root one)
 * when a class in a controlled page is first used in the Tapestry page pool. Existing 
 * contexts may be reused for a given class, specially when in production mode.
 * 
 * @see ComponentInstantiatorSource
 * @see DynamicDependenciesDefinition
 * @since 5.8.3
 */
public interface PageClassloaderContextManager 
{

    /**
     * Processes a class, given its class name and the root context.
     * @param className the class fully qualified name.
     * @param root the root {@link PageClassloaderContext}.
     * @param plasticProxyFactoryProvider a function that receives a 
     * {@linkplain} ClassLoader} and returns a new {@linkplain PlasticProxyFactory}.
     * @return the {@link PageClassloaderContext} associated with that class.
     */
    PageClassloaderContext get(String className);
    
    /**
     * Invalidates page classloader contexts and returns a set containing the names
     * of all classes that should be invalidated.
     */
    Set<String> invalidate(PageClassloaderContext... contexts);
    
    /**
     * Invalidates page classloader contexts and invalidates the classes in the context as well.
     */
    void invalidateAndFireInvalidationEvents(PageClassloaderContext... contexts);
    
    /**
     * Returns the root context.
     */
    PageClassloaderContext getRoot();
    
    /**
     * Clears any state held by this manager.
     */
    void clear();
    
    /**
     * Returns whether contexts are being merged.
     */
    boolean isMerging();

    /**
     * Removes one specific class from this manager, invalidating the context where
     * it is.
     */
    void clear(String className);

    /**
     * Initializes this service with the root context and a Plastic proxy factory provider.
     * Method can only be called once. None of the parameters may be null.
     */
    void initialize(PageClassloaderContext root, Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider);
    
    /**
     * Returns the Class instance appropriate for a given component given a page name.
     * @param clasz the class instance.
     * @param pageName the page name.
     * @return a Class instance.
     */
    Class<?> getClassInstance(Class<?> clasz, String pageName);
    
}
