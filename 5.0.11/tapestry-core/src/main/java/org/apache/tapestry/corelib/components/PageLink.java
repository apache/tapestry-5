// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractLink;
import org.apache.tapestry.ioc.annotations.Inject;

import java.util.List;

/**
 * Generates a render request link to some other page in the application. If an activation context is supplied (as the
 * context parameter), then the context values will be encoded into the URL. If no context is supplied, then the target
 * page itself will supply the context via a passivate event.
 * <p/>
 * Pages are not required to have an activation context. When a page does have an activation context, the value
 * typically represents the identity of some object displayed or otherwise manipulated by the page.
 */
public class PageLink extends AbstractLink implements ClientElement
{
    /**
     * The logical name of the page to link to.
     */
    @Parameter(required = true, defaultPrefix = "literal")
    private String _page;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _support;

    /**
     * If true, then then no link element is rendered (and no informal parameters as well). The body is, however, still
     * rendered.
     */
    @Parameter("false")
    private boolean _disabled;

    private String _clientId;

    /**
     * If provided, this is the activation context for the target page (the information will be encoded into the URL).
     * If not provided, then the target page will provide its own activation context.
     */
    @Parameter
    private List _context;

    private final Object[] _emptyContext = new Object[0];

    void beginRender(MarkupWriter writer)
    {
        if (_disabled) return;

        _clientId = _support.allocateClientId(_resources);

        Object[] activationContext = _context != null ? _context.toArray() : _emptyContext;

        Link link = _resources.createPageLink(_page, _resources.isBound("context"), activationContext);

        writeLink(writer, _clientId, link);
    }

    void afterRender(MarkupWriter writer)
    {
        if (_disabled) return;

        writer.end(); // <a>
    }

    public String getClientId()
    {
        return _clientId;
    }

    void setDisabled(boolean disabled)
    {
        _disabled = disabled;
    }
}
