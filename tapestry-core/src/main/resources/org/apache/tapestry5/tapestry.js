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

var Tapestry = {

    /** Event that allows observers to perform cross-form validation after individual
     *  fields have performed their validation. The form element is passed as the
     *  event memo. Observers may set the validationError property of the Form's Tapestry object to true (which
     *  will prevent form submission).
     */
    FORM_VALIDATE_EVENT : "tapestry:formvalidate",

    /** Event fired just before the form submits, to allow observers to make
     *  final preparations for the submission, such as updating hidden form fields.
     *  The form element is passed as the event memo.
     */
    FORM_PREPARE_FOR_SUBMIT_EVENT : "tapestry:formprepareforsubmit",

    /**
     *  Form event fired after prepare.
     */
    FORM_PROCESS_SUBMIT_EVENT : "tapestry:formprocesssubmit",

    /** Event, fired on a field element, to cause observers to validate the input. Passes a memo object with
     * two keys: "value" (the raw input value) and "translated" (the parsed value, usually meaning a number
     * parsed from a string).  Observers may invoke Element.showValidationMessage()
     *  to identify that the field is in error (and decorate the field and show a popup error message).
     */
    FIELD_VALIDATE_EVENT : "tapestry:fieldvalidate",

    /** Event, fired on the document object, which identifies the current focus input element. */
    FOCUS_CHANGE_EVENT : "tapestry:focuschange",

    /** Event, fired on a zone element when the zone is updated with new content. */
    ZONE_UPDATED_EVENT : "tapestry:zoneupdated",

    /** When false, the default, the Tapestry.debug() function will be a no-op. */
    DEBUG_ENABLED : false,

    /** Time, in seconds, that console messages are visible. */
    CONSOLE_DURATION : 10,

    // Initially, false, set to true once the page is fully loaded.

    pageLoaded : false,

    /**
     * Invoked from onclick event handlers built into links and forms. Raises a dialog
     * if the page is not yet fully loaded.
     */
    waitForPage : function(event)
    {
        if (Tapestry.pageLoaded) return;

        Event.extend(event || window.event).stop();

        var body = $(document.body);

        // The overlay is stretched to cover the full screen (including scrolling areas)
        // and is used to fade out the background ... and prevent keypresses (its z-order helps there).

        var overlay = new Element("div", {
            'class' : 't-dialog-overlay'
        });
        overlay.setOpacity(0.0);

        body.insert({
            top: overlay
        });

        new Effect.Appear(overlay, {
            duration: 0.2,
            from: 0.0
        });

        var messageDiv = new Element("div", {
            'class' : 't-page-loading-banner'
        }).update(Tapestry.Messages.pageIsLoading);
        overlay.insert({
            top: messageDiv
        });

        var hideDialog = function()
        {
            new Effect.Fade(overlay, {
                duration: 0.2,
                afterFinish: function()
                {
                    overlay.remove();
                }
            });
        };

        document.observe("dom:loaded", hideDialog);

        // A rare race condition.

        if (Tapestry.pageLoaded)
            hideDialog.call(null);
    },


    // Adds a callback function that will be invoked when the DOM is loaded (which
    // occurs *before* window.onload, which has to wait for images and such to load
    // first.  This simply observes the dom:loaded event on the document object (support for
    // which is provided by Prototype).
    onDOMLoaded : function(callback)
    {
        document.observe("dom:loaded", callback);
    },

    /** Find all elements marked with the "t-invisible" CSS class and hide()s them, so that
     * Prototype's visible() method operates correctly. In addition,
     * finds form control elements and adds additional listeners to them to support
     * form field input validation.
     *
     * <p>This is invoked when the
     * DOM is first loaded, and AGAIN whenever dynamic content is loaded via the Zone
     * mechanism.
     */
    onDomLoadedCallback : function()
    {
        // Turn off click & submit protection inside Tapestry.waitForPage().

        Tapestry.pageLoaded = true;

        Tapestry.ScriptManager.initialize();

        $$(".t-invisible").each(function(element)
        {
            element.hide();
            element.removeClassName("t-invisible");
        });

        // Adds a focus observer that fades all error popups except for the
        // field in question.

        $$("INPUT", "SELECT", "TEXTAREA").each(function(element)
        {
            // Due to Ajax, we may execute the callback multiple times,
            // and we don't want to add multiple listeners to the same
            // element.

            var t = $T(element);

            if (! t.observingFocusChange)
            {
                element.observe("focus", function()
                {
                    if (element != Tapestry.currentFocusField)
                    {
                        document.fire(Tapestry.FOCUS_CHANGE_EVENT, element);

                        Tapestry.currentFocusField = element;
                    }
                });

                t.observingFocusChange = true;
            }
        });

        // When a submit element is clicked, record the name of the element
        // on the associated form. This is necessary for some Ajax processing,
        // see TAPESTRY-2324.

        $$("INPUT[type=submit]").each(function(element)
        {
            var t = $T(element);

            if (!t.trackingClicks)
            {
                element.observe("click", function()
                {
                    $T(element.form).lastSubmit = element;
                });

                t.trackingClicks = true;
            }
        });
    },

    /* Generalized initialize function for Tapestry, used to help minimize the amount of JavaScript
     * for the page by removing redundancies such as repeated Object and method names. The spec
     * is a hash whose keys are the names of methods of the Tapestry.Initializer object.
     * The value is an array of arrays.  The outer arrays represent invocations
     * of the method.  The inner array are the parameters for each invocation.
     * As an optimization, the inner value may not be an array but instead
     * a single value.
     */
    init : function(spec)
    {
        $H(spec).each(function(pair)
        {
            var functionName = pair.key;

            var initf = Tapestry.Initializer[functionName];

            if (initf == undefined)
            {
                Tapestry.error(Tapestry.Messages.missingInitializer, {
                    name:functionName
                });
                return;
            }

            pair.value.each(function(parameterList)
            {
                if (! Object.isArray(parameterList))
                {
                    parameterList = [
                        parameterList
                    ];
                }

                initf.apply(this, parameterList);
            });
        });
    },

    /** Formats and displays an error message on the console. */
    error : function (message, substitutions)
    {
        Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.error);
    },

    /** Formats and displays a warning on the console. */
    warn : function (message, substitutions)
    {
        Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.warn);
    },

    /** Formats and displays a debug message on the console. */
    debug : function (message, substitutions)
    {
        Tapestry.invokeLogger(message, substitutions, Tapestry.Logging.debug);
    },

    invokeLogger : function(message, substitutions, loggingFunction)
    {
        if (substitutions != undefined)
            message = message.interpolate(substitutions);

        loggingFunction.call(this, message);
    },

    /**
     * Passed the JSON content of a Tapestry partial markup response, extracts
     * the script and stylesheet information.  JavaScript libraries and stylesheets are loaded,
     * then the callback is invoked.  All three keys are optional:
     * <dl>
     * <dt>redirectURL</dt> <dd>URL to redirect to (in which case, the callback is not invoked)</dd>
     * <dt>scripts</dt><dd>Array of strings (URIs of scripts)</dd>
     * <dt>stylesheets</dt><dd>Array of hashes, each hash has key href and optional key media</dd>
     *
     * @param reply JSON response object from the server
     * @param callback function invoked after the scripts have all loaded (presumably, to update the DOM)
     */
    loadScriptsInReply : function(reply, callback)
    {
        var redirectURL = reply.redirectURL;

        if (redirectURL)
        {
            // Check for complete URL.
            if (/^https?:/.test(redirectURL))
            {
                window.location = redirectURL;
                return;
            }

            window.location.pathname = redirectURL;

            // Don't bother loading scripts or invoking the callback.

            return;
        }

        Tapestry.ScriptManager.addStylesheets(reply.stylesheets);

        Tapestry.ScriptManager.addScripts(reply.scripts,
                function()
                {
                    callback.call(this);

                    // After the callback updates the DOM
                    // (presumably), continue on with
                    // evaluating the reply.script
                    // and other final steps.

                    if (reply.script) eval(reply.script);

                    Tapestry.onDomLoadedCallback();

                });
    },

    /**
     * Default function for handling Ajax-related failures.
     */
    ajaxFailureHandler : function(response)
    {
        var message = response.getHeader("X-Tapestry-ErrorMessage");

        Tapestry.error("Communication with the server failed: " + message);

        Tapestry.debug(Tapestry.Messages.ajaxFailure + message, response);
    },

    /**
     * Processes a typical Ajax request for a URL invoking the provided handler on success.
     * On failure, error() is invoked to inform the user.
     *
     * @param url of Ajax request
     * @param successHandler to invoke on success
     * @return the Ajax.Request object
     */
    ajaxRequest : function(url, successHandler)
    {
        return new Ajax.Request(url, {
            onSuccess: function(response, jsonResponse)
            {
                // When the page is unloaded, pending Ajax requests appear to terminate
                // as succesful (but with no reply value). Since we're trying to navigate
                // to a new page anyway, we just ignore those false success callbacks.
                // We have a listener for the window's "beforeunload" event that sets
                // this flag.

                if (Tapestry.windowUnloaded)
                    return;

                if (! response.request.success())
                {
                    Tapestry.error(Tapestry.Messages.ajaxRequestUnsuccessful);
                    return;
                }

                try
                {
                    // Re-invoke the success handler, capturing any exceptions.
                    successHandler.call(this, response, jsonResponse);
                }
                catch (e)
                {
                    Tapestry.error(Tapestry.Messages.clientException + e);
                }
            },
            onException: Tapestry.ajaxFailureHandler,
            onFailure: Tapestry.ajaxFailureHandler
        });
    },

    /** Obtains the Tapestry.ZoneManager object associated with a triggering element
     * (an <a> or <form>) configured to update a zone. Writes errors to the AjaxConsole
     * if the zone and ZoneManager can not be resolved.
     *
     * @param element   triggering element (id or instance)
     * @return Tapestry.ZoneManager instance for updated zone, or null if not found.
     */
    findZoneManager : function(element)
    {
        var zoneId = $T(element).zoneId;

        return Tapestry.findZoneManagerForZone(zoneId);
    },

    /**
     * Obtains the Tapestry.ZoneManager object associated with a zone element (usually
     * a <div>). Writes errors to the Ajax console if the element or manager
     * can not be resolved.
     * @param zoneElement  zone element (id or instance)
     * @return Tapestry.ZoneManager instance for zone, or null if not found
     */
    findZoneManagerForZone : function(zoneElement)
    {
        var element = $(zoneElement);

        if (!zoneElement)
        {
            Tapestry.error(Tapestry.Messages.missingZone, {
                id:zoneElement
            });
            return null;
        }

        var manager = $T(zoneElement).zoneManager;

        if (!manager)
        {
            Tapestry.error(Tapestry.Messages.noZoneManager, element);
            return null;
        }

        return manager;
    },

    /**
     * Used to reconstruct a complete URL from a path that is (or may be) relative to window.location.
     * This is used when determining if a JavaScript library or CSS stylesheet has already been loaded.
     * Recognizes complete URLs (which are returned unchanged), otherwise the URLs are expected to be
     * absolute paths.
     *
     * @param path
     * @return complete URL as string
     */
    rebuildURL : function(path)
    {
        if (path.match(/^https?:/))
        {
            return path;
        }

        if (! path.startsWith("/"))
        {
            Tapestry.error(Tapestry.Messages.pathDoesNotStartWithSlash, {
                path: path
            });

            return path;
        }

        var l = window.location;
        return l.protocol + "//" + l.host + path;
    },

    stripToLastSlash : function(URL)
    {
        var slashx = URL.lastIndexOf("/");

        return URL.substring(0, slashx + 1);
    },

    /**
     * Convert a user-provided localized number to an ordinary number (not a string).
     * Removes seperators and leading/trailing whitespace. Disallows the decimal point if isInteger is true.
     * @param number string provided by user
     * @param isInteger if true, disallow decimal point
     */
    formatLocalizedNumber : function(number, isInteger)
    {
        // We convert from localized string to a canonical string,
        // stripping out  group seperators (normally commas). If isInteger is
        // true, we don't allow a decimal point.

        var minus = Tapestry.decimalFormatSymbols.minusSign;
        var grouping = Tapestry.decimalFormatSymbols.groupingSeparator;
        var decimal = Tapestry.decimalFormatSymbols.decimalSeparator;

        var canonical = "";

        number.strip().toArray().each(function(ch)
        {
            if (ch == minus)
            {
                canonical += "-";
                return;
            }

            if (ch == grouping)
            {
                return;
            }

            if (ch == decimal)
            {
                if (isInteger) throw Tapestry.Messages.notAnInteger;

                ch = ".";
            }
            else if (ch < "0" || ch > "9") throw Tapestry.Messages.invalidCharacter;

            canonical += ch;
        });

        return Number(canonical);
    },

    /**
     * Marks a number of script libraries as loaded; this is used with virtual scripts (which combine multiple
     * actual scripts). This is necessary so that subsequent Ajax requests do not load scripts that have
     * already been loaded
     * @param scripts     array of script paths
     */
    markScriptLibrariesLoaded : function(scripts)
    {
        $(scripts).each(function (script)
        {
            var complete = Tapestry.rebuildURL(script);
            Tapestry.ScriptManager.virtualScripts.push(complete);
        });
    }
};


