/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
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

/**
 * ## t5/core/validation
 * 
 * Support for Tapestry's built-in set of translators and validators.
 * @packageDocumentation
 */

import _ from "underscore";
import dom from "t5/core/dom";
import events from "t5/core/events";
import utils from "t5/core/utils";
import messages from "t5/core/messages";
import { ElementWrapper } from "t5/core/types";

const REGEXP_META = "t5:regular-expression";

const minus = messages("decimal-symbols.minus");
const grouping = messages("decimal-symbols.group");
const decimal = messages("decimal-symbols.decimal");

// Formats a string for a localized number into a simple number. This uses localization
// information provided by `t5/core/messages` to remove grouping seperators and convert the
// minus sign and decimal seperator into english norms ("-" and ".") that are compatible
// with the `Number` constructor. May throw `Error` if the input can not be parsed.
//
// A little state machine does the parsing; the input may have a leading minus sign.
// It then consists primarily of numeric digits. At least one digit must occur
// between each grouping character. Grouping characters are not allowed
// after the decimal point.
//
// * input - input string to be converted
// * isInteger - restrict to integer values (decimal point not allowed)
const parseNumber = function(input: string, isInteger: boolean) {

  let canonical = "";

  const accept = (ch: string) => canonical += ch;

  const acceptDigitOnly = function(ch: string) {
    if ((ch < "0") || (ch > "9")) {
      throw new Error(messages("core-input-not-numeric"));
    }

    accept(ch);
  };

  const mustBeDigit = function(ch: string) {
    acceptDigitOnly(ch);
    return any;
  };

  var decimalPortion = function(ch: string) {
    acceptDigitOnly(ch);
    return decimalPortion;
  };

  var any = function(ch: string) {
    switch (ch) {
      case grouping: return mustBeDigit;
      case decimal:
        if (isInteger) {
          throw new Error(messages("core-input-not-integer"));
        }

        accept(".");
        return decimalPortion;
      default:
        return mustBeDigit(ch);
    }
  };

  const leadingMinus = function(ch: string) {
    if (ch === minus) {
      accept("-");
      return mustBeDigit;
    } else {
      return any(ch);
    }
  };

  let state = leadingMinus;

  for (var ch of Array.from(utils.trim(input))) {
    state = (state(ch));
  }

  return Number(canonical);
};

const matches = function(input: string, re: RegExp) {
  const groups = input.match(re);

  // Unlike Java, there isn't an easy way to match the entire string. This
  // gets the job done.

  if (groups === null) { return false; }

  return groups[0] === input;
};

const emailRE = new RegExp("[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?");

const translate = function(field: ElementWrapper, memo: any, isInteger: boolean) {
  try {
    const result = parseNumber(memo.value, isInteger);

    if (_.isNaN(result)) {
      throw messages("core-input-not-numeric");
    }

    return memo.translated = result;
  } catch (e: any) {
    memo.error = (field.attr("data-translation-message")) || e.message || "ERROR";
    return false;
  }
};

dom.onDocument(events.field.optional, "[data-optionality=required]", function(event, memo: any) {

  if (utils.isBlank(memo.value)) {
    // @ts-ignore
    return memo.error =  (this.attr("data-required-message")) || "REQUIRED";
  }
});

dom.onDocument(events.field.translate, "[data-translation=numeric]", function(event, memo) {
  // @ts-ignore
  return translate(this, memo, false);
});

dom.onDocument(events.field.translate, "[data-translation=integer]", function(event, memo) {
  // @ts-ignore
  return translate(this, memo, true);
});

dom.onDocument(events.field.validate, "[data-validate-min-length]", function(event, memo) {
  // @ts-ignore
  const min = parseInt(this.attr("data-validate-min-length"));

  // @ts-ignore
  if (memo.translated.length < min) {
    // @ts-ignore
    memo.error = (this.attr("data-min-length-message")) || "TOO SHORT";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-validate-max-length]", function(event, memo) {
  // @ts-ignore
  const max = parseInt(this.attr("data-validate-max-length"));

  // @ts-ignore
  if (memo.translated.length > max) {
    // @ts-ignore
    memo.error = (this.attr("data-max-length-message")) || "TOO LONG";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-validate-max]", function(event, memo) {
  // @ts-ignore
  const max = parseInt(this.attr("data-validate-max"));

  // @ts-ignore
  if (memo.translated > max) {
    // @ts-ignore
    memo.error = (this.attr("data-max-message")) || "TOO LARGE";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-validate-min]", function(event, memo) {
  // @ts-ignore
  const min = parseInt(this.attr("data-validate-min"));

  // @ts-ignore
  if (memo.translated < min) {
    // @ts-ignore
    memo.error = (this.attr("data-min-message")) || "TOO SMALL";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-validate-email]", function(event, memo) {

  // @ts-ignore
  if (!matches(memo.translated, emailRE)) {
    // @ts-ignore
    memo.error = (this.attr("data-email-message")) || "INVALID EMAIL";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-validate-regexp]", function(event, memo) {

  // Cache the compiled regular expression.
  // @ts-ignore
  let re = this.meta(REGEXP_META);

  if (!re) {
    // @ts-ignore
    re = new RegExp(this.attr("data-validate-regexp"));
    // @ts-ignore
    this.meta(REGEXP_META, re);
  }

  // @ts-ignore
  if (!matches(memo.translated, re)) {
    // @ts-ignore
    memo.error = (this.attr("data-regexp-message")) || "INVALID";
    return false;
  }
});

dom.onDocument(events.field.validate, "[data-expected-status=checked]", function(event, memo) {

  // @ts-ignore
  if (!memo.value) {
    // @ts-ignore
    return memo.error =  (this.attr("data-checked-message")) || "MUST BE CHECKED";
  }
});

dom.onDocument(events.field.validate, "[data-expected-status=unchecked]", function(event, memo) {

  // @ts-ignore
  if (memo.value) {
    // @ts-ignore
    return memo.error =  (this.attr("data-checked-message")) || "MUST NOT BE CHECKED";
  }
});

    // Export the number parser, just to be nice (and to support some testing).
export default { parseNumber };