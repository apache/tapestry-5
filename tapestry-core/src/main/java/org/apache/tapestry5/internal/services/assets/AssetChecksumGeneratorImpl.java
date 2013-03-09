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

import org.apache.commons.codec.binary.Hex;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class AssetChecksumGeneratorImpl implements AssetChecksumGenerator
{
    private final static int BUFFER_SIZE = 4096;

    private final StreamableResourceSource streamableResourceSource;

    private final ResourceChangeTracker tracker;

    public AssetChecksumGeneratorImpl(StreamableResourceSource streamableResourceSource, ResourceChangeTracker tracker)
    {
        this.streamableResourceSource = streamableResourceSource;
        this.tracker = tracker;
    }

    public String generateChecksum(Resource resource) throws IOException
    {
        // TODO: Caching, and cache invalidation.

        StreamableResource streamable = streamableResourceSource.getStreamableResource(resource, StreamableResourceProcessing.COMPRESSION_DISABLED,
                tracker);

        return generateChecksum(streamable);
    }

    @Override
    public String generateChecksum(StreamableResource resource) throws IOException
    {
        return toChecksum(resource.openStream());
    }

    private String toChecksum(InputStream is) throws IOException
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            digestStream(digest, is);

            is.close();

            byte[] bytes = digest.digest();
            char[] encoded = Hex.encodeHex(bytes);

            return new String(encoded);
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        } finally
        {
            InternalUtils.close(is);
        }
    }

    private void digestStream(MessageDigest digest, InputStream stream) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];

        while (true)
        {
            int length = stream.read(buffer);

            if (length < 0)
            {
                return;
            }

            digest.update(buffer, 0, length);
        }
    }
}
