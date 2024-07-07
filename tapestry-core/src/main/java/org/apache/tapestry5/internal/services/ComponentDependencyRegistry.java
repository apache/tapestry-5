// Copyright 2022 The Apache Software Foundation
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

import java.util.Set;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;


/**
 * Internal service that registers direct dependencies between components (including components, pages and
 * base classes). Even though methods receive {@link ComponentPageElement} parameters, dependencies
 * are tracked using their fully qualified classs names.
 *
 * @since 5.8.3
 */
public interface ComponentDependencyRegistry {

    /**
     * Enum class defining the types of dependency components, pages and mixins can
     * have among them.
     */
    public static enum DependencyType
    {
        
        /**
         * Simple usage of components and mixins in components and pages
         */
        USAGE,
        
        /**
         * Superclass/subclass dependency.
         */
        SUPERCLASS,
        
        /**
         * Dependency by usage of the {@linkplain InjectPage} annotation.
         */
        INJECT_PAGE;
    }

    /**
     * Default file where the dependency information is stored between webapp runs.
     */
    String FILENAME = "tapestryComponentDependencies.json";

    /**
     * Register all the dependencies of a given class.
     */
    void register(Class<?> clasz);

    /**
     * Register all the dependencies of a given class and uses a given
     * classloader to load other classes if needed.
     * @since 5.8.7
     */
    void register(Class<?> clasz, ClassLoader classLoader);

    /**
     * Register all the dependencies of a given component.
     */
    void register(ComponentPageElement componentPageElement);
    
    /**
     * Register a dependency of a component class with another through annotations
     * such as {@link InjectPage}, {@link InjectComponent} and {@link Component}.
     */
    void register(PlasticField plasticField, MutableComponentModel componentModel);
    
    /**
     * Clears all dependency information for a given component.
     */
    void clear(String className);
    
    /**
     * Clears all dependency information for a given component.
     */
    void clear(ComponentPageElement componentPageElement);
    
    /**
     * Clears all dependency information.
     */
    void clear();

    /**
     * Returns the fully qualified names of the direct dependencies of a given component.
     */
    Set<String> getDependents(String className);
    
    /**
     * Returns the fully qualified names of the direct dependencies of a given component
     * and a given dependency type.
     * @see DependencyType
     */
    Set<String> getDependencies(String className, DependencyType type);
    
    /**
     * Returns all dependencies of a given class, direct and indirect.
     * @param className a class name.
     */
    Set<String> getAllNonPageDependencies(String className);
    
    /**
     * Signs up this registry to invalidation events from a given hub.
     */
    void listen(InvalidationEventHub invalidationEventHub);

    /**
     * Writes the current component dependency data to a file so it can be reused in a new run later.
     * @see #FILENAME
     */
    void writeFile();
    
    /**
     * Tells whether this registry already contans a given class name.
     */
    boolean contains(String className);
    
    /**
     * Returns the set of all class names in the registry.
     */
    Set<String> getClassNames();
    
    /**
     * Returns the set of all root classes (i.e. ones with no dependencies).
     */
    Set<String> getRootClasses();
    
    /**
     * Returns whether stored dependency information is present.
     */
    boolean isStoredDependencyInformationPresent();
    
    /**
     * Tells this service to ignore invalidations in this thread.
     */
    void disableInvalidations();
    
    /**
     * Tells this service to stop ignoring invalidations in this thread.
     */
    void enableInvalidations();

}
