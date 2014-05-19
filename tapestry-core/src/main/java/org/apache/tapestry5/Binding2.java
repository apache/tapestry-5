// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.tapestry5.services.BindingFactory;

/**
 * Extension to {@link Binding} that adds a method to access the generic property type.
 * {@link BindingFactory} instances should ideally return Binding2 objects, not Binding.
 * This is only primarily of interest to {@link ComponentResources}.
 * 
 * @since 5.4
 */
public interface Binding2 extends Binding
{
    /**
     * Returns the generic type of the binding
     * 
     * @see Method#getGenericReturnType()
     * @see java.lang.reflect.Field#getGenericType()
     */
    Type getBindingGenericType();
}