Element.addMethods(
{

    /**
     * Works upward from the element, checking to see if the element is visible. Returns false
     * if it finds an invisible container. Returns true if it makes it as far as a (visible) FORM element.
     *
     * Note that this only applies to the CSS definition of visible; it doesn't check that the element
     * is scolled into view.
     *
     * @param element to search up from
     * @return true if visible (and containers visible), false if it or container are not visible
     */
    isDeepVisible : function(element)
    {
        var current = $(element);

        while (true)
        {
            if (! current.visible()) return false;

            if (current.tagName == "FORM") break;

            current = $(current.parentNode)
        }

        return true;
    }
});

Element.addMethods('FORM',
{
    /**
     * Gets or creates the Tapestry.FormEventManager for the form.
     *
     * @param form form element
     */
    getFormEventManager : function(form)
    {
        form = $(form);
        var t = $T(form);

        var manager = t.formEventManager;

        if (manager == undefined)
        {
            manager = new Tapestry.FormEventManager(form);
            t.formEventManager = manager;
        }

        return manager;
    },

    /**
     * Sends an Ajax request to the Form's action. This encapsulates
     * a few things, such as a default onFailure handler, and working
     * around bugs/features in Prototype concerning how
     * submit buttons are processed.
     *
     * @param form used to define the data to be sent in the request
     * @param options      standard Prototype Ajax Options
     * @return Ajax.Request the Ajax.Request created for the request
     */
    sendAjaxRequest : function (form, url, options)
    {
        form = $(form);

        // Generally, options should not be null or missing,
        // because otherwise there's no way to provide any callbacks!

        options = Object.clone(options || { });

        // Set a default failure handler if none is provided.

        options.onFailure |= Tapestry.ajaxFailureHandler;

        // Find the elements, skipping over any submit buttons.
        // This works around bugs in Prototype 1.6.0.2.

        var elements = form.getElements().reject(function(e)
        {
            return e.tagName == "INPUT" && e.type == "submit";
        });

        var hash = Form.serializeElements(elements, true);

        var lastSubmit = $T(form).lastSubmit;

        // Put the last submit clicked into the hash, emulating
        // what a normal form submit would do.

        if (lastSubmit && lastSubmit.name)
        {
            hash[lastSubmit.name] = $F(lastSubmit);
        }


        // Copy the parameters in, overwriting field values,
        // because Prototype 1.6.0.2 does not.

        Object.extend(hash, options.parameters);

        options.parameters = hash;

        // Ajax.Request will convert the hash into a query string and post it.

        return new Ajax.Request(url, options);
    }
});

