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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CaseInsensitiveMap;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.MultiKey;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

/**
 * A utility class that encapsulates all the logic for reading properties files and assembling {@link Messages} from
 * them, in accordance with extension rules and locale. This represents code that was refactored out of
 * {@link ComponentMessagesSourceImpl}. This class can be used as a base class, though the existing code base uses it as
 * a utility. Composition trumps inheritance!
 *
 * The message catalog for a component is the combination of all appropriate properties files for the component, plus
 * any keys inherited form base components and, ultimately, the application global message catalog. At some point we
 * should add support for per-library message catalogs.
 *
 * Message catalogs are read using the UTF-8 character set. This is tricky in JDK 1.5; we read the file into memory then
 * feed that bytestream to Properties.load().
 */
public class MessagesSourceImpl extends InvalidationEventHubImpl implements MessagesSource
{
    private final URLChangeTracker<MessagesTrackingInfo> tracker;

    private final PropertiesFileParser propertiesFileParser;

    private final ComponentResourceLocator resourceLocator;
    
    private final ComponentClassResolver componentClassResolver;
    
    private final boolean multipleClassLoaders;
    
    private final Logger logger;        
    
    /**
     * Keyed on bundle id and ComponentResourceSelector.
     */
    private final Map<MultiKey, Messages> messagesByBundleIdAndSelector = CollectionFactory.newConcurrentMap();

    /**
     * Keyed on bundle id and ComponentResourceSelector, the cooked properties include properties inherited from less
     * locale-specific properties files, or inherited from parent bundles.
     */
    private final Map<MultiKey, Map<String, String>> cookedProperties = CollectionFactory.newConcurrentMap();

    /**
     * Raw properties represent just the properties read from a specific properties file, in isolation.
     */
    private final Map<Resource, Map<String, String>> rawProperties = CollectionFactory.newConcurrentMap();

    private final Map<String, String> emptyMap = Collections.emptyMap();

    public MessagesSourceImpl(boolean productionMode, boolean multipleClassLoaders, URLChangeTracker tracker,
                              ComponentResourceLocator resourceLocator, PropertiesFileParser propertiesFileParser,
                              ComponentClassResolver componentClassResolver,
                              Logger logger)
    {
        super(productionMode, logger);

        this.tracker = tracker;
        this.propertiesFileParser = propertiesFileParser;
        this.resourceLocator = resourceLocator;
        this.logger = logger;
        this.componentClassResolver = componentClassResolver;
        this.multipleClassLoaders = multipleClassLoaders;
    }

