// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.apache.tapestry5.services.UpdateListener;

import java.net.URL;
import java.util.Map;

public class ResourceCacheImpl extends InvalidationEventHubImpl implements ResourceCache,
        UpdateListener
{
    private final URLChangeTracker tracker;

    private final ResourceDigestGenerator digestGenerator;

    private final Map<Resource, Cached> cache = CollectionFactory.newConcurrentMap();

    final static long MISSING_RESOURCE_TIME_MODIFIED = -1L;

    private class Cached
    {
        final boolean requiresDigest;

        final String digest;

        final long timeModified;

        final StreamableResource streamable;

        Cached(Resource resource)
        {
            requiresDigest = digestGenerator.requiresDigest(resource.getPath());

            URL url = resource.toURL();

            // The url may be null when a request for a protected asset arrives, because the
            // Resource initially is for the file with the digest incorporated into the path, which
            // means
            // no underlying file exists. Subsequently, we'll strip out the digest and resolve
            // to an actual resource.

            digest = (requiresDigest && url != null) ? digestGenerator.generateDigest(url)
                                                     : null;

            timeModified = url != null ? tracker.add(url) : MISSING_RESOURCE_TIME_MODIFIED;

            streamable = url == null ? null : new StreamableResourceImpl(url, timeModified);
        }
    }

    public ResourceCacheImpl(final ResourceDigestGenerator digestGenerator, ClasspathURLConverter classpathURLConverter)
    {
        this.digestGenerator = digestGenerator;
        tracker = new URLChangeTracker(classpathURLConverter,true);
    }

    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            cache.clear();
            tracker.clear();

            fireInvalidationEvent();
        }
    }

    private Cached get(Resource resource)
    {
        Cached result = cache.get(resource);

        if (result == null)
        {
            result = new Cached(resource);
            cache.put(resource, result);
        }

        return result;
    }

    public String getDigest(Resource resource)
    {
        return get(resource).digest;
    }

    public long getTimeModified(Resource resource)
    {
        return get(resource).timeModified;
    }

    public boolean requiresDigest(Resource resource)
    {
        return get(resource).requiresDigest;
    }

    public StreamableResource getStreamableResource(Resource resource)
    {
        return get(resource).streamable;
    }
}
