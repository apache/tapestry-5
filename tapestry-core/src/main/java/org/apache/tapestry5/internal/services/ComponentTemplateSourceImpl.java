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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.MultiKey;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.slf4j.Logger;

/**
 * Service implementation that manages a cache of parsed component templates.
 */
public final class ComponentTemplateSourceImpl extends InvalidationEventHubImpl implements ComponentTemplateSource,
        UpdateListener
{
    private final TemplateParser parser;

    private final URLChangeTracker<TemplateTrackingInfo> tracker;

    private final ComponentResourceLocator locator;
    
    private final ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer;
    
    private final ThreadLocale threadLocale;
    
    private final Logger logger;
    
    private final boolean multipleClassLoaders;

    /**
     * Caches from a key (combining component name and locale) to a resource. Often, many different keys will point to
     * the same resource (i.e., "foo:en_US", "foo:en_UK", and "foo:en" may all be parsed from the same "foo.tml"
     * resource). The resource may end up being null, meaning the template does not exist in any locale.
     */
    private final Map<MultiKey, Resource> templateResources = CollectionFactory.newConcurrentMap();

    /**
     * Cache of parsed templates, keyed on resource.
     */
    private final Map<Resource, ComponentTemplate> templates = CollectionFactory.newConcurrentMap();

    private final ComponentTemplate missingTemplate = new ComponentTemplate()
    {
        public Map<String, Location> getComponentIds()
        {
            return Collections.emptyMap();
        }

        public Resource getResource()
        {
            return null;
        }

        public List<TemplateToken> getTokens()
        {
            return Collections.emptyList();
        }

        public boolean isMissing()
        {
            return true;
        }

        public List<TemplateToken> getExtensionPointTokens(String extensionPointId)
        {
            return null;
        }

        public boolean isExtension()
        {
            return false;
        }

        public boolean usesStrictMixinParameters()
        {
            return false;
        }

        @Override
        public Set<String> getExtensionPointIds() 
        {
            return Collections.emptySet();
        }
        
    };

    public ComponentTemplateSourceImpl(@Inject
                                       @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                       boolean productionMode, 
                                       @Inject
                                       @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS)
                                       boolean multipleClassLoaders,                                        
                                       TemplateParser parser, ComponentResourceLocator locator,
                                       ClasspathURLConverter classpathURLConverter,
                                       ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer,
                                       ThreadLocale threadLocale, Logger logger)
    {
        this(productionMode, multipleClassLoaders, parser, locator, new URLChangeTracker<TemplateTrackingInfo>(classpathURLConverter), componentRequestSelectorAnalyzer, threadLocale, logger);
    }

    ComponentTemplateSourceImpl(boolean productionMode, boolean multipleClassLoaders, TemplateParser parser, ComponentResourceLocator locator,
                                URLChangeTracker<TemplateTrackingInfo> tracker, ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer,
                                ThreadLocale threadLocale, Logger logger)
    {
        super(productionMode, logger);

        this.parser = parser;
        this.locator = locator;
        this.tracker = tracker;
        this.componentRequestSelectorAnalyzer = componentRequestSelectorAnalyzer;
        this.threadLocale = threadLocale;
        this.logger = logger;
        this.multipleClassLoaders = multipleClassLoaders;
    }

    @PostInjection
    public void registerAsUpdateListener(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
    }

    @PostInjection
    public void setupReload(ReloadHelper helper)
    {
        helper.addReloadCallback(new Runnable()
        {
            public void run()
            {
                invalidate();
            }
        });
    }

    public ComponentTemplate getTemplate(ComponentModel componentModel, ComponentResourceSelector selector)
    {
        String componentName = componentModel.getComponentClassName();

        MultiKey key = new MultiKey(componentName, selector);

        // First cache is key to resource.

        Resource resource = templateResources.get(key);

        if (resource == null)
        {
            resource = locateTemplateResource(componentModel, selector);
            templateResources.put(key, resource);
        }

        // If we haven't yet parsed the template into the cache, do so now.

        ComponentTemplate result = templates.get(resource);

        if (result == null)
        {
            result = parseTemplate(resource, componentModel.getComponentClassName());
            templates.put(resource, result);
        }

        return result;
    }

    /**
     * Resolves the component name to a localized {@link Resource} (using the {@link ComponentTemplateLocator} chain of
     * command service). The localized resource is used as the key to a cache of {@link ComponentTemplate}s.
     *
     * If a template doesn't exist, then the missing ComponentTemplate is returned.
     */
    public ComponentTemplate getTemplate(ComponentModel componentModel, Locale locale)
    {
        final Locale original = threadLocale.getLocale();
        try
        {
            threadLocale.setLocale(locale);
            return getTemplate(componentModel, componentRequestSelectorAnalyzer.buildSelectorForRequest());
        }
        finally {
            threadLocale.setLocale(original);
        }
    }

    private ComponentTemplate parseTemplate(Resource r, String className)
    {
        // In a race condition, we may parse the same template more than once. This will likely add
        // the resource to the tracker multiple times. Not likely this will cause a big issue.

        if (!r.exists())
            return missingTemplate;

        tracker.add(r.toURL(), new TemplateTrackingInfo(r.getPath(), className));

        return parser.parseTemplate(r);
    }

    private Resource locateTemplateResource(ComponentModel initialModel, ComponentResourceSelector selector)
    {
        ComponentModel model = initialModel;
        while (model != null)
        {
            Resource localized = locator.locateTemplate(model, selector);

            if (localized != null)
                return localized;

            // Otherwise, this component doesn't have its own template ... lets work up to its
            // base class and check there.

            model = model.getParentModel();
        }

        // This will be a Resource whose URL is null, which will be picked up later and force the
        // return of the empty template.

        return initialModel.getBaseResource().withExtension(TapestryConstants.TEMPLATE_EXTENSION);
    }

    /**
     * Checks to see if any parsed resource has changed. If so, then all internal caches are cleared, and an
     * invalidation event is fired. This is brute force ... a more targeted dependency management strategy may come
     * later.
     * Actually, TAP5-2742 did exactly that! :D
     */
    public void checkForUpdates()
    {
        final Set<TemplateTrackingInfo> changedResourcesInfo = tracker.getChangedResourcesInfo();
        if (!changedResourcesInfo.isEmpty())
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Changed template(s) found: {}", String.join(", ", 
                        changedResourcesInfo.stream().map(TemplateTrackingInfo::getTemplate).collect(Collectors.toList())));
            }
            
            if (multipleClassLoaders)
            {
            
                final Iterator<Entry<MultiKey, Resource>> templateResourcesIterator = templateResources.entrySet().iterator();
                for (TemplateTrackingInfo info : changedResourcesInfo) 
                {
                    while (templateResourcesIterator.hasNext())
                    {
                        final MultiKey key = templateResourcesIterator.next().getKey();
                        if (info.getClassName().equals((String) key.getValues()[0]))
                        {
                            templates.remove(templateResources.get(key));
                            templateResourcesIterator.remove();
                        }
                    }
                }
                
                fireInvalidationEvent(changedResourcesInfo.stream().map(TemplateTrackingInfo::getClassName).collect(Collectors.toList()));
                
            }
            else
            {
                invalidate();
            }
        }
    }

    private void invalidate()
    {
        tracker.clear();
        templateResources.clear();
        templates.clear();
        fireInvalidationEvent();
    }

    public InvalidationEventHub getInvalidationEventHub()
    {
        return this;
    }
}
