// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.util.MultiKey;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;

import java.util.List;

/**
 * Extends {@link org.apache.tapestry.services.ClassTransformation} with additional methods that may
 * only be used internally by Tapestry.
 */
public interface InternalClassTransformation extends ClassTransformation
{
    /**
     * Invoked after all {@link ComponentClassTransformWorker}s have had their chance to work over
     * the class. This performs any final operations for the class transformation, which includes
     * coming up with the final constructor method for the class.
     */
    void finish();

    /**
     * Called (after {@link #finish()}) to construct an instantiator for the component.
     *
     * @param componentClass the class to be instantiated
     * @return the component's instantiator
     */
    Instantiator createInstantiator(Class componentClass);

    /**
     * Returns a copy of the transformation's IdAllocator. Used when creating a child class
     * transformation. May only be invoked on a frozen transformation.
     */
    IdAllocator getIdAllocator();

    /**
     * Returns a copy of the list of constructor arguments for this class.
     */
    List<ConstructorArg> getConstructorArgs();

    /**
     * Searchs for an existing injection of an object, returning the name of the protected field
     * into which the value was injected.
     */
    String searchForPreviousInjection(MultiKey key);
}
