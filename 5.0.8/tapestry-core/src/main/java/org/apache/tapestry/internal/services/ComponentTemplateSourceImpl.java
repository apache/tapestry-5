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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry.internal.events.UpdateListener;
import org.apache.tapestry.internal.parser.ComponentTemplate;
import org.apache.tapestry.internal.parser.TemplateToken;
import org.apache.tapestry.internal.util.MultiKey;
import org.apache.tapestry.internal.util.URLChangeTracker;
import org.apache.tapestry.ioc.Resource;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry.model.ComponentModel;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Service implementation that manages a cache of parsed component templates.
 */
public final class ComponentTemplateSourceImpl extends InvalidationEventHubImpl implements ComponentTemplateSource, UpdateListener
{

    private final TemplateParser _parser;

    private final PageTemplateLocator _locator;

    private final URLChangeTracker _tracker;

    /**
     * Caches from a key (combining component name and locale) to a resource. Often, many different keys will point to
     * the same resource (i.e., "foo:en_US", "foo:en_UK", and "foo:en" may all be parsed from the same "foo.tml"
     * resource). The resource may end up being null, meaning the template does not exist in any locale.
     */
    private final Map<MultiKey, Resource> _templateResources = newConcurrentMap();

    /**
     * Cache of parsed templates, keyed on resource.
     */
    private final Map<Resource, ComponentTemplate> _templates = newConcurrentMap();

    private final ComponentTemplate _missingTemplate = new ComponentTemplate()
    {
        public Set<String> getComponentIds()
        {
            return null;
        }

        public Resource getResource()
        {
            return null;
        }

        public List<TemplateToken> getTokens()
        {
            return null;
        }

        public boolean isMissing()
        {
            return true;
        }
    };

    public ComponentTemplateSourceImpl(TemplateParser parser, PageTemplateLocator locator)
    {
        this(parser, locator, new URLChangeTracker());
    }

    ComponentTemplateSourceImpl(TemplateParser parser, PageTemplateLocator locator, URLChangeTracker tracker)
    {
        _parser = parser;
        _locator = locator;
        _tracker = tracker;
    }

    /**
     * Resolves the component name to a {@link Resource} and finds the localization of that resource (the combination of
     * component name and locale is resolved to a resource). The localized resource is used as the key to a cache of
     * {@link ComponentTemplate}s.
     * <p/>
     * If a template doesn't exist, then the missing ComponentTemplate is returned.
     */
    public ComponentTemplate getTemplate(ComponentModel componentModel, Locale locale)
    {
        String componentName = componentModel.getComponentClassName();

        MultiKey key = new MultiKey(componentName, locale);

        // First cache is key to resource.

        Resource resource = _templateResources.get(key);

        if (resource == null)
        {
            resource = locateTemplateResource(componentModel, locale);
            _templateResources.put(key, resource);
        }

        // If we haven't yet parsed the template into the cache, do so now.

        ComponentTemplate result = _templates.get(resource);

        if (result == null)
        {
            result = parseTemplate(resource);
            _templates.put(resource, result);
        }

        return result;
    }

    private ComponentTemplate parseTemplate(Resource r)
    {
        // In a race condition, we may parse the same template more than once. This will likely add
        // the resource to the tracker multiple times. Not likely this will cause a big issue.

        URL resourceURL = r.toURL();

        if (resourceURL == null) return _missingTemplate;

        _tracker.add(resourceURL);

        return _parser.parseTemplate(r);
    }

    private Resource locateTemplateResource(ComponentModel initialModel, Locale locale)
    {
        ComponentModel model = initialModel;
        while (model != null)
        {

            Resource baseResource = baseResourceForModel(model);
            Resource localized = baseResource.forLocale(locale);

            // In a race condition, we may hit this method a couple of times, and overwrite previous
            // results with identical new results.

            // If found a properly localized version of the base resource for the model,
            // then we've found a match (even if we had to ascend a couple of levels
            // to reach it).

            if (localized != null) return localized;

            // Not on the classpath, the the locator see if its a) a page and b) a resource inside
            // the context

            localized = _locator.findPageTemplateResource(model, locale);

            if (localized != null) return localized;

            // Otherwise, this component doesn't have its own template ... lets work up to its
            // base class and check there.

            model = model.getParentModel();
        }

        // This will be a Resource whose URL is null, which will be picked up later and force the
        // return of the empty template.

        return baseResourceForModel(initialModel);
    }

    private Resource baseResourceForModel(ComponentModel model)
    {
        return model.getBaseResource().withExtension(InternalConstants.TEMPLATE_EXTENSION);
    }

    /**
     * Checks to see if any parsed resource has changed. If so, then all internal caches are cleared, and an
     * invalidation event is fired. This is brute force ... a more targeted dependency management strategy may come
     * later.
     */
    public void checkForUpdates()
    {
        if (_tracker.containsChanges())
        {
            _tracker.clear();
            _templateResources.clear();
            _templates.clear();
            fireInvalidationEvent();
        }
    }
}
