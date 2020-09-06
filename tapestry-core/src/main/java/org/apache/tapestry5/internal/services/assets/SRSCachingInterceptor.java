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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * An interceptor for the {@link StreamableResourceSource} service that handles caching of content.
 */
public class SRSCachingInterceptor extends DelegatingSRS
{
    private final Map<Resource, SoftReference<StreamableResource>> cache = CollectionFactory.newConcurrentMap();

    public SRSCachingInterceptor(StreamableResourceSource delegate, ResourceChangeTracker tracker)
    {
        super(delegate);

        tracker.clearOnInvalidation(cache);
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies)
            throws IOException
    {
        if (!enableCache(processing))
        {
            return delegate.getStreamableResource(baseResource, processing, dependencies);
        }

        StreamableResource result = TapestryInternalUtils.getAndDeref(cache, baseResource);

        if (result == null)
        {
            result = delegate.getStreamableResource(baseResource, processing, dependencies);

            if (isCacheable(result))
            {
                dependencies.addDependency(baseResource);

                cache.put(baseResource, new SoftReference<StreamableResource>(result));
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

    /**
     * Returns true unless the processing is {@link StreamableResourceProcessing#FOR_AGGREGATION}.
     * Subclasses may override. When the cache is not enabled, the request is passed on to the interceptor's
     * {@link #delegate}, and no attempt is made to read or update this interceptor's cache.
     *
     * @since 5.3.5
     */
    protected boolean enableCache(StreamableResourceProcessing processing)
    {
        return processing != StreamableResourceProcessing.FOR_AGGREGATION;
    }
}
