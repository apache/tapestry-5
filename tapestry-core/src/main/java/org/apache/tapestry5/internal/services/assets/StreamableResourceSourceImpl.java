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
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.services.CompressionAnalyzer;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.services.assets.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class StreamableResourceSourceImpl implements StreamableResourceSource
{
    private final Map<String, ResourceTransformer> configuration;

    private final ContentTypeAnalyzer contentTypeAnalyzer;

    private final CompressionAnalyzer compressionAnalyzer;

    private final ResourceChangeTracker resourceChangeTracker;

    private final AssetChecksumGenerator checksumGenerator;

    public StreamableResourceSourceImpl(Map<String, ResourceTransformer> configuration,
                                        ContentTypeAnalyzer contentTypeAnalyzer, CompressionAnalyzer compressionAnalyzer,
                                        ResourceChangeTracker resourceChangeTracker, AssetChecksumGenerator checksumGenerator)
    {
        this.configuration = configuration;
        this.contentTypeAnalyzer = contentTypeAnalyzer;
        this.compressionAnalyzer = compressionAnalyzer;
        this.resourceChangeTracker = resourceChangeTracker;
        this.checksumGenerator = checksumGenerator;
    }

    public Set<String> fileExtensionsForContentType(ContentType contentType)
    {
        Set<String> result = CollectionFactory.newSet();

        for (Map.Entry<String, ResourceTransformer> me : configuration.entrySet())
        {
            if (me.getValue().getTransformedContentType().equals(contentType))
            {
                result.add(me.getKey());
            }
        }

        return result;
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies)
            throws IOException
    {
        assert baseResource != null;

        if (!baseResource.exists())
        {
            throw new IOException(String.format("Resource %s does not exist.", baseResource));
        }

        String fileSuffix = TapestryInternalUtils.toFileSuffix(baseResource.getFile());

        // Optionally, transform the resource. The main driver for this is to allow
        // for libraries like LessJS (http://lesscss.org/) or
        // http://jashkenas.github.com/coffee-script/
        ResourceTransformer rt = configuration.get(fileSuffix);

        InputStream transformed = rt == null ? baseResource.openStream() : rt.transform(baseResource, dependencies);

        assert transformed != null;

        BytestreamCache bytestreamCache = readStream(transformed);

        transformed.close();

        ContentType contentType = rt == null
                ? new ContentType(contentTypeAnalyzer.getContentType(baseResource))
                : rt.getTransformedContentType();

        boolean compressable = compressionAnalyzer.isCompressable(contentType.getMimeType());

        long lastModified = resourceChangeTracker.trackResource(baseResource);

        return new StreamableResourceImpl(baseResource.toString(), contentType, compressable ? CompressionStatus.COMPRESSABLE
                : CompressionStatus.NOT_COMPRESSABLE, lastModified, bytestreamCache, checksumGenerator, null);
    }

    private BytestreamCache readStream(InputStream stream) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        TapestryInternalUtils.copy(stream, bos);

        stream.close();

        return new BytestreamCache(bos);
    }

}
