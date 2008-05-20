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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;

/**
 * Provides some form of injection when the value for an {@link org.apache.tapestry5.ioc.annotations.Inject} annotation is
 * present. In this case, the provider is responsible for determining the value to be injected from the field name and
 * field type.
 * <p/>
 * This interface will be used as part of a {@link org.apache.tapestry5.ioc.services.ChainBuilder chain of command}.
 */
public interface InjectionProvider
{
    /**
     * Peform the injection, if possible. Most often, this will result in a call to {@link
     * ClassTransformation#injectField(String, Object)}. The caller is responsible for invoking {@link
     * ClassTransformation#claimField(String, Object)}.
     *
     * @param fieldName      the name of the field requesting injection
     * @param fieldType      the type of the field
     * @param locator        allows services to be located
     * @param transformation allows the code for the class to be transformed
     * @param componentModel defines the relevant aspects of the component
     * @return true if an injection has been made (terminates the command chain), false to continue down the chain
     */
    boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
                             ClassTransformation transformation, MutableComponentModel componentModel);
}
