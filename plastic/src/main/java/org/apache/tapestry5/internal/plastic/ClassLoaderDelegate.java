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

/**
 * Externalizes the logic for defining which classes will be loaded (and possibly transformed) by the class loader, and
 * which will be loaded by the parent class loader.
 */
public interface ClassLoaderDelegate
{
    /**
     * Identifies which classes are to be loaded.
     * 
     * @param className
     *            fully qualified class name
     * @return true if the class should be intercepted, false to let parent class loader load class
     */
    boolean shouldInterceptClassLoading(String className);

    /**
     * Load the class, transforming it as necessary.
     * 
     * @param className
     *            binary class name
     * @return loaded and (if not an inner class) transformed class
     * @throws ClassNotFoundException
     */
    Class<?> loadAndTransformClass(String className) throws ClassNotFoundException;
}
