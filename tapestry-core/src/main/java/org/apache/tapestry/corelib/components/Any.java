// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.annotations.Inject;

/**
 * Renders an arbitrary element including informal parameters.
 */
@SupportsInformalParameters
public class Any implements ClientElement
{
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String element;

    /**
     * The desired client id, which defaults to the components id.
     */
    @Parameter(value = "prop:componentResources.id", defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String clientId;

    private Element anyElement;

    private String uniqueId;

    @Inject
    private ComponentResources resources;

    @Inject
    private PageRenderSupport pageRenderSupport;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        anyElement = writer.element(element);

        uniqueId = null;

        resources.renderInformalParameters(writer);
    }

    /**
     * Returns the client id.  This has side effects: this first time this is called (after the Any component renders
     * its start tag), a unique id is allocated (based on, and typically the same as, the clientId parameter, which
     * defaults to the component's id). The rendered element is updated, with its id attribute set to the unique client
     * id, which is then returned.
     *
     * @return unique client id for this component
     */
    public String getClientId()
    {
        if (uniqueId == null)
        {
            uniqueId = pageRenderSupport.allocateClientId(clientId);
            anyElement.forceAttributes("id", clientId);
        }

        return uniqueId;
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // the element
    }

    void inject(PageRenderSupport support, ComponentResources resources, String element, String clientId)
    {
        this.pageRenderSupport = support;
        this.resources = resources;
        this.element = element;
        this.clientId = clientId;
    }
}