Element.addMethods([
    'INPUT',
    'SELECT',
    'TEXTAREA'
],
{
    /**
     * Invoked on a form element (INPUT, SELECT, etc.), gets or creates the
     * Tapestry.FieldEventManager for that field.
     *
     * @param field field element
     */
    getFieldEventManager : function(field)
    {
        field = $(field);
        var t = $T(field);

        var manager = t.fieldEventManager;

        if (manager == undefined)
        {
            manager = new Tapestry.FieldEventManager(field);
            t.fieldEventManager = manager;
        }

        return manager;
    },

    /**
     * Obtains the Tapestry.FieldEventManager and asks it to show
     * the validation message.   Sets the  validationError property of the elements tapestry object to true.
     * @param element
     * @param message to display
     */
    showValidationMessage : function(element, message)
    {
        element = $(element);

        element.getFieldEventManager().showValidationMessage(message);

        return element;
    },

    /**
     * Removes any validation decorations on the field, and
     * hides the error popup (if any) for the field.
     */
    removeDecorations : function(element)
    {
        $(element).getFieldEventManager().removeDecorations();

        return element;
    },


    /**
     * Adds a standard validator for the element, an observer of
     * Tapestry.FIELD_VALIDATE_EVENT. The validator function will be
     * passed the current field value and should throw an error message if
     * the field's value is not valid.
     * @param element field element to validate
     * @param validator function to be passed the field value
     */
    addValidator : function(element, validator)
    {
        element.observe(Tapestry.FIELD_VALIDATE_EVENT, function(event)
        {
            try
            {
                validator.call(this, event.memo.translated);
            }
            catch (message)
            {
                element.showValidationMessage(message);
            }
        });

        return element;
    }
});

