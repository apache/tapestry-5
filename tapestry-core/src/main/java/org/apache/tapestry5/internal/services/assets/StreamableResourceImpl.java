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

import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.ResponseCustomizer;
import org.apache.tapestry5.services.assets.StreamableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class StreamableResourceImpl implements StreamableResource
{
    protected final String description;

    private final ContentType contentType;

    protected final CompressionStatus compression;

    protected final long lastModified;

    protected final BytestreamCache bytestreamCache;

    protected final AssetChecksumGenerator assetChecksumGenerator;

    protected final ResponseCustomizer responseCustomizer;

    public StreamableResourceImpl(String description, ContentType contentType, CompressionStatus compression, long lastModified, BytestreamCache bytestreamCache, AssetChecksumGenerator assetChecksumGenerator, ResponseCustomizer responseCustomizer)
    {
        this.lastModified = lastModified;
        this.description = description;
        this.bytestreamCache = bytestreamCache;
        this.contentType = contentType;
        this.compression = compression;
        this.assetChecksumGenerator = assetChecksumGenerator;
        this.responseCustomizer = responseCustomizer;
    }

    public String getDescription()
    {
        return description;
    }

    public CompressionStatus getCompression()
    {
        return compression;
    }

    public ContentType getContentType()
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

    public String getChecksum() throws IOException
    {
        // Currently, we rely on AssetChecksumGenerator to manage a cache, but that may be better done
        // here (but must be threads-afe).
        return assetChecksumGenerator.generateChecksum(this);
    }

    @Override
    public StreamableResource addResponseCustomizer(final ResponseCustomizer customizer)
    {
        final ResponseCustomizer oldCustomizer = responseCustomizer;

        if (oldCustomizer == null)
        {
            return withNewResourceCustomizer(customizer);
        }

        return withNewResourceCustomizer(new ResponseCustomizer()
        {
            @Override
            public void customizeResponse(StreamableResource resource, Response response) throws IOException
            {
                oldCustomizer.customizeResponse(resource, response);
                customizer.customizeResponse(resource, response);
            }
        });
    }

    @Override
    public ResponseCustomizer getResponseCustomizer()
    {
        return responseCustomizer;
    }

    @Override
    public StreamableResource withContentType(ContentType newContentType)
    {
        return new StreamableResourceImpl(description, newContentType, compression, lastModified, bytestreamCache, assetChecksumGenerator, responseCustomizer);
    }

    private StreamableResourceImpl withNewResourceCustomizer(ResponseCustomizer customizer)
    {
        return new StreamableResourceImpl(description, contentType, compression, lastModified, bytestreamCache, assetChecksumGenerator, customizer);
    }

    @Override
    public int hashCode() 
    {
        return Objects.hash(bytestreamCache.size(), compression, contentType, description, lastModified);
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj) 
        {
            return true;
        }
        if (!(obj instanceof StreamableResourceImpl)) 
        {
            return false;
        }
        StreamableResourceImpl other = (StreamableResourceImpl) obj;
        return Objects.equals(bytestreamCache.size(), other.bytestreamCache.size()) && compression == other.compression && Objects.equals(contentType, other.contentType)
                && Objects.equals(description, other.description) && lastModified == other.lastModified;
    }
    
    
}
