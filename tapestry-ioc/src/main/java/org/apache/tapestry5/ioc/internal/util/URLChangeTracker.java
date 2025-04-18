// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;

/**
 * Given a (growing) set of URLs, can periodically check to see if any of the underlying resources has changed. This
 * class is capable of using either millisecond-level granularity or second-level granularity. Millisecond-level
 * granularity is used by default. Second-level granularity is provided for compatibility with browsers vis-a-vis
 * resource caching -- that's how granular they get with their "If-Modified-Since", "Last-Modified" and "Expires"
 * headers.
 * 
 * @param <T> The type of the optional information about the tracked resource. This type should
 * implement <code>equals()</code> and <code>hashCode()</code>.
 */
public class URLChangeTracker<T>
{
    private static final long FILE_DOES_NOT_EXIST_TIMESTAMP = -1L;

    private final Map<File, TrackingInfo> fileToTimestamp = CollectionFactory.newConcurrentMap();

    private final boolean granularitySeconds;

    private final boolean trackFolderChanges;

    private final ClasspathURLConverter classpathURLConverter;

    public static final ClasspathURLConverter DEFAULT_CONVERTER = new ClasspathURLConverterImpl();

    /**
     * Creates a tracker using the default (does nothing) URL converter, with default (millisecond)
     * granularity and folder tracking disabled.
     * 
     * @since 5.2.1
     */
    public URLChangeTracker()
    {
        this(DEFAULT_CONVERTER, false, false);
    }

    /**
     * Creates a new URL change tracker with millisecond-level granularity and folder checking enabled.
     * 
     * @param classpathURLConverter
     *            used to convert URLs from one protocol to another
     */
    public URLChangeTracker(ClasspathURLConverter classpathURLConverter)
    {
        this(classpathURLConverter, false);

    }

    /**
     * Creates a new URL change tracker, using either millisecond-level granularity or second-level granularity and
     * folder checking enabled.
     * 
     * @param classpathURLConverter
     *            used to convert URLs from one protocol to another
     * @param granularitySeconds
     *            whether or not to use second granularity (as opposed to millisecond granularity)
     */
    public URLChangeTracker(ClasspathURLConverter classpathURLConverter, boolean granularitySeconds)
    {
        this(classpathURLConverter, granularitySeconds, true);
    }

    /**
     * Creates a new URL change tracker, using either millisecond-level granularity or second-level granularity.
     * 
     * @param classpathURLConverter
     *            used to convert URLs from one protocol to another
     * @param granularitySeconds
     *            whether or not to use second granularity (as opposed to millisecond granularity)
     * @param trackFolderChanges
     *            if true, then adding a file URL will also track the folder containing the file (this
     *            is useful when concerned about additions to a folder)
     * @since 5.2.1
     */
    public URLChangeTracker(ClasspathURLConverter classpathURLConverter, boolean granularitySeconds,
            boolean trackFolderChanges)
    {
        this.granularitySeconds = granularitySeconds;
        this.classpathURLConverter = classpathURLConverter;
        this.trackFolderChanges = trackFolderChanges;
    }

    /**
     * Converts a URL with protocol "file" to a File instance.
     *
     * @since 5.2.0
     */
    public static File toFileFromFileProtocolURL(URL url)
    {
        assert url != null;

        if (!url.getProtocol().equals("file"))
            throw new IllegalArgumentException(String.format("URL %s does not use the 'file' protocol.", url));

        // http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html

        try
        {
            return new File(url.toURI());
        } catch (URISyntaxException ex)
        {
            return new File(url.getPath());
        }
    }

