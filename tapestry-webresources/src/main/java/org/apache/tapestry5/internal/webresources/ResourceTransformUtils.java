// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.webresources;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;

import org.apache.tapestry5.commons.Resource;

/**
 * @since 5.4
 */
public class ResourceTransformUtils
{
    private static final double NANOS_TO_MILLIS = 1.0d / 1000000.0d;

    public static double nanosToMillis(long nanos)
    {
        return ((double) nanos) * NANOS_TO_MILLIS;
    }

    public static long toChecksum(Resource resource)
    {
        Adler32 checksum = new Adler32();

        byte[] buffer = new byte[1024];

        InputStream is = null;

        try
        {
            is = resource.openStream();

            while (true)
            {
                int length = is.read(buffer);

                if (length < 0)
                {
                    break;
                }

                checksum.update(buffer, 0, length);
            }

            is.close();

            // Reduces it down to just 32 bits which we express in hex.'
            return checksum.getValue();
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
