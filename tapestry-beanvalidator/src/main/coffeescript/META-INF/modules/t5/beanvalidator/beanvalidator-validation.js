/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
// Copyright 2012-2013 The Apache Software Foundation
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

// ## t5/beanvalidator/beanvalidator-validation
//
// The awkward name is to accomidate the "docco" documentation tool; it doesn't understand
// having the same named file in multiple folders. See https://github.com/jashkenas/docco/issues/201.
//
// Supports extra validations related to the beanvalidator module.
define(["underscore", "t5/core/dom", "t5/core/events", "t5/core/utils", "t5/core/validation"],
  function(_, dom, events, utils) {

    const rangeValue = function(element, attribute, defaultValue) {
      const v = element.attr(attribute);
      if (v === null) {
        return defaultValue;
      } else {
        return parseInt(v);
      }
    };

    const countOptions = function(e) {
      // A select that is used as part of a palette is different; the validation attributes
      // are attached to the selected (right side) <select>, and anything there counts as part
      // of the selection.
      if (e.findParent(".palette")) {
        return e.element.options.length;
      } else {
        // An ordinary <select> may have multiple options (the clumsy control-click way)
        return _.filter(e.element.options, o => o.selected).length;
      }
    };

    const doRangeValidate = function(element, value, memo) {
      const min = rangeValue(element, "data-range-min", 0);
      const max = rangeValue(element, "data-range-max", Number.MAX_VALUE);

      // If the translated value is still a string, and not a number, then the
      // size refers to the length of the string, not its numeric value.
      if (_.isString(value)) {
        value = value.length;
      }

      if (!(min <= value && value <= max)) {
        memo.error = (element.attr("data-range-message")) || "RANGE ERROR";
        return false;
      }

      return true;
    };

    dom.onDocument(events.field.optional, "[data-optionality=prohibited]", function(event, memo) {

      if (!utils.isBlank(memo.value)) {
        memo.error = (this.attr("data-prohibited-message")) || "PROHIBITED";
        return false;
      }

      return true;
    });

    dom.onDocument(events.field.validate, "input[data-range-min], input[data-range-max], textarea[data-range-min], textarea[data-range-max]", function(event, memo) {
      return doRangeValidate(this, memo.translated, memo);
    });

    dom.onDocument(events.field.validate, "select[data-range-min], select[data-range-max]", function(event, memo) {
      return doRangeValidate(this, (countOptions(this)), memo);
    });

});
