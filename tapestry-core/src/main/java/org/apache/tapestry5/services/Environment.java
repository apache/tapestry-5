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

package org.apache.tapestry5.services;

import java.util.NoSuchElementException;

/**
 * Provides access to environment objects, which are almost always provided to enclosed components by enclosing
 * components. Environmental services are a form of very late binding.
 * <p/>
 * The Environment acts like a collection of stacks. Each stack contains environmental objects of a given type. Most
 * often, a stack has zero or one elements, but on occasion, a particular component will push an override onto the stack
 * for the benefit of the components it encloses.
 *
 * @see org.apache.tapestry5.annotations.Environmental
 * @see org.apache.tapestry5.services.EnvironmentalShadowBuilder
 */
public interface Environment
{
    /**
     * Peeks at the current top of the indicated stack.
     *
     * @param <T>  the type of environmental object
     * @param type class used to select the object
     * @return the current object of that type, or null if no service of that type has been added
     */
    <T> T peek(Class<T> type);

    /**
     * Peeks at the current top of the indicated stack (which must have a non-null value).
     *
     * @param <T>  the type of environmental object
     * @param type class used to select the object
     * @return the current object of the specified type
     * @throws RuntimeException if no service of that type has been added
     */
    <T> T peekRequired(Class<T> type);

    /**
     * Removes and returns the top environmental object of the selected type.
     *
     * @param <T>  the type of environmental object
     * @param type class used to select the object
     * @return the object just removed
     * @throws NoSuchElementException if the environmental stack (for the specified type) is empty
     */
    <T> T pop(Class<T> type);

    /**
     * Pushes a new service onto the stack. The old service at the top of the stack is returned (it may be null).
     *
     * @param <T>      the type of environmental object
     * @param type     class used to select the object
     * @param instance the service object
     * @return the previous top service
     */
    <T> T push(Class<T> type, T instance);

    /**
     * Clears all stacks; used when initializing the Environment before a render.
     */
    void clear();

    /**
     * For some type, returns a temporary access object for the type. The access object is efficient because it ties
     * directly to the thread's instance of the Environment service, it also caches the current value. <p>The access
     * object must be discarded at the end of the request (it will be unusable at that point anyway).
     *
     * @param type type of environmental object
     * @param <T>
     * @return access object that can be used to see the current environmental object of the type
     * @since 5.0.19
     */
    <T> EnvironmentalAccess<T> getAccess(Class<T> type);
}