/** Container of functions that may be invoked by the Tapestry.init() function. */
Tapestry.Initializer = {

    ajaxFormLoop : function(spec)
    {
        var rowInjector = $(spec.rowInjector);

        $(spec.addRowTriggers).each(function(triggerId)
        {
            $(triggerId).observe("click", function(event)
            {
                $(rowInjector).trigger();

                Event.stop(event);
            })
        });
    },

    formLoopRemoveLink : function(spec)
    {
        var link = $(spec.link);
        var fragmentId = spec.fragment;

        link.observe("click", function(event)
        {
            Event.stop(event);

            var successHandler = function(transport)
            {
                var container = $(fragmentId);
                var fragment = $T(container).formFragment;

                if (fragment != undefined)
                {
                    fragment.hideAndRemove();
                }
                else
                {
                    var effect = Tapestry.ElementEffect.fade(container);

                    effect.options.afterFinish = function()
                    {
                        container.remove();
                    };
                }
            }

            Tapestry.ajaxRequest(spec.url, successHandler);
        });
    },


    /**
     * Convert a form or link into a trigger of an Ajax update that
     * updates the indicated Zone.
     * @param element id or instance of <form> or <a> element
     * @param zoneId id of the element to update when link clicked or form submitted
     * @param url absolute component event request URL
     */
    linkZone : function(element, zoneId, url)
    {
        element = $(element);

        // Update the element with the id of zone div. This may be changed dynamically on the client
        // side.

        $T(element).zoneId = zoneId;

        if (element.tagName == "FORM")
        {
            // Turn normal form submission off.

            element.getFormEventManager().preventSubmission = true;

            // After the form is validated and prepared, this code will
            // process the form submission via an Ajax call.  The original submit event
            // will have been cancelled.

            element.observe(Tapestry.FORM_PROCESS_SUBMIT_EVENT, function()
            {
                var zoneManager = Tapestry.findZoneManager(element);

                if (!zoneManager) return;

                var successHandler = function(transport)
                {
                    zoneManager.processReply(transport.responseJSON);
                };

                element.sendAjaxRequest(url, {
                    onSuccess : successHandler
                });
            });

            return;
        }

        // Otherwise, assume it's just an ordinary link.

        element.observe("click", function(event)
        {
            Event.stop(event);

            var zoneObject = Tapestry.findZoneManager(element);

            if (!zoneObject) return;

            zoneObject.updateFromURL(url);
        });
    },

    validate : function (field, specs)
    {
        field = $(field);

        // Force the creation of the form and field event managers.

        $(field.form).getFormEventManager();
        $(field).getFieldEventManager();

        specs.each(function(spec)
        {
            // spec is a 2 or 3 element array.
            // validator function name, message, optional constraint

            var name = spec[0];
            var message = spec[1];
            var constraint = spec[2];

            var vfunc = Tapestry.Validator[name];

            if (vfunc == undefined)
            {
                Tapestry.error(Tapestry.Messages.missingValidator, {
                    name:name,
                    fieldName:field.id
                });
                return;
            }

            // Pass the extend field, the provided message, and the constraint object
            // to the Tapestry.Validator function, so that it can, typically, invoke
            // field.addValidator().

            vfunc.call(this, field, message, constraint);
        });
    },

    zone : function(spec)
    {
        new Tapestry.ZoneManager(spec);
    },

    formFragment : function(spec)
    {
        new Tapestry.FormFragment(spec)
    },

    formInjector : function(spec)
    {
        new Tapestry.FormInjector(spec);
    },

    // Links a FormFragment to a trigger (a radio or a checkbox), such that changing the trigger will hide
    // or show the FormFragment. Care should be taken to render the page with the
    // checkbox and the FormFragment('s visibility) in agreement.

    linkTriggerToFormFragment : function(trigger, element)
    {
        trigger = $(trigger);

        if (trigger.type == "radio")
        {
            $(trigger.form).observe("click", function()
            {
                $T(element).formFragment.setVisible(trigger.checked);
            });

            return;
        }

        // Otherwise, we assume it is a checkbox.  The difference is
        // that we can observe just the single checkbox element,
        // rather than handling clicks anywhere in the form (as with
        // the radio).

        trigger.observe("click", function()
        {
            $T(element).formFragment.setVisible(trigger.checked);
        });

    }
};

