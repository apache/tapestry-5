// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.services.BindingFactory;

/** The "component:" binding prefix, which allows access to a child component via its id. */
public class ComponentBindingFactory implements BindingFactory
{
    public Binding newBinding(String description, ComponentResources container, ComponentResources component,
            String expression, Location location)
    {
        return new ComponentBinding(description, container, expression, location);
    }

}
