// Copyright 2006-2013 The Apache Software Foundation
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
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.QuietOperationTracker;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetSource;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class AssetSourceImplTest extends InternalBaseTestCase
{
    private final Resource baseResource = new ClasspathResource(
            "org/apache/tapestry5/internal/services/SimpleComponent.class");

    private final Resource rootResource = new ClasspathResource("/");

    private final OperationTracker tracker = new QuietOperationTracker();

    @Test
    public void relative_asset()
    {
        AssetFactory factory = mockAssetFactory();
        ThreadLocale threadLocale = mockThreadLocale();
        Asset asset = mockAsset();

        Resource expectedResource = baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        // First try creates it:

        assertSame(source.getAsset(baseResource, "SimpleComponent.properties", Locale.UK), asset);

        // Second try shows that it is cached

        assertSame(source.getAsset(baseResource, "SimpleComponent.properties", Locale.UK), asset);

        verify();
    }

    @Test
    public void get_classpath_asset()
    {
        AssetFactory factory = mockAssetFactory();
        ThreadLocale threadLocale = mockThreadLocale();
        Asset asset = mockAsset();

        Resource expectedResource = baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        // First try creates it:

        assertSame(source.getClasspathAsset("org/apache/tapestry5/internal/services/SimpleComponent.properties",
                Locale.UK), asset);

        verify();
    }

    @Test
    public void get_expanded_asset()
    {
        AssetFactory factory = mockAssetFactory();
        Asset asset = mockAsset();
        SymbolSource symbolSource = mockSymbolSource();

        Resource expectedResource = baseResource.forFile("SimpleComponent.properties");

        train_getRootResource(factory, rootResource);

        train_createAsset(factory, expectedResource, asset);

        train_expandSymbols(symbolSource, "${path}/SimpleComponent.properties",
                "org/apache/tapestry5/internal/services/SimpleComponent.properties");

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(null, configuration, symbolSource, null, tracker);

        // First try creates it:

        assertSame(source.getExpandedAsset("${path}/SimpleComponent.properties"), asset);

        verify();

    }

    @Test
    public void get_classpath_asset_for_unspecified_locale()
    {
        AssetFactory factory = mockAssetFactory();
        ThreadLocale threadLocale = mockThreadLocale();
        Asset asset = mockAsset();
        Locale locale = Locale.UK;

        Resource expectedResource = baseResource.forFile("SimpleComponent_en_GB.properties");

        train_getRootResource(factory, rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        train_getLocale(threadLocale, locale);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        assertSame(source.getClasspathAsset("org/apache/tapestry5/internal/services/SimpleComponent.properties"), asset);

        verify();
    }

    @Test
    public void absolute_asset_with_known_prefix()
    {
        AssetFactory factory = mockAssetFactory();
        ThreadLocale threadLocale = mockThreadLocale();
        Asset asset = mockAsset();

        Resource expectedResource = rootResource
                .forFile("org/apache/tapestry5/internal/services/SimpleComponent_en_GB.properties");

        train_getRootResource(factory, rootResource);

        train_createAsset(factory, expectedResource, asset);

        Map<String, AssetFactory> configuration = Collections.singletonMap("classpath", factory);

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        assertSame(source.getAsset(baseResource,
                "classpath:org/apache/tapestry5/internal/services/SimpleComponent.properties", Locale.UK), asset);

        // Check that a leading slash is not a problem:

        assertSame(source.getAsset(baseResource,
                "classpath:/org/apache/tapestry5/internal/services/SimpleComponent.properties", Locale.UK), asset);

        verify();
    }

    @Test
    public void unknown_asset_prefix()
    {
        ThreadLocale threadLocale = mockThreadLocale();

        Map<String, AssetFactory> configuration = Collections.emptyMap();

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        try
        {
            source.getAsset(baseResource,
                    "classpath:org/apache/tapestry5/internal/services/SimpleComponent.properties", Locale.UK);
            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                    "Unknown prefix for asset path 'classpath:org/apache/tapestry5/internal/services/SimpleComponent.properties'.");
        }

        verify();
    }

    @Test
    public void missing_resource()
    {
        ThreadLocale threadLocale = mockThreadLocale();

        Map<String, AssetFactory> configuration = Collections.emptyMap();

        replay();

        AssetSource source = new AssetSourceImpl(threadLocale, configuration, null, null, tracker);

        try
        {
            source.getAsset(baseResource, "DoesNotExist.properties", Locale.UK);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unable to locate asset 'classpath:org/apache/tapestry5/internal/services/DoesNotExist.properties' (the file does not exist).");
        }

        verify();
    }

}
