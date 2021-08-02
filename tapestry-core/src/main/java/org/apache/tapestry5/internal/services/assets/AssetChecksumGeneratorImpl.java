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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.Adler32;

public class AssetChecksumGeneratorImpl implements AssetChecksumGenerator
{

    private final StreamableResourceSource streamableResourceSource;

    private final ResourceChangeTracker tracker;

    private final Map<Integer, String> cache = CollectionFactory.newConcurrentMap();

    public AssetChecksumGeneratorImpl(StreamableResourceSource streamableResourceSource, ResourceChangeTracker tracker)
    {
        this.streamableResourceSource = streamableResourceSource;
        this.tracker = tracker;

        tracker.clearOnInvalidation(cache);
    }

    public String generateChecksum(Resource resource) throws IOException
    {
        StreamableResource streamable = streamableResourceSource.getStreamableResource(resource, StreamableResourceProcessing.COMPRESSION_DISABLED,
                tracker);

        return generateChecksum(streamable);
    }

    public String generateChecksum(StreamableResource resource) throws IOException
    {
        return cache.computeIfAbsent(resource.hashCode(), 
                r -> {
                    try {
                        return toChecksum(resource.openStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private String toChecksum(InputStream is) throws IOException
    {
        // Adler32 is very fast and suitable for these purposes (MD5 and SHA are slower, and
        // are targetted at cryptographic solutions).
        Adler32 checksum = new Adler32();

        byte[] buffer = new byte[1024];

        try
        {
            while (true)
            {
                int length = is.read(buffer);

                if (length < 0)
                {
                    break;
                }

                checksum.update(buffer, 0, length);
            }

            // Reduces it down to just 32 bits which we express in hex.
            return Long.toHexString(checksum.getValue());
        } finally
        {
            is.close();
        }
    }
}
