// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeMap;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Given a (growing) set of URLs, can periodically check to see if any of the underlying resources
 * has changed.
 */
public class URLChangeTracker
{
    private final Map<URL, Long> _urlToTimestamp = newThreadSafeMap();

    /**
     * Stores a new URL into the tracker, or returns the previous time stamp for a previously added
     * URL.
     * 
     * @param url
     *            of the resource to add
     * @return the current timestamp for the URL
     */
    public long add(URL url)
    {
        if (_urlToTimestamp.containsKey(url))
            return _urlToTimestamp.get(url);

        long timestamp = readTimestamp(url);

        _urlToTimestamp.put(url, timestamp);

        return timestamp;
    }

    /**
     * Clears all URL and timestamp data stored in the tracker.
     */
    public void clear()
    {
        _urlToTimestamp.clear();
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
        
        for (Map.Entry<URL, Long> entry : _urlToTimestamp.entrySet())
        {
            long newTimestamp = readTimestamp(entry.getKey());
            long current = entry.getValue();

            if (current == newTimestamp)
                continue;

            result = true;
            entry.setValue(newTimestamp);
        }

        return result;
    }

    private long readTimestamp(URL url)
    {
        try
        {
            URLConnection connection = url.openConnection();

            connection.connect();

            long result = connection.getLastModified();

            // System.out.println(url + " --> " + result);

            // So ... do you ever close the connection? So far, no problems using it like this.
            // It must have a finalize() to close the connection if that's even necessary.

            return result;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(UtilMessages.unableToReadLastModified(url, ex), ex);
        }

    }

    /**
     * Needed for testing; changes file timestamps so that a change will be detected by
     * {@link #containsChanges()}.
     */
    public void forceChange()
    {
        for (Map.Entry<URL, Long> e : _urlToTimestamp.entrySet())
        {
            e.setValue(0l);
        }
    }
}
