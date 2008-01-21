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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Location;

public class BlockBinding extends AbstractBinding
{
    private final String _description;

    private final ComponentResources _component;

    private final String _blockId;

    public BlockBinding(final String description, final ComponentResources component,
                        final String blockId, Location location)
    {
        super(location);

        _description = description;
        _component = component;
        _blockId = blockId;
    }

    public Object get()
    {
        return _component.getBlock(_blockId);
    }

    @Override
    public String toString()
    {
        return String.format("BlockBinding[%s: %s]", _description, _blockId);
    }
}
