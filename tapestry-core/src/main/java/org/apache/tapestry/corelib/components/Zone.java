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
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.services.ClientBehaviorSupport;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.json.JSONObject;


/**
 * A Zone is portion of the output page designed for easy dynamic updating via Ajax or other client-side effects.  A
 * Zone renders out as a &lt;div&gt; element and may have content initially, or may only get its content as a result of
 * client side activity.
 * <p/>
 * Often, Zone's are initially invisible, in which case the visible parameter may be set to false (it defaults to
 * false).
 * <p/>
 * <p/>
 * When a user clicks an {@link org.apache.tapestry.corelib.components.ActionLink} whose zone parameter is set, the
 * corresponding client-side Tapestry.Zone object is located. It will update the content of the Zone's &lt;div&gt; and
 * then invoke either a show method (if the div is not visible) or an update method (if the div is visible).  The show
 * and update parameters are the <em>names</em> of functions attached to the Tapestry.ElementEffect object.
 * <p/>
 * Renders informal parameters, adding CSS class "t-zone" and possibly, "t-invisible".
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
}
