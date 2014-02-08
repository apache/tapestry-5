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

package org.apache.tapestry5.alerts;

import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

/**
 * Manages {@link Alert}s (using the {@link AlertStorage} SSO.  The behavior of the service switches during
 * an Ajax request to directly add JavaScript to present the alerts.
 *
 * @since 5.3
 */
public interface AlertManager
{
   /**
     * Adds an {@link Severity#SUCCESS} alert with the default duration, {@link Duration#SINGLE}.
     *
     * @param message to present to the user
     * @since 5.3.6
     */
    void success(String message);

    /**
     * Adds an {@link Severity#INFO} alert with the default duration, {@link Duration#SINGLE}.
     *
     * @param message to present to the user
     */
    void info(String message);

    /**
     * Adds an {@link Severity#WARN} alert with the default duration, {@link Duration#SINGLE}.
     *
     * @param message to present to the user
     */
    void warn(String message);

    /**
     * Adds an {@link Severity#ERROR} alert with the default duration, {@link Duration#SINGLE}.
     *
     * @param message to present to the user
     */
    void error(String message);

    /**
     * Adds an alert with configurable severity and duration. Message isn't treated
     * as HTML, being HTML-escaped.
     *
     * @param duration controls how long the alert is presented to the user
     * @param severity controls how the alert is presented to the user
     * @param message  to present to the user
     */
    void alert(Duration duration, Severity severity, String message);
    
    /**
     * Adds an alert with configurable severity and duration.
     *
     * @param duration controls how long the alert is presented to the user
     * @param severity controls how the alert is presented to the user
     * @param message  to present to the user
     * @param markup   whether to treat the message as raw HTML (true) or escape it (false).
     */
    @IncompatibleChange(details = "Added in 5.4 in order to support HTML in alerts", release = "5.4")
    void alert(Duration duration, Severity severity, String message, boolean markup);
    
}
