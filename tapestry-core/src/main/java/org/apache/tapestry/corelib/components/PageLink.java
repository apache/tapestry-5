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

import java.util.List;

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;

/**
 * Generates a render request link to some other page in the application. If an activation context
 * is supplied (as the context parameter), then the context values will be encoded into the URL. If
 * no context is supplied, then the target page itself will supply the context via a passivate
 * event.
 * <p>
 * Pages are not required to have an activation context. When a page does have an activation
 * context, the value typically represents the identity of some object displayed or otherwise
 * manipulated by the page.
 */
@SupportsInformalParameters
public class PageLink implements ClientElement
{
    /** The logical name of the page to link to. */
    @Parameter(required = true, defaultPrefix = "literal")
    private String _page;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _support;

    private String _clientId;

    /**
     * If provided, this is the activation context for the target page (the information will be
     * encoded into the URL). If not provided, then the target page will provide its own activation
     * context.
     */
    @Parameter
    private List _context;

    private final Object[] _emptyContext = new Object[0];

    void beginRender(MarkupWriter writer)
    {
        _clientId = _support.allocateClientId(_resources.getId());

        Object[] activationContext = _context != null ? _context.toArray() : _emptyContext;

        Link link = _resources.createPageLink(_page, activationContext);

        writer.element("a", "href", link, "id", _clientId);

        _resources.renderInformalParameters(writer);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // <a>
    }

    public String getClientId()
    {
        return _clientId;
    }

}
