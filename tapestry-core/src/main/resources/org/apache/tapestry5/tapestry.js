// Copyright 2007, 2008 The Apache Software Foundation
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

    /** Event, triggered on a Form element, to spur fields within the form to validate thier input. */
    FORM_VALIDATE_EVENT : "tapestry:formvalidate",

    /** Event, triggered on a Form element, to allow callbacks to perpare for a form submission (this
     * occurs after validation).
     */
    FORM_PREPARE_FOR_SUBMIT_EVENT : "tapestry:formprepareforsubmit",

    /**
     * Form event fired after prepare.
     */
    FORM_PROCESS_SUBMIT_EVENT : "tapestry:formprocesssubmit",

    /** Event, triggered on the document object, which identifies the current focus element. */
    FOCUS_CHANGE_EVENT : "tapestry:focuschange",

    /** When false, the default, the Tapestry.debug() function will be a no-op. */
    DEBUG_ENABLED : false,

    /** Time, in seconds, that console messages are visible. */
    CONSOLE_DURATION : 60,

    FormEvent : Class.create(),

    FormEventManager : Class.create(),

    FieldEventManager : Class.create(),

    Zone : Class.create(),

    FormFragment : Class.create(),

    FormInjector : Class.create(),

    ErrorPopup : Class.create(),

    DependentExecutor : Class.create(),

    // Adds a callback function that will be invoked when the DOM is loaded (which
    // occurs *before* window.onload, which has to wait for images and such to load
    // first.  This simply observes the dom:loaded event on the document object (support for
    // which is provided by Prototype).
    onDOMLoaded : function(callback)
    {
        document.observe("dom:loaded", callback);
    },

    /** Find all elements marked with the "t-invisible" CSS class and hide()s them, so that
     * Prototype's visible() method operates correctly. This is invoked when the
     * DOM is first loaded, and AGAIN whenever dynamic content is loaded via the Zone
     * mechanism.     In addition, adds a focus listener for each form element.
     */
    onDomLoadedCallback : function()
    {
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

            if (! element.isObservingFocusChange)
            {
                element.observe("focus", function()
                {
                    document.fire(Tapestry.FOCUS_CHANGE_EVENT, element);
                });

                element.isObservingFocusChange = true;
            }
        });
    },

    // Generalized initialize function for Tapestry, used to help minimize the amount of JavaScript
    // for the page by removing redundancies such as repeated Object and method names. The spec
    // is a hash whose keys are the names of methods of the Tapestry.Initializer object.
    // The value is an array of arrays.  The outer arrays represent invocations
    // of the method.  The inner array are the parameters for each invocation.
    // As an optimization, the inner value may not be an array but instead
    // a single value.

    init : function(spec)
    {
        $H(spec).each(function(pair)
        {
            var functionName = pair.key;

            // Tapestry.logWarning("Initialize: #{name} ...", { name:functionName });

            var initf = Tapestry.Initializer[functionName];

            if (initf == undefined)
            {
                Tapestry.error("Function Tapestry.Initializer.#{name}() does not exist.", { name:functionName });
                return;
            }

            pair.value.each(function(parameterList)
            {
                if (! Object.isArray(parameterList))
                {
                    parameterList = [parameterList];
                }

                initf.apply(this, parameterList);
            });
        });
    },

    error : function (message, substitutions)
    {
        Tapestry.updateConsole("t-err", message, substitutions);
    },

    warn : function (message, substitutions)
    {
        Tapestry.updateConsole("t-warn", message, substitutions);
    },

    debug : function (message, substitutions)
    {
        if (Tapestry.DEBUG_ENABLED)
            Tapestry.updateConsole("t-debug", message, substitutions);
    },

    updateConsole : function (className, message, substitutions)
    {
        if (substitutions != undefined)
            message = message.interpolate(substitutions);

        if (Tapestry.console == undefined)
        {
            var body = $$("BODY").first();

            Tapestry.console = new Element("div", { 'class': "t-console" });

            body.insert({ bottom: Tapestry.console });
        }

        var div = new Element("div", { 'class': className }).update(message);

        Tapestry.console.insert({ bottom: div });

        var effect = new Effect.BlindUp(div, { delay: Tapestry.CONSOLE_DURATION,
            afterFinish: function()
            {
                div.remove();
            }});

        div.observe("click", function()
        {
            effect.cancel();
            div.remove();
        });

    },

    getFormEventManager : function(form)
    {
        form = $(form);

        var manager = form.eventManager;

        if (manager == undefined)
            manager = new Tapestry.FormEventManager(form);

        return manager;
    },

    /**
     * Passed the JSON content of a Tapestry partial markup response, extracts
     * the script and stylesheet information.  JavaScript libraries and stylesheets are loaded,
     * then any script code is evaluated.  All three keys are optional:
     * <dl>
     * <dt>scripts</dt><dd>Array of strings (URIs of scripts)</dd>
     * <dt>stylesheets</dt><dd>Array of hashes, each hash has key href and optional key media</dd>
     * <dt>script</dt> <dd>JavaScript to be executed once all scripts are loaded</dd></dl>
     */
    processScriptInReply : function(reply)
    {
        Tapestry.ScriptManager.addScripts(reply.scripts, reply.script);

        Tapestry.ScriptManager.addStylesheets(reply.stylesheets);

        Tapestry.onDomLoadedCallback();
    },

    // Adds a validator for a field.  A FieldEventManager is added, if necessary.
    // The validator will be called only for non-blank values, unless acceptBlank is
    // true (in most cases, acceptBlank is flase). The validator is a function
    // that accepts the current field value as its first parameter, and a
    // Tapestry.FormEvent as its second.  It can invoke recordError() on the event
    // if the input is not valid.

    addValidator : function(field, acceptBlank, validator)
    {
        this.getFieldEventManager(field).addValidator(acceptBlank, validator);
    },

    getFieldEventManager : function(field)
    {
        field = $(field);

        var manager = field.fieldEventManager;

        if (manager == undefined) manager = new Tapestry.FieldEventManager(field);

        return manager;
    },

    /**
     * Used with validation to see if an element is visible: i.e., it and all of its containers, up to the
     * containing form, are all visible. Only deeply visible elements are subject to validation.
     */
    isDeepVisible : function(element)
    {
        // This started as a recursively defined method attach to Element, but was converted
        // to a stand-alone as part of TAPESTRY-2424.

        var current = $(element);

        while (true)
        {
            if (! current.visible()) return false;

            if (current.tagName == "FORM") break;

            current = $(current.parentNode)
        }

        return true;
    },

    /** Focuses on a field, selecting its text. */
    focus : function (field)
    {
        field = $(field);

        if (field.focus) field.focus();

        if (field.select) field.select();
    },

    /**
     * Default function for handling Ajax-related failures.
     */
    ajaxFailureHandler : function()
    {
        Tapestry.error("Communication with the server failed.");
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
        return new Ajax.Request(url, { onSuccess: successHandler, onFailure: Tapestry.ajaxFailureHandler })
    },


    /**
     * Used to reconstruct a complete URL from a path that is (or may be) relative to window.location.
     * This is used when determining if a JavaScript library or CSS stylesheet has already been loaded.
     * Recognizes complete URLs (which are returned unchanged) and absolute paths (which are prefixed
     * with the window.location protocol and host).  Otherwise the correct path is built.  The
     * path may be prefixed with "./" and "../", which will be resolved correctly.
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

        if (path.startsWith("/"))
        {
            var l = window.location;
            return l.protocol + "//" + l.host + path;
        }

        var rootPath = this.stripToLastSlash(window.location.href);

        while (true)
        {
            if (path.startsWith("../"))
            {
                rootPath = this.stripToLastSlash(rootPath.substr(0, rootPath.length - 1));
                path = path.substring(3);
                continue;
            }

            if (path.startsWith("./"))
            {
                path = path.substr(2);
                continue;
            }

            return rootPath + path;
        }
    },

    stripToLastSlash : function(URL)
    {
        var slashx = URL.lastIndexOf("/");

        return URL.substring(0, slashx + 1);
    }

};

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

        link.observe("click", function(event)
        {
            Event.stop(event);

            var successHandler = function(transport)
            {
                var container = $(spec.fragment);

                if (container.formFragment != undefined)
                {
                    container.formFragment.hideAndRemove();
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
     */
    linkZone : function(element, zoneDiv)
    {
        element = $(element);

        // Update the element with the id of zone div. This may be changed dynamically on the client
        // side.

        element.zone = zoneDiv;

        var successHandler = function(transport)
        {
            var reply = transport.responseJSON;

            // Find the zone id for the element, and from there, the Tapestry.Zone object
            // responsible for the zone.

            $(element.zone).zone.show(reply.content);

            Tapestry.processScriptInReply(reply);
        };

        if (element.tagName == "FORM")
        {
            // Turn normal form submission off.

            Tapestry.getFormEventManager(element).preventSubmission = true;

            // After the form is validated and prepared, this code will
            // process the form submission via an Ajax call.  The original submit event
            // will have been cancelled.

            element.observe(Tapestry.FORM_PROCESS_SUBMIT_EVENT, function()
            {
                element.request({ onSuccess : successHandler, onFailure: Tapestry.ajaxFailureHandler });
            });

            return;
        }

        // Otherwise, assume it's just an ordinary link.

        var handler = function(event)
        {
            Tapestry.ajaxRequest(element.href, successHandler);

            Event.stop(event);
        };

        element.observe("click", handler);
    },

    validate : function (field, specs)
    {
        field = $(field);

        Tapestry.getFormEventManager(field.form);

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
                Tapestry.error("Function Tapestry.Validator.#{name}() does not exist for field '#{fieldName}'.", {name:name, fieldName:pair.key});
                return;
            }

            vfunc.call(this, field, message, constraint);
        });
    },

    zone : function(spec)
    {
        new Tapestry.Zone(spec);
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
                $(element).formFragment.setVisible(trigger.checked);
            });

            return;
        }

        // Otherwise, we assume it is a checkbox.  The difference is
        // that we can observe just the single checkbox element,
        // rather than handling clicks anywhere in the form (as with
        // the radio).

        trigger.observe("click", function()
        {
            $(element).formFragment.setVisible(trigger.checked);
        });

    }
};

