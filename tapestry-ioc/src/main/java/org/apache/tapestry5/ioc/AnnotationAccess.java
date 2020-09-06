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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.commons.AnnotationProvider;

/**
 * Introduced for Tapestry 5.3, contains new methods to provide access to annotations on the class,
 * and on methods of the class. In rare cases, the same annotation type will appear on the service interface
 * and on the class (or method implementation in the class); the implementation annotation always
 * has precedence over the interface annotation.
 * 
 * @since 5.3
 */
public interface AnnotationAccess
{
    /**
     * Returns a provider for annotations on the service class and interface. This will reflect annotations defined by
     * the implementation class itself, plus annotations defined by the service interface (implementation class
     * annotations take precedence).
     * @return an AnnotationProvider instance.
     */
    AnnotationProvider getClassAnnotationProvider();

    /**
     * Returns a provider for annotations of a method of the class. This includes annotations on
     * the implementation method, plus annotations on the corresponding service interface method
     * (if such a method exists), with precedence on the implementation class method annotations.
     * @param methodName the name of the method.
     * @param parameterTypes the types of the parameters of the method.
     * @return an AnnotationProvider instance.     * 
     */
    AnnotationProvider getMethodAnnotationProvider(String methodName, Class... parameterTypes);

}
