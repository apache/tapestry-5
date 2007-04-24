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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.Asset;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.AssetSource;

public class AssetSourceImpl implements AssetSource
{
    private final StrategyRegistry<AssetFactory> _registry;

    private final ThreadLocale _threadLocale;

    private final Map<String, Resource> _prefixToRootResource = newMap();

    private final Map<Resource, Asset> _cache = newConcurrentMap();

    public AssetSourceImpl(ThreadLocale threadLocale, Map<String, AssetFactory> configuration)
    {
        _threadLocale = threadLocale;

        Map<Class, AssetFactory> byResourceClass = newMap();

        for (Map.Entry<String, AssetFactory> e : configuration.entrySet())
        {
            String prefix = e.getKey();
            AssetFactory factory = e.getValue();

            Resource rootResource = factory.getRootResource();

            byResourceClass.put(rootResource.getClass(), factory);

            _prefixToRootResource.put(prefix, rootResource);
        }

        _registry = StrategyRegistry.newInstance(AssetFactory.class, byResourceClass);
    }

    public Asset getClasspathAsset(String path)
    {
        return getClasspathAsset(path, _threadLocale.getLocale());
    }

    public Asset getClasspathAsset(String path, Locale locale)
    {
        Resource baseResource = _prefixToRootResource.get("classpath");

        return findAsset(baseResource, path, locale);
    }

    public Asset findAsset(Resource baseResource, String path, Locale locale)
    {
        notNull(baseResource, "baseResource");
        notBlank(path, "path");
        notNull(locale, "locale");

        int colonx = path.indexOf(':');

        if (colonx < 0) return findRelativeAsset(baseResource, path, locale);

        String prefix = path.substring(0, colonx);

        Resource rootResource = _prefixToRootResource.get(prefix);

        if (rootResource == null)
            throw new IllegalArgumentException(ServicesMessages.unknownAssetPrefix(path));

        return findRelativeAsset(rootResource, path.substring(colonx + 1), locale);
    }

    private Asset findRelativeAsset(Resource baseResource, String path, Locale locale)
    {
        Resource unlocalized = baseResource.forFile(path);
        Resource localized = unlocalized.forLocale(locale);

        if (localized == null)
            throw new RuntimeException(ServicesMessages.assetDoesNotExist(unlocalized));

        return getAssetForResource(localized);
    }

    private Asset getAssetForResource(Resource resource)
    {
        Asset result = _cache.get(resource);

        if (result == null)
        {
            result = createAssetFromResource(resource);
            _cache.put(resource, result);
        }

        return result;
    }

    private Asset createAssetFromResource(Resource resource)
    {
        // The class of the resource is derived from the class of the base resource.
        // So we can then use the class of the resource as a key to locate the correct asset
        // factory.

        Class resourceClass = resource.getClass();

        AssetFactory factory = _registry.get(resourceClass);

        return factory.createAsset(resource);
    }

}
