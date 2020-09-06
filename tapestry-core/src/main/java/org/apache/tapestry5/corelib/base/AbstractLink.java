// Copyright 2007-2013The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.Map;

/**
 * Provides base utilities for classes that generate clickable links.
 */
@SupportsInformalParameters
public abstract class AbstractLink implements ClientElement
{
    /**
     * An anchor value to append to the generated URL (the hash separator will be added automatically).
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String anchor;

    /**
     * If true, then then no link element is rendered (and no informal parameters as well). The body is, however, still
     * rendered.
     */
    @Parameter("false")
    private boolean disabled;

    /**
     * If specified, the parameters are added to the link as query parameters in key=value fashion.
     * Values will be coerced to string using value encoder; keys should be Strings.
     * @since 5.3
     */
    @Parameter(allowNull = false)
    private Map<String, ?> parameters;

    @Inject
    protected ComponentResources resources;

    @Inject
    private JavaScriptSupport jsSupport;

    private Link link;

    private Element element;

    private String clientId;

    private String buildHref(Link link)
    {
        String href = link.toURI();

        if (anchor == null)
            return href;

        return href + "#" + anchor;
    }

    @SetupRender
    void resetElementAndClientId()
    {
        element = null;
        clientId = null;
    }

    /**
     * Writes an &lt;a&gt; element with the provided link as the href attribute. A call to
     * {@link org.apache.tapestry5.MarkupWriter#end()} is <em>not</em> provided. Automatically appends an anchor if
     * the component's anchor parameter is non-null. Informal parameters are rendered as well.
     * 
     * @param writer
     *            to write markup to
     * @param link
     *            the link that will form the href
     * @param namesAndValues
     *            additional attributes to write
     */
    protected final void writeLink(MarkupWriter writer, Link link, Object... namesAndValues)
    {
        addParameters(link);

        element = writer.element("a", "href", buildHref(link));

        writer.attributes(namesAndValues);

        resources.renderInformalParameters(writer);

        this.link = link;
    }

    /**
     * Adds any user-defined parameters as query parameters.
     * @param link a {@link org.apache.tapestry5.http.Link}.
     */
    protected final void addParameters(Link link)
    {
       if (!resources.isBound("parameters"))
           return;

       for(Map.Entry<String,?> entry : parameters.entrySet())
       {
           String name = entry.getKey();

           // Per TAP5-2126, we want to override any prior value (typically, set from an ActivationRequestParameter)
           // with the new value.

           link.removeParameter(name);

           link.addParameterValue(name, entry.getValue());
       }
    }

    /**
     * Returns the most recently rendered {@link org.apache.tapestry5.http.Link} for this component. Subclasses calculate
     * their link value as they render, and the value is valid until the end of the request, or the next time the same
     * component renders itself (if inside a loop).
     * 
     * @return the most recent link, or null
     */
    public Link getLink()
    {
        return link;
    }

    /**
     * Returns the unique client id for this element. This is valid only after the component has rendered (its start
     * tag). A client id is generated the first time this method is invoked, after the link renders its start tag.
     */
    public final String getClientId()
    {
        if (clientId == null)
        {
            if (element == null)
                throw new IllegalStateException(String.format(
                        "Client id for %s is not available as it did not render yet (or was disabled).",
                        resources.getCompleteId()));

            clientId = jsSupport.allocateClientId(resources);

            element.forceAttributes("id", clientId);
        }

        return clientId;
    }

    /**
     * Returns true if the component is disabled (as per its disabled parameter). Disabled link components should not
     * render a tag, but should still render their body.
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Used for testing.
     */
    final void inject(String anchor, ComponentResources resources)
    {
        this.anchor = anchor;
        this.resources = resources;
    }
}
