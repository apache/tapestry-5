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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.ClientBehaviorSupport;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;


/**
 * A Zone is portion of the output page designed for easy dynamic updating via Ajax or other client-side effects.  A
 * Zone renders out as a &lt;div&gt; element and may have content initially, or may only get its content as a result of
 * client side activity.
 * <p/>
 * Often, Zones are initially invisible, in which case the visible parameter may be set to false (it defaults to true).
 * <p/>
 * When a user clicks an {@link org.apache.tapestry5.corelib.components.ActionLink} whose zone parameter is set, the
 * corresponding client-side Tapestry.Zone object is located. It will update the content of the Zone's &lt;div&gt; and
 * then invoke either a show method (if the div is not visible) or an update method (if the div is visible).  The show
 * and update parameters are the <em>names</em> of functions attached to the Tapestry.ElementEffect object.    Likewise,
 * a {@link org.apache.tapestry5.corelib.components.Form} component may also trigger an update of a client-side Zone.
 * <p/>
 * Renders informal parameters, adding CSS class "t-zone" and possibly, "t-invisible".
 * <p/>
 * You will often want to specify the id parameter of the Zone, in addition to it's Tapestry component id; this "locks
 * down" the client-side id, so the same value is used even in later partial renders of the page (essential if the Zone
 * is nested inside another Zone).  When you specify the client-side id, it is used exactly as provided (meaning that
 * you are responsible for ensuring that there will not be an id conflict even in the face of multiple partial renders
 * of the page). Failure to provide an explicit id results in a new, and non-predictable, id being generated for each
 * partial render, which will often result in client-side failures to locate the element to update when the Zone is
 * triggered.
 */
@SupportsInformalParameters
public class Zone implements ClientElement
{
    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make the Zone's
     * &lt;div&gt; visible before being updated.  If not specified, then the basic "show" method is used.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked after the Zone's content has
     * been updated. If not specified, then the basic "highlight" method is used, which performs a classic "yellow fade"
     * to indicate to the user that and update has taken place.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String update;

    /**
     * If bound, then the id attribute of the rendered element will be this exact value. If not bound, then a unique id
     * is generated for the element.
     */
    @Parameter(name = "id", defaultPrefix = BindingConstants.LITERAL)
    private String clientId;

    @Environmental
    private RenderSupport renderSupport;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    /**
     * If true (the default) then the zone will render normally.  If false, then the "t-invisible" CSS class is added,
     * which will make the zone initially invisible.
     */
    @Parameter
    private boolean visible = true;

    @Inject
    private ComponentResources resources;

    void beginRender(MarkupWriter writer)
    {
        if (!resources.isBound("id"))
            clientId = renderSupport.allocateClientId(resources);

        Element e = writer.element("div", "id", clientId);

        resources.renderInformalParameters(writer);

        e.addClassName("t-zone");

        if (!visible) e.addClassName("t-invisible");

        // And continue on to render the body

        JSONObject spec = new JSONObject();
        spec.put("div", clientId);

        clientBehaviorSupport.addZone(clientId, show, update);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // div
    }

    public String getClientId()
    {
        return clientId;
    }

    /**
     * Returns the zone's body (the content enclosed by its start and end tags). This is often used as part of an Ajax
     * partial page render to update the client with a fresh render of the content inside the zone.
     *
     * @return the zone's body as a Block
     */
    public Block getBody()
    {
        return resources.getBody();
    }
}
