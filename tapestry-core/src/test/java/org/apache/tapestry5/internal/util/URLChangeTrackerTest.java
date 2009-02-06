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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;

public class URLChangeTrackerTest extends TapestryTestCase
{
    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();
    
    @Test
    public void contains_change_when_empty()
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        assertFalse(t.containsChanges());
    }

    @Test
    public void add_null_returns_zero()
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        assertEquals(t.add(null), 0l);
    }

    @Test
    public void contains_changes() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        t.add(url);

        // The file, and the directory containing the file.

        assertEquals(t.trackedFileCount(), 2);

        assertFalse(t.containsChanges());

        boolean changed = false;

        // Because of clock accuracy, we need to try a couple of times
        // to ensure that the change to the file is visible in the
        // lastUpdated time stamp on the URL.

        for (int i = 0; i < 10 && !changed; i++)
        {
            Thread.sleep(100);

            touch(f);

            changed = t.containsChanges();
        }

        assertTrue(changed);

        // And, once a change has been observed ...

        assertFalse(t.containsChanges());
    }

    @Test
    public void creating_a_new_file_is_a_change() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        t.add(url);

        assertFalse(t.containsChanges());

        File dir = f.getParentFile();

        // Create another file in the temporary directory.

        long timestamp = dir.lastModified();

        while (true)
        {
            File.createTempFile("changetracker1", ".tmp");

            if (dir.lastModified() != timestamp) break;

            // Sometime Java need a moment to catch up in terms of
            // file system changes. Sleep for a few milliseconds
            // and wait for it to catch up.

            Thread.sleep(100);
        }

        assertTrue(t.containsChanges());
    }

    @Test
    public void non_file_URLs_are_ignored() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        URL url = new URL("ftp://breeblebrox.com");

        t.add(url);

        assertEquals(t.trackedFileCount(), 0);
    }

    @Test
    public void caching() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker(converter);

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        long initial = t.add(url);

        touch(f);

        long current = t.add(url);

        assertEquals(current, initial);

        assertTrue(t.containsChanges());

        t.clear();

        current = t.add(url);

        assertFalse(current == initial);
    }

    @Test
    public void deleted_files_show_as_changes() throws Exception
    {
        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        URLChangeTracker t = new URLChangeTracker(converter);

        long timeModified = t.add(url);

        assertTrue(timeModified > 0);

        // File + Directory
        assertEquals(t.trackedFileCount(), 2);

        assertFalse(t.containsChanges());

        assertTrue(f.delete());

        assertTrue(t.containsChanges());
    }

    @Test
    public void second_level_granularity() throws Exception
    {
        URLChangeTracker t = new URLChangeTracker(converter, true);

        File f = File.createTempFile("changetracker0", ".tmp");
        URL url = f.toURL();

        touch(f);
        long timestamp1 = t.add(url);
        assertEquals(0, timestamp1 % 1000);
        assertFalse(t.containsChanges());

        Thread.sleep(1500);

        touch(f);
        long timestamp2 = t.add(url);
        assertEquals(0, timestamp2 % 1000);
        assertTrue(t.containsChanges());
    }

}