    /**
     * Stores a new URL into the tracker, or returns the previous time stamp for a previously added URL. Filters out all
     * non-file URLs.
     * 
     * @param url
     *            of the resource to add, or null if not known
     * @return the current timestamp for the URL (possibly rounded off for granularity reasons), or 0 if the URL is
     *         null
     */
    public long add(URL url)
    {
        return add(url, null);
    }
    /**
     * Stores a new URL and associated memo (most probably a related class name)
     * into the tracker, or returns the previous time stamp for a previously added URL. Filters out all
     * non-file URLs.
     * 
     * @param url
     *            of the resource to add, or null if not known
     * @param resourceInfo
     *            an optional object containing information about the tracked URL. It's
     *            returned in the {@link #getChangedResourcesInfo()} method.
     * @return the current timestamp for the URL (possibly rounded off for granularity reasons), or 0 if the URL is
     *         null
     * @since 5.8.3
     */
    public long add(URL url, T resourceInfo)
    {
        if (url == null)
            return 0;

        URL converted = classpathURLConverter.convert(url);

        if (!converted.getProtocol().equals("file"))
            return timestampForNonFileURL(converted);

        File resourceFile = toFileFromFileProtocolURL(converted);

        if (fileToTimestamp.containsKey(resourceFile))
            return fileToTimestamp.get(resourceFile).timestamp;

        long timestamp = readTimestamp(resourceFile);

        // A quick and imperfect fix for TAPESTRY-1918. When a file
        // is added, add the directory containing the file as well.

        fileToTimestamp.put(resourceFile, new TrackingInfo(timestamp, resourceInfo));

        if (trackFolderChanges)
        {
            File dir = resourceFile.getParentFile();

            if (!fileToTimestamp.containsKey(dir))
            {
                long dirTimestamp = readTimestamp(dir);
                fileToTimestamp.put(dir, new TrackingInfo(dirTimestamp, null));
            }
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

        for (Map.Entry<File, TrackingInfo> entry : fileToTimestamp.entrySet())
        {
            long newTimestamp = readTimestamp(entry.getKey());
            long current = entry.getValue().timestamp;

            if (current == newTimestamp)
                continue;

            result = true;
            entry.getValue().timestamp = newTimestamp;
        }

        return result;
    }
    
    /**
     * Re-acquires the last updated timestamp for each URL and returns the non-null resource information for all files with a changed timestamp.
     */
    public Set<T> getChangedResourcesInfo()
    {
        
        Set<T> changedResourcesInfo = new HashSet<>();

        for (Map.Entry<File, TrackingInfo> entry : fileToTimestamp.entrySet())
        {
            long newTimestamp = readTimestamp(entry.getKey());
            final TrackingInfo value = entry.getValue();
            long current = value.timestamp;

            if (current != newTimestamp)
            {
                if (value.resourceInfo != null)
                {
                    changedResourcesInfo.add(value.resourceInfo);
                }
                value.timestamp = newTimestamp;
            }
        }

        return changedResourcesInfo;
    }


    /**
     * Returns the time that the specified file was last modified, possibly rounded down to the nearest second.
     */
    private long readTimestamp(File file)
    {
        if (!file.exists())
            return FILE_DOES_NOT_EXIST_TIMESTAMP;

        return applyGranularity(file.lastModified());
    }

    private long applyGranularity(long timestamp)
    {
        // For coarse granularity (accurate only to the last second), remove the milliseconds since
        // the last full second. This is for compatibility with client HTTP requests, which
        // are only accurate to one second. The extra level of detail creates false positives
        // for changes, and undermines HTTP response caching in the client.

        if (granularitySeconds)
            return timestamp - (timestamp % 1000);

        return timestamp;
    }

    /**
     * Needed for testing; changes file timestamps so that a change will be detected by {@link #containsChanges()}.
     */
    public void forceChange()
    {
        for (Map.Entry<File, TrackingInfo> e : fileToTimestamp.entrySet())
        {
            e.getValue().timestamp = 0l;
        }
    }

    /**
     * Needed for testing.
     */
    int trackedFileCount()
    {
        return fileToTimestamp.size();
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        final List<File> files = new ArrayList<>(fileToTimestamp.keySet());
        Collections.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        
        for (File file : files)
        {
            builder.append(file.getName());
            builder.append(": ");
            builder.append(fileToTimestamp.get(file));
            builder.append("\n");
        }
        
        return builder.toString();
    }
    
    private final class TrackingInfo
    {
        
        private long timestamp;
        private T resourceInfo;

        public TrackingInfo(long timestamp, T resourceInfo) 
        {
            this.timestamp = timestamp;
            this.resourceInfo = resourceInfo;
        }

        @Override
        public String toString() 
        {
            return "Info [timestamp=" + timestamp + ", resourceInfo=" + resourceInfo + "]";
        }
        
    }

}
