// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeMap;

import java.net.URL;
import java.util.Map;

import org.apache.tapestry.events.UpdateListener;
import org.apache.tapestry.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry.internal.util.URLChangeTracker;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.ResourceDigestGenerator;

public class ResourceCacheImpl extends InvalidationEventHubImpl implements ResourceCache,
        UpdateListener
{
    private final URLChangeTracker _tracker;

    private final ResourceDigestGenerator _digestGenerator;

    private final Map<Resource, Cached> _cache = newThreadSafeMap();

    final static long MISSING_RESOURCE_TIME_MODIFIED = -1l;

    private class Cached
    {
        final boolean _requiresDigest;

        final String _digest;

        final long _timeModified;

        Cached(Resource resource)
        {
            _requiresDigest = _digestGenerator.requiresDigest(resource.getPath());

            URL url = resource.toURL();

            // The url may be null when a request for a protected asset arrives, because the
            // Resource initially is for the file with the digest incorporated into the path, which
            // means
            // no underlying file exists. Subsequently, we'll strip out the digest and resolve
            // to an actual resource.

            _digest = (_requiresDigest && url != null) ? _digestGenerator.generateDigest(url)
                    : null;

            _timeModified = url != null ? _tracker.add(url) : MISSING_RESOURCE_TIME_MODIFIED;
        }
    }

    public ResourceCacheImpl(final ResourceDigestGenerator digestGenerator)
    {
        this(digestGenerator, new URLChangeTracker());
    }

    ResourceCacheImpl(final ResourceDigestGenerator digestGenerator, URLChangeTracker tracker)
    {
        _digestGenerator = digestGenerator;
        _tracker = tracker;
    }

    public void checkForUpdates()
    {
        if (_tracker.containsChanges())
        {
            _cache.clear();
            _tracker.clear();

            fireInvalidationEvent();
        }
    }

    private Cached get(Resource resource)
    {
        Cached result = _cache.get(resource);

        if (result == null)
        {
            result = new Cached(resource);
            _cache.put(resource, result);
        }

        return result;
    }

    public String getDigest(Resource resource)
    {
        return get(resource)._digest;
    }

    public long getTimeModified(Resource resource)
    {
        return get(resource)._timeModified;
    }

    public boolean requiresDigest(Resource resource)
    {
        return get(resource)._requiresDigest;
    }

}