    public void checkForUpdates()
    {
        if (tracker != null)
        {
            final Set<MessagesTrackingInfo> changedResources = tracker.getChangedResourcesInfo();
            if (!changedResources.isEmpty() && logger.isInfoEnabled())
            {
                logger.info("Changed message file(s): {}", changedResources.stream()
                        .map(MessagesTrackingInfo::getResource)
                        .map(Resource::toString)
                        .collect(Collectors.joining(", ")));
            }
            
            boolean applicationLevelChange = false;
            
            for (MessagesTrackingInfo info : changedResources) 
            {
                
                final String className = info.getClassName();
                
                // An application-level file was changed, so we need to invalidate everything.
                if (className == null || !multipleClassLoaders)
                {
                    invalidate();
                    applicationLevelChange = true;
                    break;
                }
                else
                {
                    
                    final Iterator<Entry<MultiKey, Messages>> messagesByBundleIdAndSelectorIterator = 
                            messagesByBundleIdAndSelector.entrySet().iterator();
                    
                    while (messagesByBundleIdAndSelectorIterator.hasNext())
                    {
                        final Entry<MultiKey, Messages> entry = messagesByBundleIdAndSelectorIterator.next();
                        if (className.equals(entry.getKey().getValues()[0]))
                        {
                            messagesByBundleIdAndSelectorIterator.remove();
                        }
                    }
                    
                    final Iterator<Entry<MultiKey, Map<String, String>>> cookedPropertiesIterator = 
                            cookedProperties.entrySet().iterator();
                    
                    while (cookedPropertiesIterator.hasNext())
                    {
                        final Entry<MultiKey, Map<String, String>> entry = cookedPropertiesIterator.next();
                        if (className.equals(entry.getKey().getValues()[0]))
                        {
                            cookedPropertiesIterator.remove();
                        }
                    }
                    
                    final String resourceFile = info.getResource().getFile();
                    final Iterator<Entry<Resource, Map<String, String>>> rawPropertiesIterator = rawProperties.entrySet().iterator();
                    while (rawPropertiesIterator.hasNext())
                    {
                        final Entry<Resource, Map<String, String>> entry = rawPropertiesIterator.next();
                        if (resourceFile.equals(entry.getKey().getFile()))
                        {
                            rawPropertiesIterator.remove();
                        }
                    }
                    
                }
            }
            
            if (!changedResources.isEmpty() && !applicationLevelChange)
            {
                fireInvalidationEvent(changedResources.stream()
                        .filter(Objects::nonNull)
                        .map(ClassNameHolder::getClassName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
        }
    }

    public void invalidate()
    {
        messagesByBundleIdAndSelector.clear();
        cookedProperties.clear();
        rawProperties.clear();

        tracker.clear();

        fireInvalidationEvent();
    }

    public Messages getMessages(MessagesBundle bundle, ComponentResourceSelector selector)
    {
        MultiKey key = new MultiKey(bundle.getId(), selector);

        Messages result = messagesByBundleIdAndSelector.get(key);

        if (result == null)
        {
            result = buildMessages(bundle, selector);
            messagesByBundleIdAndSelector.put(key, result);
        }

        return result;
    }

    private Messages buildMessages(MessagesBundle bundle, ComponentResourceSelector selector)
    {
        Map<String, String> properties = findBundleProperties(bundle, selector);

        return new MapMessages(selector.locale, properties);
    }

    /**
     * Assembles a set of properties appropriate for the bundle in question, and the desired locale. The properties
     * reflect the properties of the bundles' parent (if any) for the locale, overalyed with any properties defined for
     * this bundle and its locale.
     */
    private Map<String, String> findBundleProperties(MessagesBundle bundle, ComponentResourceSelector selector)
    {
        if (bundle == null)
            return emptyMap;

        MultiKey key = new MultiKey(bundle.getId(), selector);

        Map<String, String> existing = cookedProperties.get(key);

        if (existing != null)
            return existing;

        // What would be cool is if we could maintain a cache of bundle id + locale -->
        // Resource. That would optimize quite a bit of this; may need to use an alternative to
        // LocalizedNameGenerator.

        Resource propertiesResource = bundle.getBaseResource().withExtension("properties");

        List<Resource> localizations = resourceLocator.locateMessageCatalog(propertiesResource, selector);

        // Localizations are now in least-specific to most-specific order.

        Map<String, String> previous = findBundleProperties(bundle.getParent(), selector);

        for (Resource localization : F.flow(localizations).reverse())
        {
            Map<String, String> rawProperties = getRawProperties(localization, bundle);

            // Would be nice to write into the cookedProperties cache here,
            // but we can't because we don't know the selector part of the MultiKey.

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
        if (rawProperties.isEmpty())
            return base;

        // Make a copy of the base Map

        Map<String, String> result = new CaseInsensitiveMap<String>(base);

        // Add or overwrite properties to the copy

        result.putAll(rawProperties);

        return result;
    }

    private Map<String, String> getRawProperties(Resource localization, MessagesBundle bundle)
    {
        Map<String, String> result = rawProperties.get(localization);

        if (result == null)
        {
            result = readProperties(localization, bundle);

            rawProperties.put(localization, result);
        }

        return result;
    }

    /**
     * Creates and returns a new map that contains properties read from the properties file.
     * @param bundle 
     */
    private Map<String, String> readProperties(Resource resource, MessagesBundle bundle)
    {
        if (!resource.exists())
            return emptyMap;

        if (tracker != null)
        {
            MessagesTrackingInfo info = new MessagesTrackingInfo(
                    resource, bundle != null ? bundle.getId() : bundle, getClassName(bundle));
            tracker.add(resource.toURL(), info);
        }

        try
        {
            return propertiesFileParser.parsePropertiesFile(resource);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to read message catalog from %s: %s", resource, ex), ex);
        }
    }

    private String getClassName(MessagesBundle bundle)
    {
        String className = null;
        if (bundle != null && bundle.getBaseResource().getPath() != null)
        {
            final String path = bundle.getBaseResource().getPath();
            if (path.endsWith(".class"))
            {
                className = path.replace('/', '.').replace(".class", "");
                if (!componentClassResolver.isPage(className)) 
                {
                    className = null;
                }
            }
        }
        return className;
    }

}
