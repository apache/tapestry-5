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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;

public class ComponentBinding extends AbstractBinding
{
    private final String _description;

    private final ComponentResources _resources;

    private final String _componentId;

    public ComponentBinding(String description, ComponentResources resources, String componentId,
                            Location location)
    {
        super(location);

        _description = description;
        _resources = resources;
        _componentId = componentId;
    }

    public Object get()
    {
        return _resources.getEmbeddedComponent(_componentId);
    }

    @Override
    public String toString()
    {
        return String.format("ComponentResources[%s %s]", _description, _componentId);
    }
}
