// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResourceDigestManagerTest extends InternalBaseTestCase
{
    private static final String PATH = "foo/Bar.gif";

    private static final String DIGEST = "abc123";

    private ResourceChangeTracker resourceChangeTracker;

    @BeforeClass
    public void setup()
    {
        resourceChangeTracker = getService(ResourceChangeTracker.class);
    }

    @AfterClass
    public void cleanup()
    {
        resourceChangeTracker = null;
    }

    @Test
    public void properties_for_simple_resource() throws Exception
    {
        ResourceDigestGenerator generator = mockResourceDigestGenerator();

        File f = createTestFile();
        URL url = f.toURL();
        Resource r = mockResource();

        long lastUpdated = f.lastModified();
        lastUpdated -= lastUpdated % 1000;

        train_getPath(r, PATH);
        train_toURL(r, url);

        train_requiresDigest(generator, PATH, false);

        replay();

        ResourceDigestManagerImpl cache = new ResourceDigestManagerImpl(generator, resourceChangeTracker);

        assertEquals(cache.requiresDigest(r), false);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), null);

        verify();
    }

    @Test
    public void properties_for_missing_resource() throws Exception
    {
        ResourceDigestGenerator generator = mockResourceDigestGenerator();

        Resource r = mockResource();

        train_getPath(r, PATH);
        train_toURL(r, null);

        train_requiresDigest(generator, PATH, true);

        replay();

        ResourceDigestManagerImpl cache = new ResourceDigestManagerImpl(generator, null);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), ResourceDigestManagerImpl.MISSING_RESOURCE_TIME_MODIFIED);
        assertEquals(cache.getDigest(r), null);

        verify();
    }

    @Test
    public void properties_for_protected_resource() throws Exception
    {
        ResourceDigestGenerator generator = mockResourceDigestGenerator();

        File f = createTestFile();
        URL url = f.toURL();
        Resource r = mockResource();

        long lastUpdated = f.lastModified();
        lastUpdated -= lastUpdated % 1000;

        train_getPath(r, PATH);
        train_toURL(r, url);

        train_requiresDigest(generator, PATH, true);
        train_generateChecksum(generator, url, DIGEST);

        replay();

        ResourceDigestManagerImpl cache = new ResourceDigestManagerImpl(generator, resourceChangeTracker);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), DIGEST);

        verify();
    }

    @Test
    public void caching_and_invalidation() throws Exception
    {
        // Alas, mixing and matching live code with mocks
        ResourceDigestGenerator generator = mockResourceDigestGenerator();
        InvalidationListener listener = mockInvalidationListener();
        File f = createTestFile();
        URL url = f.toURL();
        Resource r = mockResource();

        long lastUpdated = f.lastModified();
        lastUpdated -= lastUpdated % 1000;

        train_getPath(r, PATH);
        train_toURL(r, url);

        train_requiresDigest(generator, PATH, true);
        train_generateChecksum(generator, url, DIGEST);

        replay();

        ResourceDigestManagerImpl cache = new ResourceDigestManagerImpl(generator, resourceChangeTracker);
        cache.listenForInvalidations();
        cache.addInvalidationListener(listener);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), DIGEST);

        // No updates yet
        getService(UpdateListenerHub.class).fireCheckForUpdates();

        verify();

        Thread.sleep(1500);
        touch(f);

        lastUpdated = f.lastModified();
        lastUpdated -= lastUpdated % 1000;

        String expectedDigest = "FREDBARNEY";

        train_getPath(r, PATH);
        train_toURL(r, url);
        train_requiresDigest(generator, PATH, true);
        train_generateChecksum(generator, url, expectedDigest);

        listener.objectWasInvalidated();

        replay();

        getService(UpdateListenerHub.class).fireCheckForUpdates();

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), expectedDigest);

        verify();
    }

    private File createTestFile() throws IOException
    {
        return File.createTempFile("ResourceCacheImplTest.", ".tst");
    }
}
