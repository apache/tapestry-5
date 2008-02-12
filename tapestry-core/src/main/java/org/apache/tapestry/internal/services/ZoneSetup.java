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

package org.apache.tapestry.internal.services;

/**
 * Collects details about zone usage for effecient initialization on the client side.
 */
public interface ZoneSetup
{
    /**
     * Adds a new client-side Tapestry.Zone object. Zones are linked to a
     * an element (typically, a &lt;div&gt;).  A Zone may have handlers
     * used to initially show it, or to highlight it when its content changes.
     * Such handlers are referenced by name, as functions of the
     * Tapestry.ZoneEffect object.
     *
     * @param clientId           client-side id of the element that will be updated by the zone
     * @param showFunctionName   name of the function used to initially show the zone (if not visible), or null for default
     * @param updateFunctionName name of function used to highlight the function after an update, or null for default
     */
    void addZone(String clientId, String showFunctionName, String updateFunctionName);

    /**
     * Sets the client-side onclick handler for an &lt;a&gt; element to perform an Ajax update
     * of a zone.
     *
     * @param linkId    id of the link to Ajax enable
     * @param elementId id of an element that has been previously registered as a Zone
     */
    void linkZone(String linkId, String elementId);
}

