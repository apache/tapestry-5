T5.define("pubsub", function() {

    var _ = T5._;

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
        _.each(publishers, function(publisher) {
            if (publisher.topic === topic) {
                publisher.listeners = undefined;
            }
        });
    }

    function findListeners(topic, element) {
        var gross = _.select(subscribers, function(subscriber) {
            return subscriber.topic === topic;
        });

        var primary = _.select(gross, function(subscriber) {
            return subscriber.element === element;
        });

        var secondary = _.select(gross, function(subscriber) {
            // Match where the element is null or undefined
            return !subscriber.element;
        });

        // Return the listenerfn property from each match.
        return _(primary).chain().union(secondary).pluck("listenerfn").value();
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
     *
     *            The listener function is passed a message object as the first parameter; this is provided
     *            on each call to the topic's publish function. The second parameter is an object with two
     *            properties:  An element property to identify the source of the message, and a cancel() function property
     *            that prevents further listeners from being invoked.
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
            subscribers = _.without(subscribers, subscriber);
            purgePublisherCache(subscriber.topic);
        }
    }

    /**
     * Creates a publish function for the indicated topic name and DOM element. For global
     * events, the convention is to use the document object.
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
     * Listener functions are passed the message object and a second (optional) object.
     * The second object contains two keys:  The first, "element", identifies the element for which the publisher was created, i.e.,
     * the source of the message. The second, "cancel", is a function used to prevent further listeners
     * from being invoked.
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
     *            result must not be null.   The element will be passed to listener function as
     *            the second parameter.
     * @return publisher function used to publish a message
     */
    function createPublisher(topic, element) {

        element = $(element);

        if (element == null) {
            throw "Element may not be null when creating a publisher.";
        }

        var existing = _.detect(publishers, function(publisher) {
            return publisher.topic === topic && publisher.element === element;
        });

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

                var canceled = false;

                var meta = {
                    element : publisher.element,
                    cancel : function() {
                        canceled = true;
                    }
                };

                var result = [];

                for (var i = 0; i < publisher.listeners.length; i++) {

                    var listenerfn = publisher.listeners[i];

                    result.push(listenerfn(message, meta));

                    if (canceled) {
                        break;
                    }
                }

                return result;
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
        subscribers = _.reject(subscribers, function(subscriber) {
            return subscriber.element === element
        });

        // A little evil to modify the publisher object at the same time it is
        // being removed.

        publishers = _.reject(publishers, function(publisher) {
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
 * Create aliases on T5 directly: pub -&gt; T5.pubsub.publish and sub -&gt;
 * T5.pubsub.subscribe.
 */
T5.extend(T5, {
    pub : T5.pubsub.publish,
    sub : T5.pubsub.subscribe
});