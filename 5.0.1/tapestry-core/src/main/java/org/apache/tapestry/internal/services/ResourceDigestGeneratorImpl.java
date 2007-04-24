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

package org.apache.tapestry.internal.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.services.ResourceDigestGenerator;

/**
 * Implementation of {@link ResourceDigestGenerator} that generates MD5 digests.
 * Available as service:tapestry.ResourceDigestGenerator or
 * infrastructure:ResourceDigestGenerator.
 */
public class ResourceDigestGeneratorImpl implements ResourceDigestGenerator
{
    private static final int BUFFER_SIZE = 5000;

    public String generateDigest(URL url)
    {
        InputStream stream = null;

        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            stream = new BufferedInputStream(url.openStream());

            digestStream(digest, stream);

            stream.close();
            stream = null;

            byte[] bytes = digest.digest();
            char[] encoded = Hex.encodeHex(bytes);

            return new String(encoded);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            TapestryUtils.close(stream);
        }
    }

    private void digestStream(MessageDigest digest, InputStream stream) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true)
        {
            int length = stream.read(buffer);

            if (length < 0)
                return;

            digest.update(buffer, 0, length);
        }
    }

    /** Current implementation: any path that ends with ".class", but this will expand in the future. */
    public boolean requiresDigest(String path)
    {
        return path.endsWith(".class");
    }

}
