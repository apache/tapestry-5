// Copyright 2009-2013 The Apache Software Foundation
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

import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;

public class ContextResourceSymbolProviderTest extends InternalBaseTestCase
{
    private static final String CONTENT = "homer=simpson\r\nmonty=burns";

    @Test
    public void access() throws Exception
    {
        File f = File.createTempFile("foo", ".properties");

        setupFile(f);

        Context context = mockContext();

        expect(context.getRealFile("/bar/" + f.getName())).andReturn(f);

        replay();

        ContextResourceSymbolProvider provider = new ContextResourceSymbolProvider(context, "bar/" + f.getName());

        /* test general access */
        assertEquals(provider.valueForSymbol("homer"), "simpson");
        assertEquals(provider.valueForSymbol("monty"), "burns");

        /* check for case-insensitivity */
        assertEquals(provider.valueForSymbol("HOMER"), "simpson");

        /* non-existent keys should return null */
        assertNull(provider.valueForSymbol("marge"));

        verify();

        f.delete();
    }

    private void setupFile(File f) throws Exception
    {
        FileOutputStream fos = new FileOutputStream(f);

        fos.write(CONTENT.getBytes());

        fos.close();
    }
}
