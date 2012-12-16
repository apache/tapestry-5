# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

## core/translator
#
# Support for Tapestry's built-in set of translators and validators.
#
define ["_", "core/dom", "core/events", "core/utils", "core/messages", "core/fields"],
  (_, dom, events, utils, messages) ->

    REGEXP_META = "t5:regular-expression"

    minus = messages "decimal-symbols.minus"
    grouping = messages "decimal-symbols.group"
    decimal = messages "decimal-symbols.decimal"

    # Formats a string for a localized number into a simple number. This uses localization
    # information provided by `core/messages` to remove grouping seperators and convert the
    # minus sign and decimal seperator into english norms ("-" and ".") that are compatible
    # with the `Number` constructor. May throw `Error` if the input can not be parsed.
    #
    # A little state machine does the parsing; the input may have a leading minus sign.
    # It then consists primarily of numeric digits. At least one digit must occur
    # between each grouping character. Grouping characters are not allowed
    # after the decimal point.
    #
    # * input - input string to be converted
    # * isInteger - restrict to integer values (decimal point not allowed)
    parseNumber = (input, isInteger) ->

      canonical = ""

      accept = (ch) -> canonical += ch

      acceptDigitOnly = (ch) ->
        if ch < "0" or ch > "9"
          throw new Error messages "core-input-not-numeric"

        accept ch
        return

      mustBeDigit = (ch) ->
        acceptDigitOnly ch
        return any

      decimalPortion = (ch) ->
        acceptDigitOnly ch
        return decimalPortion

      any = (ch) ->
        switch ch
          when grouping then return mustBeDigit
          when decimal
            if isInteger
              throw new Error messages "core-input-not-integer"

            accept "."
            return decimalPortion
          else
            mustBeDigit ch

      leadingMinus = (ch) ->
        if ch is minus
          accept "-"
          return mustBeDigit
        else
          any ch

      state = leadingMinus

      for ch in utils.trim input
        state = (state ch)

      return Number canonical

    translate = (field, memo, isInteger) ->
      try
        result = parseNumber memo.value, isInteger

        if _.isNaN result
          throw messages "core-input-not-numeric"

        memo.translated = result
      catch e
        memo.error = (field.attribute "data-translation-message") or e.message or "ERROR"
        return false

    dom.onDocument events.field.optional, "[data-optionality=required]", (event, memo) ->

      if utils.isBlank memo.value
        memo.error =  (this.attribute "data-required-message") or "REQUIRED"

    dom.onDocument events.field.translate, "[data-translation=numeric]", (event, memo) ->
      translate this, memo, false

    dom.onDocument events.field.translate, "[data-translation=integer]", (event, memo) ->
      translate this, memo, true

    dom.onDocument events.field.validate, "[data-validate-min-length]", (event, memo) ->
      min = parseInt this.attribute "data-validate-min-length"

      if memo.translated.length < min
        memo.error = (this.attribute "data-min-length-message") or "TOO SHORT"
        return false

    dom.onDocument events.field.validate, "[data-validate-max-length]", (event, memo) ->
      max = parseInt this.attribute "data-validate-max-length"

      if memo.translated.length > max
        memo.error = (this.attribute "data-max-length-message") or "TOO LONG"
        return false

    dom.onDocument events.field.validate, "[data-validate-max]", (event, memo) ->
      max = parseInt this.attribute "data-validate-max"

      if memo.translated > max
        memo.error = (this.attribute "data-max-message") or "TOO LARGE"
        return false

    dom.onDocument events.field.validate, "[data-validate-min]", (event, memo) ->
      min = parseInt this.attribute "data-validate-min"

      if memo.translated < min
        memo.error = (this.attribute "data-min-message") or "TOO SMALL"
        return false

    dom.onDocument events.field.validate, "[data-validate-regexp]", (event, memo) ->

      # Cache the compiled regular expression.
      re = this.meta REGEXP_META
      unless re
        re = new RegExp(this.attribute "data-validate-regexp")
        this.meta REGEXP_META, re

      unless re.test memo.translated
        memo.error = (this.attribute "data-regexp-message") or "INVALID"
        return false

    # Export the number parser, just to be nice (and to support some testing).
    return { parseNumber }