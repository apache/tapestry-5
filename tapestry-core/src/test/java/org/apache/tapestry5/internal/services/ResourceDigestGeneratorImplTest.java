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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class ResourceDigestGeneratorImplTest extends InternalBaseTestCase
{
    @Test
    public void typical_file() throws Exception
    {
        URL url = getClass().getResource("ResourceDigestGeneratorImplTest.class");

        ResourceDigestGenerator g = new ResourceDigestGeneratorImpl(Arrays.asList("class"));

        String digest = g.generateDigest(url);

        assertTrue(digest.length() > 0);

        String checksum2 = g.generateDigest(url);

        assertEquals(checksum2, digest);
    }

    @Test
    public void digest_changes_with_changes_to_file() throws Exception
    {
        File file = File.createTempFile("digest.", ".dat");

        URL url = file.toURL();

        ResourceDigestGenerator g = new ResourceDigestGeneratorImpl(Arrays.asList("class"));

        String prevDigest = g.generateDigest(url);

        for (int i = 0; i < 5; i++)
        {
            writeJunkTofile(file);

            String digest = g.generateDigest(url);

            assertFalse(digest.equals(prevDigest));

            prevDigest = digest;

        }
    }

    private void writeJunkTofile(File file) throws IOException
    {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(file, true));

        OutputStreamWriter writer = new OutputStreamWriter(os);

        for (int i = 0; i < 1000; i++)
            writer.write("All work and no play makes Jack a dull boy. ");

        writer.close();
    }
}
