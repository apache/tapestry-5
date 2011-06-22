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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetPathConverter;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.InvalidationListener;

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

    private final Map<Resource, String> resourceToDefaultPath = CollectionFactory.newConcurrentMap();

    private final ClasspathResource rootResource;

    private final AssetPathConverter converter;

    private final boolean invariant;

    public ClasspathAssetFactory(ResourceCache cache, ClasspathAssetAliasManager aliasManager,
                                 AssetPathConverter converter)
    {
        this.cache = cache;
        this.aliasManager = aliasManager;
        this.converter = converter;

        rootResource = new ClasspathResource("");

        invariant = converter.isInvariant();
    }

    public void objectWasInvalidated()
    {
        resourceToDefaultPath.clear();
    }

    private String clientURL(Resource resource)
    {
        String defaultPath = resourceToDefaultPath.get(resource);

        if (defaultPath == null)
        {
            defaultPath = buildDefaultPath(resource);

            resourceToDefaultPath.put(resource, defaultPath);
        }

        return converter.convertAssetPath(defaultPath);
    }

    private String buildDefaultPath(Resource resource)
    {
        boolean requiresDigest = cache.requiresDigest(resource);

        String path = resource.getPath();

        if (requiresDigest)
        {
            // Resources with extensions go from foo/bar/baz.txt --> foo/bar/baz.CHECKSUM.txt

            int lastdotx = path.lastIndexOf('.');

            path = path.substring(0, lastdotx + 1) + cache.getDigest(resource) + path.substring(lastdotx);
        }

        return aliasManager.toClientURL(path);
    }

    public Asset createAsset(final Resource resource)
    {
        return new AbstractAsset(invariant)
        {
            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                return clientURL(resource);
            }
        };
    }

    public Resource getRootResource()
    {
        return rootResource;
    }
}
