/* Copyright 2011, 2012 The Apache Software Foundation
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
 * Adapts Tapestry's SPI (Service Provider Interface) to make use of the
 * Prototype JavaScript library. May also make modifications to Prototype to
 * work with Tapestry.
 */
define("core/compat/t5-spi", ["core/spi", "core/compat/t5", "core/compat/t5-events", "core/compat/t5-pubsub"], function (spi) {
    T5.define("spi", function () {

        function observe(element, eventName, listener) {

            var handler = spi.on(element, eventName, listener);

            element = null;
            eventName = null;
            listener = null;

            return function () {
                handler.stop();
            };
        }

        /** This will likely go soon. */
        spi.domReady(function () {
            T5.sub(T5.events.REMOVE_EVENT_HANDLERS, null, function (element) {
                        // TODO: Remaining Prototype dependency here:
                        Event.stopObserving(element);
                    }
            );
        });

        function appendMarkup(element, markup) {
            spi(element).append(markup);

            return element;
        }

        return {
            observe: observe,
            find: Element.down,
            show: Element.show,
            hide: Element.hide,
            appendMarkup: appendMarkup
        };
    });

});
