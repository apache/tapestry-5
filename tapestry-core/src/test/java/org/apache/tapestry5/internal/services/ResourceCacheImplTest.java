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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ResourceCacheImplTest extends InternalBaseTestCase
{
    private static final String PATH = "foo/Bar.gif";

    private static final String DIGEST = "abc123";
    
    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

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

        ResourceCacheImpl cache = new ResourceCacheImpl(generator, converter);

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

        ResourceCacheImpl cache = new ResourceCacheImpl(generator, converter);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), ResourceCacheImpl.MISSING_RESOURCE_TIME_MODIFIED);
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

        ResourceCacheImpl cache = new ResourceCacheImpl(generator, converter);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), DIGEST);

        verify();
    }

    @Test
    public void caching_and_invalidation() throws Exception
    {
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

        ResourceCacheImpl cache = new ResourceCacheImpl(generator, converter);
        cache.addInvalidationListener(listener);

        assertEquals(cache.requiresDigest(r), true);
        assertEquals(cache.getTimeModified(r), lastUpdated);
        assertEquals(cache.getDigest(r), DIGEST);

        // No updates yet.

        cache.checkForUpdates();

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

        cache.checkForUpdates();

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
