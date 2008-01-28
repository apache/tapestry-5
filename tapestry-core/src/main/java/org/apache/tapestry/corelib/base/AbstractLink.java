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

package org.apache.tapestry.corelib.base;

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import static org.apache.tapestry.TapestryConstants.LITERAL_BINDING_PREFIX;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.ioc.annotations.Inject;

/**
 * Provides base utilities for classes that generate clickable links.
 */
@SupportsInformalParameters
public abstract class AbstractLink implements ClientElement
{
    @Inject
    private ComponentInvocationMap _componentInvocationMap;

    /**
     * An anchor value to append to the generated URL (the hash separator will be added automatically).
     */
    @Parameter(defaultPrefix = LITERAL_BINDING_PREFIX)
    private String _anchor;

    /**
     * If true, then then no link element is rendered (and no informal parameters as well). The body is, however, still
     * rendered.
     */
    @Parameter("false")
    private boolean _disabled;

    @Inject
    private ComponentResources _resources;

    private Link _link;

    private String _clientId;

    private String buildHref(Link link)
    {
        String href = link.toURI();

        if (_anchor == null) return href;

        return href + "#" + _anchor;
    }


    /**
     * Writes an &lt;a&gt; element with the provided link as the href attribute.  A call to {@link
     * org.apache.tapestry.MarkupWriter#end()} is <em>not</em> provided.            Automatically appends an anchor if
     * the component's anchor parameter is non-null.  Informal parameters are rendered as well.
     *
     * @param writer         to write markup to
     * @param clientId       value written as the id attribute
     * @param link           the link that will form the href
     * @param namesAndValues additional attributes to write
     */
    protected final void writeLink(MarkupWriter writer, String clientId, Link link, Object... namesAndValues)
    {
        Element e = writer.element("a", "href", buildHref(link), "id", clientId);

        writer.attributes(namesAndValues);

        _resources.renderInformalParameters(writer);

        _componentInvocationMap.store(e, link);

        _link = link;
        _clientId = clientId;
    }

    /**
     * Returns the most recently rendered {@link org.apache.tapestry.Link} for this component.  Subclasses calculate
     * their link value as they render, and the value is valid until the end of the request, or the next time the same
     * component renders itself (if inside a loop).
     *
     * @return the most recent link, or null
     */
    public Link getLink()
    {
        return _link;
    }

    /**
     * Returns the unique client id for this element. This is valid only after the component has rendered (its start
     * tag), and then only if the component is {@linkplain #isDisabled() enabled}.
     */
    public String getClientId()
    {
        return _clientId;
    }

    /**
     * Returns true if the component is disabled (as per its disabled parameter). Disabled link components should not
     * render a tag, but should still render their body.
     */
    public boolean isDisabled()
    {
        return _disabled;
    }

    /**
     * Used for testing.
     */
    final void inject(String anchor, ComponentInvocationMap map, ComponentResources resources)
    {
        _anchor = anchor;
        _componentInvocationMap = map;
        _resources = resources;
    }
}
