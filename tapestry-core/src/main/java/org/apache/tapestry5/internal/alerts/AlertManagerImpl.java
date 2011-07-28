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
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class AlertManagerImpl implements AlertManager
{
    private final ApplicationStateManager asm;

    private final Request request;

    private final JavaScriptSupport javaScriptSupport;

    public AlertManagerImpl(ApplicationStateManager asm, Request request, JavaScriptSupport javaScriptSupport)
    {
        this.asm = asm;
        this.request = request;
        this.javaScriptSupport = javaScriptSupport;
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
        Alert alert = new Alert(duration, severity, message);

        boolean ajax = request.isXHR();

        if (ajax)
        {
            javaScriptSupport.addInitializerCall("addAlert", alert.toJSON());
        }

        // In Ajax mode, ony persistent alerts need to be stored for later requests (so that they can
        // be re-presented until explicitly dismissed). In traditional mode, the alerts are added here
        // to the AlertStorage and then later, during the render, the Alerts component will take
        // care of JavaScript initialization for them.
        if (!ajax || duration.persistent)
        {
            asm.get(AlertStorage.class).add(alert);
        }
    }

}