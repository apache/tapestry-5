// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.ioc.util.LocalizedNameGenerator;
import org.apache.tapestry5.model.ComponentModel;

import java.util.List;

/**
 * A central service that encapsulates the rules for locating resources for components. The service can be
 * overridden, or simply decorated, to implement customized rules for locating templates across
 * one or more {@linkplain ComponentResourceSelector#getAxis(Class) axes}; this is the approach used to skin
 * Tapestry applications.
 *
 * @see org.apache.tapestry5.services.templates.ComponentTemplateLocator
 * @since 5.3
 */
public interface ComponentResourceLocator
{
    /**
     * Locates the template for a component (including pages and base classes). The implementation takes into
     * account the locale and other axes specified by the selector. If the method returns null, then the component
     * will have no template (which is common for components, but rare for pages).
     *
     * @param model
     *         defines the component, including its {@linkplain ComponentModel#getBaseResource() base resource}.
     * @param selector
     *         used to identify locale, etc., for the template
     * @return Resource for component template, or null if not found
     */
    Resource locateTemplate(ComponentModel model, ComponentResourceSelector selector);

    /**
     * Locates the properties files that make up the message catalog for a specific component. The properties
     * files are returned in order of specificity: the properties provided by the first resource override
     * properties in later resources. Only resources specific to the class associated with the model
     * should be concerned (message inheritance from base classes is handled by Tapestry).
     *
     * @param baseResource
     *         the resource for the base component properties file (i.e., with the ".properties" file extension)
     * @param selector
     *         defined the locale and other axes used to locate individual properties files
     * @return list of properties file Resources
     * @see LocalizedNameGenerator
     */
    List<Resource> locateMessageCatalog(Resource baseResource, ComponentResourceSelector selector);
}
