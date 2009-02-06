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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Given a (growing) set of URLs, can periodically check to see if any of the underlying resources has changed. This
 * class is capable of using either millisecond-level granularity or second-level granularity. Millisecond-level
 * granularity is used by default. Second-level granularity is provided for compatibility with browsers vis-a-vis
 * resource caching -- that's how granular they get with their "If-Modified-Since", "Last-Modified" and "Expires"
 * headers.
 */
public class URLChangeTracker
{
    private static final long FILE_DOES_NOT_EXIST_TIMESTAMP = -1L;

    private final Map<File, Long> fileToTimestamp = CollectionFactory.newConcurrentMap();

    private final boolean granularitySeconds;
    
    private ClasspathURLConverter classpathURLConverter; 

    /**
     * Creates a new URL change tracker with millisecond-level granularity.
     * 
     * @param classpathURLConverter used to convert URLs from one protocol to another
     */
    public URLChangeTracker(ClasspathURLConverter classpathURLConverter)
    {
        this(classpathURLConverter, false);
        
    }

    /**
     * Creates a new URL change tracker, using either millisecond-level granularity or second-level granularity.
     *
     * @param classpathURLConverter used to convert URLs from one protocol to another
     * @param granularitySeconds whether or not to use second granularity (as opposed to millisecond granularity)
     */
    public URLChangeTracker(ClasspathURLConverter classpathURLConverter, boolean granularitySeconds)
    {
        this.granularitySeconds = granularitySeconds;
        
        this.classpathURLConverter = classpathURLConverter;
    }

    /**
     * Stores a new URL into the tracker, or returns the previous time stamp for a previously added URL. Filters out all
     * non-file URLs.
     *
     * @param url of the resource to add, or null if not known
     * @return the current timestamp for the URL (possibly rounded off for granularity reasons), or 0 if the URL is
     *         null
     */
    public long add(URL url)
    {
        if (url == null) return 0;
        
        URL converted = classpathURLConverter.convert(url);

        if (!converted.getProtocol().equals("file")) return timestampForNonFileURL(converted);

        File resourceFile = toFile(converted);

        if (fileToTimestamp.containsKey(resourceFile)) return fileToTimestamp.get(resourceFile);

        long timestamp = readTimestamp(resourceFile);

        // A quick and imperfect fix for TAPESTRY-1918.  When a file
        // is added, add the directory containing the file as well.

        fileToTimestamp.put(resourceFile, timestamp);

        File dir = resourceFile.getParentFile();

        if (!fileToTimestamp.containsKey(dir))
        {
            long dirTimestamp = readTimestamp(dir);
            fileToTimestamp.put(dir, dirTimestamp);
        }


        return timestamp;
    }

    private long timestampForNonFileURL(URL url)
    {
        long timestamp;

        try
        {
            timestamp = url.openConnection().getLastModified();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        return applyGranularity(timestamp);
    }

    private File toFile(URL url)
    {
        // http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html

        try
        {
            return new File(url.toURI());
        }
        catch (URISyntaxException ex)
        {
            return new File(url.getPath());
        }
    }

    /**
     * Clears all URL and timestamp data stored in the tracker.
     */
    public void clear()
    {
        fileToTimestamp.clear();
    }

    /**
     * Re-acquires the last updated timestamp for each URL and returns true if any timestamp has changed.
     */
    public boolean containsChanges()
    {
        boolean result = false;

        // This code would be highly suspect if this method was expected to be invoked
        // concurrently, but CheckForUpdatesFilter ensures that it will be invoked
        // synchronously.

        for (Map.Entry<File, Long> entry : fileToTimestamp.entrySet())
        {
            long newTimestamp = readTimestamp(entry.getKey());
            long current = entry.getValue();

            if (current == newTimestamp) continue;

            result = true;
            entry.setValue(newTimestamp);
        }

        return result;
    }

    /**
     * Returns the time that the specified file was last modified, possibly rounded down to the nearest second.
     */
    private long readTimestamp(File file)
    {
        if (!file.exists()) return FILE_DOES_NOT_EXIST_TIMESTAMP;

        return applyGranularity(file.lastModified());
    }

    private long applyGranularity(long timestamp)
    {
        // For coarse granularity (accurate only to the last second), remove the milliseconds since
        // the last full second. This is for compatibility with client HTTP requests, which
        // are only accurate to one second. The extra level of detail creates false positives
        // for changes, and undermines HTTP response caching in the client.

        if (granularitySeconds) return timestamp - (timestamp % 1000);

        return timestamp;
    }

    /**
     * Needed for testing; changes file timestamps so that a change will be detected by {@link #containsChanges()}.
     */
    public void forceChange()
    {
        for (Map.Entry<File, Long> e : fileToTimestamp.entrySet())
        {
            e.setValue(0l);
        }
    }

    /**
     * Needed for testing.
     */
    int trackedFileCount()
    {
        return fileToTimestamp.size();
    }

}