// New methods added to Element.

Tapestry.ElementAdditions = {
    // This is added to all Elements, but really only applys to form control elements. This method is invoked
    // when a validation error is associated with a field. This gives the field a chance to decorate itself, its label
    // and its icon.
    decorateForValidationError : function(element, message)
    {
        Tapestry.getFieldEventManager(element).addDecorations(message);
    },

    removeDecorations : function(element)
    {
        Tapestry.getFieldEventManager(element).removeDecorations();
    }
};

Element.addMethods(Tapestry.ElementAdditions);

if (window.console)
{
    var createlog = function (log)
    {
        return function(message, substitutions)
        {
            if (substitutions != undefined)
                message = message.interpolate(substitutions);

            log.call(this, message);
        };
    };

    Tapestry.error = createlog(window.console.error);
    Tapestry.warn = createlog(window.console.warn);
    Tapestry.debug = createlog(window.console.debug);
}

// Collection of field based functions related to validation. Each
// function takes a field, a message and an optional constraint value.

Tapestry.Validator = {
    required : function(field, message)
    {
        Tapestry.addValidator(field, true, function(value, event)
        {
            if (value.strip() == '')
                event.recordError(message);
        });
    },

    minlength : function(field, message, length)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value.length < length)
                event.recordError(message);
        });
    },

    maxlength : function(field, message, maxlength)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value.length > maxlength)
                event.recordError(message);
        });
    },

    min : function(field, message, minValue)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value < minValue)
                event.recordError(message);
        });
    },

    max : function(field, message, maxValue)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value > maxValue)
                event.recordError(message);
        });
    },

    regexp : function(field, message, pattern)
    {
        var regexp = new RegExp(pattern);

        Tapestry.addValidator(field, false, function(value, event)
        {
            if (! regexp.test(value))
                event.recordError(message);
        });
    }
};


