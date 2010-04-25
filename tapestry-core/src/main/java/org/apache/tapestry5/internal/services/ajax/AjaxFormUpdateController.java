// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.services.FormSupport;

/**
 * Coordinates the rendering of page partials in the context of an
 * Ajax update to an existing Form.
 * 
 * @see AjaxFormUpdateFilter
 * @see MultiZoneUpdate
 * @since 5.2.0
 */
public interface AjaxFormUpdateController
{
    void initializeForForm(String formComponentId, String formClientId);

    /**
     * Called before starting to render a zone's content; initializes
     * the {@link FormSupport} and {@link ValidationTracker} environmentals
     * and starts a heartbeat.
     */
    void setupBeforePartialZoneRender(MarkupWriter writer);

    /**
     * Ends the heartbeat, executes deferred Form actions,
     * and cleans up the environmentals.
     */
    void cleanupAfterPartialZoneRender();
}
