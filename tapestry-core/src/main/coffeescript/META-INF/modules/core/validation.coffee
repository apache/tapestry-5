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
define ["_", "core/spi", "core/events", "core/utils", "core/messages", "core/fields"],
  (_, spi, events, utils, messages) ->

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

    spi.onDocument events.field.optional, "[data-optionality=required]", (event, memo) ->

      if utils.isBlank memo.value
        memo.error =  (this.attribute "data-required-message") || "REQUIRED"

    spi.onDocument events.field.translate, "[data-translation=numeric]", (event, memo) ->
      translate this, memo, false

    spi.onDocument events.field.translate, "[data-translation=integer]", (event, memo) ->
      translate this, memo, true

    spi.onDocument events.field.validate, "[data-validate-min-length]", (event, memo) ->
      min = parseInt this.attribute "data-validate-min-length"

      if memo.translated.length < min
        memo.error = (this.attribute "data-min-length-message") or "TOO SHORT"

    spi.onDocument events.field.validate, "[data-validate-max-length]", (event, memo) ->
      min = parseInt this.attribute "data-validate-max-length"

      if memo.translated.length > min
        memo.error = (this.attribute "data-max-length-message") or "TOO LONG"

    # Export the number parser, just to be nice (and to support some testing).
    return { parseNumber }