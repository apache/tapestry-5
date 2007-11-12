// Copyright 2007 The Apache Software Foundation
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

    registerForm : function(form)
    {
        form = $(form);

        form.errorDiv = $(form.id + ':errors');

        if (form.errorDiv)
        {
            Tapestry.hideInvisible(form.errorDiv);

            form.errorList = form.errorDiv.getElementsBySelector("ul").first();
            if (! form.errorList)
            {
                // create it now
                form.errorList = document.createElement("ul");
                form.errorDiv.appendChild(form.errorList);
            }
        }
    
    // This can probably be cleaned up with bind() ...

        form.onsubmit = function()
        {
            var event = new Tapestry.FormEvent(form);

            form.firstError = true;

            if (form.errorList)
            {
                form.errorList.innerHTML = "";
            }

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

	  // On a failure result, display the error div.

            if (form.errorDiv)
            {
                if (event.result)
                {
                    if (form.errorDiv.visible())
                        new Effect.BlindUp(form.errorDiv);
                }
                else if (! form.errorDiv.visible())
                {
                    new Effect.BlindDown(form.errorDiv);
                }
            }

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

            if (form.errorList)
                new Insertion.Bottom(form.errorList, "<li>" + message + "</li>");

        };
    },

// Checks the element; if it has the "t-invisible" CSS class, then
// the element is hidden,and the t-invisible CSS class is removed. This is necessary
// for Prototype's visible() method, which can't determine visibility when it's defined via
// CSS.

    hideInvisible : function(element)
    {
        element = $(element);

        if (element.classNames().include("t-invisible"))
        {
            element.hide();
            element.removeClassName("t-invisible");
        }
    },

    FormEvent : Class.create(),

    FieldEventManager : Class.create(),

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

// Collection of field based functions related to validation.

Tapestry.Field = {
    required : function(field, message)
    {
        Tapestry.addValidator(field, true, function(value, event)
        {
            if (value == '')
                event.recordError(message);
        });
    },

    minlength : function(field, length, message)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value.length < length)
                event.recordError(message);
        });
    },

    maxlength : function(field, maxlength, message)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value.length > maxlength)
                event.recordError(message);
        });
    },

    min : function(field, minValue, message)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value < minValue)
                event.recordError(message);
        });
    },

    max : function(field, maxValue, message)
    {
        Tapestry.addValidator(field, false, function(value, event)
        {
            if (value > maxValue)
                event.recordError(message);
        });
    },

    regexp : function(field, pattern, message)
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

Tapestry.FieldEventManager.prototype = {

    initialize : function(field)
    {
        this.field = $(field);

        field.fieldEventManager = this;

        this.validators = [ ];

        var id = field.id;
        this.label = $(id + ':label');
        this.icon = $(id + ':icon');
        if (this.icon)
            Tapestry.hideInvisible(this.icon);

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

    },


// Invoked from the Form's onsubmit event handler. Gets the fields value and invokes
// each validator (unless the value is blank) until a validator returns false. Validators
// should not modify the field's value.

    validateInput : function(event)
    {
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


 