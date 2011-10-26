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

import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamableResourceImpl implements StreamableResource
{
    private final String description;

    private final String contentType;

    private final CompressionStatus compression;

    private final long lastModified;

    private final BytestreamCache bytestreamCache;

    public StreamableResourceImpl(String description, String contentType, CompressionStatus compression, long lastModified,
                                  BytestreamCache bytestreamCache)
    {
        this.description = description;
        this.contentType = contentType;
        this.compression = compression;
        this.lastModified = lastModified;
        this.bytestreamCache = bytestreamCache;
    }

    public String getDescription()
    {
        return description;
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

    public long getLastModified()
    {
        return lastModified;
    }

    public void streamTo(OutputStream os) throws IOException
    {
        bytestreamCache.writeTo(os);
    }

    public InputStream openStream() throws IOException
    {
        return bytestreamCache.openStream();
    }

    @Override
    public String toString()
    {
        return String.format("StreamableResource<%s %s %s lastModified: %tc size: %d>", contentType, description, compression.name(),
                lastModified, getSize());
    }
}
