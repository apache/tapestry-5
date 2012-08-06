// Copyright 2011, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

/**
 * Specialization of {@link SRSCachingInterceptor} that only attempts to cache
 * compressed resources.
 */
public class SRSCompressedCachingInterceptor extends SRSCachingInterceptor
{
    public SRSCompressedCachingInterceptor(StreamableResourceSource delegate, ResourceChangeTracker tracker)
    {
        super(delegate, tracker);
    }

    /**
     * Return true only if the resource is compressed.
     */
    @Override
    protected boolean isCacheable(StreamableResource resource)
    {
        return resource.getCompression() == CompressionStatus.COMPRESSED;
    }

    /**
     * Returns true just when the processing enables compression.
     */
    @Override
    protected boolean enableCache(StreamableResourceProcessing processing)
    {
        return processing == StreamableResourceProcessing.COMPRESSION_ENABLED;
    }
}
