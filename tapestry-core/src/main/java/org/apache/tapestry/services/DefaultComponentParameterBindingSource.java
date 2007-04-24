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

package org.apache.tapestry.services;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;

/**
 * An assistant to components that wish to generate a binding matching the name of a property of
 * their container, should such a property exist.
 */
public interface DefaultComponentParameterBindingSource
{
    /**
     * Checks to see if the container of the component contains a property matching the component's
     * id. If so, a binding for that property is returned.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param componentResources
     *            the resources of the component for which a binding is needed
     * @return the binding, or null if the container does not have a matching property
     */
    Binding createDefaultBinding(String parameterName, ComponentResources componentResources);
}
