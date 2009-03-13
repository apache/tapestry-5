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

package org.apache.tapestry5.internal.services;

import javassist.CtClass;

/**
 * Encapsulates all the work performed by the {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSource}
 * when it loads and transforms a class.
 */
public interface ComponentClassTransformer
{
    /**
     * Performs a transformation on the class, accessing the class from the class pool.
     *
     * @param ctClass     compile time class to be transformed
     * @param classLoader class loader used to resolve references to other classes (both transformed and not)
     */
    void transformComponentClass(CtClass ctClass, ClassLoader classLoader);

    /**
     * Creates a new instantiator instance.
     *
     * @param componentClassName fully qualified name of component class to instantiate
     */
    Instantiator createInstantiator(String componentClassName);
}