// A Tapestry.FormEvent is used when the form sends presubmit and submit events to
// a FieldEventManager. It allows the associated handlers to indirectly invoke
// the Form's invalidField() method, and it tracks a result flag (true for success ==
// no field errors, false if any field errors).

Tapestry.FormEvent.prototype = {

    initialize : function(form)
    {
        this.form = $(form);
        this.result = true;
    },

    // Invoked by a validator function (which is passed the event) to record an error
    // for the associated field. The event knows the field and form and invoke's
    // the (added) form method invalidField().  Sets the event's result field to false
    // (i.e., don't allow the form to submit), and sets the event's error field to
    // true.

    recordError : function(message)
    {
        if (this.focusField == undefined)
            this.focusField = this.field;

        this.field.decorateForValidationError(message);

        this.result = false;
        this.error = true;
    }
};

Tapestry.ErrorPopup.prototype = {

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
        this.outerDiv = $(new Element("div", { 'class' : 't-error-popup' })).update(this.innerSpan).hide();

        var body = $$('BODY').first();

        body.insert({ bottom: this.outerDiv });

        this.outerDiv.absolutize();

        this.outerDiv.observe("click", function(event)
        {
            this.ignoreNextFocus = true;

            this.stopAnimation();

            this.outerDiv.hide();

            Tapestry.focus(this.field);

            Event.stop(event);  // Should be domevent.stop(), but that fails under IE
        }.bindAsEventListener(this));

        this.queue = { position: 'end', scope: this.field.id };

        Event.observe(window, "resize", this.repositionBubble.bind(this));

        document.observe(Tapestry.FOCUS_CHANGE_EVENT, function(event)
        {
            if (this.ignoreNextFocus)
            {
                this.ignoreNextFocus = false;
                return;
            }

            // Tapestry.debug("Focus change: #{memo} for #{field}", { memo: event.memo.id, field: this.field.id });

            var focused = event.memo;

            if (focused == this.field)
            {
                this.fadeIn();
            }
            else
            {
                this.fadeOut();
            }
        }.bind(this));
    },

    showMessage : function(message)
    {
        // Tapestry.debug("Show message: #{message} for #{field}", { message: message, field: this.field.id });

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
            height: this.BUBBLE_HEIGHT });
    },

    fadeIn : function()
    {
        // Tapestry.debug("fadeIn: " + this.field.id);

        if (! this.hasMessage) return;

        this.repositionBubble();

        if (this.status == "fadeIn") return;

        if (this.outerDiv.visible()) return;

        this.animation = new Effect.Appear(this.outerDiv, { queue: this.queue });

        this.status = "fadeIn";
    },

    stopAnimation : function()
    {
        if (this.animation) this.animation.cancel();

        this.animation = null;
        this.status = null;
    },

    fadeOut : function ()
    {
        // Tapestry.debug("fadeOut: " + this.field.id);

        if (this.status == "fadeOut") return;

        this.animation = new Effect.Fade(this.outerDiv, { queue : this.queue });

        this.status = "fadeOut";
    },

    hide : function()
    {
        this.hasMessage = false;

        this.stopAnimation();

        this.outerDiv.hide();
    }
};

