// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Location;

public class ComponentBinding extends AbstractBinding
{
    private final String description;

    private final ComponentResources resources;

    private final String componentId;

    public ComponentBinding(Location location, String description, ComponentResources resources, String componentId
    )
    {
        super(location);

        this.description = description;
        this.resources = resources;
        this.componentId = componentId;
    }

    public Object get()
    {
        return resources.getEmbeddedComponent(componentId);
    }

    @Override
    public String toString()
    {
        return String.format("ComponentResources[%s %s]", description, componentId);
    }
}
