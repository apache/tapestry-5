// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.net.URL;
import java.util.Map;

import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.ResourceDigestGenerator;

public class ResourceDigestManagerImpl implements ResourceDigestManager, InvalidationListener
{
    private final ResourceDigestGenerator digestGenerator;

    private final ResourceChangeTracker resourceChangeTracker;

    private final Map<Resource, Cached> cache = CollectionFactory.newConcurrentMap();

    final static long MISSING_RESOURCE_TIME_MODIFIED = -1L;

    private class Cached
    {
        final boolean requiresDigest;

        final String digest;

        final long timeModified;

        Cached(Resource resource)
        {
            requiresDigest = digestGenerator.requiresDigest(resource.getPath());

            URL url = resource.toURL();

            digest = (requiresDigest && url != null) ? digestGenerator.generateDigest(url) : null;

            timeModified = url != null ? resourceChangeTracker.trackResource(resource) : MISSING_RESOURCE_TIME_MODIFIED;
        }
    }

    public ResourceDigestManagerImpl(ResourceDigestGenerator digestGenerator,
            ResourceChangeTracker resourceChangeTracker)
    {
        this.digestGenerator = digestGenerator;
        this.resourceChangeTracker = resourceChangeTracker;
    }

    @PostInjection
    public void listenForInvalidations()
    {
        resourceChangeTracker.addInvalidationListener(this);
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

    public void addInvalidationListener(InvalidationListener listener)
    {
        resourceChangeTracker.addInvalidationListener(listener);
    }

    public void objectWasInvalidated()
    {
        cache.clear();
    }

}
