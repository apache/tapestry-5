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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 *
 * This mixin periodically refreshs a {@link org.apache.tapestry5.corelib.components.Zone zone}
 * by triggering an event on the server using ajax requests.
 *
 *
 * Server-side, the mixin triggers the "refresh" event with the mixin's context. A container may but
 * does not need to handle the event. If the event is handled and a value is returned, that value is
 * used to render the response. Otherwise, the Zone's body is re-rendered.
 *
 * <b>Note: </b> This mixin is only meant for a {@link org.apache.tapestry5.corelib.components.Zone zone}.
 *
 * @tapestrydoc
 */
@Events(EventConstants.REFRESH)
public class ZoneRefresh
{
    /**
     * Period between two consecutive refreshes (in seconds). If a new refresh occurs before the
     * previous refresh has completed, it will be skipped.
     */
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private int period;

    /**
     * Context passed to the event
     */
    @Parameter
    private Object[] context;

    @InjectContainer
    private Zone zone;

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private ComponentResources resources;

    //For testing purpose
    ZoneRefresh(Object[] context, ComponentResources resources, JavaScriptSupport javaScriptSupport, Zone zone)
    {
        this.context = context;
        this.resources = resources;
        this.javaScriptSupport = javaScriptSupport;
        this.zone = zone;
    }

    @AfterRender
    void addJavaScript()
    {
        Link link = resources.createEventLink("zoneRefresh", context);

        javaScriptSupport.require("t5/core/zone-refresh").with(zone.getClientId(), period, link.toString());
    }

    Object onZoneRefresh(EventContext eventContext)
    {
        CaptureResultCallback<Object> callback = new CaptureResultCallback<Object>();
        resources.triggerContextEvent(EventConstants.REFRESH, eventContext, callback);

        if (callback.getResult() != null)
        {
            return callback.getResult();
        }

        return zone.getBody();
    }

}