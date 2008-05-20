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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.internal.parser.AttributeToken;
import org.apache.tapestry5.internal.parser.ExpansionToken;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.structure.PageElement;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.services.BindingSource;

import java.util.Locale;

/**
 * Used by the {@link org.apache.tapestry5.internal.services.PageLoader} to create page elements
 */
public interface PageElementFactory
{
    PageElement newAttributeElement(ComponentResources componentResources, AttributeToken token);

    PageElement newExpansionElement(ComponentResources componentResources, ExpansionToken token);

    /**
     * Creates a new binding as with {@link BindingSource#newBinding(String, ComponentResources, ComponentResources,
     * String, String, Location)}. However, if the binding contains an expansion (i.e., <code>${...}</code>), then a
     * binding that returns the fully expanded expression will be returned.
     */
    Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                       ComponentResources embeddedComponentResources, String defaultBindingPrefix, String expression,
                       Location location);

    /**
     * Creates a new component and adds it to the page and to its container.
     * <p/>
     * Note: doesn't add the component as a child of the container.
     *
     * @param page               the page that will ultimately contain the new component
     * @param container          the existing component that contains the new component
     * @param id                 the id, unique within the container, of the new component
     * @param componentType      the type of the component (as defined in the template or the {@link Component}
     *                           annotation)
     * @param componentClassName the fully qualfied class name used when the componentType is blank (null or the empty
     *                           string)
     * @param elementName        name of element in template
     * @param location           location of the component's element within its container's template
     * @return the newly created component page element, after adding it to the page and container
     */
    ComponentPageElement newComponentElement(Page page, ComponentPageElement container, String id, String componentType,
                                             String componentClassName, String elementName, Location location);

    /**
     * Creates a new root component for a page. Adds any mixins defined by the components model.
     *
     * @param page      the page that will contain the root component
     * @param className the fully qualified class name of the root component
     * @param locale    the locale for the page
     * @return the root page element
     */
    ComponentPageElement newRootComponentElement(Page page, String className, Locale locale);

    /**
     * Adds a mixin to the element, resolving the mixin type to a mixin class.
     * <p/>
     * Sure, this isn't quite a <em>factory</em> method, but PEF has all the tools to accomplish this handy, as opposed
     * to PageLoaderImpl.
     *
     * @param component the component to which a mixin will be added
     * @param mixinType used to resolve the mixin class name
     */
    void addMixinByTypeName(ComponentPageElement component, String mixinType);

    /**
     * Adds a mixin to the element.
     * <p/>
     * Sure, this isn't quite a <em>factory</em> method, but PEF has all the tools to accomplish this handy, as opposed
     * to PageLoaderImpl.
     *
     * @param component      the component to which a mixin will be added
     * @param mixinClassName fully qualified class name of the mixin
     */
    void addMixinByClassName(ComponentPageElement component, String mixinClassName);
}
