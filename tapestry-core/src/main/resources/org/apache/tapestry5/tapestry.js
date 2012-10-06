/* Copyright 2007-2012 The Apache Software Foundation
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

define("core/compat/tapestry", [
    "_",
    "core/spi",
    "core/events",
    "core/ajax",
    "core/forms",
    "core/compat/t5-dom",
    "core/compat/t5-console",
    "core/compat/t5-init"], function (_, spi, events, ajax, forms) {

    window.Tapestry = {

        Logging: T5.console,

        /**
         * Event that allows observers to perform cross-form validation after
         * individual fields have performed their validation. The form element is
         * passed as the event memo. Observers may set the validationError property
         * of the Form's Tapestry object to true (which will prevent form
         * submission).
         */
        FORM_VALIDATE_EVENT: events.form.validate,

        /**
         * Event fired just before the form submits, to allow observers to make
         * final preparations for the submission, such as updating hidden form
         * fields. The form element is passed as the event memo.
         */
        FORM_PREPARE_FOR_SUBMIT_EVENT: events.form.prepareForSubmit,

        /**
         * Form event fired after prepare.
         */
        FORM_PROCESS_SUBMIT_EVENT: events.form.processSubmit,

        /**
         * Event, fired on a field element, to cause observers to validate the
         * input. Passed a memo object with two keys: "value" (the raw input value)
         * and "translated" (the parsed value, usually meaning a number parsed from
         * a string). Observers may invoke Element.showValidationMessage() to
         * identify that the field is in error (and decorate the field and show a
         * popup error message).
         */
        FIELD_VALIDATE_EVENT: events.field.validate,

        /**
         * Event notification, on a form object, that is used to trigger validation
         * on all fields within the form (observed by each field's
         * Tapestry.FieldEventManager).
         */
        FORM_VALIDATE_FIELDS_EVENT: events.form.validateFields,

        /**
         * Event, fired on the document object, which identifies the current focus
         * input element.
         */
        FOCUS_CHANGE_EVENT: "tapestry:focuschange",

        /** Event, triggered on a zone element after the zone's content has been updated. */
        ZONE_UPDATED_EVENT: events.zone.didUpdate,

        /**
         * Event fired on a form fragment element to change the visibility of the
         * fragment. The event memo object includes a key, visible, that should be
         * true or false.
         */
        CHANGE_VISIBILITY_EVENT: events.formfragment.changeVisibility,

        /**
         * Event fired on a form fragment element to hide the element and remove it
         * from the DOM.
         */
        HIDE_AND_REMOVE_EVENT: events.formfragment.remove,

        /**
         * Event fired on a link or submit to request that it request that the
         * correct ZoneManager update from a provided URL.
         */
        TRIGGER_ZONE_UPDATE_EVENT: "tapestry:triggerzoneupdate",

        /** Event used when intercepting and canceling the normal click event. */
        ACTION_EVENT: "tapestry:action",

        /** When false, the default, the Tapestry.debug() function will be a no-op. */
        DEBUG_ENABLED: false,

        /** Time, in seconds, that console messages are visible. */
        CONSOLE_DURATION: 10,

        /**
         * CSS Class added to a &lt;form&gt; element that directs Tapestry to
         * prevent normal (HTTP POST) form submission, in favor of Ajax
         * (XmlHttpRequest) submission.   This is still supported in Tapestry 5.4, but
         * replaced with the data-prevent-submission attribute.
         *
         * @deprecated Use data-prevent-submission="true" instead
         */
        PREVENT_SUBMISSION: "t-prevent-submission",

        /** Initially, false, set to true once the page is fully loaded. */
        pageLoaded: false,

        /**
         * Invoked from onclick event handlers built into links and forms. Raises a
         * dialog if the page is not yet fully loaded. Gutted in 5.4, does nothing ...
         * the page loads fast enough :-).
         */
        waitForPage: function () {
        },

        /**
         * Adds a callback function that will be invoked when the DOM is loaded
         * (which occurs *before* window.onload, which has to wait for images and
         * such to load first. This simply observes the dom:loaded event on the
         * document object (support for which is provided by Prototype).
         */
        onDOMLoaded: spi.domReady,

        /**
         * Find all elements marked with the "t-invisible" CSS class and hide()s
         * them, so that Prototype's visible() method operates correctly. In
         * addition, finds form control elements and adds additional listeners to
         * them to support form field input validation.
         *
         * <p>
         * This is invoked when the DOM is first loaded, and AGAIN whenever dynamic
         * content is loaded via the Zone mechanism.
         */
        onDomLoadedCallback: function () {

            Tapestry.pageLoaded = true;

            $$(".t-invisible").each(function (element) {
                element.hide();
                element.removeClassName("t-invisible");
            });

            /*
             * Adds a focus observer that fades all error popups except for the
             * field in question.
             */
            $$("INPUT", "SELECT", "TEXTAREA").each(function (element) {
                /*
                 * Due to Ajax, we may execute the callback multiple times, and we
                 * don't want to add multiple listeners to the same element.
                 */
                var t = $T(element);

                if (!t.observingFocusChange) {
                    element.observe("focus", function () {
                        if (element != Tapestry.currentFocusField) {
                            document.fire(Tapestry.FOCUS_CHANGE_EVENT, element);

                            Tapestry.currentFocusField = element;
                        }
                    });

                    t.observingFocusChange = true;
                }
            });
        },

        /*
         * Generalized initialize function for Tapestry, used to help minimize the
         * amount of JavaScript for the page by removing redundancies such as
         * repeated Object and method names. The spec is a hash whose keys are the
         * names of methods of the Tapestry.Initializer object. The value is an
         * array of arrays. The outer arrays represent invocations of the method.
         * The inner array are the parameters for each invocation. As an
         * optimization, the inner value may not be an array but instead a single
         * value.
         */
        // Probably not used anymore:
        init: function (spec) {
            $H(spec).each(function (pair) {
                var functionName = pair.key;

                var initf = Tapestry.Initializer[functionName];

                if (initf == undefined) {
                    Tapestry.error(Tapestry.Messages.missingInitializer, {
                        name: functionName
                    });
                    return;
                }

                pair.value.each(function (parameterList) {
                    if (!Object.isArray(parameterList)) {
                        parameterList = [ parameterList ];
                    }

                    initf.apply(this, parameterList);
                });
            });
        },

        /**
         * Processes a typical Ajax request for a URL. In the simple case, a success
         * handler is provided (as options). In a more complex case, an options
         * object is provided, with keys as per Ajax.Request. The onSuccess key will
         * be overwritten, and defaults for onException and onFailure will be
         * provided. The handler should take up-to two parameters: the
         * XMLHttpRequest object itself, and the JSON Response (from the X-JSON
         * response header, usually null).
         *
         * This has been re-implemented in 5.4 as a wrapper around the core/ajax module.
         *
         * @param url
         *            of Ajax request
         * @param options
         *            either a success handler, or a set of options compatible with the core/ajax module.
         * @return the Ajax.Request object (perhaps, if prototype is the underlying
         */
        ajaxRequest: function (url, options) {
            if (_.isFunction(options)) {
                options = { onsuccess: options };
            }

            // Prototype and Tapestry 5.3 uses "onSuccess", "onException", "onFailure", but
            // core/ajax (Tapestry 5.4) uses "onsuccess", etc.

            var newOptions = {};
            _.each(options, function (value, key) {
                var newKey = key.substr(0, 2) === "on" ? key.toLowerCase() : key;

                newOptions[newKey] = value;
            });

            return ajax(url, newOptions);
        },

        /** Formats and displays an error message on the console. */
        error: function (message, substitutions) {
            Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.error);
        },

        /** Formats and displays a warning on the console. */
        warn: function (message, substitutions) {
            Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.warn);
        },

        /** Formats and displays an info message on the console. */
        info: function (message, substitutions) {
            Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.info);
        },

        /**
         * Formats and displays a debug message on the console. This function is a no-op unless Tapestry.DEBUG_ENABLED is true
         * (which will be the case when the application is running in development mode).
         */
        debug: function (message, substitutions) {
            if (Tapestry.DEBUG_ENABLED) {
                Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.debug);
            }
        },

        invokeLogger: function (message, substitutions, loggingFunction) {
            if (substitutions != undefined)
                message = message.interpolate(substitutions);

            loggingFunction.call(this, message);
        },

        /**
         * Obtains the Tapestry.ZoneManager object associated with a triggering
         * element (an &lt;a&gt; or &lt;form&gt;) configured to update a zone.
         * Writes errors to the AjaxConsole if the zone and ZoneManager can not be
         * resolved.
         *
         * @param element
         *            triggering element (id or instance)
         * @return Tapestry.ZoneManager instance for updated zone, or null if not
         *         found.
         */
        findZoneManager: function (element) {
            var zoneId = $T(element).zoneId;

            return Tapestry.findZoneManagerForZone(zoneId);
        },

        /**
         * Obtains the Tapestry.ZoneManager object associated with a zone element
         * (usually a &lt;div&gt;). Writes errors to the Ajax console if the element
         * or manager can not be resolved.
         *
         * @param zoneElement
         *            zone element (id or instance)
         * @return Tapestry.ZoneManager instance for zone, or null if not found
         */
        findZoneManagerForZone: function (zoneElement) {
            var element = $(zoneElement);

            if (!element) {
                Tapestry.error(Tapestry.Messages.missingZone, {
                    id: zoneElement
                });
                return null;
            }

            var manager = $T(element).zoneManager;

            if (!manager) {
                Tapestry.error(Tapestry.Messages.noZoneManager, element);
                return null;
            }

            return manager;
        },

        /**
         * Convert a user-provided localized number to an ordinary number (not a
         * string). Removes seperators and leading/trailing whitespace. Disallows
         * the decimal point if isInteger is true.
         *
         * @param number
         *            string provided by user
         * @param isInteger
         *            if true, disallow decimal point
         */
        formatLocalizedNumber: function (number, isInteger) {
            /*
             * We convert from localized string to a canonical string, stripping out
             * group seperators (normally commas). If isInteger is true, we don't
             * allow a decimal point.
             */

            var minus = Tapestry.decimalFormatSymbols.minusSign;
            var grouping = Tapestry.decimalFormatSymbols.groupingSeparator;
            var decimal = Tapestry.decimalFormatSymbols.decimalSeparator;

            var canonical = "";

            number.strip().toArray().each(function (ch) {
                if (ch == minus) {
                    canonical += "-";
                    return;
                }

                if (ch == grouping) {
                    return;
                }

                if (ch == decimal) {
                    if (isInteger)
                        throw Tapestry.Messages.notAnInteger;

                    ch = ".";
                } else if (ch < "0" || ch > "9")
                    throw Tapestry.Messages.invalidCharacter;

                canonical += ch;
            });

            return Number(canonical);
        },

        /**
         * Creates a clone of the indicated element, but with the alternate tag
         * name. Attributes of the original node are copied to the new node. Tag
         * names should be all upper-case. The content of the original element is
         * copied to the new element and the original element is removed. Event
         * observers on the original element will be lost.
         *
         * @param element
         *            element or element id
         * @since 5.2.0
         */
        replaceElementTagName: function (element, newTagName) {

            element = $(element);

            var tag = element.tagName;

            /* outerHTML is IE only; this simulates it on any browser. */

            var dummy = document.createElement('html');
            dummy.appendChild(element.cloneNode(true));
            var outerHTML = dummy.innerHTML;

            var replaceHTML = outerHTML.replace(new RegExp("^<" + tag, "i"),
                    "<" + newTagName).replace(new RegExp("</" + tag + ">$", "i"),
                    "</" + newTagName + ">");

            element.insert({
                before: replaceHTML
            });

            T5.dom.remove(element);
        },

        /**
         * Removes an element and all of its direct and indirect children. The
         * element is first purged, to ensure that Internet Explorer doesn't leak
         * memory if event handlers associated with the element (or its children)
         * have references back to the element.
         *
         * @since 5.2.0
         * @deprecated Since 5.3, use T5.dom.remove() instead
         */
        remove: T5.dom.remove,

        /** @deprecated Since 5.3, use T5.dom.purgeChildren instead */
        purgeChildren: T5.dom.purgeChildren
    };

    Element.addMethods({

        /**
         * Works upward from the element, checking to see if the element is visible.
         * Returns false if it finds an invisible container. Returns true if it
         * makes it as far as a (visible) FORM element.
         *
         * Note that this only applies to the CSS definition of visible; it doesn't
         * check that the element is scrolled into view.
         *
         * @param element
         *            to search up from
         * @param options
         *            Optional map of options. Only used key currently is "bound" which should be a javascript function name
         *            that determines whether the current element bounds the search. The default is to stop the search when
         *            the
         * @return true if visible (and containers visible), false if it or
         *         container are not visible
         */
        isDeepVisible: function (element, options) {
            var current = $(element);
            var boundFunc = (options && options.bound) || function (el) {
                return el.tagName == "FORM"
            };

            while (true) {
                if (!current.visible())
                    return false;

                if (boundFunc(current))
                    break;

                current = $(current.parentNode);
            }

            return true;
        },

        /**
         * Observes an event and turns it into a Tapestry.ACTION_EVENT. The original
         * event is stopped. The original event object is passed as the memo when
         * the action event is fired. This allows the logic for clicking an element
         * to be separated from the logic for processing that click event, which is
         * often useful when the click logic needs to be intercepted, or when the
         * action logic needs to be triggered outside the context of a DOM event.
         *
         * $T(element).hasAction will be true after invoking this method.
         *
         * @param element
         *            to observe events from
         * @param eventName
         *            name of event to observer, typically "click"
         * @param handler
         *            function to be invoked; it will be registered as a observer of
         *            the Tapestry.ACTION_EVENT.
         */
        observeAction: function (element, eventName, handler) {
            element.observe(eventName, function (event) {

                event.stop();

                element.fire(Tapestry.ACTION_EVENT, event);
            });

            element.observe(Tapestry.ACTION_EVENT, handler);

            $T(element).hasAction = true;
        }
    });

    Element
            .addMethods(
            'FORM',
            {
                /**
                 * Identifies in the form what is the cause of the
                 * submission. The element's id is stored into the t:submit
                 * hidden field (created as needed).
                 *
                 * @param form
                 *            to update
                 * @param element
                 *            id or element that is the cause of the submit
                 *            (a Submit or LinkSubmit)
                 */
                setSubmittingElement: function (form, element) {
                    forms.setSubmittingElement(spi(form), spi(element));
                },

                /**
                 * Turns off client validation for the next submission of
                 * the form.
                 */
                skipValidation: function (form) {
                    forms.skipValidation(spi(form));
                },

                /**
                 * Programmatically perform a submit, invoking the onsubmit
                 * event handler (if present) before calling form.submit().
                 */
                performSubmit: function (form, event) {
                    if (form.onsubmit == undefined
                            || form.onsubmit.call(window.document, event)) {
                        form.submit();
                    }
                },

                /**
                 * Sends an Ajax request to the Form's action. This
                 * encapsulates a few things, such as a default onFailure
                 * handler, and working around bugs/features in Prototype
                 * concerning how submit buttons are processed.
                 *
                 * @param form
                 *            used to define the data to be sent in the
                 *            request
                 * @param options
                 *            standard Prototype Ajax Options
                 * @return Ajax.Request the Ajax.Request created for the
                 *         request
                 */
                sendAjaxRequest: function (form, url, options) {
                    form = $(form);

                    /*
                     * Generally, options should not be null or missing,
                     * because otherwise there's no way to provide any
                     * callbacks!
                     */
                    options = Object.clone(options || {});

                    /*
                     * Find the elements, skipping over any submit buttons.
                     * This works around bugs in Prototype 1.6.0.2.
                     */
                    var elements = form.getElements().reject(function (e) {
                        return e.tagName == "INPUT" && e.type == "submit";
                    });

                    var hash = Form.serializeElements(elements, true);

                    /*
                     * Copy the parameters in, overwriting field values,
                     * because Prototype 1.6.0.2 does not.
                     */
                    Object.extend(hash, options.parameters);

                    options.parameters = hash;

                    /*
                     * ajaxRequest will convert the hash into a query
                     * string and post it.
                     */

                    return Tapestry.ajaxRequest(url, options);
                }
            });

    Element.addMethods([ 'INPUT', 'SELECT', 'TEXTAREA' ], {
        /**
         * Invoked on a form element (INPUT, SELECT, etc.), gets or creates the
         * Tapestry.FieldEventManager for that field.
         *
         * @param field
         *            field element
         */
        getFieldEventManager: function (field) {
            field = $(field);
            var t = $T(field);

            var manager = t.fieldEventManager;

            if (manager == undefined) {
                manager = new Tapestry.FieldEventManager(field);
                t.fieldEventManager = manager;
            }

            return manager;
        },

        /**
         * Obtains the Tapestry.FieldEventManager and asks it to show the validation
         * message. Sets the validationError property of the elements tapestry
         * object to true.
         *
         * @param element
         * @param message
         *            to display
         */
        showValidationMessage: function (element, message) {
            element = $(element);

            element.getFieldEventManager().showValidationMessage(message);

            return element;
        },

        /**
         * Removes any validation decorations on the field, and hides the error
         * popup (if any) for the field.
         */
        removeDecorations: function (element) {
            $(element).getFieldEventManager().removeDecorations();

            return element;
        },

        /**
         * Adds a standard validator for the element, an observer of
         * Tapestry.FIELD_VALIDATE_EVENT. The validator function will be passed the
         * current field value and should throw an error message if the field's
         * value is not valid.
         *
         * @param element
         *            field element to validate
         * @param validator
         *            function to be passed the field value
         */
        addValidator: function (element, validator) {

            element.observe(Tapestry.FIELD_VALIDATE_EVENT, function (event) {
                try {
                    validator.call(this, event.memo.translated);
                } catch (message) {
                    element.showValidationMessage(message);
                }
            });

            return element;
        }
    });

    /** Compatibility: set Tapestry.Initializer equal to T5.initializers. */

    Tapestry.Initializer = T5.initializers;

    /** Container of functions that may be invoked by the Tapestry.init() function. */
    T5.extendInitializers({

        /** Make the given field the active field (focus on the field). */
        activate: function (id) {
            $(id).activate();
        },

        /**
         * evalScript is a synonym for the JavaScript eval function. It is
         * used in Ajax requests to handle any setup code that does not fit
         * into a standard Tapestry.Initializer call.
         */
        evalScript: eval,

        ajaxFormLoop: function (spec) {
            var rowInjector = $(spec.rowInjector);

            $(spec.addRowTriggers).each(function (triggerId) {
                $(triggerId).observeAction("click", function (event) {
                    $(rowInjector).trigger();
                });
            });
        },

        formLoopRemoveLink: function (spec) {
            var link = $(spec.link);
            var fragmentId = spec.fragment;

            link.observeAction("click", function (event) {
                var successHandler = function (transport) {
                    var container = $(fragmentId);

                    Tapestry.remove(container);
                };

                ajax(spec.url, { onsuccess: successHandler});
            });
        },

        /**
         * Convert a form or link into a trigger of an Ajax update that
         * updates the indicated Zone.
         *
         * @param spec.linkId
         *            id or instance of &lt;form&gt; or &lt;a&gt; element
         * @param spec.zoneId
         *            id of the element to update when link clicked or form
         *            submitted
         * @param spec.url
         *            absolute component event request URL
         */
        linkZone: function (spec) {
            Tapestry.Initializer.updateZoneOnEvent("click", spec.linkId,
                    spec.zoneId, spec.url);
        },

        /**
         * Converts a link into an Ajax update of a Zone. The url includes
         * the information to reconnect with the server-side Form.
         *
         * @param spec.selectId
         *            id or instance of &lt;select&gt;
         * @param spec.zoneId
         *            id of element to update when select is changed
         * @param spec.url
         *            component event request URL
         */
        linkSelectToZone: function (spec) {
            Tapestry.Initializer.updateZoneOnEvent("change", spec.selectId,
                    spec.zoneId, spec.url);
        },

        linkSubmit: function (spec) {

            Tapestry.replaceElementTagName(spec.clientId, "A");

            $(spec.clientId).writeAttribute("href", "#");

            if (spec.mode == "cancel") {
                $(spec.clientId).writeAttribute("name", "cancel");
            }

            $(spec.clientId).observeAction("click", function (event) {

                var form = $(spec.form);

                if (spec.mode != "normal") {
                    form.skipValidation();
                }

                form.setSubmittingElement(this);

                form.performSubmit(event);
            });
        },

        /**
         * Used by other initializers to connect an element (either a link
         * or a form) to a zone.
         *
         * @param eventName
         *            the event on the element to observe
         * @param element
         *            the element to observe for events
         * @param zoneId
         *            identified a Zone by its clientId. Alternately, the
         *            special value '^' indicates that the Zone is a
         *            container of the element (the first container with the
         *            't-zone' CSS class).
         * @param url
         *            The request URL to be triggered when the event is
         *            observed. Ultimately, a partial page update JSON
         *            response will be passed to the Zone's ZoneManager.
         */
        updateZoneOnEvent: function (eventName, element, zoneId, url) {
            element = $(element);

            $T(element).zoneUpdater = true;

            var zoneElement = zoneId == '^' ? $(element).up('.t-zone')
                    : $(zoneId);

            if (!zoneElement) {
                Tapestry
                        .error(
                        "Could not find zone element '#{zoneId}' to update on #{eventName} of element '#{elementId}'.",
                        {
                            zoneId: zoneId,
                            eventName: eventName,
                            elementId: element.id
                        });
                return;
            }

            /*
             * Update the element with the id of zone div. This may be
             * changed dynamically on the client side.
             */

            $T(element).zoneId = zoneElement.id;

            if (element.tagName == "FORM") {

                // Create the FEM if necessary.
                element.addClassName(Tapestry.PREVENT_SUBMISSION);

                /*
                 * After the form is validated and prepared, this code will
                 * process the form submission via an Ajax call. The
                 * original submit event will have been cancelled.
                 */

                element
                        .observe(
                        Tapestry.FORM_PROCESS_SUBMIT_EVENT,
                        function () {
                            var mgr = Tapestry.findZoneManager(element);

                            if (!mgr) {return;}

                            var successHandler = function (transport) {
                                mgr.processReply(transport);
                            };

                            element.sendAjaxRequest(url, {
                                parameters: {
                                    "t:zoneid": zoneId
                                },
                                onSuccess: successHandler
                            });
                        });

                return;
            }

            /* Otherwise, assume it's just an ordinary link or input field. */

            element.observeAction(eventName, function (event) {
                element.fire(Tapestry.TRIGGER_ZONE_UPDATE_EVENT);
            });

            element.observe(Tapestry.TRIGGER_ZONE_UPDATE_EVENT, function () {

                var zoneObject = Tapestry.findZoneManager(element);

                if (!zoneObject)
                    return;

                /*
                 * A hack related to allowing a Select to perform an Ajax
                 * update of the page.
                 */

                var parameters = {};

                if (element.tagName == "SELECT" && element.value) {
                    parameters["t:selectvalue"] = element.value;
                }

                zoneObject.updateFromURL(url, parameters);
            });
        },

        /**
         * Keys in the masterSpec are ids of field control elements. Value
         * is a list of validation specs. Each validation spec is a 2 or 3
         * element array.
         */
        validate: function (masterSpec) {
            $H(masterSpec)
                    .each(
                    function (pair) {

                        var field = $(pair.key);

                        /*
                         * Force the creation of the field event
                         * manager.
                         */

                        $(field).getFieldEventManager();

                        $A(pair.value)
                                .each(function (spec) {
                                    /*
                                     * Each pair value is an array of specs, each spec is a 2 or 3 element array. validator function name, message, optional constraint
                                     */

                                    var name = spec[0];
                                    var message = spec[1];
                                    var constraint = spec[2];

                                    var vfunc = Tapestry.Validator[name];

                                    if (vfunc == undefined) {
                                        Tapestry
                                                .error(Tapestry.Messages.missingValidator, {
                                                    name: name,
                                                    fieldName: field.id
                                                });
                                        return;
                                    }

                                    /*
                                     * Pass the extended field, the provided message, and the constraint object to the Tapestry.Validator function, so that it can, typically, invoke field.addValidator().
                                     */
                                    vfunc.call(this, field, message, constraint);
                                });
                    });
        },

        zone: function (spec) {
            new Tapestry.ZoneManager(spec);
        },


        formInjector: function (spec) {
            new Tapestry.FormInjector(spec);
        },


        /**
         * Invoked on a submit element to indicate that it forces form to submit as a cancel (bypassing client-side validation
         * and most server-side processing).
         * @param clientId of submit element
         */
        enableBypassValidation: function (clientId) {

            /*
             * Set the form's skipValidation property and allow the event to
             * continue, which will ultimately submit the form.
             */
            $(clientId).observeAction("click", function (event) {
                $(this.form).skipValidation();
                $(this.form).setSubmittingElement($(clientId));
                $(this.form).performSubmit(event);
            });
        }

    });

    /*
     * Collection of field based functions related to validation. Each function
     * takes a field, a message and an optional constraint value. Some functions are
     * related to Translators and work on the format event, other's are from
     * Validators and work on the validate event.
     */

    Tapestry.Validator = {

        required: function (field, message) {
            $(field).getFieldEventManager().requiredCheck = function (value) {
                if ((_.isString(value) && value.strip() == '')
                        || value == null)
                    $(field).showValidationMessage(message);
            };
        },

        /** Supplies a client-side numeric translator for the field. */
        numericformat: function (field, message, isInteger) {
            $(field).getFieldEventManager().translator = function (input) {
                try {
                    return Tapestry.formatLocalizedNumber(input, isInteger);
                } catch (e) {
                    $(field).showValidationMessage(message);
                }
            };
        },

        minlength: function (field, message, length) {
            field.addValidator(function (value) {
                if (value.length < length)
                    throw message;
            });
        },

        maxlength: function (field, message, maxlength) {
            field.addValidator(function (value) {
                if (value.length > maxlength)
                    throw message;
            });
        },

        min: function (field, message, minValue) {
            field.addValidator(function (value) {
                if (value < minValue)
                    throw message;
            });
        },

        max: function (field, message, maxValue) {
            field.addValidator(function (value) {
                if (value > maxValue)
                    throw message;
            });
        },

        regexp: function (field, message, pattern) {
            var regexp = new RegExp(pattern);

            field.addValidator(function (value) {
                if (!regexp.test(value))
                    throw message;
            });
        }
    };

    Tapestry.ErrorPopup = Class.create({

        /*
         * If the images associated with the error popup are overridden (by
         * overriding Tapestry's default.css stylesheet), then some of these values
         * may also need to be adjusted.
         */
        BUBBLE_VERT_OFFSET: -34,

        BUBBLE_HORIZONTAL_OFFSET: -20,

        BUBBLE_WIDTH: "auto",

        BUBBLE_HEIGHT: "39px",

        IE_FADE_TIME: 500,

        initialize: function (field) {
            this.field = $(field);

            // The UI elements (outerDiv and friends) are created by the first call to setMessage().
            this.outerDiv = null;
        },

        /**
         * Invoked once, from setMessage(), to create the outerDiv and innerSpan elements, as well as necessary listeners
         *  (to hide the popup if clicked), and reposition the popup as necessary when the window resizes.
         */
        createUI: function () {
            this.innerSpan = new Element("span");
            this.outerDiv = $(new Element("div", {
                'id': this.field.id + "_errorpopup",
                'class': 't-error-popup'
            })).update(this.innerSpan).hide();

            var body = $(document.body);

            body.insert({
                bottom: this.outerDiv
            });

            this.outerDiv.absolutize();

            this.outerDiv.observe("click", function (event) {
                this.ignoreNextFocus = true;

                this.stopAnimation();

                this.outerDiv.hide();

                this.field.activate();

                event.stop();
            }.bindAsEventListener(this));

            this.queue = {
                position: 'end',
                scope: this.field.id
            };

            Event.observe(window, "resize", this.repositionBubble.bind(this));

            document.observe(Tapestry.FOCUS_CHANGE_EVENT, function (event) {
                if (this.ignoreNextFocus) {
                    this.ignoreNextFocus = false;
                    return;
                }

                if (event.memo == this.field) {
                    this.fadeIn();
                    return;
                }

                /*
                 * If this field is not the focus field after a focus change, then
                 * its bubble, if visible, should fade out. This covers tabbing
                 * from one form to another.
                 */
                this.fadeOut();

            }.bind(this));
        },

        showMessage: function (message) {

            if (this.outerDiv == null) {
                this.createUI();
            }

            this.stopAnimation();

            this.innerSpan.update(message);

            this.hasMessage = true;

            this.fadeIn();
        },

        repositionBubble: function () {
            var fieldPos = this.field.cumulativeOffset();

            this.outerDiv.setStyle({
                top: (fieldPos[1] + this.BUBBLE_VERT_OFFSET) + "px",
                left: (fieldPos[0] + this.BUBBLE_HORIZONTAL_OFFSET) + "px",
                width: this.BUBBLE_WIDTH,
                height: this.BUBBLE_HEIGHT
            });
        },

        fadeIn: function () {
            if (!this.hasMessage)
                return;

            this.repositionBubble();

            if (this.animation)
                return;

            if (Prototype.Browser.IE) {

                this.outerDiv.show();

                var bound = _.bind(this.hideIfNotFocused, this);

                _.delay(bound, this.IE_FADE_TIME);

                return;
            }

            this.animation = new Effect.Appear(this.outerDiv, {
                queue: this.queue,
                afterFinish: function () {
                    this.animation = null;

                    if (this.field != Tapestry.currentFocusField)
                        this.fadeOut();
                }.bind(this)
            });
        },

        /** Used in IE to hide the field if not the focus field. */
        hideIfNotFocused: function () {

            if (this.outerDiv != null && this.field != Tapestry.currentFocusField) {
                this.outerDiv.hide();
            }
        },


        stopAnimation: function () {
            if (this.animation)
                this.animation.cancel();

            this.animation = null;
        },

        fadeOut: function () {
            if (this.animation || this.outerDiv == null)
                return;

            if (Prototype.Browser.IE) {

                var div = this.outerDiv;

                _.delay(function () {
                    div.hide();
                }, this.IE_FADE_TIME);

                return;
            }

            this.animation = new Effect.Fade(this.outerDiv, {
                queue: this.queue,
                afterFinish: function () {
                    this.animation = null;
                }.bind(this)
            });
        },

        hide: function () {
            this.hasMessage = false;

            this.stopAnimation();

            this.outerDiv && this.outerDiv.hide();
        }
    });

    Tapestry.FieldEventManager = Class.create({

        initialize: function (field) {

            this.field = $(field);

            this.translator = Prototype.K;

            // This marker clues in the Form that validation should be triggered on this
            // element.
            this.field.writeAttribute("data-validation", true);

            var _this = this;

            $(this.field).observe(Tapestry.FORM_VALIDATE_FIELDS_EVENT,
                    function (event) {

                        _this.validateInput();

                        if (_this.inError()) {
                            event.memo.error = true;
                        }
                    }
            );
        },

        getLabel: function () {
            if (!this.label) {
                var selector = "label[for='" + this.field.id + "']";
                this.label = this.field.form.down(selector);
            }

            return this.label;
        },

        getIcon: function () {
            return null;
        },

        /**
         * Removes validation decorations if present. Hides the ErrorPopup, if it
         * exists.
         */
        removeDecorations: function () {
            this.field.removeClassName("t-error");

            this.getLabel() && this.getLabel().removeClassName("t-error");

            if (this.errorPopup)
                this.errorPopup.hide();
        },

        /**
         * Show a validation error message, which will add decorations to the field
         * and it label, make the icon visible, and raise the field's
         * Tapestry.ErrorPopup to show the message.
         *
         * @param message
         *            validation message to display
         */
        showValidationMessage: function (message) {

            this.field.addClassName("t-error");

            this.getLabel() && this.getLabel().addClassName("t-error");

            if (this.errorPopup == undefined)
                this.errorPopup = new Tapestry.ErrorPopup(this.field);

            this.errorPopup.showMessage(message);
        },

        inError: function () {
            return this.field.hasClassName("t-error");
        },

        /**
         * Invoked when a form is submitted to perform
         * field validations. Field validations are skipped for disabled fields or fields that are not visible.
         * If any validation fails, an error popup is raised for the field, to display the
         * validation error message.
         *
         */
        validateInput: function () {
            this.removeDecorations();

            if (this.field.disabled)
                return;

            if (!this.field.isDeepVisible())
                return;

            var value = $F(this.field);

            if (this.requiredCheck) {
                this.requiredCheck.call(this, value);

                if (this.inError()) { return; }
            }

            /*
             * Don't try to validate blank values; if the field is required, that
             * error is already noted and presented to the user.
             */
            if (!(_.isString(value) && value.blank())) {
                var translated = this.translator(value);

                /*
                 * If Format went ok, perhaps do the other validations.
                 */
                if (!this.inError()) {
                    this.field.fire(Tapestry.FIELD_VALIDATE_EVENT, {
                        value: value,
                        translated: translated
                    });
                }

            }
        }
    });

    function _show(element) {
        return new spi(element).show();
    }

    function _hide(element) {
        return new spi(element).hide();
    }

    function _none(element) {
        return new spi(element);
    }

    /*
     * Wrappers around Prototype and Scriptaculous effects. All the functions of
     * this object should have all-lowercase names. The methods all return the
     * Effect object they create.
     *
     * 5.4 notes: there are no longer any effects, and what's returned is
     * an ElementWrapper (as defined in module "core/spi").
     */
    Tapestry.ElementEffect = {

        /**
         * Was: Fades the element in.
         * Now: show the element.
         */
        show: _show,

        /**
         * Was: The classic yellow background fade.
         * Now: Does nothing, returns the ElementWrapper.
         */
        highlight: _none,

        /* Was: Scrolls the content down.
         * Now: show the element.
         * */
        slidedown: _show,

        /**
         * Was: Slides the content back up (opposite of slidedown).
         * Now: Hides the element.
         */
        slideup: _hide,

        /**
         * Was: Fades the content out (opposite of show).
         * Now: Hides the element.
         */
        fade: _hide,

        /**
         * Was: Does nothing to the element, returns the element.
         * Now: Does nothing, returns the ElementWrapper.
         */
        none: _none
    };

    /**
     * Manages a &lt;div&gt; (or other element) for dynamic updates.
     *
     */
    Tapestry.ZoneManager = Class.create({
        /*
         * spec are the parameters for the Zone: trigger:
         * spec.element -- id or instance of div element
         * spec.parameters -- additional parameters (related to Zones nested inside Forms) (optional)
         * Prior releases included spec.hide and spec.show (to control animations) but these have been
         * deprecated.
         */
        initialize: function (spec) {
            this.elementId = spec.element;

            // When updates arrive, the outer element is always made visible.
            this.element = spi(spec.element);
            this.specParameters = spec.parameters;

            /* Link the div back to this zone. */

            $T(this.element.element).zoneManager = this;
        },

        /**
         * Updates the zone's content; as of 5.4 this is just a shell
         * that triggers the events.zone.update event; see the core/zone module
         * for the default handler for that event.
         *
         * @param content
         */
        show: function (content) {
            this.element.trigger(events.zone.update, { content: content });
        },

        /**
         * Invoked with a reply (i.e., transport.responseJSON), this updates the
         * managed element and processes any JavaScript in the reply. The zone's
         * content is only updated if the response has a content key.
         *
         * @param reply
         *            response in JSON format appropriate to a Tapestry.Zone
         */
        processReply: function (response) {
            /*
             * In a multi-zone update, the reply.content may be missing, in
             * which case, leave the current content in place. TAP5-1177
             */
            var reply = response.responseJSON;

            reply && reply.content != undefined && this.show(reply.content);
        },

        /**
         * Initiates an Ajax request to update this zone by sending a request to the
         * URL. Expects the correct JSON reply (wth keys content, etc.).
         *
         * @param URL
         *            component event request URL
         * @param parameters
         *            object containing additional key/value pairs (optional)
         */
        updateFromURL: function (URL, parameters) {

            var fullParameters = _.extend({ "t:zoneid": this.elementId },
                    this.specParameters,
                    parameters);

            ajax(URL, {
                parameters: fullParameters,
                onsuccess: _.bind(this.processReply, this)
            });
        }
    });

    Tapestry.FormInjector = Class.create({

        initialize: function (spec) {
            this.element = $(spec.element);
            this.url = spec.url;
            this.below = spec.below;

            this.showFunc = Tapestry.ElementEffect[spec.show]
                    || Tapestry.ElementEffect.highlight;

            this.element.trigger = function () {

                var successHandler = function (transport) {

                    var reply = transport.responseJSON;

                    /*
                     * Clone the FormInjector element (usually a div) to create the
                     * new element, that gets inserted before or after the
                     * FormInjector's element.
                     */
                    var newElement = new Element(this.element.tagName, {
                        'class': this.element.className
                    });

                    /* Insert the new element before or after the existing element. */

                    var param = {};
                    param[this.below ? "after" : "before"] = newElement;

                    this.element.insert(param);

                    /*
                     * Update the empty element with the content from the server
                     */

                    newElement.update(reply.content);

                    newElement.id = reply.elementId;

                    /*
                     * Add some animation to reveal it all.
                     */

                    this.showFunc(newElement);
                }.bind(this);

                ajax(this.url, { onsuccess: successHandler });

                return false;

            }.bind(this);
        }
    });

    /**
     * In the spirit of $(), $T() exists to access a hash of extra data about an
     * element. In release 5.1 and prior, a hash attached to the element by Tapestry
     * was returned. In 5.2, Prototype's storage object is returned, which is less
     * likely to cause memory leaks in IE.
     *
     * @deprecated With no specific replacement. To be removed after Tapestry 5.2.
     * @param element
     *            an element instance or element id
     * @return object Prototype storage object for the element
     */
    window.$T = function (element) {
        return $(element).getStorage();
    }

    spi.domReady(Tapestry.onDomLoadedCallback);
    spi.on(window, "beforeunload", function () { Tapestry.windowUnloaded = true; });

    return Tapestry;
});
