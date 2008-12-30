// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationEventHub;

import java.util.Locale;

/**
 * Provides access to cached {@link org.apache.tapestry5.internal.parser.ComponentTemplate}s. The source acts as a
 * invalidation event hub, and will broadcast invalidation events when any loaded template resource changes. The
 * listener for these invalidation events is the page source, which stores cached page instances.
 * <p/>
 * Any search for a template will end with success (a non-null template), but the template returned may be the {@link
 * ComponentTemplate#isMissing() missing template}.
 */
public interface ComponentTemplateSource
{
    /**
     * Provides access to a template. The template will be parsed as necessary. If no template for the exact component
     * is found, then the template for the component's parent is returned. In this way, it is possible for a component
     * to extend the behavior of its super-class without duplicating the super-class component's template.
     * <p/>
     * In some cases, the empty template will be returned.
     *
     * @param componentModel model for the component whose template is to be accessed
     * @param locale         the locale to find the template within
     * @return the cached template instance
     */
    ComponentTemplate getTemplate(ComponentModel componentModel, Locale locale);

    /**
     * Event hub used to notify listeners that underlying component template files have changed.
     *
     * @see org.apache.tapestry5.services.ComponentTemplates
     * @since 5.1.0.0
     */
    InvalidationEventHub getInvalidationEventHub();
}
