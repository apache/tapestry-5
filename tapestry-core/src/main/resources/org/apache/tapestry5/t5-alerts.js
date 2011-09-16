T5.extendInitializers(function() {

        var $ = T5.$;
        var on = T5.dom.observe;
        var find = T5.dom.find;
        var DISMISS_ALERTS = "tapestry:dismiss-all";
        var addAlertPublisher = T5.pubsub.createPublisher(T5.events.ADD_ALERT, document);

        function construct(clientId, dismissText) {
            $(clientId).innerHTML = "<div class='t-alert-container'></div>" +
                "<div class='t-alert-controls'><a href='#'>" + dismissText + "</a></div>";

            var list = find(clientId, "div");
            var link = find(clientId, "a");

            T5.dom.publishEvent(link, "click", DISMISS_ALERTS);

            return list;
        }

        /**
         * Specification with keys:
         * <dl>
         *   <dt>id</dt> <dd>id of empty client element</dd>
         *   <dt>dismissURL</dt> <dd>URL used to dismiss an alert</dd>
         * </dl>
         */
        function alertManager(spec) {

            var visible = true;
            var constructed = false;
            var list = null;

            T5.sub(DISMISS_ALERTS, null, function() {
                if (constructed) {
                    visible = false;
                    T5.dom.hide(spec.id);
                    visible = false;

                    T5.dom.removeChildren(list);

                    // Don't care about the response.
                    Tapestry.ajaxRequest(spec.dismissURL);
                }
            });

            // For the moment, there's a bit of prototype linkage here.

            T5.sub(T5.events.ADD_ALERT, null, function(alertSpec) {
                if (!constructed) {
                    list = construct(spec.id, spec.dismissText);
                    constructed = true;
                }

                if (!visible) {
                    T5.dom.show(spec.id);
                    visible = true;
                }

                // This part is Prototype specific, alas.

                var alertDiv = new Element("div", { "class": alertSpec['class'] }).update("<div class='t-dismiss' title='Dismiss'></div>" +
                    "<div class='t-message-container'>" + alertSpec.message + "</div>");

                list.insert({ bottom: alertDiv});

                var dismiss = find(alertDiv, ".t-dismiss");


                function removeAlert() {
                    T5.dom.remove(alertDiv);
                    if (list.innerHTML == '') {
                        T5.dom.hide(spec.id);
                        visible = false;
                    }
                }

                // transient is a reserved word in JavaScript, which cause YUICompressor
                // to fail.
                if (alertSpec['transient']) {
                    setTimeout(removeAlert, T5.alerts.TRANSIENT_DELAY);
                }

                on(dismiss, "click", function(event) {
                    event.stop();

                    removeAlert();

                    // TODO: Switch this to T5.ajax.sendRequest when implemented/available

                    // Send a request, we don't care about the response.

                    if (alertSpec.id) {
                        Tapestry.ajaxRequest(spec.dismissURL,
                            { parameters: {
                                id : alertSpec.id
                            }});
                    }
                });

            });
        }

        return {
            alertManager : alertManager,
            addAlert : addAlertPublisher
        }
    }

)
    ;

T5.define('alerts', {
    /** Time, in ms, that a transient message is displayed before automatically dismissing. */
    TRANSIENT_DELAY : 15000
});

