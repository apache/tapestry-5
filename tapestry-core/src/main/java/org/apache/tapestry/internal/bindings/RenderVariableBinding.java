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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;

public class RenderVariableBinding extends AbstractBinding
{
    private final String _description;
    private final ComponentResources _resources;
    private final String _name;

    public RenderVariableBinding(String description, ComponentResources resources, String name, Location location)
    {
        super(location);

        _description = description;
        _resources = resources;
        _name = name;
    }

    @Override
    public void set(Object value)
    {
        _resources.storeRenderVariable(_name, value);
    }

    /**
     * Returns false, render variables are always variable.
     */
    @Override
    public boolean isInvariant()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("RenderVariable[%s %s]", _description, _name);
    }


    public Object get()
    {
        return _resources.getRenderVariable(_name);
    }

    /**
     * Always returns Object since we don't (statically) know the type of object.
     */
    @Override
    public Class getBindingType()
    {
        return Object.class;
    }
}
