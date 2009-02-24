// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.corelib.data.InsertPosition;

/**
 * Collects details about zone usage for efficient initialization of the client side objects.  This has grown to include
 * the client-side behavior associated with {@link org.apache.tapestry5.corelib.components.FormFragment}s.
 *
 * @see org.apache.tapestry5.corelib.components.Zone
 */
public interface ClientBehaviorSupport
{
    /**
     * Adds a new client-side Tapestry.Zone object. Zones are linked to a an element (typically, a &lt;div&gt;).  A Zone
     * may have handlers used to initially show it, or to highlight it when its content changes. Such handlers are
     * referenced by name, as functions of the Tapestry.ElementEffect object.
     *
     * @param clientId           client-side id of the element that will be updated by the zone
     * @param showFunctionName   name of the function used to initially show the zone (if not visible), or null for
     *                           default
     * @param updateFunctionName name of function used to highlight the function after an update, or null for default
     */
    void addZone(String clientId, String showFunctionName, String updateFunctionName);

    /**
     * Sets the client-side onclick handler for an &lt;a&gt; element to perform an Ajax update of a zone.
     *
     * @param linkId    id of the link to Ajax enable
     * @param elementId id of an element that has been previously registered as a Zone
     * @param eventLink
     */
    void linkZone(String linkId, String elementId, Link eventLink);

    /**
     * Adds a new client-side Tapestry.FormFragment object.  FormFragment's are used to make parts of a client-side form
     * visible or invisible, which involves interactions with both the server-side and client-side validation.
     *
     * @param clientId         client-side id of the element that will be made visible or invisible
     * @param showFunctionName name of function (of the Tapestry.ElementEffect object) used to make the SubForm visible,
     *                         or null for the default
     * @param hideFunctionName name of the function used to make the SubForm invisible, or null for the default
     */
    void addFormFragment(String clientId, String showFunctionName, String hideFunctionName);

    /**
     * Adds a new client-side Tapestry.FormInjector object.  FormInjectors are used to extend an existing Form with new
     * content.
     *
     * @param clientId         client-side id of the element that identifiess where the new content will be placed
     * @param link             action request link used to trigger the server-side object, to render the new content
     * @param insertPosition   where the new content should go (above or below the element)
     * @param showFunctionName name of function (of the Tapestry.ElementEffect object) used to make the new element
     *                         visible, or null for the default
     */
    void addFormInjector(String clientId, Link link, InsertPosition insertPosition, String showFunctionName);

    /**
     * Collects field validation information.
     *
     * @param field          for which validation is being generated
     * @param validationName name of validation method (see Tapestry.Validation in tapestry.js)
     * @param message        the error message to display if the field is invalid
     * @param constraint     additional constraint value, or null for validations that don't require a constraint
     */
    void addValidation(Field field, String validationName, String message, Object constraint);
}

