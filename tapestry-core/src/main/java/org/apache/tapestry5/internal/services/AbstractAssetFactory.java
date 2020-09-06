// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.io.IOException;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

public abstract class AbstractAssetFactory implements AssetFactory
{
    private final AssetPathConstructor assetPathConstructor;

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final StreamableResourceSource streamableResourceSource;

    private final ResourceChangeTracker resourceChangeTracker;

    private final Resource rootResource;

    public AbstractAssetFactory(ResponseCompressionAnalyzer compressionAnalyzer,
                                ResourceChangeTracker resourceChangeTracker,
                                StreamableResourceSource streamableResourceSource,
                                AssetPathConstructor assetPathConstructor,
                                Resource rootResource)
    {
        this.compressionAnalyzer = compressionAnalyzer;
        this.resourceChangeTracker = resourceChangeTracker;
        this.streamableResourceSource = streamableResourceSource;
        this.assetPathConstructor = assetPathConstructor;
        this.rootResource = rootResource;
    }

    protected boolean isCompressable(StreamableResource resource)
    {
        return compressionAnalyzer.isGZipEnabled(resource.getContentType());
    }

    /**
     * Returns the root {@link ContextResource}.
     */
    public Resource getRootResource()
    {
        return rootResource;
    }

    protected Asset createAsset(final Resource resource, final String folder, final String resourcePath)
    {
        assert resource != null;
        assert InternalUtils.isNonBlank(folder);
        assert InternalUtils.isNonBlank(resourcePath);

        return new AbstractAsset(false)
        {
            public String toClientURL()
            {
                // TODO: Some caching to ensure this is fast!  Fortunately, the SRS pipeline includes caching already,
                // but the results of assetPathConstructor could be cached as well, maybe.

                try
                {
                    // Get the uncompressed version, so that we can identify its content type (and remember, the extension is not enough,
                    // as some types get translated to new content types by the SRS).

                    StreamableResource uncompressed = streamableResourceSource.getStreamableResource(resource, StreamableResourceProcessing.COMPRESSION_DISABLED, resourceChangeTracker);

                    StreamableResource forRequest = isCompressable(uncompressed)
                            ? streamableResourceSource.getStreamableResource(resource, StreamableResourceProcessing.COMPRESSION_ENABLED, resourceChangeTracker)
                            : uncompressed;

                    return assetPathConstructor.constructAssetPath(folder, resourcePath, forRequest);
                } catch (IOException ex)
                {
                    throw new RuntimeException(String.format("Unable to construct asset path for %s: %s",
                            resource, ExceptionUtils.toMessage(ex)), ex);
                }
            }

            public Resource getResource()
            {
                return resource;
            }
        };

    }
}
