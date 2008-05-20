// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetSource;

import java.util.Locale;
import java.util.Map;

public class AssetSourceImpl implements AssetSource
{
    private static final String CLASSPATH = "classpath";

    private final StrategyRegistry<AssetFactory> registry;

    private final ThreadLocale threadLocale;

    private final Map<String, Resource> prefixToRootResource = CollectionFactory.newMap();

    private final Map<Resource, Asset> cache = CollectionFactory.newConcurrentMap();

    public AssetSourceImpl(ThreadLocale threadLocale,

                           Map<String, AssetFactory> configuration)
    {
        this.threadLocale = threadLocale;

        Map<Class, AssetFactory> byResourceClass = CollectionFactory.newMap();

        for (Map.Entry<String, AssetFactory> e : configuration.entrySet())
        {
            String prefix = e.getKey();
            AssetFactory factory = e.getValue();

            Resource rootResource = factory.getRootResource();

            byResourceClass.put(rootResource.getClass(), factory);

            prefixToRootResource.put(prefix, rootResource);
        }

        registry = StrategyRegistry.newInstance(AssetFactory.class, byResourceClass);
    }

    public Asset getClasspathAsset(String path)
    {
        return getClasspathAsset(path, null);
    }

    public Asset getClasspathAsset(String path, Locale locale)
    {
        return getAsset(null, path, locale);
    }

    public Asset getAsset(Resource baseResource, String path, Locale locale)
    {
        Defense.notBlank(path, "path");

        if (baseResource == null) baseResource = prefixToRootResource.get(CLASSPATH);

        if (locale == null) locale = threadLocale.getLocale();

        int colonx = path.indexOf(':');

        if (colonx < 0) return findRelativeAsset(baseResource, path, locale);

        String prefix = path.substring(0, colonx);

        Resource rootResource = prefixToRootResource.get(prefix);

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
        Asset result = cache.get(resource);

        if (result == null)
        {
            result = createAssetFromResource(resource);
            cache.put(resource, result);
        }

        return result;
    }

    private Asset createAssetFromResource(Resource resource)
    {
        // The class of the resource is derived from the class of the base resource.
        // So we can then use the class of the resource as a key to locate the correct asset
        // factory.

        Class resourceClass = resource.getClass();

        AssetFactory factory = registry.get(resourceClass);

        return factory.createAsset(resource);
    }

}
