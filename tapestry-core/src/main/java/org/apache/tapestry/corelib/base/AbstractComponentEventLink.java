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

package org.apache.tapestry.corelib.base;

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.internal.services.ClientBehaviorSupport;
import org.apache.tapestry.ioc.annotations.Inject;

import java.util.List;

/**
 * Base class for link-generating components that are based on a component event request. Such events have an event
 * context and may also update a {@link org.apache.tapestry.corelib.components.Zone}.
 */
public abstract class AbstractComponentEventLink extends AbstractLink
{
    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private List<?> _context;

    /**
     * Binding the zone parameter turns the link into a an Ajax control that causes the related zone to be updated.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _zone;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _support;

    @Environmental
    private ClientBehaviorSupport _clientBehaviorSupport;

    void beginRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        String clientId = _support.allocateClientId(_resources);

        Object[] contextArray = _context == null ? new Object[0] : _context.toArray();

        Link link = createLink(contextArray);

        writeLink(writer, clientId, link);

        if (_zone != null) _clientBehaviorSupport.linkZone(clientId, _zone);
    }

    /**
     * Invoked to create the Link that will become the href attribute of the output.
     */
    protected abstract Link createLink(Object[] eventContext);

    void afterRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        writer.end(); // <a>
    }

}