// Collection of field based functions related to validation. Each
// function takes a field, a message and an optional constraint value.
// Some functions are related to Translators and work on the format event,
// other's are from Validators and work on the validate event.

Tapestry.Validator = {

    required : function(field, message)
    {
        $(field).getFieldEventManager().requiredCheck = function(value)
        {
            if (value.strip() == '')
                $(field).showValidationMessage(message);
        };
    },

    /** Supplies a client-side numeric translator for the field. */
    numericformat : function (field, message, isInteger)
    {
        $(field).getFieldEventManager().translator = function(input)
        {
            try
            {
                return Tapestry.formatLocalizedNumber(input, isInteger);
            }
            catch (e)
            {
                $(field).showValidationMessage(message);
            }
        };
    },

    minlength : function(field, message, length)
    {
        field.addValidator(function(value)
        {
            if (value.length < length) throw message;
        });
    },

    maxlength : function(field, message, maxlength)
    {
        field.addValidator(function(value)
        {
            if (value.length > maxlength) throw message;
        });
    },

    min : function(field, message, minValue)
    {
        field.addValidator(function(value)
        {
            if (value < minValue) throw message;
        });
    },

    max : function(field, message, maxValue)
    {
        field.addValidator(function(value)
        {
            if (value > maxValue) throw message;
        });
    },

    regexp : function(field, message, pattern)
    {
        var regexp = new RegExp(pattern);

        field.addValidator(function(value)
        {
            if (! regexp.test(value)) throw message;
        });
    }
};

Tapestry.ErrorPopup = Class.create({

    // If the images associated with the error popup are overridden (by overriding Tapestry's default.css stylesheet),
    // then some of these values may also need to be adjusted.

    BUBBLE_VERT_OFFSET : -34,

    BUBBLE_HORIZONTAL_OFFSET : -20,

    BUBBLE_WIDTH: "auto",

    BUBBLE_HEIGHT: "39px",

    initialize : function(field)
    {
        this.field = $(field);

        this.innerSpan = new Element("span");
        this.outerDiv = $(new Element("div", {
            'id' : this.field.id + ":errorpopup",
            'class' : 't-error-popup'
        })).update(this.innerSpan).hide();

        var body = $(document.body);

        body.insert({
            bottom: this.outerDiv
        });

        this.outerDiv.absolutize();

        this.outerDiv.observe("click", function(event)
        {
            this.ignoreNextFocus = true;

            this.stopAnimation();

            this.outerDiv.hide();

            this.field.activate();

            Event.stop(event);  // Should be domevent.stop(), but that fails under IE
        }.bindAsEventListener(this));

        this.queue = {
            position: 'end',
            scope: this.field.id
        };

        Event.observe(window, "resize", this.repositionBubble.bind(this));

        document.observe(Tapestry.FOCUS_CHANGE_EVENT, function(event)
        {
            if (this.ignoreNextFocus)
            {
                this.ignoreNextFocus = false;
                return;
            }

            if (event.memo == this.field)
            {
                this.fadeIn();
                return;
            }

            // If this field is not the focus field after a focus change, then it's bubble,
            // if visible, should fade out. This covers tabbing from one form to another. 
            this.fadeOut();

        }.bind(this));
    },

    showMessage : function(message)
    {
        this.stopAnimation();

        this.innerSpan.update(message);

        this.hasMessage = true;

        this.fadeIn();
    },

    repositionBubble : function()
    {
        var fieldPos = this.field.cumulativeOffset();

        this.outerDiv.setStyle({
            top: (fieldPos[1] + this.BUBBLE_VERT_OFFSET) + "px",
            left: (fieldPos[0] + this.BUBBLE_HORIZONTAL_OFFSET) + "px",
            width: this.BUBBLE_WIDTH,
            height: this.BUBBLE_HEIGHT
        });
    },

    fadeIn : function()
    {
        if (! this.hasMessage) return;

        this.repositionBubble();

        if (this.animation) return;

        this.animation = new Effect.Appear(this.outerDiv, {
            queue: this.queue,
            afterFinish: function()
            {
                this.animation = null;

                if (this.field != Tapestry.currentFocusField)
                    this.fadeOut();
            }.bind(this)
        });
    },

    stopAnimation : function()
    {
        if (this.animation) this.animation.cancel();

        this.animation = null;
    },

    fadeOut : function ()
    {
        if (this.animation) return;

        this.animation = new Effect.Fade(this.outerDiv, {
            queue : this.queue,
            afterFinish: function()
            {
                this.animation = null;
            }.bind(this)
        });
    },

    hide : function()
    {
        this.hasMessage = false;

        this.stopAnimation();

        this.outerDiv.hide();
    }
});

