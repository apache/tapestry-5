// Copyright 2008, 2009 The Apache Software Foundation
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

Tapestry.DateField = Class.create({

    // Initializes a DateField from a JSON specification.

    initialize : function(spec)
    {
        this.field = $(spec.field);
        this.trigger = $(spec.field + "-trigger");
        this.parseURL = spec.parseURL;
        this.formatURL = spec.formatURL;

        this.trigger.observe("click", this.triggerClicked.bind(this));

        this.popup = null;

        if (spec.localization)
        {
            DatePicker.months = spec.localization.months;
            DatePicker.days = spec.localization.days.toArray();

            Tapestry.DateField.prototype.firstDay = spec.localization.firstDay;
        }
    },

    triggerClicked : function()
    {
        if (this.field.disabled) return;

        if (this.popup == null)
        {
            this.createPopup();

        }
        else
        {
            if (this.popup.visible())
            {
                this.hidePopup();
                return;
            }
        }


        var value = $F(this.field);

        if (value == "")
        {
            this.datePicker.setDate(null);

            this.positionPopup();

            this.revealPopup();

            return;
        }

        var resultHandler = function(result)
        {
            var date = new Date();

            date.setTime(result);

            this.datePicker.setDate(date);

            this.positionPopup();

            this.revealPopup();
        };

        var errorHandler = function(message)
        {
            this.field.showValidationMessage(message);
            this.field.activate();
        };

        this.sendServerRequest(this.parseURL, value, resultHandler, errorHandler);
    },

    sendServerRequest : function (url, input, resultHandler, errorHandler)
    {
        var successHandler = function(response)
        {
            var json = response.responseJSON;

            var result = json.result;

            if (result)
            {
                resultHandler.call(this, result);
                return;
            }

            errorHandler.call(this, json.error);
        }.bind(this);

        new Ajax.Request(url,
        {
            method: 'get',
            parameters: {
                input: input
            },
            onSuccess: successHandler,
            onFailure: Tapestry.ajaxFailureHandler
        });
    },

    createPopup : function()
    {
        this.datePicker = new DatePicker();

        this.datePicker.setFirstWeekDay(this.firstDay);

        this.popup = $(this.datePicker.create());

        this.field.insert({
            after : this.popup
        });

        this.popup.absolutize().hide();

        this.datePicker.onselect = function()
        {
            var date = this.datePicker.getDate();

            var resultHandler = function(result)
            {
                this.field.value = result;

                this.hidePopup();

                new Effect.Highlight(this.field);
            };

            var errorHandler = function(message)
            {
                this.field.showValidationMessage(message);
                this.field.activate();

                this.hidePopup();
            };

            // If the field is blank, don't bother going to the server to parse!

            if (date == null)
            {
                resultHandler.call(this, "");
                return;
            }

            this.sendServerRequest(this.formatURL, date.getTime(), resultHandler, errorHandler);
        }.bind(this);
    },

    positionPopup : function()
    {
        // The field may be a hidden field, in which csae, position the popup based on the trigger, not
        // the hidden.

        var reference = this.field.type == "text" ? this.field : this.trigger;

        this.popup.clonePosition(reference, {
            offsetTop: reference.getHeight() + 2
        }).setStyle({
            width: "",
            height: ""
        });
    },

    /** Duration, in seconds, used when fading the popup in or out. */

    FADE_DURATION : .20,

    hidePopup : function()
    {
        new Effect.Fade(this.popup, {
            duration: this.FADE_DURATION
        });
    },

    revealPopup : function()
    {

        // Only show one DateField popup at a time.

        if (Tapestry.DateField.activeDateField != undefined &&
            Tapestry.DateField.activeDateField != this)
        {
            Tapestry.DateField.activeDateField.hidePopup();
        }

        new Effect.Appear(this.popup, {
            duration: this.FADE_DURATION
        });

        Tapestry.DateField.activeDateField = this;
    }
});

Tapestry.DateField.localized = false;

Tapestry.Initializer.dateField = function(spec)
{
    new Tapestry.DateField(spec);
}