// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import javassist.CtClass;

/**
 * Used when generating new classes on the fly.
 *
 * @see org.apache.tapestry5.ioc.services.ClassFactory
 */
public interface CtClassSource
{
    /**
     * Returns the number of classes created.
     */
    int getCreatedClassCount();

    /**
     * Converts an existing class to a CtClass instance.
     */
    CtClass toCtClass(Class searchClass);

    /**
     * Converts a class name to a CtClass instance.
     */
    CtClass toCtClass(String name);

    /**
     * Createa a new CtClass instance.
     */
    CtClass newClass(String name, Class superClass);

    /**
     * Used after constructing the CtClass fully, to convert it into a Class ready to be instantiated.
     */
    Class createClass(CtClass ctClass);
}