Tapestry.FormEventManager = Class.create({

    initialize : function(form)
    {
        this.form = $(form);

        this.form.onsubmit = this.handleSubmit.bindAsEventListener(this);
    },

    handleSubmit : function(domevent)
    {
        var t = $T(this.form);

        t.validationError = false;

        var firstErrorField = null;

        // Locate elements that have an event manager (and therefore, validations)
        // and let those validations execute, which may result in calls to recordError().


        this.form.getElements().each(function(element)
        {
            var fem = $T(element).fieldEventManager;

            if (fem != undefined)
            {
                // Ask the FEM to validate input for the field, which fires
                // a number of events.
                var error = fem.validateInput();

                if (error && ! firstErrorField)
                {
                    firstErrorField = element;
                }
            }
        });

        // Allow observers to validate the form as a whole.  The FormEvent will be visible
        // as event.memo.  The Form will not be submitted if event.result is set to false (it defaults
        // to true).  Still trying to figure out what should get focus from this
        // kind of event.

        this.form.fire(Tapestry.FORM_VALIDATE_EVENT, this.form);

        if (t.validationError)
        {
            Event.stop(domevent); // Should be domevent.stop(), but that fails under IE

            if (firstErrorField) firstErrorField.activate();

            // Because the submission failed, the last submit property is cleared,
            // since the form may be submitted for some other reason later.

            t.lastSubmit = null;

            return false;
        }

        this.form.fire(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT, this.form);

        // This flag can be set to prevent the form from submitting normally.
        // This is used for some Ajax cases where the form submission must
        // run via Ajax.Request.

        if (this.preventSubmission)
        {
            // Prevent the normal submission.

            Event.stop(domevent);

            // Instead ...

            this.form.fire(Tapestry.FORM_PROCESS_SUBMIT_EVENT);

            return false;
        }

        // Validation is OK, not doing Ajax, continue as planned.

        return true;
    }
});

Tapestry.FieldEventManager = Class.create({

    initialize : function(field)
    {
        this.field = $(field);

        var id = this.field.id;
        this.label = $(id + '-label');
        this.icon = $(id + '-icon');

        this.translator = Prototype.K;

        document.observe(Tapestry.FOCUS_CHANGE_EVENT, function(event)
        {
            // If changing focus *within the same form* then
            // perform validation.  Note that Tapestry.currentFocusField does not change
            // until after the FOCUS_CHANGE_EVENT notification.

            if (Tapestry.currentFocusField == this.field &&
                this.field.form == event.memo.form)
                this.validateInput();

        }.bindAsEventListener(this));
    },


    /** Removes validation decorations if present. Hides the ErrorPopup,
     *  if it exists.
     */
    removeDecorations : function()
    {
        this.field.removeClassName("t-error");

        if (this.label)
            this.label.removeClassName("t-error");

        if (this.icon)
            this.icon.hide();

        if (this.errorPopup)
            this.errorPopup.hide();
    },


    /**
     * Show a validation error message, which will add decorations to the
     * field and it label, make the icon visible, and raise the
     * field's Tapestry.ErrorPopup to show the message.
     * @param message validation message to display
     */
    showValidationMessage : function(message)
    {
        $T(this.field).validationError = true;
        $T(this.field.form).validationError = true;

        this.field.addClassName("t-error");

        if (this.label)
            this.label.addClassName("t-error");

        if (this.icon)
        {
            if (! this.icon.visible())
                new Effect.Appear(this.icon);
        }

        if (this.errorPopup == undefined)
            this.errorPopup = new Tapestry.ErrorPopup(this.field);

        this.errorPopup.showMessage(message);
    },

    /**
     * Invoked when a form is submitted, or when leaving a field, to perform
     * field validations. Field validations are skipped for disabled fields.
     * If all validations are succesful, any decorations are removed. If any validation
     * fails, an error popup is raised for the field, to display the validation
     * error message.
     *
     * @return true if the field has a validation error
     */
    validateInput : function()
    {
        if (this.field.disabled) return false;

        if (! this.field.isDeepVisible()) return false;

        var t = $T(this.field);

        var value = $F(this.field);

        t.validationError = false;

        if (this.requiredCheck)
            this.requiredCheck.call(this, value);

        // Don't try to validate blank values; if the field is required, that error is already
        // noted and presented to the user.

        if (!t.validationError && ! value.blank())
        {
            var translated = this.translator(value);

            // If Format went ok, perhaps do the other validations.

            if (! t.validationError)
            {
                this.field.fire(Tapestry.FIELD_VALIDATE_EVENT, {
                    value: value,
                    translated: translated
                });
            }
        }

        // Lastly, if no validation errors were found, remove the decorations.

        if (! t.validationError)
            this.field.removeDecorations();

        return t.validationError;
    }
});

// Wrappers around Prototype and Scriptaculous effects.
// All the functions of this object should have all-lowercase names.
// The methods all return the Effect object they create.

Tapestry.ElementEffect = {

    /** Fades in the element. */
    show : function(element)
    {
        return new Effect.Appear(element);
    },

    /** The classic yellow background fade. */
    highlight : function(element)
    {
        return new Effect.Highlight(element);
    },

    /** Scrolls the content down. */
    slidedown : function (element)
    {
        return new Effect.SlideDown(element);
    },

    /** Slids the content back up (opposite of slidedown). */
    slideup : function(element)
    {
        return new Effect.SlideUp(element);
    },

    /** Fades the content out (opposite of show). */
    fade : function(element)
    {
        return new Effect.Fade(element);
    }
};


/**
 * Manages a &lt;div&lt; (or other element) for dynamic updates.
 *
 */
