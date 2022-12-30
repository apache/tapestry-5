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

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.internal.structure.ComponentPageElement;


/**
 * Internal service that registers direct dependencies between components (including components, pages and
 * base classes). Even though methods receive {@link ComponentPageElement} parameters, dependencies
 * are tracked using their fully qualified classs names.
 *
 * @since 5.8.3
 */
public interface ComponentDependencyRegistry {
    
    /**
     * Register all the dependencies of a given component.
     */
    void register(ComponentPageElement componentPageElement);
    
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
     * Returns the fully qualified names of the direct dependencies of a given component.
     */
    Set<String> getDependencies(String className);
    
    /**
     * Signs up this registry to invalidation events from a given hub.
     */
    void listen(InvalidationEventHub invalidationEventHub);
    
}
