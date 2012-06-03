// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetPathConverter;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.ClasspathProvider;

/**
 * Generates Assets for files on the classpath. Caches generated client URLs internally, and clears that cache when
 * notified to do so by the {@link ResourceDigestManager}.
 *
 * @see AssetDispatcher
 */
@Marker(ClasspathProvider.class)
public class ClasspathAssetFactory implements AssetFactory
{
    private final ResourceDigestManager digestManager;

    private final ClasspathAssetAliasManager aliasManager;

    private final ClasspathResource rootResource;

    private final AssetPathConverter converter;

    private final boolean invariant;

    public ClasspathAssetFactory(ResourceDigestManager digestManager, ClasspathAssetAliasManager aliasManager,
                                 AssetPathConverter converter)
    {
        this.digestManager = digestManager;
        this.aliasManager = aliasManager;
        this.converter = converter;

        rootResource = new ClasspathResource("");

        invariant = converter.isInvariant();
    }

    private String clientURL(Resource resource)
    {
        String defaultPath = buildDefaultPath(resource);

        return converter.convertAssetPath(defaultPath);
    }

    private String buildDefaultPath(Resource resource)
    {
        boolean requiresDigest = digestManager.requiresDigest(resource);

        String path = resource.getPath();

        if (requiresDigest)
        {
            // Resources with extensions go from foo/bar/baz.txt --> foo/bar/baz.CHECKSUM.txt

            int lastdotx = path.lastIndexOf('.');

            path = path.substring(0, lastdotx + 1) + digestManager.getDigest(resource) + path.substring(lastdotx);
        }

        return aliasManager.toClientURL(path);
    }

    public Asset createAsset(Resource resource)
    {
        if (invariant)
        {
            return createInvariantAsset(resource);
        }

        return createVariantAsset(resource);
    }

    /**
     * A variant asset must pass the resource through clientURL() all the time; very inefficient.
     */
    private Asset createVariantAsset(final Resource resource)
    {
        return new AbstractAsset(false)
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

    /**
     * An invariant asset is normal, and only needs to compute the clientURL for the resource once.
     */
    private Asset createInvariantAsset(final Resource resource)
    {
        return new AbstractAsset(true)
        {
            private String clientURL;

            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                if (clientURL == null)
                {
                    clientURL = clientURL(resource);
                }

                return clientURL;
            }
        };
    }

    public Resource getRootResource()
    {
        return rootResource;
    }
}
