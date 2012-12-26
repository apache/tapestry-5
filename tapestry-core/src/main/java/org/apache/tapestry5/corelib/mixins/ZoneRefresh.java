// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * <p>
 * This mixin periodically refreshs a @{link org.apache.tapestry5.corelib.components.Zone zone}
 * by triggering an event on the server using ajax requests.
 * </p>
 * <p/>
 * <b>Note: </b> This mixin is only meant for a @{link org.apache.tapestry5.corelib.components.Zone zone}
 *
 * @tapestrydoc
 */
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

    Object onZoneRefresh()
    {
        CaptureResultCallback<Object> callback = new CaptureResultCallback<Object>();
        resources.triggerEvent(EventConstants.REFRESH, context, callback);

        if (callback.getResult() != null)
        {
            return callback.getResult();
        }

        return zone.getBody();
    }

}