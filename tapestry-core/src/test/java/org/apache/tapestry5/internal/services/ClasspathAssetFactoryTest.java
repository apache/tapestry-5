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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.testng.annotations.Test;

public class ClasspathAssetFactoryTest extends InternalBaseTestCase
{
    private final IdentityAssetPathConverter converter = new IdentityAssetPathConverter();

    @Test
    public void asset_client_URL_is_cached()
    {
        ResourceCache cache = mockResourceCache();

        Resource r = new ClasspathResource("foo/Bar.txt");

        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();

        train_requiresDigest(cache, r, false);

        String expectedClientURL = "/context/asset/foo/Bar.txt";

        train_toClientURL(aliasManager, "foo/Bar.txt", expectedClientURL);

        replay();

        ClasspathAssetFactory factory = new ClasspathAssetFactory(cache, aliasManager,
                                                                  converter);

        Asset asset = factory.createAsset(r);

        assertEquals(asset.toClientURL(), expectedClientURL);

        // Now, to check the cache:

        assertEquals(asset.toClientURL(), expectedClientURL);

        verify();

        // Now, to test cache clearing:
        train_requiresDigest(cache, r, false);

        train_toClientURL(aliasManager, "foo/Bar.txt", expectedClientURL);

        replay();

        factory.objectWasInvalidated();

        assertEquals(asset.toClientURL(), expectedClientURL);

        verify();
    }

    @Test
    public void simple_asset_client_URL()
    {
        ResourceCache cache = mockResourceCache();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();

        Resource r = new ClasspathResource("foo/Bar.txt");

        train_requiresDigest(cache, r, false);

        String expectedClientURL = "/context/asset/foo/Bar.txt";

        train_toClientURL(aliasManager, "foo/Bar.txt", expectedClientURL);

        replay();

        AssetFactory factory = new ClasspathAssetFactory(cache, aliasManager, new IdentityAssetPathConverter());

        Asset asset = factory.createAsset(r);

        assertSame(asset.getResource(), r);
        assertEquals(asset.toClientURL(), expectedClientURL);
        assertEquals(asset.toString(), expectedClientURL);

        verify();
    }

    @Test
    public void protected_asset_client_URL()
    {
        ResourceCache cache = mockResourceCache();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();

        Resource r = new ClasspathResource("foo/Bar.txt");

        train_requiresDigest(cache, r, true);

        expect(cache.getDigest(r)).andReturn("ABC123");

        String expectedClientURL = "/context/asset/foo/Bar.ABC123.txt";

        train_toClientURL(aliasManager, "foo/Bar.ABC123.txt", expectedClientURL);

        replay();

        AssetFactory factory = new ClasspathAssetFactory(cache, aliasManager, new IdentityAssetPathConverter());

        Asset asset = factory.createAsset(r);

        assertSame(asset.getResource(), r);
        assertEquals(asset.toClientURL(), expectedClientURL);
        assertEquals(asset.toString(), expectedClientURL);

        verify();
    }

}
