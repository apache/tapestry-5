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
import java.util.Map;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

/**
 * An interceptor for the {@link StreamableResourceSource} service that handles caching of content.
 */
public class SRSCachingInterceptor implements StreamableResourceSource, InvalidationListener
{
    private final ResourceChangeTracker tracker;

    private final StreamableResourceSource delegate;

    private final Map<Resource, StreamableResource> cache = CollectionFactory.newConcurrentMap();

    public SRSCachingInterceptor(ResourceChangeTracker tracker, StreamableResourceSource delegate)
    {
        this.tracker = tracker;
        this.delegate = delegate;
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing)
            throws IOException
    {
        if (processing == StreamableResourceProcessing.FOR_AGGREGATION)
            return delegate.getStreamableResource(baseResource, processing);

        StreamableResource result = cache.get(baseResource);

        if (result == null)
        {
            result = delegate.getStreamableResource(baseResource, processing);

            if (isCacheable(result))
            {
                tracker.trackResource(baseResource);

                cache.put(baseResource, result);
            }
        }

        return result;
    }

    /**
     * Always returns true; a subclass may extend this to only cache the resource in some circumstances.
     * 
     * @param resource
     * @return true to cache the resource
     */
    protected boolean isCacheable(StreamableResource resource)
    {
        return true;
    }

    public void objectWasInvalidated()
    {
        cache.clear();
    }
}
