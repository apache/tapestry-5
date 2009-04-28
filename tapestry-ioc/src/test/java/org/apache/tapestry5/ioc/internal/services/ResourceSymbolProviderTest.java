// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ResourceSymbolProviderTest extends IOCTestCase
{
    private static final String CONTENT = "homer=simpson\r\nmonty=burns";

    @Test
    public void access() throws Exception
    {
        Resource resource = mockResource();

        InputStream is = new ByteArrayInputStream(CONTENT.getBytes());

        expect(resource.openStream()).andReturn(is);

        replay();

        ResourceSymbolProvider provider = new ResourceSymbolProvider(resource);

        /* test general access */
        assertEquals(provider.valueForSymbol("homer"), "simpson");
        assertEquals(provider.valueForSymbol("monty"), "burns");

        /* check for case-insensitivity */
        assertEquals(provider.valueForSymbol("HOMER"), "simpson");

        /* non-existent keys should return null */
        assertNull(provider.valueForSymbol("marge"));

        verify();
    }
}
