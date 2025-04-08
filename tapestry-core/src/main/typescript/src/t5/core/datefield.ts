// Copyright 2012, 2013, 2025 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// ## t5/core/datefield
//
// Provides support for the `core/DateField` component.
import dom from "t5/core/dom";
import events from "t5/core/events";
import messages from "t5/core/messages";
import ajax from  "t5/core/ajax";
import _ from "underscore";
import datepicker from "t5/core/datepicker";
import "t5/core/fields";

// Translate from the provided order (SUNDAY = 0, MONDAY = 1), to
// the order needed by the DatePicker component (MONDAY = 0 ... SUNDAY = 6)
let name;
const serverFirstDay = parseInt(messages("date-symbols.first-day"));
const datePickerFirstDay = serverFirstDay === 0 ? 6 : serverFirstDay - 1;

// Localize a few other things.
const days = (messages("date-symbols.days")).split(",");

// Shuffle sunday to the end, so that monday is first.

days.push(days.shift());

const monthsLabels = (messages("date-symbols.months")).split(",");
let abbreviateWeekDay = name => name.substr(0, 1).toLowerCase();
const locale = (document.documentElement.getAttribute("data-locale")) || "en";
if ((locale.indexOf('zh')) === 0) {
  // TAP5-1886, Chinese weekdays cannot be abbreviated using the first character
  abbreviateWeekDay = name => name.substr(name.length-1);
}
const daysLabels = ((() => {
  const result = [];
  for (name of Array.from(days)) {         result.push(abbreviateWeekDay(name));
  }
  return result;
})());
const todayLabel = messages("core-datefield-today");
const noneLabel = messages("core-datefield-none");


// Track the active popup; only one allowed at a time. May look to rework this
// later so that there's just one popup and it is moved around the viewport, or
// around the DOM.
let activePopup = null;


const isPartOfPopup = element => (element.findParent(".labelPopup") != null) || (element.findParent(".datefield-popup") != null);

dom.body.on("click", function() {
  if (activePopup && !isPartOfPopup(this)) {
    activePopup.hide();
    activePopup = null;
  }
});


class Controller {
  constructor(container) {
    this.container = container;
    this.field = this.container.findFirst('input:not([name="t:formdata"])');
    this.trigger = this.container.findFirst("button");

    this.trigger.on("click", () => {
      this.doTogglePopup();
      return false;
    });
  }

  showPopup() {
    if (activePopup && (activePopup !== this.popup)) {
      activePopup.hide();
    }

    this.popup.show();
    return activePopup = this.popup;
  }

  hidePopup() {
    this.popup.hide();
    return activePopup = null;
  }

  doTogglePopup() {
    if (this.field.element.disabled) { return; }

    if (!this.popup) {
      this.createPopup();
      if (activePopup != null) {
        activePopup.hide();
      }
    } else if (this.popup.visible()) {
      this.hidePopup();
      return;
    }

    const value = this.field.value();

    if (value === "") {
      this.datePicker.setDate(null);
      this.showPopup();
      return;
    }

    this.field.addClass("ajax-wait");

    return ajax((this.container.attr("data-parse-url")), {
      data: {
        input: value
      },
      onerror: message => {
        this.field.removeClass("ajax-wait");
        this.fieldError(message);

        this.showPopup();
      },

      success: response => {
        this.field.removeClass("ajax-wait");
        const reply = response.json;

        if (reply.result) {
          this.clearFieldError();
          const [year, month, day] = Array.from(reply.result.split('-'));

          const date = new Date(year, month-1, day);
          this.datePicker.setDate(date);
        }

        if (reply.error) {
          this.fieldError((_.escape(reply.error)));

          this.datePicker.setDate(null);
        }

        this.showPopup();
      }
    }
    );
  }

  fieldError(message) {
    return this.field.focus().trigger(events.field.showValidationError, { message });
  }

  clearFieldError() {
    return this.field.trigger(events.field.clearValidationError);
  }

  createPopup() {
    this.datePicker = new DatePicker();
    this.datePicker.setFirstWeekDay(datePickerFirstDay);
    
    this.datePicker.setLocalizations(monthsLabels, daysLabels, todayLabel, noneLabel);
    
    this.popup = dom.create("div", { class: "datefield-popup well"}).append(this.datePicker.create());
    this.container.insertAfter(this.popup);

    return this.datePicker.onselect = _.bind(this.onSelect, this);
  }

  onSelect() {
    const date = this.datePicker.getDate();

    if (date === null) {
      this.hidePopup();
      this.clearFieldError();
      this.field.value("");
      return;
    }

    this.field.addClass("ajax-wait");

    const normalizedFormat = `${date.getFullYear()}-${date.getMonth()+1}-${date.getDate()}`;
    return ajax((this.container.attr("data-format-url")), {
      data: {
        input: normalizedFormat
      },
      failure: (response, message) => {
        this.field.removeClass("ajax-wait");
        return this.fieldError(message);
      },
      success: response => {
        this.field.removeClass("ajax-wait");
        this.clearFieldError();
        this.field.value(response.json.result);
        return this.hidePopup();
      }
    }
    );
  }
}


// Initialization:

dom.scanner("[data-component-type='core/DateField']", function(container) {
  // Hide it from later scans
  container.attr("data-component-type", null);

  return new Controller(container);
});