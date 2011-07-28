/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Defines the names of events used with the publish/subscribe framework.
 */
T5.define("events", {

    /**
     * Published as an element is being removed from the DOM, to allow framework-specific
     * approaches to removing any event listeners for the element. This is published on the document object,
     * and the message is the DOM element for which event handlers should be removed.
     */
    REMOVE_EVENT_HANDLERS : "tapestry:remove-event-handlers",

    /** Event used to display a new alert to the user. The message is the alert specification, with keys:
     * <dl>
     *  <dt>id</dt>
     *  <dd>unique numeric id for the alert, if the alert is persistent on the server (omitted for non-persistent
     *  alerts)</dd>
     *  <dt>transient</dt>
     *  <dd>If true (may be omitted), then the alert will automatically dismiss itself after a period of time.</dd>
     *  <dt>class</dt>
     *  <dd>The CSS class, which should be 't-info', 't-warn' or 't-error'.</dd>
     *  <dt>message</dt>
     *  <dd>The alert message content.</dd>
     *  </dl>
     */
    ADD_ALERT : "tapestry:add-alert"

});