Tapestry.FormEventManager.prototype = {

    initialize : function(form)
    {
        this.form = $(form);
        this.form.eventManager = this;

        this.form.onsubmit = this.handleSubmit.bindAsEventListener(this);
    },

    handleSubmit : function(domevent)
    {
        // Locate elements that have an event manager (and therefore, validations)
        // and let those validations execute, which may result in calls to recordError().

        var event = new Tapestry.FormEvent(this.form);

        this.form.getElements().each(function(element)
        {
            if (element.fieldEventManager != undefined)
            {
                event.field = element;
                element.fieldEventManager.validateInput(event);

                if (event.abort) throw $break;
            }
        });

        // Allow observers to validate the form as a whole.  The FormEvent will be visible
        // as event.memo.  The Form will not be submitted if event.result is set to false (it defaults
        // to true).

        this.form.fire(Tapestry.FORM_VALIDATE_EVENT, event);

        if (! event.result)
        {
            // Calling focus() does not trigger this event, so we do it manually.
            // Defer it long enough for the animations to start.

            event.focusField.activate();

            Event.stop(domevent); // Should be domevent.stop(), but that fails under IE

            return;
        }

        this.form.fire(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT);

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
    }
};

Tapestry.FieldEventManager.prototype = {

    initialize : function(field)
    {
        this.field = $(field);

        field.fieldEventManager = this;

        this.validators = [ ];

        var id = field.id;
        this.label = $(id + ':label');
        this.icon = $(id + ':icon');

        this.field.observe("blur", function()
        {
            var event = new Tapestry.FormEvent(this.field.form);

            // This prevents the field from taking focus if there is an error.
            event.focusField = this.field;

            event.field = this.field;

            this.validateInput(event);
        }.bindAsEventListener(this));
    },

    // Adds a validator.  acceptBlank is true if the validator should be invoked regardless of
    // the value.  Usually acceptBlank is false, meaning that the validator will be skipped if
    // the field's value is blank. The validator itself is a function that is passed the
    // field's value and the Tapestry.FormEvent object.  When a validator invokes event.recordError(),
    // any subsequent validators for that field are skipped.

    addValidator : function(acceptBlank, validator)
    {
        this.validators.push([ acceptBlank, validator]);
    },

    // Removes decorations on the field and label (the "t-error" CSS class) and makes the icon
    // invisible.  A field that has special decoration needs will override this method.

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

    // Adds decorations to the field (including label and icon if present).
    // event - the validation event
    // message - error message

    addDecorations : function(message)
    {
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


    // Invoked from the Form's onsubmit event handler. Gets the fields value and invokes
    // each validator (unless the value is blank) until a validator returns false. Validators
    // should not modify the field's value.

    validateInput : function(event)
    {
        if (this.field.disabled) return;

        if (! Tapestry.isDeepVisible(this.field)) return;

        var value = $F(event.field);
        var isBlank = (value == '');

        event.error = false;

        this.validators.each(function(tuple)
        {
            var acceptBlank = tuple[0];
            var validator = tuple[1];

            if (acceptBlank || !isBlank)
            {

                validator(value, event);

                // event.error is set by Tapestry.FormEvent.recordError().

                if (event.error) throw $break;
            }
        });

        if (! event.error)
            this.removeDecorations();
    }
};

// Wrappers around Prototype and Scriptaculous effects.
// All the functions of this object should have all-lowercase names.
// The methods all return the Effect object they create.

Tapestry.ElementEffect = {

    show : function(element)
    {
        return new Effect.Appear(element);
    },

    highlight : function(element)
    {
        return new Effect.Highlight(element);
    },

    slidedown : function (element)
    {
        return new Effect.SlideDown(element);
    },

    slideup : function(element)
    {
        return new Effect.SlideUp(element);
    },

    fade : function(element)
    {
        return new Effect.Fade(element);
    }
};


Tapestry.Zone.prototype = {
    // spec are the parameters for the Zone:
    // trigger: required -- name or instance of link.
    // element: required -- name or instance of div element to be shown, hidden and updated
    // show: name of Tapestry.ElementEffect function used to reveal the zone if hidden
    // update: name of Tapestry.ElementEffect function used to highlight the zone after it is updated
    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = { element: spec }

        this.element = $(spec.element);
        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.show;
        this.updateFunc = Tapestry.ElementEffect[spec.update] || Tapestry.ElementEffect.highlight;

        // Link the div back to this zone.

        this.element.zone = this;

        // Look inside the Zone element for an element with the CSS class "t-zone-update".
        // If present, then this is the elements whose content will be changed, rather
        // then the entire Zone div.  This allows a Zone div to contain "wrapper" markup
        // (borders and such).  Typically, such a Zone element will initially be invisible.
        // The show and update functions apply to the Zone element, not the update element.

        var updates = this.element.select(".t-zone-update");

        this.updateElement = updates.length == 0 ? this.element : updates[0];
    },

    // Updates the content of the div controlled by this Zone, then
    // invokes the show function (if not visible) or the update function (if visible).

    show: function(content)
    {
        this.updateElement.update(content);

        var func = this.element.visible() ? this.updateFunc : this.showFunc;

        func.call(this, this.element);
    }
};

