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

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.tapestry.Asset;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.AssetSource;
import org.testng.annotations.Test;

public class AssetObjectProviderTest extends InternalBaseTestCase
{
    @Test
    public void incorrect_object_type()
    {
        AssetSource source = newAssetSource();
        ThreadLocale threadLocale = newThreadLocale();
        Resource root = newResource();
        ServiceLocator locator = newServiceLocator();

        replay();

        ObjectProvider provider = new AssetObjectProvider(source, threadLocale, root);

        try
        {
            provider.provide("foo/bar/baz.gif", BigDecimal.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Asset path \'foo/bar/baz.gif\' may not be assigned to type java.math.BigDecimal. Use type java.lang.Object or (preferrably) org.apache.tapestry.Asset.");
        }

        verify();
    }

    @Test
    public void normal_conversion()
    {
        AssetSource source = newAssetSource();
        ThreadLocale threadLocale = newThreadLocale();
        Resource root = newResource();
        ServiceLocator locator = newServiceLocator();
        Asset asset = newAsset();
        Locale locale = Locale.GERMAN;
        String path = "foo/bar/baz.gif";

        train_getLocale(threadLocale, locale);
        train_findAsset(source, root, path, locale, asset);

        replay();

        ObjectProvider provider = new AssetObjectProvider(source, threadLocale, root);

        Asset result = provider.provide(path, Asset.class, locator);

        assertSame(result, asset);

        verify();
    }

    private void train_findAsset(AssetSource source, Resource root, String path, Locale locale,
            Asset asset)
    {
        expect(source.findAsset(root, path, locale)).andReturn(asset);
    }
}
