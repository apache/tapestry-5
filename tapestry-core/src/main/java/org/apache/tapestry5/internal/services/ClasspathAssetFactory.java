// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.events.InvalidationListener;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;

import java.util.Map;

/**
 * Generates Assets for files on the classpath. Caches generated client URLs internally, and clears that cache when
 * notified to do so by the {@link ResourceCache}.
 *
 * @see AssetDispatcher
 */
public class ClasspathAssetFactory implements AssetFactory, InvalidationListener
{
    private final ResourceCache cache;

    private final ClasspathAssetAliasManager aliasManager;

    private final Map<Resource, String> resourceToClientURL = newConcurrentMap();

    public ClasspathAssetFactory(final ResourceCache cache, final ClasspathAssetAliasManager aliasManager)
    {
        this.cache = cache;
        this.aliasManager = aliasManager;
    }

    public void objectWasInvalidated()
    {
        resourceToClientURL.clear();
    }

    private String clientURL(Resource resource)
    {
        String clientURL = resourceToClientURL.get(resource);

        if (clientURL == null)
        {
            clientURL = buildClientURL(resource);
            resourceToClientURL.put(resource, clientURL);
        }

        // The path generated is partially request-dependent and therefore can't be cached, it will even
        // vary from request to the next.

        return aliasManager.toClientURL(clientURL);
    }

    private String buildClientURL(Resource resource)
    {
        boolean requiresDigest = cache.requiresDigest(resource);

        String path = resource.getPath();

        if (requiresDigest)
        {
            // Resources with extensions go from foo/bar/baz.txt --> foo/bar/baz.CHECKSUM.txt

            int lastdotx = path.lastIndexOf('.');

            path = path.substring(0, lastdotx + 1) + cache.getDigest(resource) + path.substring(lastdotx);
        }

        return path;
    }

    public Asset createAsset(final Resource resource)
    {
        return new Asset()
        {
            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                return clientURL(resource);
            }

            @Override
            public String toString()
            {
                return toClientURL();
            }
        };
    }

    public Resource getRootResource()
    {
        return new ClasspathResource("");
    }

}
