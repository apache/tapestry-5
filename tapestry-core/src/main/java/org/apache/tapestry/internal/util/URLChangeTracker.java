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

package org.apache.tapestry.internal.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Given a (growing) set of URLs, can periodically check to see if any of the underlying resources
 * has changed. This class is capable of using either millisecond-level granularity or second-level
 * granularity. Millisecond-level granularity is used by default. Second-level granularity is
 * provided for compatibility with browsers vis-a-vis resource caching -- that's how granular they
 * get with their "If-Modified-Since", "Last-Modified" and "Expires" headers.
 */
public class URLChangeTracker
{
    private static final long FILE_DOES_NOT_EXIST_TIMESTAMP = -1l;

    private final Map<File, Long> _fileToTimestamp = newConcurrentMap();

    private boolean _granularitySeconds;

    /**
     * Creates a new URL change tracker with millisecond-level granularity.
     */
    public URLChangeTracker()
    {
        this(false);
    }

    /**
     * Creates a new URL change tracker, using either millisecond-level granularity or second-level
     * granularity.
     * 
     * @param granularitySeconds
     *            whether or not to use second-level granularity
     */
    public URLChangeTracker(boolean granularitySeconds)
    {
        _granularitySeconds = granularitySeconds;
    }

    /**
     * Stores a new URL into the tracker, or returns the previous time stamp for a previously added
     * URL. Filters out all non-file URLs.
     * 
     * @param url
     *            of the resource to add
     * @return the current timestamp for the URL, or 0 if not a file URL
     */
    public long add(URL url)
    {
        if (!url.getProtocol().equals("file")) return 0;

        try
        {
            URI resourceURI = url.toURI();
            File resourceFile = new File(resourceURI);

            if (_fileToTimestamp.containsKey(resourceFile))
                return _fileToTimestamp.get(resourceFile);

            long timestamp = readTimestamp(resourceFile);

            _fileToTimestamp.put(resourceFile, timestamp);

            return timestamp;
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Clears all URL and timestamp data stored in the tracker.
     */
    public void clear()
    {
        _fileToTimestamp.clear();
    }

    /**
     * Re-acquires the last updated timestamp for each URL and returns true if any timestamp has
     * changed.
     */
    public boolean containsChanges()
    {
        boolean result = false;

        // This code would be highly suspect if this method was expected to be invoked
        // concurrently, but CheckForUpdatesFilter ensures that it will be invoked
        // synchronously.

        for (Map.Entry<File, Long> entry : _fileToTimestamp.entrySet())
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
     * Returns the time that the specified file was last modified, possibly rounded down to the
     * nearest second.
     */
    private long readTimestamp(File file)
    {
        if (!file.exists()) return FILE_DOES_NOT_EXIST_TIMESTAMP;

        long timestamp = file.lastModified();
        if (_granularitySeconds) timestamp -= timestamp % 1000;
        return timestamp;
    }

    /**
     * Needed for testing; changes file timestamps so that a change will be detected by
     * {@link #containsChanges()}.
     */
    public void forceChange()
    {
        for (Map.Entry<File, Long> e : _fileToTimestamp.entrySet())
        {
            e.setValue(0l);
        }
    }

    /** Needed for testing. */
    int trackedFileCount()
    {
        return _fileToTimestamp.size();
    }

}
