// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import javassist.CtClass;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.util.List;

/**
 * Extends {@link org.apache.tapestry5.services.ClassTransformation} with additional methods that may only be used
 * internally by Tapestry.
 */
public interface InternalClassTransformation extends ClassTransformation
{
    /**
     * Invoked after all {@link ComponentClassTransformWorker}s have had their chance to work over the class. This
     * performs any final operations for the class transformation, which includes coming up with the final constructor
     * method for the class.
     */
    void finish();

    /**
     * Called (after {@link #finish()}) to construct an instantiator for the component.
     *
     * @return the component's instantiator
     */
    Instantiator createInstantiator();

    /**
     * Returns a copy of the transformation's IdAllocator. Used when creating a child class transformation. May only be
     * invoked on a frozen transformation.
     */
    IdAllocator getIdAllocator();

    /**
     * Returns a copy of the list of constructor arguments for this class.
     */
    List<ConstructorArg> getConstructorArgs();

    /**
     * Searchs for an existing injection of an object, returning the name of the protected field into which the value
     * was injected.
     * <p/>
     * TODO: Howard sayz: Uggh! At least define a real key (MultiKey is intended for internal use, never part of an
     * API). Is this necessary?  The cost of re-injection is tiny.
     */
    String searchForPreviousInjection(InjectionKey key);

    InternalClassTransformation createChildTransformation(CtClass childClass, MutableComponentModel childModel);

    /**
     * Returns the parent transformation, or null for a root class.
     */
    InternalClassTransformation getParentTransformation();

    /**
     * Creates a new method by copying the body of an existing method.  This is part of the scheme for providing method
     * advice.
     *
     * @param sourceMethod  method to be copied
     * @param modifiers     modifiers for the new method
     * @param newMethodName name of new method to create
     */
    void copyMethod(TransformMethodSignature sourceMethod, int modifiers, String newMethodName);

    /**
     * Returns true if the provided signature is a method implemented by the transformed class.
     *
     * @param signature
     * @return true if implemented
     */
    boolean isMethod(TransformMethodSignature signature);
}
