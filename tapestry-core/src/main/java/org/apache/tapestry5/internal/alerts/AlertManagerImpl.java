// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.alerts;

import org.apache.tapestry5.alerts.*;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class AlertManagerImpl implements AlertManager
{
    private final ApplicationStateManager asm;

    private final Request request;

    private final AjaxResponseRenderer ajaxResponseRenderer;

    private final PerThreadValue<Boolean> needAlertStorageCleanup;

    public AlertManagerImpl(ApplicationStateManager asm, Request request, AjaxResponseRenderer ajaxResponseRenderer, PerthreadManager perThreadManager)
    {
        this.asm = asm;
        this.request = request;
        this.ajaxResponseRenderer = ajaxResponseRenderer;

        needAlertStorageCleanup = perThreadManager.createValue();
    }

    public void success(String message)
    {
        alert(Duration.SINGLE, Severity.SUCCESS, message);
    }
    
    public void info(String message)
    {
        alert(Duration.SINGLE, Severity.INFO, message);
    }

    public void warn(String message)
    {
        alert(Duration.SINGLE, Severity.WARN, message);
    }

    public void error(String message)
    {
        alert(Duration.SINGLE, Severity.ERROR, message);
    }
    
    public void alert(Duration duration, Severity severity, String message)
    {
        alert(duration, severity, message, false);
    }

    public void alert(Duration duration, Severity severity, String message, boolean markup)
    {
        final Alert alert = new Alert(duration, severity, message, markup);

        if (request.isXHR())
        {
            addCallbackForAlert(alert);
        }

        // Add it to the storage; this is always done, even in an Ajax request, because we may end up
        // redirecting to a new page, rather than doing a partial page update on the current page ... in which
        // case we need the alerts stored persistently until we render the new page.

        getAlertStorage().add(alert);
    }

    private void addCallbackForAlert(final Alert alert)
    {
        ajaxResponseRenderer.addCallback(new JavaScriptCallback()
        {
            public void run(JavaScriptSupport javascriptSupport)
            {
                javascriptSupport.require("t5/core/alert").with(alert.toJSON());
            }
        });

        addAlertStorageCleanupCallback();
    }

    private void addAlertStorageCleanupCallback()
    {
        // Add a callback that exists just to clear the non-persistent alerts.
        // Only one of these is needed.

        if (needAlertStorageCleanup.get(true))
        {
            ajaxResponseRenderer.addCallback(new JavaScriptCallback()
            {
                public void run(JavaScriptSupport javascriptSupport)
                {
                    // In an Ajax request, the Alerts are added, just so that they can be removed if not persistent.
                    // Again, this is for the rare case where there's a redirect to another page.

                    getAlertStorage().dismissNonPersistent();
                }
            });

            needAlertStorageCleanup.set(false);
        }
    }

    private AlertStorage getAlertStorage()
    {
        return asm.get(AlertStorage.class);
    }

}