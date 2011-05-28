// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.util.MultiKey;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;

/**
 * Service implementation that manages a cache of parsed component templates.
 */
public final class ComponentTemplateSourceImpl extends InvalidationEventHubImpl implements ComponentTemplateSource,
        UpdateListener
{
    private final TemplateParser parser;

    private final URLChangeTracker tracker;

    private final ComponentResourceLocator locator;

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
    };

    public ComponentTemplateSourceImpl(@Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    boolean productionMode, TemplateParser parser, ComponentResourceLocator locator,
            ClasspathURLConverter classpathURLConverter)
    {
        this(productionMode, parser, locator, new URLChangeTracker(classpathURLConverter));
    }

    ComponentTemplateSourceImpl(boolean productionMode, TemplateParser parser, ComponentResourceLocator locator,
            URLChangeTracker tracker)
    {
        super(productionMode);

        this.parser = parser;
        this.locator = locator;
        this.tracker = tracker;
    }

    @PostInjection
    public void registerAsUpdateListener(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
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
            result = parseTemplate(resource);
            templates.put(resource, result);
        }

        return result;
    }

    /**
     * Resolves the component name to a localized {@link Resource} (using the {@link ComponentTemplateLocator} chain of
     * command service). The localized resource is used as the key to a cache of {@link ComponentTemplate}s.
     * <p/>
     * If a template doesn't exist, then the missing ComponentTemplate is returned.
     */
    public ComponentTemplate getTemplate(ComponentModel componentModel, Locale locale)
    {
        return getTemplate(componentModel, new ComponentResourceSelector(locale));
    }

    private ComponentTemplate parseTemplate(Resource r)
    {
        // In a race condition, we may parse the same template more than once. This will likely add
        // the resource to the tracker multiple times. Not likely this will cause a big issue.

        if (!r.exists())
            return missingTemplate;

        tracker.add(r.toURL());

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
     */
    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            tracker.clear();
            templateResources.clear();
            templates.clear();
            fireInvalidationEvent();
        }
    }

    public InvalidationEventHub getInvalidationEventHub()
    {
        return this;
    }
}
