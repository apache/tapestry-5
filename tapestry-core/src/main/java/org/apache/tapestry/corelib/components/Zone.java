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

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.services.ZoneSetup;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.json.JSONObject;


/**
 * A Zone is portion of the output page designed for easy dynamic updating via Ajax or other
 * client-side effects.  A Zone renders out as a &lt;div&gt; element a may have content initially,
 * or may only get its content as a result of client side activity.
 * <p/>
 * <p/>
 * Renders informal parameters, adding CSS class "t-zone" and possibly, "t-invisible".
 */
@SupportsInformalParameters
public class Zone implements ClientElement
{
    private String _clientId;

    @Environmental
    private PageRenderSupport _pageRenderSupport;

    @Environmental
    private ZoneSetup _zoneSetup;

    /**
     * If true (the default) then the zone will render normally.  If false, then the "t-invisible"
     * CSS class is added, which will make the zone initially invisible.
     */
    @Parameter
    private boolean _visible = true;

    @Inject
    private ComponentResources _resources;

    void beginRender(MarkupWriter writer)
    {
        _clientId = _pageRenderSupport.allocateClientId(_resources.getId());

        Element e = writer.element("div", "id", _clientId);

        _resources.renderInformalParameters(writer);

        e.addClassName("t-zone");

        if (!_visible) e.addClassName("t-invisible");

        // And continue on to render the body

        JSONObject spec = new JSONObject();
        spec.put("div", _clientId);

        _zoneSetup.addZone(_clientId, null, null);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // div
    }

    public String getClientId()
    {
        return _clientId;
    }
}
