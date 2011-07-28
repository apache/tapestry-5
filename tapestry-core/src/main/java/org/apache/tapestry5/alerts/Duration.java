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

/**
 * Controls how long an {@link Alert} is displayed to the end user.
 *
 * @since 5.3
 */
public enum Duration
{
    /**
     * Transient alerts are displayed to the user, then automatically clear themselves after a few seconds. Transient alerts
     * are not stored in the {@link AlertStorage}.
     */
    TRANSIENT(false),

    /**
     * The default duration; displayed to the user a single time; once the page updates, the alert will not be presented to the
     * user. Single duration alerts are not stored in {@link AlertStorage}.
     */
    SINGLE(false),

    /**
     * For most important alerts; the alert will continue to be presented to the user until it is dismissed by the user
     * (either by clicking on the individual alert to dismiss it, or by clicking the UI control for dismissing all alerts).
     * Such alerts are stored persistently in {@link AlertStorage}.
     */
    UNTIL_DISMISSED(true);

    /**
     * True if the alert type should persist between requests. This is only true for {@link #UNTIL_DISMISSED}.
     */
    public final boolean persistent;

    private Duration(boolean persistent)
    {
        this.persistent = persistent;
    }
}
