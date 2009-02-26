// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;

/**
 * A component used to implement the <a href="http://en.wikipedia.org/wiki/Progressive_enhancement">progressive
 * enhancement</a> web design strategy; the component renders itself with a simplified initial content (i.e., "loading
 * ...") and an Ajax request then supplies the component's true body. This results in much faster page loads. You can
 * even nest these!
 * <p/>
 * The component simply does not render its body on initial render, but then supplies its body as the partial render
 * content in the Ajax request.
 *
 * @since 5.1.0.1
 */
@SupportsInformalParameters
@IncludeJavaScriptLibrary("ProgressiveDisplay.js")
public class ProgressiveDisplay
{
    /**
     * The initial content to display until the real content arrives. Defaults to "Loading ..." and an Ajax activity
     * icon.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "block:defaultInitial")
    private Block initial;

    @Inject
    private ComponentResources resources;

    @Environmental
    private RenderSupport renderSupport;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked after the elements's body
     * content has been updated. If not specified, then the basic "highlight" method is used, which performs a classic
     * "yellow fade" to indicate to the user that and update has taken place.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String update;

    Block beginRender(MarkupWriter writer)
    {
        String clientId = renderSupport.allocateClientId(resources);
        String elementName = resources.getElementName("div");

        Element e = writer.element(elementName, "id", clientId);

        resources.renderInformalParameters(writer);

        e.addClassName("t-zone");

        Link link = resources.createEventLink(EventConstants.ACTION);

        JSONObject spec = new JSONObject();

        if (InternalUtils.isNonBlank(update))
            spec.put("update", update.toLowerCase());

        spec.put("element", clientId);
        spec.put("url", link.toAbsoluteURI());

        renderSupport.addInit("progressiveDisplay", spec);

        return initial;
    }

    Block onAction()
    {
        return resources.getBody();
    }

    boolean beforeRenderBody()
    {
        return false;
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }
}
