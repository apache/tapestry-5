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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Messages;
import org.testng.annotations.Test;

public class PropertyOverridesImplTest extends InternalBaseTestCase
{
    @Test
    public void block_found()
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockInternalComponentResources();
        Block block = mockBlock();

        String name = "alfred";

        train_getContainerMessages(resources, messages);

        train_getBlockParameter(resources, name, block);

        replay();

        PropertyOverrides po = new PropertyOverridesImpl(resources);

        assertSame(po.getOverrideBlock(name), block);

        verify();
    }

    @Test
    public void block_not_found()
    {
        Messages messages = mockMessages();
        ComponentResources resources = mockInternalComponentResources();

        String name = "alfred";

        train_getContainerMessages(resources, messages);

        train_getBlockParameter(resources, name, null);

        replay();

        PropertyOverrides po = new PropertyOverridesImpl(resources);

        assertNull(po.getOverrideBlock(name));

        verify();
    }
}
