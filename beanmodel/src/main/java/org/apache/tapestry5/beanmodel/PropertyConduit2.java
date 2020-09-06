// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;


/**
 * Extension to {@link PropertyConduit} that adds a method to access the generic property type.
 * {@link PropertyConduitSource} instances should ideally return PropertyConduit2 objects, not PropertyConduit.
 * This is only primarily of interest to <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/Binding2.html">Binding2</a>.
 * 
 * @since 5.4
 */
public interface PropertyConduit2 extends PropertyConduit
{
    /**
     * Returns the generic type of the property
     * 
     * @see Method#getGenericReturnType()
     * @see java.lang.reflect.Field#getGenericType()
     * 
     */
    Type getPropertyGenericType();
}
