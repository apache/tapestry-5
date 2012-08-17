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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.alerts.Alert;
import org.apache.tapestry5.alerts.AlertStorage;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Renders out an empty {@code <div>} element and provides JavaScript initialization to make the element
 * the container for alerts. After rendering markup (and initialization JavaScript), it
 * {@linkplain org.apache.tapestry5.alerts.AlertStorage#dismissNonPersistent() removes all non-persistent alerts}.
 *
 * @tapestrydoc
 * @since 5.3
 */
@SupportsInformalParameters
public class Alerts implements ClientElement
{

    @Parameter(value="message:dismiss-label", defaultPrefix=BindingConstants.LITERAL)
    private String dismissText;

    @Inject
    private ComponentResources resources;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @SessionState(create = false)
    private AlertStorage storage;

    private String clientId;

    public String getClientId()
    {
        return clientId;
    }

    boolean beginRender(MarkupWriter writer)
    {
        clientId = javaScriptSupport.allocateClientId(resources);

        writer.element("div", "id", clientId);
        resources.renderInformalParameters(writer);
        writer.end();

        JSONObject spec = new JSONObject("id", clientId,
                "dismissURL", resources.createEventLink("dismiss").toURI(),
                "dismissText", dismissText);

        javaScriptSupport.addInitializerCall(InitializationPriority.EARLY, "alertManager", spec);

        if (storage != null)
        {
            addAlertsFromStorage();
        }


        return false;
    }

    Object onDismiss(@RequestParameter(value = "id", allowBlank = true) Long alertId)
    {
        // If the alert was created inside an Ajax request and AlertStorage did not previously
        // exist, it can be null when the dismiss event comes up from the client.
        if (storage != null)
        {
            if (alertId != null)
            {
                storage.dismiss(alertId);
            } else
            {
                storage.dismissAll();
            }
        }

        return new JSONObject();
    }

    @HeartbeatDeferred
    void addAlertsFromStorage()
    {
        for (Alert alert : storage.getAlerts())
        {
            javaScriptSupport.addInitializerCall("addAlert", alert.toJSON());
        }

        storage.dismissNonPersistent();
    }
}
