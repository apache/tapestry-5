// Copyright 2011 The Apache Software Foundation
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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;

public class StreamableResourceImpl implements StreamableResource
{
    private final String contentType;

    private final CompressionStatus compression;

    private final BytestreamCache bytestreamCache;

    public StreamableResourceImpl(String contentType, CompressionStatus compression, BytestreamCache bytestreamCache)
    {
        this.contentType = contentType;
        this.compression = compression;
        this.bytestreamCache = bytestreamCache;
    }

    public CompressionStatus getCompression()
    {
        return compression;
    }

    public String getContentType()
    {
        return contentType;
    }

    public int getSize()
    {
        return bytestreamCache.size();
    }

    public void streamTo(OutputStream os) throws IOException
    {
        bytestreamCache.writeTo(os);
    }

    @Override
    public String toString()
    {
        return String.format("StreamableResource<%s %s size: %d>", contentType, compression.name(), getSize());
    }
}
