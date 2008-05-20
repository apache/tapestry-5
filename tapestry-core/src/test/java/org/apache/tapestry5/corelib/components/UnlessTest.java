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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class UnlessTest extends InternalBaseTestCase
{
    @Test
    public void false_test_renders_body()
    {
        Unless component = new Unless();

        component.setup(false, null);

        assertNull(component.beginRender());
        assertTrue(component.beforeRenderBody());
    }

    @Test
    public void true_test_renders_else_block()
    {
        Block block = mockBlock();

        replay();

        Unless component = new Unless();

        component.setup(true, block);

        assertSame(component.beginRender(), block);
        assertFalse(component.beforeRenderBody());

        verify();
    }
}
