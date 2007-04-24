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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.Asset;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.AssetSource;
import org.testng.annotations.Test;

public class AssetSourceImplTest extends InternalBaseTestCase
{
    private final Resource _baseResource = new ClasspathResource(
            "org/apache/tapestry/internal/services/SimpleComponent.class");

    private final Resource _rootResource = new ClasspathResource("/");

    @Test
    public void relative_asset()
    {
        AssetFactory factory = newAssetFactory();
        ThreadLocale threadLocale = newThreadLocale();
        Asset asset = newAsset();

        Resource expectedResource = _baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, _rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        // First try creates it:

        assertSame(source.findAsset(_baseResource, "SimpleComponent.properties", Locale.UK), asset);

        // Second try shows that it is cached

        assertSame(source.findAsset(_baseResource, "SimpleComponent.properties", Locale.UK), asset);

        verify();
    }

    @Test
    public void get_classpath_asset()
    {
        AssetFactory factory = newAssetFactory();
        ThreadLocale threadLocale = newThreadLocale();
        Asset asset = newAsset();

        Resource expectedResource = _baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, _rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        // First try creates it:

        assertSame(source.getClasspathAsset(
                "org/apache/tapestry/internal/services/SimpleComponent.properties",
                Locale.UK), asset);

        verify();
    }

    @Test
    public void get_classpath_asset_for_unspecified_locale()
    {
        AssetFactory factory = newAssetFactory();
        ThreadLocale threadLocale = newThreadLocale();
        Asset asset = newAsset();
        Locale locale = Locale.UK;

        Resource expectedResource = _baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, _rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        train_getLocale(threadLocale, locale);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        assertSame(
                source
                        .getClasspathAsset("org/apache/tapestry/internal/services/SimpleComponent.properties"),
                asset);

        verify();
    }

    @Test
    public void absolute_asset_with_known_prefix()
    {
        AssetFactory factory = newAssetFactory();
        ThreadLocale threadLocale = newThreadLocale();
        Asset asset = newAsset();

        Resource expectedResource = _rootResource
                .forFile("org/apache/tapestry/internal/services/SimpleComponent_en_GB.properties");

        train_getRootResource(factory, _rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        assertSame(source.findAsset(
                _baseResource,
                "classpath:org/apache/tapestry/internal/services/SimpleComponent.properties",
                Locale.UK), asset);

        // Check that a leading slash is not a problem:

        assertSame(source.findAsset(
                _baseResource,
                "classpath:/org/apache/tapestry/internal/services/SimpleComponent.properties",
                Locale.UK), asset);

        verify();
    }

    @Test
    public void unknown_asset_prefix()
    {
        ThreadLocale threadLocale = newThreadLocale();

        Map<String, AssetFactory> configuration = Collections.emptyMap();

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        try
        {
            source.findAsset(
                    _baseResource,
                    "classpath:org/apache/tapestry/internal/services/SimpleComponent.properties",
                    Locale.UK);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unknown prefix for asset path 'classpath:org/apache/tapestry/internal/services/SimpleComponent.properties'.");
        }

        verify();
    }

    @Test
    public void missing_resource()
    {
        ThreadLocale threadLocale = newThreadLocale();

        Map<String, AssetFactory> configuration = Collections.emptyMap();

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration);

        try
        {
            source.findAsset(_baseResource, "DoesNotExist.properties", Locale.UK);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unable to locate asset 'classpath:org/apache/tapestry/internal/services/DoesNotExist.properties' (the file does not exist).");
        }

        verify();
    }

}
