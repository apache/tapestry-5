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
 * Defines the SPI (service provider interface). This represents an abstract
 * layer between Tapestry's JavaScript and an underlying framework (such as
 * Prototype and JQuery).
 *
 * <p>
 * The SPI defines placeholders for functions whose implementations are provided
 * elsewhere. In some cases, an SPI may define a function in terms of other SPI
 * functions; a framework layer may leave such a function alone or re-implement
 * it.
 */
T5.define("spi", {

    /**
     * Observes a DOM event.
     *
     *  @param element DOM element or element id
     *  @param event name of event to observe
     *  @param listener function to be invoked; the function is passed the DOM event object
     *  @return cancel function to stop observing the event with the listener
     */
    observe : undefined,

    /**
     * Find the first child element matching a CSS selector.
     *
     * @param element DOM element or element id
     * @param selector CSS selector to locate
     * @return the element, or undefined if not found
     */
    find : undefined,

    /** Hides an element making it invisible.
     * @param element DOM element or element id
     */
    hide : undefined,

    /** Reveals an element, making it visible again.
     *
     * @param element DOM element or element id
     */
    show : undefined,

    /**
     * Appends new markup to an existing element's body.
     *
     * @param element DOM element or element id
     * @param markup new content as markup string
     * @return element
     */
    appendMarkup: undefined,

    /**
     * Performs an ajax request, as per T5.ajax.request(). Supplied by the SPI
     * implementation. The options parameter is not yet fully defined in Tapestry 5.3
     * (this SPI function is not yet used anywhere).
     *
     * @param url
     *            URL for Ajax request
     * @param options
     *            additional options defined by T5.ajax.request().
     */
    ajaxRequest : undefined
});