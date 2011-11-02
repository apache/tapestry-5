// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.AssetConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetSource;

import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("all")
public class AssetSourceImpl implements AssetSource
{
    private final StrategyRegistry<AssetFactory> registry;

    private final ThreadLocale threadLocale;

    private final Map<String, Resource> prefixToRootResource = CollectionFactory.newMap();

    private final Map<Resource, SoftReference<Asset>> cache = CollectionFactory.newWeakHashMap();

    private final SymbolSource symbolSource;

    public AssetSourceImpl(ThreadLocale threadLocale,

                           Map<String, AssetFactory> configuration, SymbolSource symbolSource)
    {
        this.threadLocale = threadLocale;
        this.symbolSource = symbolSource;

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

    public Asset getContextAsset(String path, Locale locale)
    {
        return getAsset(prefixToRootResource.get(AssetConstants.CONTEXT), path, locale);
    }

    public Asset getAsset(Resource baseResource, String path, Locale locale)
    {
        return getAssetInLocale(baseResource, path, defaulted(locale));
    }

    public Resource resourceForPath(String path)
    {
        return getUnlocalizedResource(null, path);
    }

    public Asset getExpandedAsset(String path)
    {
        return getUnlocalizedAsset(symbolSource.expandSymbols(path));
    }

    public Asset getUnlocalizedAsset(String path)
    {
        return getAssetInLocale(null, path, null);
    }

    private Asset getAssetInLocale(Resource baseResource, String path, Locale locale)
    {
        return getLocalizedAssetFromResource(getUnlocalizedResource(baseResource, path), locale);
    }

    private Resource getUnlocalizedResource(Resource baseResource, String path)
    {
        assert InternalUtils.isNonBlank(path);
        int colonx = path.indexOf(':');

        if (colonx < 0)
        {
            Resource root = baseResource != null ? baseResource : prefixToRootResource.get(AssetConstants.CLASSPATH);

            return root.forFile(path);
        }

        String prefix = path.substring(0, colonx);

        Resource root = prefixToRootResource.get(prefix);

        if (root == null)
            throw new IllegalArgumentException(ServicesMessages.unknownAssetPrefix(path));

        return root.forFile(path.substring(colonx + 1));
    }

    private Asset getLocalizedAssetFromResource(Resource unlocalized, Locale locale)
    {
        Resource localized = locale == null ? unlocalized : unlocalized.forLocale(locale);

	if (localized == null || !localized.exists())
            throw new RuntimeException(ServicesMessages.assetDoesNotExist(unlocalized));

        return getAssetForResource(localized);
    }

    private synchronized Asset getAssetForResource(Resource resource)
    {
        Asset result = TapestryInternalUtils.getAndDeref(cache, resource);

        if (result == null)
        {
            result = createAssetFromResource(resource);
            cache.put(resource, new SoftReference(result));
        }

        return result;
    }

    private Locale defaulted(Locale locale)
    {
        return locale != null ? locale : threadLocale.getLocale();
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