// A class that managed an element (usually a <div>) that is conditionally visible and
// part of the form when visible.

Tapestry.FormFragment.prototype = {

    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = { element: spec };

        this.element = $(spec.element);

        this.element.formFragment = this;

        this.hidden = $(spec.element + ":hidden");

        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.slidedown;
        this.hideFunc = Tapestry.ElementEffect[spec.hide] || Tapestry.ElementEffect.slideup;

        $(this.hidden.form).observe(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT, function()
        {
            // On a submission, if the fragment is not visible, then wipe out its
            // form submission data, so that no processing or validation occurs on the server.

            if (! Tapestry.isDeepVisible(this.element))
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
};


Tapestry.FormInjector.prototype = {

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

                var newElement = new Element(this.element.tagName, { 'class' : this.element.className });

                // Insert the new element before or after the existing element.

                var param = { };
                var key = this.below ? "after" : "before";
                param[key] = newElement;

                // Add the new element with the downloaded content.

                this.element.insert(param);

                // Update the empty element with the content from the server

                newElement.update(reply.content);

                newElement.id = reply.elementId;

                // Handle any scripting issues.

                Tapestry.processScriptInReply(reply);

                // Add some animation to reveal it all.

                this.showFunc(newElement);

            }.bind(this);

            Tapestry.ajaxRequest(this.url, successHandler);

            return false;
        }.bind(this);
    }
};

