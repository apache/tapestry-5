// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.parser.AttributeToken;
import org.apache.tapestry5.internal.parser.ExpansionToken;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.BindingSource;

/**
 * Used by the {@link org.apache.tapestry5.internal.services.PageLoader} to create partialar page elements. This has
 * evolved and focused to mostly concern bindings and expansions.
 */
public interface PageElementFactory
{
    /**
     * Creates a RenderCommand for rendering an attribute, when the attribute contains expansions.
     *
     * @param componentResources identifies component
     * @param token              token containing value with expansions
     * @return render command to render the text with expansions expanded
     */
    RenderCommand newAttributeElement(ComponentResources componentResources, AttributeToken token);

    /**
     * Converts an expansion token into a command that renders the expanded value.
     *
     * @param componentResources identifies the component
     * @param token              contains expansion expression
     * @return command to render expansion
     */
    RenderCommand newExpansionElement(ComponentResources componentResources, ExpansionToken token);

    /**
     * Creates a new binding as with {@link BindingSource#newBinding(String, ComponentResources, ComponentResources,
     * String, String, Location)}. However, if the binding contains an expansion (i.e., <code>${...}</code>), then a
     * binding that returns the fully expanded expression will be returned.
     */
    Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                       ComponentResources embeddedComponentResources, String defaultBindingPrefix, String expression,
                       Location location);

}
