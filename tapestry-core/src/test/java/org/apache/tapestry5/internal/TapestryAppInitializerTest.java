// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.util.Transformer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class TapestryAppInitializerTest extends Assert
{
    @SuppressWarnings("unchecked")
    @Test
    public void testLoadAppModule()
    {
        Registry registry = new TapestryAppInitializer("org.apache.tapestry5.integration.app0",
                                                       "foo", "").getRegistry();

        Transformer<String> s1 = registry.getService("Service1", Transformer.class);
        assertEquals(s1.transform("a"), "A");
    }

    @Test
    public void testNoAppModule()
    {
        // Apparently just checking to see that it doesn't fail.

        new TapestryAppInitializer("non_existing.package", "foo", "").getRegistry();
    }

}