/**
 * Coordinates the execution of JavaScript code blocks (via eval) with the loading
 * of an array of <script> elements.
 */
Tapestry.DependentExecutor.prototype = {

    initialize : function(prereqs, dependent)
    {
        this.dependent = dependent;
        this.loaded = 0;
        this.toload = prereqs.length;

        var executor = this;

        prereqs.each(function (scriptElement)
        {
            if (Prototype.Browser.IE)
            {
                var loaded = false;

                scriptElement.onreadystatechange = function ()
                {
                    Tapestry.debug("State #{state} for #{script} (loaded is #{loaded})", { state:this.readyState, script:scriptElement.src, loaded:loaded });

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
    },

    loadComplete : function(element)
    {
        this.loaded++;

        Tapestry.debug("Script #{loaded} of #{toload} loaded", this);

        // Evaluated the dependent script only once all the elements have loaded.

        if (this.loaded == this.toload)
            eval(this.dependent);
    }
};

Tapestry.ScriptManager = {

    initialize : function()
    {

        // Check to see if document.script is supported; if not (for example, FireFox),
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
        Tapestry.debug("Checking previously loaded for: " + assetURL);

        return $A(collection).any(function (element)
        {
            var existing = element[prop];

            if (existing.blank()) return false;

            var complete =
                    Prototype.Browser.IE ? Tapestry.rebuildURL(existing) : existing;

            Tapestry.debug("Previously loaded: " + complete);

            return complete == assetURL;
        });

        return false;
    },

    addScripts: function(scripts, dependent)
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

                if (Tapestry.ScriptManager.contains(document.scripts, "src", assetURL)) return; // continue to next script

                Tapestry.debug("Loading script: " + assetURL);

                var element = new Element('script', { src: assetURL, type: 'text/javascript' });

                head.insert({bottom:element});

                added.push(element);

                if (emulated) document.scripts.push(element);
            });

        }

        if (!dependent) return;

        if (added.length)
        {
            new Tapestry.DependentExecutor(added, dependent);
            return;
        }

        eval(dependent);
    },

    addStylesheets : function(stylesheets)
    {
        if (!stylesheets) return;

        var head = $$('head').first();

        $(stylesheets).each(function(s)
        {
            var assetURL = Tapestry.rebuildURL(s.href);

            if (Tapestry.ScriptManager.contains(document.styleSheets, 'href', assetURL)) return; // continue

            var element = new Element('link', { type: 'text/css', rel: 'stylesheet', href: assetURL });

            // Careful about media types, some browser will break if it ends up as 'null'.

            if (s.media != undefined)
                element.writeAttribute('media', s.media);

            head.insert({bottom: element});

        });
    }
};

Tapestry.onDOMLoaded(Tapestry.onDomLoadedCallback);
