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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.io.IOException;

/**
 * A component used to implement the <a
 * href="http://en.wikipedia.org/wiki/Progressive_enhancement">progressive
 * enhancement</a> web design strategy; the component renders itself with a
 * simplified initial content (i.e., "loading
 * ...") and an Ajax request then supplies the component's true body. This
 * results in much faster page loads. You can
 * even nest these!
 *
 * The component simply does not render its body on initial render. On the subsequent action event request, it fires a
 * {@link org.apache.tapestry5.EventConstants#PROGRESSIVE_DISPLAY} event to inform the container about the (optional)
 * event context. The event handler method may return a renderable object; if not then the component's body is rendered
 * as the partial markup response.
 *
 * @tapestrydoc
 * @since 5.1.0.1
 */
@SupportsInformalParameters
@Events(EventConstants.PROGRESSIVE_DISPLAY)
@SuppressWarnings("all")
public class ProgressiveDisplay
{
    /**
     * The initial content to display until the real content arrives. Defaults
     * to "Loading ..." and an Ajax activity
     * icon.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "block:defaultInitial")
    private Block initial;

    /**
     * If provided, this is the event context, which will be provided via the
     * {@link org.apache.tapestry5.EventConstants#PROGRESSIVE_DISPLAY event}.
     */
    @Parameter
    private Object[] context;

    @Inject
    private ComponentResources resources;

    @Environmental
    private JavaScriptSupport jsSupport;

    @Environmental
    private TrackableComponentEventCallback eventCallback;

    @Inject
    private DeprecationWarning deprecationWarning;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that
     * is invoked after the elements's body
     * content has been updated. If not specified, then the basic "highlight"
     * method is used, which performs a classic
     * "yellow fade" to indicate to the user that and update has taken place.
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String update;

    void pageLoaded() {
        deprecationWarning.ignoredComponentParameters(resources, "update");
    }

    Block beginRender(MarkupWriter writer)
    {
        String clientId = jsSupport.allocateClientId(resources);
        String elementName = resources.getElementName("div");

        writer.element(elementName, "id", clientId, "data-container-type", "zone");
        resources.renderInformalParameters(writer);

        Link link = resources.createEventLink(EventConstants.ACTION, context);

        jsSupport.require("t5/core/zone").invoke("deferredZoneUpdate").with(clientId, link.toURI());

        // Return the placeholder for the full content. That will render instead of the main body
        // of the component.
        return initial;
    }

    Object onAction(EventContext context) throws IOException
    {
        resources.triggerContextEvent(EventConstants.PROGRESSIVE_DISPLAY, context, eventCallback);

        if (eventCallback.isAborted())
            return null;

        return getBody();
    }

    boolean beforeRenderBody()
    {
        return false;
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    /**
     * Returns the body of the ProgressiveDisplay, which is sometimes (in the
     * context of a
     * {@linkplain org.apache.tapestry5.services.ajax.AjaxResponseRenderer#addRender(org.apache.tapestry5.ClientBodyElement)} partial page render})
     * the content to be included.
     *
     * @return body of component
     * @since 5.2.0
     */
    public Block getBody()
    {
        return resources.getBody();
    }
}
