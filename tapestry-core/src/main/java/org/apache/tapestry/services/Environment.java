// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.util.NoSuchElementException;

/**
 * Provides access to environment services, which are almost always provided to enclosed components by enclosing
 * components. Environmental services are a form of very late binding.
 * <p/>
 * The Environment acts like a collection of stacks. Each stack contains environmental service instances of a given
 * type. Most often, a stack has zero or one elements, but on occasion, a particular component will push an override
 * onto the stack for the benefit of the components it encloses.
 *
 * @see org.apache.tapestry.annotations.Environmental
 * @see org.apache.tapestry.services.EnvironmentalShadowBuilder
 */
public interface Environment
{
    /**
     * Peeks at the current top of the indicated stack.
     *
     * @param <T>  the type of environmental service
     * @param type class used to select a service
     * @return the current service of that type, or null if no service of that type has been added
     */
    <T> T peek(Class<T> type);

    /**
     * Peeks at the current top of the indicated stack (which must have a non-null value).
     *
     * @param <T>  the type of environmental service
     * @param type class used to select a service
     * @return the current service
     * @throws RuntimeException if no service of that type has been added
     */
    <T> T peekRequired(Class<T> type);

    /**
     * Removes and returns the top environmental service of the selected type.
     *
     * @param <T>
     * @param type
     * @return
     * @throws NoSuchElementException if the environmental stack (for the specified type) is empty
     */
    <T> T pop(Class<T> type);

    /**
     * Pushes a new service onto the stack. The old service at the top of the stack is returned (it may be null).
     *
     * @param <T>
     * @param type     the type of service to store
     * @param instance the service instance
     * @return the previous top service
     */
    <T> T push(Class<T> type, T instance);

    /**
     * Clears all stacks; used when initializing the Environment before a render.
     */
    void clear();
}
