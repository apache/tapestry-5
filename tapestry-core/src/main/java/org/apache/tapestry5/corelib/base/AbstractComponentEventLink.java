// Copyright 2008-2014 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Base class for link-generating components that are based on a component event request. Such events have an event
 * context and may also update a {@link org.apache.tapestry5.corelib.components.Zone}.
 */
public abstract class AbstractComponentEventLink extends AbstractLink
{
    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

    /**
     * Binding the zone parameter turns the link into a an Ajax control that causes the related zone to be updated.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    /**
     * When true, the the link will trigger an asynchronous request (via XmlHttpRequest); the event handler method
     * can make use of the {@link org.apache.tapestry5.services.ajax.AjaxResponseRenderer} in order to force content
     * updates to the client.  This is used as an alternative to placing the link inside a {@link org.apache.tapestry5.corelib.components.Zone}
     * and binding the {@code zone} parameter.
     *
     * @since 5.4
     */
    @Parameter
    private boolean async = false;

    @Inject
    private Request request;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    void beginRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        Link link = createLink(context);

        writeLink(writer, link);

        writer.attributes("data-update-zone", zone);

        if (async)
        {
            javaScriptSupport.require("t5/core/zone");
            writer.attributes("data-async-trigger", true);
        }
    }

    /**
     * Invoked to create the Link that will become the href attribute of the output.
     *
     * @param eventContext
     *         the context as an object array, possibly null
     * @return the link, not null
     */
    protected abstract Link createLink(Object[] eventContext);

    void afterRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        writer.end(); // <a>
    }
}
