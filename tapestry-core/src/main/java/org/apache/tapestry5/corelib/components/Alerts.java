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
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.alerts.Alert;
import org.apache.tapestry5.alerts.AlertStorage;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.BaseClientElement;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;

/**
 * Renders out an empty {@code <div>} element and provides JavaScript initialization to make the element
 * the container for alerts. After rendering markup (and initialization JavaScript), it
 * {@linkplain org.apache.tapestry5.alerts.AlertStorage#dismissNonPersistent() removes all non-persistent alerts}.
 *
 * Alerts are created using the {@link org.apache.tapestry5.alerts.AlertManager} service.
 *
 * @tapestrydoc
 * @since 5.3
 */
@SupportsInformalParameters
public class Alerts extends BaseClientElement
{

    /**
     * Allows the button used to dismiss all alerts to be customized (and localized).
     *
     * @deprecated Deprecated in Tapestry 5.4; override the {@code core-dismiss-label} message key in
     * your application's message catalog. This parameter is now ignored.
     */
    @Parameter(value = "message:core-dismiss-label", defaultPrefix = BindingConstants.LITERAL)
    private String dismissText;

    /**
     * If set to true, then the "dismiss all" button will not be rendered on the client.
     *
     * @since 5.4
     */
    @Parameter(value = "message:private-core-alerts-show-dismiss-all", defaultPrefix = BindingConstants.LITERAL)
    private boolean showDismissAll;

    @SessionState(create = false)
    private AlertStorage storage;

    @Inject
    private DeprecationWarning deprecationWarning;

    @Inject
    private Request request;

    void onPageLoaded()
    {
        deprecationWarning.ignoredComponentParameters(resources, "dismissText");
    }

    boolean beginRender(MarkupWriter writer)
    {
        Link dismissLink = resources.createEventLink("dismiss");

        storeElement(writer.element("div",
                "data-container-type", "alerts",
                "data-show-dismiss-all", showDismissAll,
                "data-dismiss-url", dismissLink));

        resources.renderInformalParameters(writer);
        writer.end();

        addAlertsFromStorage();

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

        // See TAP5-1941
        if (!request.isXHR())
        {
            return true;
        }

        return new JSONObject();
    }

    @HeartbeatDeferred
    void addAlertsFromStorage()
    {
        if (storage == null)
        {
            return;
        }

        for (Alert alert : storage.getAlerts())
        {
            javaScriptSupport.require("t5/core/alert").with(alert.toJSON());
        }

        storage.dismissNonPersistent();
    }
}
