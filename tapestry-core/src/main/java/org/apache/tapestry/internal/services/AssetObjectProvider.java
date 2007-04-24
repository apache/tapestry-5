// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.Asset;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.AssetSource;

/**
 * Exposes assets (in the current locale).
 */
public class AssetObjectProvider implements ObjectProvider
{
    private final AssetSource _source;

    private final ThreadLocale _threadLocale;

    private final Resource _classpathRootResource;

    public AssetObjectProvider(AssetSource source, ThreadLocale threadLocale,
            Resource classpathRootResource)
    {
        _source = source;
        _threadLocale = threadLocale;
        _classpathRootResource = classpathRootResource;
    }

    /**
     * Provides the asset. If the expression does not identify an asset domain, with a prefix, it is
     * assumed to be a path on the classpath, relative to the root of the classpath.
     * 
     * @param expression
     *            expression used to find the asset, passed to
     *            {@link AssetSource#findAsset(Resource, String, java.util.Locale)
     * @param objectType
     *            the type of object (which must be Object or Asset)
     * @param locator
     *            not used
     */
    public <T> T provide(String expression, Class<T> objectType, ServiceLocator locator)
    {
        if (!objectType.isAssignableFrom(Asset.class))
            throw new RuntimeException(ServicesMessages.assetNotCompatible(expression, objectType));

        Asset asset = _source.findAsset(_classpathRootResource, expression, _threadLocale
                .getLocale());

        return objectType.cast(asset);
    }
}