Tapestry.ZoneManager = Class.create({
    // spec are the parameters for the Zone:
    // trigger: required -- name or instance of link.
    // element: required -- name or instance of div element to be shown, hidden and updated
    // show: name of Tapestry.ElementEffect function used to reveal the zone if hidden
    // update: name of Tapestry.ElementEffect function used to highlight the zone after it is updated
    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = {
                element: spec
            }

        this.element = $(spec.element);
        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.show;
        this.updateFunc = Tapestry.ElementEffect[spec.update] || Tapestry.ElementEffect.highlight;

        // Link the div back to this zone.

        $T(this.element).zoneManager = this;

        // Look inside the managed element for another element with the CSS class "t-zone-update".
        // If present, then this is the element whose content will be changed, rather
        // then the entire zone's element.  This allows a Zone element to contain "wrapper" markup
        // (borders and such).  Typically, such a Zone element will initially be invisible.
        // The show and update functions apply to the Zone element, not the update element.

        var updates = this.element.select(".t-zone-update");

        this.updateElement = updates.first() || this.element;
    },

    // Updates the content of the div controlled by this Zone, then
    // invokes the show function (if not visible) or the update function (if visible),

    /**
     * Updates the zone's content, and invokes either the update function (to highlight the change)
     * or the show function (to reveal a hidden element). Lastly, fires the Tapestry.ZONE_UPDATED_EVENT
     * to let listeners know that the zone was updated.
     * @param content
     */
    show: function(content)
    {
        this.updateElement.update(content);

        var func = this.element.visible() ? this.updateFunc : this.showFunc;

        func.call(this, this.element);

        this.element.fire(Tapestry.ZONE_UPDATED_EVENT);
    },

    /**
     * Invoked with a reply (i.e., transport.responseJSON), this updates the managed element
     * and processes any JavaScript in the reply.  The response should have a
     * content key, and may have  script, scripts and stylesheets keys.
     * @param reply response in JSON format appropriate to a Tapestry.Zone
     */
    processReply : function(reply)
    {
        Tapestry.loadScriptsInReply(reply, function()
        {
            // In a multi-zone update, the reply.content may be blank or missing.

            reply.content && this.show(reply.content);

            // zones is an object of zone ids and zone content that will be present
            // in a multi-zone update response.

            Object.keys(reply.zones).each(function (zoneId)
            {
                var manager = Tapestry.findZoneManagerForZone(zoneId);

                if (manager)
                {
                    var zoneContent = reply.zones[zoneId];
                    manager.show(zoneContent);
                }
            });
        }.bind(this));
    },

    /** Initiates an Ajax request to update this zone by sending a request
     *  to the URL. Expects the correct JSON reply (wth keys content, etc.).
     * @param URL component event request URL
     */
    updateFromURL : function (URL)
    {
        var successHandler = function(transport)
        {
            this.processReply(transport.responseJSON);
        }.bind(this);

        Tapestry.ajaxRequest(URL, successHandler);
    }
});

// A class that managed an element (usually a <div>) that is conditionally visible and
// part of the form when visible.

Tapestry.FormFragment = Class.create({

    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = {
                element: spec
            };

        this.element = $(spec.element);

        $T(this.element).formFragment = this;

        this.hidden = $(spec.element + ":hidden");

        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.slidedown;
        this.hideFunc = Tapestry.ElementEffect[spec.hide] || Tapestry.ElementEffect.slideup;

        var form = $(this.hidden.form);

        // TAP5-283: Force creation of the FormEventManager if it does not already exist.

        form.getFormEventManager();

        $(form).observe(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT, function()
        {
            // On a submission, if the fragment is not visible, then wipe out its
            // form submission data, so that no processing or validation occurs on the server.

            if (! this.element.isDeepVisible())
                this.hidden.value = "";
        }.bind(this));
    },

    hide : function()
    {
        if (this.element.visible())
            this.hideFunc(this.element);
    },

    hideAndRemove : function()
    {
        var effect = this.hideFunc(this.element);

        effect.options.afterFinish = function()
        {
            this.element.remove();
        }.bind(this);
    },

    show : function()
    {
        if (! this.element.visible())
            this.showFunc(this.element);
    },

    toggle : function()
    {
        this.setVisible(! this.element.visible());
    },

    setVisible : function(visible)
    {
        if (visible)
        {
            this.show();
            return;
        }

        this.hide();
    }
});

Tapestry.FormInjector = Class.create({

    initialize: function(spec)
    {
        this.element = $(spec.element);
        this.url = spec.url;
        this.below = spec.below;

        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.highlight;

        this.element.trigger = function()
        {
            var successHandler = function(transport)
            {
                var reply = transport.responseJSON;

                // Clone the FormInjector element (usually a div)
                // to create the new element, that gets inserted
                // before or after the FormInjector's element.

                var newElement = new Element(this.element.tagName, {
                    'class' : this.element.className
                });

                // Insert the new element before or after the existing element.

                var param = { };
                param[this.below ? "after" : "before"] = newElement;

                Tapestry.loadScriptsInReply(reply, function()
                {
                    // Add the new element with the downloaded content.

                    this.element.insert(param);

                    // Update the empty element with the content from the server

                    newElement.update(reply.content);

                    newElement.id = reply.elementId;

                    // Add some animation to reveal it all.

                    this.showFunc(newElement);
                }.bind(this));
            }.bind(this);

            Tapestry.ajaxRequest(this.url, successHandler);

            return false;
        }.bind(this);
    }
});

