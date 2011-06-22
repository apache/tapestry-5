// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * A cache for converting between class names and component (or other) classes. For component classes, ensures that the
 * class is the transformed class.
 */
public interface ComponentClassCache
{
    /**
     * Gets the Class instance for the given fully-qualified class name.
     * 
     * @param className
     *            fully qualified class name, or a primitive type name, or an array name (in source format)
     * @return the class instance
     */
    Class forName(String className);

    /**
     * Computes the default value for a field of the given type, returns the appropriate
     * default value. This is typically null, but may be false (for a primitive boolean) or some
     * version of 0 (for a primitive numeric field). Wrapper types will still be null.
     * 
     * @param className
     *            type of field
     * @since 5.2.0
     */
    Object defaultValueForType(String className);
}
