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

import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.util.MultiKey;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LocalizedNameGenerator;

import java.io.*;
import java.util.*;

/**
 * A utility class that encapsulates all the logic for reading properties files and assembling {@link Messages} from
 * them, in accordance with extension rules and locale. This represents code that was refactored out of {@link
 * ComponentMessagesSourceImpl}. This class can be used as a base class, though the existing code base uses it as a
 * utility. Composition trumps inheritance!
 * <p/>
 * The message catalog for a component is the combination of all appropriate properties files for the component, plus
 * any keys inherited form base components and, ultimately, the application global message catalog. At some point we
 * should add support for per-library message catalogs.
 * <p/>
 * Message catalogs are read using the UTF-8 character set. This is tricky in JDK 1.5; we read the file into memory then
 * feed that bytestream to Properties.load().
 */
public class MessagesSourceImpl extends InvalidationEventHubImpl implements MessagesSource
{
    private final URLChangeTracker tracker;

    /**
     * Keyed on bundle id and locale.
     */
    private final Map<MultiKey, Messages> messagesByBundleIdAndLocale = CollectionFactory.newConcurrentMap();

    /**
     * Keyed on bundle id and locale, the coooked properties include properties inherited from less locale-specific
     * properties files, or inherited from parent bundles.
     */
    private final Map<MultiKey, Map<String, String>> cookedProperties = CollectionFactory.newConcurrentMap();

    /**
     * Raw properties represent just the properties read from a specific properties file, in isolation.
     */
    private final Map<Resource, Map<String, String>> rawProperties = CollectionFactory.newConcurrentMap();

    private final Map<String, String> emptyMap = Collections.emptyMap();

    /**
     * Charset used when reading a properties file.
     */
    private static final String CHARSET = "UTF-8";

    /**
     * Buffer size used when reading a properties file.
     */
    private static final int BUFFER_SIZE = 2000;

    public MessagesSourceImpl(URLChangeTracker tracker)
    {
        this.tracker = tracker;
    }

    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            messagesByBundleIdAndLocale.clear();
            cookedProperties.clear();
            rawProperties.clear();

            tracker.clear();

            fireInvalidationEvent();
        }
    }

    public Messages getMessages(MessagesBundle bundle, Locale locale)
    {
        MultiKey key = new MultiKey(bundle.getId(), locale);

        Messages result = messagesByBundleIdAndLocale.get(key);

        if (result == null)
        {
            result = buildMessages(bundle, locale);
            messagesByBundleIdAndLocale.put(key, result);
        }

        return result;
    }

    private Messages buildMessages(MessagesBundle bundle, Locale locale)
    {
        Map<String, String> properties = findBundleProperties(bundle, locale);

        return new MapMessages(locale, properties);
    }

    /**
     * Assembles a set of properties appropriate for the bundle in question, and the desired locale. The properties
     * reflect the properties of the bundles' parent (if any) for the locale, overalyed with any properties defined for
     * this bundle and its locale.
     */
    private Map<String, String> findBundleProperties(MessagesBundle bundle, Locale locale)
    {
        if (bundle == null) return emptyMap;

        MultiKey key = new MultiKey(bundle.getId(), locale);

        Map<String, String> existing = cookedProperties.get(key);

        if (existing != null) return existing;

        // What would be cool is if we could maintain a cache of bundle id + locale -->
        // Resource. That would optimize quite a bit of this; may need to use an alternative to
        // LocalizedNameGenerator.

        Resource propertiesResource = bundle.getBaseResource().withExtension("properties");

        List<Resource> localizations = CollectionFactory.newList();

        for (String localizedFile : new LocalizedNameGenerator(propertiesResource.getFile(), locale))
        {
            Resource localized = propertiesResource.forFile(localizedFile);

            localizations.add(localized);
        }

        // We need them in least-specific to most-specific order, the opposite
        // of how the LocalizedNameGenerator provides them.

        Collections.reverse(localizations);

        // Localizations are now in least-specific to most-specific order.

        Map<String, String> previous = findBundleProperties(bundle.getParent(), locale);

        for (Resource localization : localizations)
        {
            Map<String, String> rawProperties = getRawProperties(localization);

            // Woould be nice to write into the cookedProperties cache here,
            // but we can't because we don't know the locale part of the MultiKey.

            previous = extend(previous, rawProperties);
        }

        cookedProperties.put(key, previous);

        return previous;
    }

    /**
     * Returns a new map consisting of all the properties in previous overlayed with all the properties in
     * rawProperties. If rawProperties is empty, returns just the base map.
     */
    private Map<String, String> extend(Map<String, String> base, Map<String, String> rawProperties)
    {
        if (rawProperties.isEmpty()) return base;

        // Make a copy of the base Map

        Map<String, String> result = CollectionFactory.newCaseInsensitiveMap(base);

        // Add or overwrite properties to the copy

        result.putAll(rawProperties);

        return result;
    }

    private Map<String, String> getRawProperties(Resource localization)
    {
        Map<String, String> result = rawProperties.get(localization);

        if (result == null)
        {
            result = readProperties(localization);

            rawProperties.put(localization, result);
        }

        return result;
    }

    /**
     * Creates and returns a new map that contains properties read from the properties file.
     */
    private Map<String, String> readProperties(Resource resource)
    {
        if (!resource.exists()) return emptyMap;

        tracker.add(resource.toURL());

        try
        {
            return readPropertiesFromStream(resource.openStream());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.failureReadingMessages(resource, ex), ex);
        }
    }

    static Map<String, String> readPropertiesFromStream(InputStream propertiesFileStream) throws IOException
    {
        Map<String, String> result = CollectionFactory.newCaseInsensitiveMap();

        Properties p = new Properties();
        InputStream is = null;

        try
        {

            is = readUTFStreamToEscapedASCII(propertiesFileStream);

            // Ok, now we have the content read into memory as UTF-8, not ASCII.

            p.load(is);

            is.close();

            is = null;
        }
        finally
        {
            InternalUtils.close(is);
        }

        for (Map.Entry e : p.entrySet())
        {
            String key = e.getKey().toString();

            String value = p.getProperty(key);

            result.put(key, value);
        }

        return result;
    }


    /**
     * Reads a UTF-8 stream, performing a conversion to ASCII (i.e., ISO8859-1 encoding). Characters outside the normal
     * range for ISO8859-1 are converted to unicode escapes. In effect, Tapestry is performing native2ascii on the
     * files, on the fly.
     */
    private static InputStream readUTFStreamToEscapedASCII(InputStream is) throws IOException
    {
        Reader reader = new InputStreamReader(is, CHARSET);

        StringBuilder builder = new StringBuilder(BUFFER_SIZE);
        char[] buffer = new char[BUFFER_SIZE];

        while (true)
        {
            int length = reader.read(buffer);

            if (length < 0) break;

            for (int i = 0; i < length; i++)
            {
                char ch = buffer[i];

                if (ch <= '\u007f')
                {
                    builder.append(ch);
                    continue;
                }

                builder.append(String.format("\\u%04x", (int) ch));
            }
        }

        reader.close();

        byte[] resourceContent = builder.toString().getBytes();

        return new ByteArrayInputStream(resourceContent);
    }
}
