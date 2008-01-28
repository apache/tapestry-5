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

    FormEvent : Class.create(),

    FieldEventManager : Class.create(),

    Zone : Class.create(),

    ErrorPopup : Class.create(),

    // An array of ErrorPopup that have been created for fields within the page

    errorPopups : [],

    // Adds a callback function that will be invoked when the DOM is loaded (which
    // occurs *before* window.onload, which has to wait for images and such to load
    // first.  This simply observes the dom:loaded event on the document object (support for
    // which is provided by Prototype).
    onDOMLoaded : function(callback)
    {
        document.observe("dom:loaded", callback);
    },

    registerForm : function(form, clientValidations)
    {
        form = $(form);
    
    // This can probably be cleaned up with bind() ...

        form.onsubmit = function()
        {
            var event = new Tapestry.FormEvent(form);

            form.firstError = true;

	  // Locate elements that have an event manager (and therefore, validations)
            // and let those validations execute, which may result in calls to recordError().

            form.getElements().each(function(element)
            {
                if (element.fieldEventManager != undefined)
                {
                    event.field = element;
                    element.fieldEventManager.validateInput(event);

                    if (event.abort) throw $break;
                }
            });

            return event.result;
        };

        form.recordError = function(field, event, message)
        {

            if (form.firstError)
            {
                field = $(field);
                if (field.focus) field.focus();
                if (field.select) field.select();

                form.firstError = false;
            }

            field.decorateForValidationError(event, message);

        };

        // And handle the validations

        this.registerValidations(form, clientValidations);
    },

    registerValidations : function(form, clientValidations)
    {
        $H(clientValidations).each(function(pair)
        {
            var field = $(pair.key);
            var specs = pair.value;

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
                    var errorMessage = "Function Tapestry.Validator.#{name}() does not exist for field '#{fieldName}'.".interpolate({name:name, fieldName:pair.key});

                    window.alert(errorMessage);
                }

                vfunc.call(this, field, message, constraint);
            });
        });
    },

    // Convert a link into a trigger of an Ajax update that
    // updates the indicated Zone.

    linkZone : function(link, zoneDiv)
    {
        link = $(link);
        var zone = $(zoneDiv).zone;

        var clickHandler = function(event)
        {
            var successHandler = function(transport)
            {
                var response = transport.responseText;
                var reply = eval("(" + response + ")");

                zone.show(reply.content);
            };

            var request = new Ajax.Request(link.href, { onSuccess : successHandler });

            return false;
        };

        link.onclick = clickHandler;
    },

    initializeZones : function (zoneSpecs, linkSpecs)
    {
        // Each spec is a hash ready to pass to Tapestry.Zone

        $A(zoneSpecs).each(function (spec)
        {
            new Tapestry.Zone(spec);
        });

        // Each spec is a pair of argument values suitable for the linkZone method

        $A(linkSpecs).each(function (spec)
        {
            Tapestry.linkZone.apply(null, spec);
        });
    },


    // Adds a validator for a field.  A FieldEventManager is added, if necessary.
    // The validator will be called only for non-blank values, unless acceptBlank is
    // true (in most cases, acceptBlank is flase). The validator is a function
    // that accepts the current field value as its first parameter, and a
    // Tapestry.FormEvent as its second.  It can invoke recordError() on the event
    // if the input is not valid.

    addValidator : function(field, acceptBlank, validator)
    {
        field = $(field);

        if (field.fieldEventManager == undefined) new Tapestry.FieldEventManager(field);

        field.fieldEventManager.addValidator(acceptBlank, validator);
    }

};

// New methods added to Element.

Tapestry.ElementAdditions = {
    // This is added to all Elements, but really only applys to form control elements. This method is invoked
    // when a validation error is associated with a field. This gives the field a chance to decorate itself, its label
    // and its icon.
    decorateForValidationError : function (element, event, message)
    {
        $(element).fieldEventManager.addDecorations(event, message);
    }
};

Element.addMethods(Tapestry.ElementAdditions);

// Collection of field based functions related to validation. Each
// function takes a field, a message and an optional constraint value.

