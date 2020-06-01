// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal;

import org.apache.tapestry5.beanmodel.PropertyConduit2;


/**
 * Extension to {@link org.apache.tapestry5.beanmodel.PropertyConduit2} that adds a method to determine the name of the property.
 * 
 * @since 5.2.0
 *
 */
public interface InternalPropertyConduit extends PropertyConduit2
{
    /**
     * Returns the name of the property read or updated by the conduit or null. 
     * If the expression points to a property on a bean (e.g. user.name) this method returns the last property in the chain. 
     * Otherwise this method returns {@code null}.
     * 
     * @return property name or {@code null}
     * 
     */
    String getPropertyName();
}