/**
 * Wait for a set of JavaScript libraries to load (in terms of DOM script elements), then invokes a callback function.
 */
Tapestry.ScriptLoadMonitor = Class.create({

    initialize : function(scriptElements, callback)
    {
        this.callback = callback;
        this.loaded = 0;
        this.toload = scriptElements.length;

        var executor = this;

        scriptElements.each(function (scriptElement)
        {
            if (Prototype.Browser.IE)
            {
                var loaded = false;

                scriptElement.onreadystatechange = function ()
                {
                    // IE may fire either loaded or complete, or perhaps even both.
                    if (! loaded && (this.readyState == 'loaded' || this.readyState == 'complete'))
                    {
                        loaded = true;
                        executor.loadComplete(scriptElement);
                    }
                };
            }
            else
            {
                // Much simpler in FF, Safari, etc.
                scriptElement.onload = executor.loadComplete.bindAsEventListener(executor, scriptElement);
            }
        });

        // If no scripts to actually load, call the callback immediately.

        if (this.toload == 0) this.callback.call(this);
    },

    loadComplete : function()
    {
        this.loaded++;

        // Evaluated the dependent script only once all the elements have loaded.

        if (this.loaded == this.toload)
            this.callback.call(this);
    }
});

Tapestry.ScriptManager = {

    /** Complete URLs of virtually loaded scripts (combined scripts loaded as a single virtual asset). */
    virtualScripts : $A([]),

    initialize : function()
    {

        // Check to see if document.scripts is supported; if not (for example, FireFox),
        // we can fake it.

        this.emulated = false;

        if (! document.scripts)
        {
            this.emulated = true;

            document.scripts = new Array();

            $$('script').each(function (s)
            {
                document.scripts.push(s);
            });
        }
    },

    /**
     * Checks to see if the given collection (of <script> or <style> elements) contains the given asset URL.
     * @param collection
     * @param prop      property to check ('src' for script, 'href' to style).
     * @param assetURL        complete URL (i.e., with protocol, host and port) to the asset
     */
    contains : function (collection, prop, assetURL)
    {
        return $A(collection).any(function (element)
        {
            var existing = element[prop];

            if (! existing || existing.blank()) return false;

            var complete =
                    Prototype.Browser.IE ? Tapestry.rebuildURL(existing) : existing;

            return complete == assetURL;
        });

        return false;
    },

    /**
     * Add scripts, as needed, to the document, then waits for them all to load, and finally, calls
     * the callback function.
     * @param scripts        Array of scripts to load
     * @param callback invoked after scripts are loaded
     */
    addScripts: function(scripts, callback)
    {
        var added = new Array();

        if (scripts)
        {
            var emulated = this.emulated;
            // Looks like IE really needs the new <script> tag to be
            // in the <head>. FF doesn't seem to care.
            // See http://unixpapa.com/js/dyna.html
            var head = $$("head").first();

            scripts.each(function(s)
            {
                var assetURL = Tapestry.rebuildURL(s);

                // Check to see if the script is already loaded, either as a virtual script, or as
                // an individual <script src=""> element.

                if (Tapestry.ScriptManager.virtualScripts.member(assetURL)) return;
                if (Tapestry.ScriptManager.contains(document.scripts, "src", assetURL)) return;

                // IE needs the type="text/javascript" as well.

                var element = new Element('script', {
                    src: assetURL,
                    type: 'text/javascript'
                });

                head.insert({
                    bottom:element
                });

                added.push(element);

                if (emulated) document.scripts.push(element);
            });

        }

        new Tapestry.ScriptLoadMonitor(added, callback);
    },

    addStylesheets : function(stylesheets)
    {
        if (!stylesheets) return;

        var head = $$('head').first();

        $(stylesheets).each(function(s)
        {
            var assetURL = Tapestry.rebuildURL(s.href);

            if (Tapestry.ScriptManager.contains(document.styleSheets, 'href', assetURL)) return; // continue

            var element = new Element('link', {
                type: 'text/css',
                rel: 'stylesheet',
                href: assetURL
            });

            // Careful about media types, some browser will break if it ends up as 'null'.

            if (s.media != undefined)
                element.writeAttribute('media', s.media);

            head.insert({
                bottom: element
            });

        });
    }
};

/**
 * In the spirit of $(), $T() exists to access the <em>Tapestry object</em> for the element. The Tapestry object
 * is used to store additional values related to the element; it is simply an annoymous object stored as property
 * <code>_tapestry</code> of the element, created the first time it is accessed.
 * <p>This mechanism acts as a namespace, and so helps prevent name
 * conflicts that would occur if properties were stored directly on DOM elements, and makes debugging a bit easier
 * (the Tapestry-specific properties are all in one place!).
 * For the moment, added methods are stored directly on the object, and are not prefixed in any way, valueing
 * readability over preventing naming conflicts.
 *
 * @param element an element instance or element id
 * @return object Tapestry object for the element
 */
function $T(element)
{
    var e = $(element);
    var t = e._tapestry;

    if (!t)
    {
        t = { };
        e._tapestry = t;
    }

    return t;
}

Tapestry.onDOMLoaded(Tapestry.onDomLoadedCallback);

// Ajax code needs to know to do nothing after the window is unloaded.
Event.observe(window, "beforeunload", function()
{
    Tapestry.windowUnloaded = true;
});

