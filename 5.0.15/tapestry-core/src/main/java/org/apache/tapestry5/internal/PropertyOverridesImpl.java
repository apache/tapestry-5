//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.ioc.Messages;

public class PropertyOverridesImpl implements PropertyOverrides
{
    private final ComponentResources resources;

    private final Messages messages;

    public PropertyOverridesImpl(ComponentResources resources)
    {
        this.resources = resources;

        messages = resources.getContainerMessages();
    }

    public Block getOverrideBlock(String name)
    {
        return resources.getBlockParameter(name);
    }

    public Messages getOverrideMessages()
    {
        return messages;
    }
}
