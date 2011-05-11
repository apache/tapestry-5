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

T5.extend(T5, function() {

	var map = T5.map;
	var mapcat = T5.mapcat;
	var each = T5.each;

	var subscribersVersion = 0;

	var subscribers = {};
	var publishers = {};

	/**
	 * Expands a selector into a array of strings representing the containers of
	 * the selector (by stripping off successive terms, breaking at slashes).
	 */
	function expandSelector(selector) {
		var result = [];
		var current = selector;

		while (true) {
			result.push(current);
			var slashx = current.lastIndexOf('/');

			if (slashx < 0)
				break;

			current = current.substring(0, slashx);
		}

		return result;
	}

	function doPublish(listeners, message) {

		return map(listeners, function(fn) {
			fn(message);
		});
	}

	/**
	 * Creates a publisher for a selector. The selector is a string consisting
	 * of individual terms separated by slashes. A publisher sends a message to
	 * listener functions. Publishers are cached internally.
	 * 
	 * <p>
	 * The returned publisher function is used to publish a message. It takes a
	 * single argument, the message object. The message object is passed to all
	 * listener functions matching the selector. The return value from the
	 * publisher function is all the return values from the listener functions.
	 * 
	 * @return publisher function
	 */
	function createPublisher(selector) {

		var publisher = publishers[selector];

		if (publisher === undefined) {
			var selectors = expandSelector(selector);

			var listeners = null;

			var subscribersVersionSnapshot = -1;

			var publisher = function(message) {

				// Recalculate the listeners whenever the subscribers map
				// has changed.

				if (subscribersVersionSnapshot !== subscribersVersion) {
					listeners = mapcat(selectors, function(selector) {
						return subscribers[selector] || [];
					});

					subscribersVersionSnapshot = subscribersVersion;
				}

				return doPublish(listeners, message);
			};

			publishers[selector] = publisher;
		}

		return publisher;
	}

	/**
	 * Creates a publisher for the selector (or uses a previously cached
	 * publisher) and publishes the message, returning the combined results of
	 * all the listener functions.
	 */
	function publish(selector, message) {

		return createPublisher(selector)(message);
	}

	function unsubscribe(selector, listenerfn) {
		var listeners = subscribers[selector];

		var editted = T5.without(listeners, listenerfn);

		if (editted !== listeners) {
			subscribers[selector] = editted;

			subscribersVersion++;
		}
	}

	/**
	 * Subscribes a listener function to a selector. The selector is a string
	 * consisting of individual terms separated by slashes. A publisher will
	 * send a message object to a selector; matching listener functions are
	 * invoked, and are passed the message object.
	 * <p>
	 * The return value is a function, of no parameters, used to unsubscribe the
	 * listener function.
	 * 
	 */
	function subscribe(selector, listenerfn) {

		var listeners = subscribers[selector];

		if (listeners === undefined) {
			listeners = [];
			subscribers[selector] = listeners;
		}

		listeners.push(listenerfn);

		// Indicate that subscribers has changed, so publishers need to
		// recalculate their listeners.

		subscribersVersion++;

		return function() {
			unsubscribe(selector, listenerfn);
		}
	}

	return {
		createPublisher : createPublisher,
		pub : publish,
		sub : subscribe
	};
});