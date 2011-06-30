T5.define("pubsub", function() {

	var arrays = T5.arrays;
	var first = arrays.first;
	var without = arrays.without;
	var filter = arrays.filter;
	var remove = arrays.remove;
	var map = arrays.map;
	var each = arrays.each;

	// Element keys: topic, element, listenerfn
	// May be multiple elements with some topic/element pair
	// element property may be undefined
	var subscribers = [];

	// Element keys: topic, element, publisherfn
	var publishers = [];

	// Necessary since T5.dom depends on T5.pubsub
	function $(element) {
		return T5.$(element);
	}

	function purgePublisherCache(topic) {
		each(function(publisher) {
			if (publisher.topic === topic) {
				publisher.listeners = undefined;
			}
		}, publishers);
	}

	function findListeners(topic, element) {
		var gross = filter(function(subscriber) {
			return subscriber.topic === topic;
		}, subscribers);

		var primary = filter(function(subscriber) {
			return subscriber.element === element;
		}, gross);

		var secondary = filter(function(subscriber) {
			// Match where the element is null or undefined
			return !subscriber.element;
		}, gross);

		// Return the listenerfn property from each match.
		return map(arrays.extractProperty("listenerfn"), primary
				.concat(secondary));
	}

	/**
	 * Subscribes a listener function to the selector. The listener function
	 * will be invoked when a message for the given topic is published. If an
	 * element is specified, then the listener will only be invoked when the
	 * subscribed element matches the published element.
	 * 
	 * @param topic
	 *            a topic name, which must not be blank
	 * @param element
	 *            a DOM element, which may be null to subscribe to all messages
	 *            for the topic. If a string, then T5.$() is used to locate the
	 *            DOM element with the matching client id.
	 * @param listenerfn
	 *            function invoked when a message for the topic is published.
	 *            The function is invoked only if the supplied selector element
	 *            is undefined OR exactly matches the source element node. The
	 *            return value of the listenerfn will be accumulated in an array
	 *            and returned to the publisher.
	 * @return a function of no arguments used to unsubscribe the listener
	 */
	function subscribe(topic, element, listenerfn) {

		var subscriber = {
			topic : topic,
			element : $(element),
			listenerfn : listenerfn
		};

		subscribers.push(subscriber);
		purgePublisherCache(subscriber.topic);

		// To prevent memory leaks via closure:

		topic = null;
		element = null;
		listenerfn = null;

		// Return a function to unsubscribe
		return function() {
			subscribers = without(subscriber, subscribers);
			purgePublisherCache(subscriber.topic);
		}
	}

	/**
	 * Creates a publish function for the indicated topic name and DOM element.
	 * 
	 * <p>
	 * The returned function is used to publish a message. Messages are
	 * published synchronously. The publish function will invoke listener
	 * functions for matching subscribers (subscribers to the same topic). Exact
	 * subscribers (matching the specific element) are invoked first, then
	 * general subscribers (not matching any specific element). The return value
	 * for the publish function is an array of all the return values from all
	 * invoked listener functions.
	 * 
	 * <p>
	 * There is not currently a way to explicitly remove a publisher; however,
	 * when the DOM element is removed properly, all publishers and subscribers
	 * for the specific element will be removed as well.
	 * 
	 * <p>
	 * Publish functions are cached, repeated calls with the same topic and
	 * element return the same publish function.
	 * 
	 * @param topic
	 *            used to select listeners
	 * @param element
	 *            the DOM element used as the source of the published message
	 *            (also used to select listeners). Passed through T5.$(), the
	 *            result must not be null.
	 * @return publisher function used to publish a message
	 */
	function createPublisher(topic, element) {

		element = $(element);

		if (element == null) {
			throw "Element may not be null when creating a publisher.";
		}

		var existing = first(function(publisher) {
			return publisher.topic === topic && publisher.element === element;
		}, publishers);

		if (existing) {
			return existing.publisherfn;
		}

		var publisher = {
			topic : topic,
			element : element,
			publisherfn : function(message) {

				if (publisher.listeners == undefined) {
					publisher.listeners = findListeners(publisher.topic,
							publisher.element);
				}

				// TODO: pass a second object to each function to allow for
				// control over message propagation, supply listener with access
				// to source element.

				return map(function(listenerfn) {
					return listenerfn(message);
				}, publisher.listeners);
			}
		};

		publishers.push(publisher);

		// If only there was an event or something that would inform us when the
		// element was removed. Certainly, IE doesn't support that! Have to rely
		// on T5.dom.remove() to inform us.

		// Mark the element to indicate it requires cleanup once removed from
		// the DOM.

		element.t5pubsub = true;

		// Don't want to hold a reference via closure:

		topic = null;
		element = null;

		return publisher.publisherfn;
	}

	/**
	 * Creates a publisher and immediately publishes the message, return the
	 * array of results.
	 */
	function publish(topic, element, message) {
		return createPublisher(topic, element)(message);
	}

	/**
	 * Invoked whenever an element is about to be removed from the DOM to remove
	 * any publishers or subscribers for the element.
	 */
	function cleanup(element) {
		subscribers = remove(function(subscriber) {
			return subscriber.element === element
		}, subscribers);

		// A little evil to modify the publisher object at the same time it is
		// being removed.

		publishers = remove(function(publisher) {
			var match = publisher.element === element;

			if (match) {
				publisher.listeners = undefined;
				publisher.element = undefined;
			}

			return match;
		});
	}

	return {
		createPublisher : createPublisher,
		subscribe : subscribe,
		publish : publish,
		cleanupRemovedElement : cleanup
	};
});

/**
 * Create aliases on T5 directly: pub -&gt; pubsub.publish and sub -&gt;
 * pubsub.subscribe.
 */
T5.extend(T5, {
	pub : T5.pubsub.publish,
	sub : T5.pubsub.subscribe
});