Tapestry.Validator = {
    required : function(field, message)
    {
        Tapestry.addValidator(field, true, function(value, event)
        {
            if (value == '')
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
        this.form.recordError(this.field, this, message);
        this.result = false;
        this.error = true;
    }
};

Tapestry.ErrorPopup.prototype = {
    initialize : function(field)
    {
        this.field = $(field);

        this.innerSpan = new Element("span");
        this.outerDiv = $(new Element("div", { 'class' : 't-error-popup' })).update(this.innerSpan).hide();

        this.field.insert({ after : this.outerDiv });

        this.outerDiv.absolutize();

        this.outerDiv.observe("click", function(event)
        {
            this.stopAnimation();

            this.outerDiv.hide();

            event.stop();
        }.bindAsEventListener(this));

        Tapestry.errorPopups.push(this);

        this.state = "hidden";

        this.queue = { position: 'end', scope: this.field.id };

        Event.observe(window, "resize", this.repositionBubble.bind(this));

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
        var fieldPos = this.field.positionedOffset();

        this.outerDiv.setStyle({ top: fieldPos[1] - 34 + "px", left: fieldPos[0] - 5 + "px", width: "auto", height: "39px" });
    },

    fadeIn : function()
    {
        this.repositionBubble();

        if (this.state == "hidden")
        {
            this.state = "visible";

            this.animation = new Effect.Appear(this.outerDiv, { afterFinish : this.afterFadeIn.bind(this), queue: this.queue });
        }
    },

    stopAnimation : function()
    {
        if (this.animation) this.animation.cancel();

        this.animation = null;
    },

    fadeOut : function ()
    {
        this.stopAnimation();

        if (this.state == "visible")
        {
            this.state = "hidden";

            this.animation = new Effect.Fade(this.outerDiv, { queue : this.queue });
        }
    },

    hide : function()
    {
        this.hasMessage = false;

        this.stopAnimation();

        this.outerDiv.hide();

        this.state = "hidden";
    },

    afterFadeIn : function()
    {
        this.animation = null;

        if (this.field != Tapestry.focusedElement) this.fadeOut();
    },

    handleFocusChange : function(element)
    {
        if (element == this.field)
        {
            if (this.hasMessage) this.fadeIn();
            return;
        }

        if (this.animation == null)
        {
            this.fadeOut();
            return;
        }

        // Must be fading in, let it finish, then fade it back out.

        this.animation = new Effect.Fade(this.outerDiv, { queue : this.queue });
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

    removeDecorations : function(event)
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

    addDecorations : function(event, message)
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
        
        // Clear out old decorations.  It's easier to remove the decorations
        // and then re-add them if the field is in error than it is to
        // toggle them on or off at the end.

        event.field.removeClassName("t-error");

        if (this.label)
            this.label.removeClassName("t-error");

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
            this.removeDecorations(event);
    }
};

// Wrappers around Prototype and Scriptaculous effects, invoked from Tapestry.Zone.show().

Tapestry.ZoneEffect = {

    show : function(element)
    {
        element.show();
    },

    highlight : function(element)
    {
        new Effect.Highlight(element);
    }
};


Tapestry.Zone.prototype = {
    // spec are the parameters for the Zone:
    // trigger: required -- name or instance of link.
    // div: required -- name or instance of div element to be shown, hidden and updated
    // show: name of Tapestry.ZoneEffect function used to reveal the zone if hidden
    // update: name of Tapestry.ZoneEffect function used to highlight the zone after it is updated
    initialize: function(spec)
    {
        this.div = $(spec.div);
        this.showFunc = Tapestry.ZoneEffect[spec.show] || Tapestry.ZoneEffect.show;
        this.updateFunc = Tapestry.ZoneEffect[spec.update] || Tapestry.ZoneEffect.highlight;

        // Link the div back to this zone.

        this.div.zone = this;

        // Look inside the Zone div for the another div with the CSS class "t-zone-update".
        // If present, then this is the elements whose content will be changed, rather
        // then the entire Zone div.  This allows a Zone div to contain "wrapper" markup
        // (borders and such).  Typically, such a Zone div will initially be invisible.
        // The show and update functions apply to the Zone div, not the update div.

        var updates = this.div.select("DIV.t-zone-update");

        this.updatediv = updates.length == 0 ? this.div : updates[0];
    },

    // Updates the content of the div controlled by this Zone, then
    // invokes the show function (if not visible) or the update function (if visible).

    show: function(content)
    {
        this.updatediv.innerHTML = content;

        var func = this.div.visible() ? this.updateFunc : this.showFunc;

        func.call(this, this.div);
    }
};

// Find all elements marked with the "t-invisible" CSS class and hide() them, so that
// Prototype's visible() method operates correctly.

Tapestry.onDOMLoaded(function()
{
    $$(".t-invisible").each(function(element)
    {
        element.hide();
        element.removeClassName("t-invisible");
    });

    // Adds a focus observer that fades all error popups except for the
    // field in question.

    $$("INPUT", "SELECT", "TEXTAREA").each(function(element)
    {
        element.observe("focus", function()
        {
            Tapestry.focusedElement = element;

            $(Tapestry.errorPopups).each(function(popup)
            {
                popup.handleFocusChange(element);
            });
        });
    });
});

