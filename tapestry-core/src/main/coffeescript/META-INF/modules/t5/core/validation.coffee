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

# ## t5/core/validation
#
# Support for Tapestry's built-in set of translators and validators.
#
define ["underscore", "t5/core/dom", "t5/core/events", "t5/core/utils", "t5/core/messages", "t5/core/fields"],
  (_, dom, events, utils, messages) ->

    REGEXP_META = "t5:regular-expression"

    minus = messages "decimal-symbols.minus"
    grouping = messages "decimal-symbols.group"
    decimal = messages "decimal-symbols.decimal"

    # Formats a string for a localized number into a simple number. This uses localization
    # information provided by `t5/core/messages` to remove grouping seperators and convert the
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

    matches = (input, re) ->
      groups = input.match re

      # Unlike Java, there isn't an easy way to match the entire string. This
      # gets the job done.

      return false if groups is null

      groups[0] is input

    emailRE = new RegExp("[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?")

    translate = (field, memo, isInteger) ->
      try
        result = parseNumber memo.value, isInteger

        if _.isNaN result
          throw messages "core-input-not-numeric"

        memo.translated = result
      catch e
        memo.error = (field.attr "data-translation-message") or e.message or "ERROR"
        return false

    dom.onDocument events.field.optional, "[data-optionality=required]", (event, memo) ->

      if utils.isBlank memo.value
        memo.error =  (@attr "data-required-message") or "REQUIRED"

    dom.onDocument events.field.translate, "[data-translation=numeric]", (event, memo) ->
      translate this, memo, false

    dom.onDocument events.field.translate, "[data-translation=integer]", (event, memo) ->
      translate this, memo, true

    dom.onDocument events.field.validate, "[data-validate-min-length]", (event, memo) ->
      min = parseInt @attr "data-validate-min-length"

      if memo.translated.length < min
        memo.error = (@attr "data-min-length-message") or "TOO SHORT"
        return false

    dom.onDocument events.field.validate, "[data-validate-max-length]", (event, memo) ->
      max = parseInt @attr "data-validate-max-length"

      if memo.translated.length > max
        memo.error = (@attr "data-max-length-message") or "TOO LONG"
        return false

    dom.onDocument events.field.validate, "[data-validate-max]", (event, memo) ->
      max = parseInt @attr "data-validate-max"

      if memo.translated > max
        memo.error = (@attr "data-max-message") or "TOO LARGE"
        return false

    dom.onDocument events.field.validate, "[data-validate-min]", (event, memo) ->
      min = parseInt @attr "data-validate-min"

      if memo.translated < min
        memo.error = (@attr "data-min-message") or "TOO SMALL"
        return false

    dom.onDocument events.field.validate, "[data-validate-email]", (event, memo) ->

      unless (matches memo.translated, emailRE)
        memo.error = (@attr "data-email-message") or "INVALID EMAIL"
        return false

    dom.onDocument events.field.validate, "[data-validate-regexp]", (event, memo) ->

      # Cache the compiled regular expression.
      re = @meta REGEXP_META

      unless re
        re = new RegExp(@attr "data-validate-regexp")
        @meta REGEXP_META, re

      unless (matches memo.translated, re)
        memo.error = (@attr "data-regexp-message") or "INVALID"
        return false

    dom.onDocument events.field.validate, "[data-expected-status=checked]", (event, memo) ->

      unless memo.value
        memo.error =  (@attr "data-checked-message") or "MUST BE CHECKED"

    dom.onDocument events.field.validate, "[data-expected-status=unchecked]", (event, memo) ->

      if memo.value
        memo.error =  (@attr "data-checked-message") or "MUST NOT BE CHECKED"

    # Export the number parser, just to be nice (and to support some testing).
    return { parseNumber }