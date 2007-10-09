// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.TapestryConstants.ACTION_EVENT;

import java.util.List;

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.ioc.annotations.Inject;

/**
 * Component that triggers an action on the server with a subsequent full page refresh.
 */
@SupportsInformalParameters
public class ActionLink implements ClientElement
{
    /**
     * The context for the link (optional parameter). This list of values will be converted into
     * strings and included in the URI. The strings will be coerced back to whatever their values
     * are and made available to event handler methods.
     */
    @Parameter
    private List<?> _context;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _support;

    /**
     * If true, then then no link element is rendered (and no informal parameters as well). The body
     * is, however, still rendered.
     */
    @Parameter("false")
    private boolean _disabled;

    private String _clientId;

    void beginRender(MarkupWriter writer)
    {
        if (_disabled) return;

        _clientId = _support.allocateClientId(_resources.getId());

        Object[] contextArray = _context == null ? new Object[0] : _context.toArray();

        Link link = _resources.createActionLink(ACTION_EVENT, false, contextArray);

        writer.element("a", "href", link, "id", _clientId);

        _resources.renderInformalParameters(writer);
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
}
