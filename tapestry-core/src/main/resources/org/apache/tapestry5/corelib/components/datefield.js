// Copyright 2008 The Apache Software Foundation
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

Tapestry.DateField = Class.create();

Tapestry.DateField.prototype = {

    // Initializes a DateField from a JSON specification.

    initialize : function(spec)
    {
        this.field = $(spec.field);
        this.trigger = $(spec.field + ":trigger");

        this.trigger.observe("click", this.triggerClicked.bind(this));

        this.popup = null;
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
        }
        else
        {

            // TODO: This is limited and americanized (not localized) to MM/DD/YYYY
            var re = /^\s*(\d+)\/(\d+)\/(\d{2,4})\s*$/;
            var matches = re.exec(value);


            // If the RE is bad, raise the date picker anyway, showing
            // the last valid date, or showing no date.

            if (matches != null)
            {

                var month = Number(matches[1]);
                var day = Number(matches[2])
                var year = Number(matches[3]);

            // For two digits, guestamate which century they want.

                if (year < 100)
                {
                    if (year >= 60) year += 1900
                    else year += 2000;
                }

                var date = new Date(value);

                date.setMonth(month - 1);
                date.setDate(day);
                date.setFullYear(year);

                this.datePicker.setDate(date);
            }
        }

        this.positionPopup();

        this.revealPopup();
    },

    createPopup : function()
    {
        this.datePicker = new DatePicker();

        this.popup = $(this.datePicker.create());

        this.field.insert({ after : this.popup });

        this.popup.absolutize().hide();

        this.datePicker.onselect = function()
        {
            this.field.value = this.formatDate(this.datePicker.getDate());

            this.hidePopup();

            new Effect.Highlight(this.field);

        }.bind(this);
    },

    formatDate : function(date)
    {
        if (date == null) return "";

        // TODO: This needs to localize; currently its Americanized (MM/DD/YYYY).
        return (date.getMonth() + 1) + "/" + date.getDate() + "/" + date.getFullYear();
    },

    positionPopup : function()
    {
        this.popup.clonePosition(this.field, { offsetTop: this.field.getHeight() + 2 }).setStyle({ width: "", height: "" });
    },

    /** Duration used when fading the popup in or out. */

    FADE_DURATION : .20,

    hidePopup : function()
    {

        new Effect.Fade(this.popup, { duration: this.FADE_DURATION });
    },

    revealPopup : function()
    {

        // Only show one DateField popup at a time.

        if (Tapestry.DateField.activeDateField != undefined &&
            Tapestry.DateField.activeDateField != this)
        {
            Tapestry.DateField.activeDateField.hidePopup();
        }

        new Effect.Appear(this.popup, { duration: this.FADE_DURATION });

        Tapestry.DateField.activeDateField = this;
    }
};

Tapestry.Initializer.dateField = function(spec)
{
    new Tapestry.DateField(spec);
}