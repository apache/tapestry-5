// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.services.PageRenderSupport;

/**
 * The Any component is a swiss-army knife that emulates any arbitary element. Renders an element
 * tag including an id attribute and any informal parameters. The id is provided by
 * {@link PageRenderSupport#allocateClientId(String)} (so it will be unique on the client side) and
 * is available after the component renders as {@link #getClientId()}. The Any component has no
 * template but does render its body.
 */
@ComponentClass
@SupportsInformalParameters
public class Any
{
    /**
     * The element to be rendered by the component. Normally, this matches the element from the
     * template, but this can be overridden if necessary.
     */
    @Parameter(value="prop:componentResources.elementName", defaultPrefix="literal")
    private String _element;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _pageRenderSupport;

    private String _clientId;

    void beginRender(MarkupWriter writer)
    {
        String componentId = _resources.getId();

        _clientId = _pageRenderSupport.allocateClientId(componentId);

        writer.element(_element, "id", _clientId);

        _resources.renderInformalParameters(writer);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    public String getClientId()
    {
        return _clientId;
    }
}
