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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.CompressionAnalyzer;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.ContentTypeAnalyzer;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

public class StreamableResourceSourceImpl implements StreamableResourceSource
{
    private final Map<String, ResourceTransformer> configuration;

    private final ContentTypeAnalyzer contentTypeAnalyzer;

    private final CompressionAnalyzer compressionAnalyzer;

    private final ResourceChangeTracker resourceChangeTracker;

    public StreamableResourceSourceImpl(Map<String, ResourceTransformer> configuration,
            ContentTypeAnalyzer contentTypeAnalyzer, CompressionAnalyzer compressionAnalyzer,
            ResourceChangeTracker resourceChangeTracker)
    {
        this.configuration = configuration;
        this.contentTypeAnalyzer = contentTypeAnalyzer;
        this.compressionAnalyzer = compressionAnalyzer;
        this.resourceChangeTracker = resourceChangeTracker;
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing)
            throws IOException
    {
        assert baseResource != null;

        URL url = baseResource.toURL();

        if (url == null)
            throw new IOException(String.format("Resource %s does not exist.", baseResource));

        String fileSuffix = TapestryInternalUtils.toFileSuffix(baseResource.getFile());

        // Optionally, transform the resource. The main driver for this is to allow
        // for libraries like LessJS (http://lesscss.org/) or
        // http://jashkenas.github.com/coffee-script/
        ResourceTransformer rt = configuration.get(fileSuffix);

        InputStream buffered = new BufferedInputStream(url.openStream());

        InputStream transformed = rt == null ? buffered : rt.transform(buffered);

        BytestreamCache bytestreamCache = readStream(transformed);

        transformed.close();
        buffered.close();

        String contentType = contentTypeAnalyzer.getContentType(baseResource);

        boolean compressable = compressionAnalyzer.isCompressable(contentType);

        long lastModified = resourceChangeTracker.trackResource(baseResource);

        return new StreamableResourceImpl(contentType, compressable ? CompressionStatus.COMPRESSABLE
                : CompressionStatus.NOT_COMPRESSABLE, lastModified, bytestreamCache);
    }

    private BytestreamCache readStream(InputStream stream) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        TapestryInternalUtils.copy(stream, bos);

        stream.close();

        return new BytestreamCache(bos);
    }

}
